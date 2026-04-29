#[cfg(feature = "cuda")]
mod enabled {
    use std::ffi::{CStr, CString, c_char, c_int, c_uint, c_void};
    use std::marker::PhantomData;
    use std::ptr;

    use crate::genome::Genome;
    use crate::problem::ProblemData;

    const RTLD_NOW: c_int = 2;
    const CUDA_SUCCESS: c_int = 0;
    const THREADS_PER_BLOCK: u32 = 128;

    type CudaResult = c_int;
    type CudaDevice = c_int;
    type CudaContext = *mut c_void;
    type CudaModule = *mut c_void;
    type CudaFunction = *mut c_void;
    type CudaStream = *mut c_void;
    type CudaDevicePtr = u64;

    type CuInit = unsafe extern "C" fn(c_uint) -> CudaResult;
    type CuDeviceGetCount = unsafe extern "C" fn(*mut c_int) -> CudaResult;
    type CuDeviceGet = unsafe extern "C" fn(*mut CudaDevice, c_int) -> CudaResult;
    type CuCtxCreate = unsafe extern "C" fn(*mut CudaContext, c_uint, CudaDevice) -> CudaResult;
    type CuCtxDestroy = unsafe extern "C" fn(CudaContext) -> CudaResult;
    type CuMemAlloc = unsafe extern "C" fn(*mut CudaDevicePtr, usize) -> CudaResult;
    type CuMemFree = unsafe extern "C" fn(CudaDevicePtr) -> CudaResult;
    type CuMemcpyHtoD = unsafe extern "C" fn(CudaDevicePtr, *const c_void, usize) -> CudaResult;
    type CuMemcpyDtoH = unsafe extern "C" fn(*mut c_void, CudaDevicePtr, usize) -> CudaResult;
    type CuModuleLoadData = unsafe extern "C" fn(*mut CudaModule, *const c_void) -> CudaResult;
    type CuModuleUnload = unsafe extern "C" fn(CudaModule) -> CudaResult;
    type CuModuleGetFunction =
        unsafe extern "C" fn(*mut CudaFunction, CudaModule, *const c_char) -> CudaResult;
    type CuLaunchKernel = unsafe extern "C" fn(
        CudaFunction,
        c_uint,
        c_uint,
        c_uint,
        c_uint,
        c_uint,
        c_uint,
        c_uint,
        CudaStream,
        *mut *mut c_void,
        *mut *mut c_void,
    ) -> CudaResult;
    type CuCtxSynchronize = unsafe extern "C" fn() -> CudaResult;
    type CuGetErrorString = unsafe extern "C" fn(CudaResult, *mut *const c_char) -> CudaResult;

    unsafe extern "C" {
        fn dlopen(filename: *const c_char, flags: c_int) -> *mut c_void;
        fn dlsym(handle: *mut c_void, symbol: *const c_char) -> *mut c_void;
    }

    pub(crate) fn device_count() -> Result<usize, String> {
        let driver = CudaDriver::load()?;
        driver.init()?;
        driver.device_count()
    }

    pub(crate) fn genome_complexities(
        population: &[Genome],
        problem: &ProblemData,
    ) -> Result<Vec<f64>, String> {
        if population.is_empty() {
            return Ok(Vec::new());
        }

        let driver = CudaDriver::load()?;
        driver.init()?;
        let _context = CudaContextGuard::create(&driver)?;
        let module = CudaModuleGuard::load(&driver, GENOME_COMPLEXITY_PTX)?;
        let function = module.function("genome_complexity")?;
        let input = GenomeComplexityInput::from_population(population, problem);

        let hidden_neurons = DeviceBuffer::from_slice(&driver, &input.hidden_neurons)?;
        let hidden_layers = DeviceBuffer::from_slice(&driver, &input.hidden_layers)?;
        let recurrent_connections =
            DeviceBuffer::from_slice(&driver, &input.recurrent_connections)?;
        let memory_cells = DeviceBuffer::from_slice(&driver, &input.memory_cells)?;
        let input_sizes = DeviceBuffer::from_slice(&driver, &input.input_sizes)?;
        let output_input_sizes = DeviceBuffer::from_slice(&driver, &input.output_input_sizes)?;
        let hebbian = DeviceBuffer::from_slice(&driver, &input.hebbian)?;
        let normalization = DeviceBuffer::from_slice(&driver, &input.normalization)?;
        let kernel_memory = DeviceBuffer::from_slice(&driver, &input.kernel_memory)?;
        let phase_encoding = DeviceBuffer::from_slice(&driver, &input.phase_encoding)?;
        let second_derivative = DeviceBuffer::from_slice(&driver, &input.second_derivative)?;
        let output = DeviceBuffer::<f64>::new(&driver, population.len())?;

        let mut hidden_neurons_ptr = hidden_neurons.device_ptr();
        let mut hidden_layers_ptr = hidden_layers.device_ptr();
        let mut recurrent_connections_ptr = recurrent_connections.device_ptr();
        let mut memory_cells_ptr = memory_cells.device_ptr();
        let mut input_sizes_ptr = input_sizes.device_ptr();
        let mut output_input_sizes_ptr = output_input_sizes.device_ptr();
        let mut hebbian_ptr = hebbian.device_ptr();
        let mut normalization_ptr = normalization.device_ptr();
        let mut kernel_memory_ptr = kernel_memory.device_ptr();
        let mut phase_encoding_ptr = phase_encoding.device_ptr();
        let mut second_derivative_ptr = second_derivative.device_ptr();
        let mut output_ptr = output.device_ptr();
        let mut population_len = population.len() as u32;
        let mut output_dimensions = problem.output_dimensions as u32;
        let mut params = [
            param(&mut hidden_neurons_ptr),
            param(&mut hidden_layers_ptr),
            param(&mut recurrent_connections_ptr),
            param(&mut memory_cells_ptr),
            param(&mut input_sizes_ptr),
            param(&mut output_input_sizes_ptr),
            param(&mut hebbian_ptr),
            param(&mut normalization_ptr),
            param(&mut kernel_memory_ptr),
            param(&mut phase_encoding_ptr),
            param(&mut second_derivative_ptr),
            param(&mut output_ptr),
            param(&mut population_len),
            param(&mut output_dimensions),
        ];
        let block_count = population_len.div_ceil(THREADS_PER_BLOCK);
        driver.check(
            unsafe {
                (driver.cu_launch_kernel)(
                    function,
                    block_count,
                    1,
                    1,
                    THREADS_PER_BLOCK,
                    1,
                    1,
                    0,
                    ptr::null_mut(),
                    params.as_mut_ptr(),
                    ptr::null_mut(),
                )
            },
            "cuLaunchKernel(genome_complexity)",
        )?;
        driver.check(
            unsafe { (driver.cu_ctx_synchronize)() },
            "cuCtxSynchronize(genome_complexity)",
        )?;
        output.copy_to_host()
    }

    fn param<T>(value: &mut T) -> *mut c_void {
        value as *mut T as *mut c_void
    }

    struct GenomeComplexityInput {
        hidden_neurons: Vec<u32>,
        hidden_layers: Vec<u32>,
        recurrent_connections: Vec<u32>,
        memory_cells: Vec<u32>,
        input_sizes: Vec<u32>,
        output_input_sizes: Vec<u32>,
        hebbian: Vec<u8>,
        normalization: Vec<u8>,
        kernel_memory: Vec<u8>,
        phase_encoding: Vec<u8>,
        second_derivative: Vec<u8>,
    }

    impl GenomeComplexityInput {
        fn from_population(population: &[Genome], problem: &ProblemData) -> Self {
            Self {
                hidden_neurons: population
                    .iter()
                    .map(|genome| genome.hidden_neurons as u32)
                    .collect(),
                hidden_layers: population
                    .iter()
                    .map(|genome| genome.hidden_layers as u32)
                    .collect(),
                recurrent_connections: population
                    .iter()
                    .map(|genome| genome.recurrent_connections as u32)
                    .collect(),
                memory_cells: population
                    .iter()
                    .map(|genome| genome.memory_cells as u32)
                    .collect(),
                input_sizes: population
                    .iter()
                    .map(|genome| genome.input_size(problem) as u32)
                    .collect(),
                output_input_sizes: population
                    .iter()
                    .map(|genome| genome.output_input_size() as u32)
                    .collect(),
                hebbian: population
                    .iter()
                    .map(|genome| u8::from(genome.hebbian_update))
                    .collect(),
                normalization: population
                    .iter()
                    .map(|genome| u8::from(genome.normalization))
                    .collect(),
                kernel_memory: population
                    .iter()
                    .map(|genome| u8::from(genome.kernel_memory))
                    .collect(),
                phase_encoding: population
                    .iter()
                    .map(|genome| u8::from(genome.phase_encoding))
                    .collect(),
                second_derivative: population
                    .iter()
                    .map(|genome| u8::from(genome.second_derivative_estimate))
                    .collect(),
            }
        }
    }

    struct CudaDriver {
        _handle: *mut c_void,
        cu_init: CuInit,
        cu_device_get_count: CuDeviceGetCount,
        cu_device_get: CuDeviceGet,
        cu_ctx_create: CuCtxCreate,
        cu_ctx_destroy: CuCtxDestroy,
        cu_mem_alloc: CuMemAlloc,
        cu_mem_free: CuMemFree,
        cu_memcpy_htod: CuMemcpyHtoD,
        cu_memcpy_dtoh: CuMemcpyDtoH,
        cu_module_load_data: CuModuleLoadData,
        cu_module_unload: CuModuleUnload,
        cu_module_get_function: CuModuleGetFunction,
        cu_launch_kernel: CuLaunchKernel,
        cu_ctx_synchronize: CuCtxSynchronize,
        cu_get_error_string: Option<CuGetErrorString>,
    }

    impl CudaDriver {
        fn load() -> Result<Self, String> {
            let library_name = CString::new("libcuda.so.1").expect("static library name is valid");
            let handle = unsafe { dlopen(library_name.as_ptr(), RTLD_NOW) };
            if handle.is_null() {
                return Err(
                    "Could not load libcuda.so.1. Is the NVIDIA driver installed?".to_string(),
                );
            }
            Ok(Self {
                _handle: handle,
                cu_init: unsafe { symbol(handle, "cuInit")? },
                cu_device_get_count: unsafe { symbol(handle, "cuDeviceGetCount")? },
                cu_device_get: unsafe { symbol(handle, "cuDeviceGet")? },
                cu_ctx_create: unsafe { symbol(handle, "cuCtxCreate_v2")? },
                cu_ctx_destroy: unsafe { symbol(handle, "cuCtxDestroy_v2")? },
                cu_mem_alloc: unsafe { symbol(handle, "cuMemAlloc_v2")? },
                cu_mem_free: unsafe { symbol(handle, "cuMemFree_v2")? },
                cu_memcpy_htod: unsafe { symbol(handle, "cuMemcpyHtoD_v2")? },
                cu_memcpy_dtoh: unsafe { symbol(handle, "cuMemcpyDtoH_v2")? },
                cu_module_load_data: unsafe { symbol(handle, "cuModuleLoadData")? },
                cu_module_unload: unsafe { symbol(handle, "cuModuleUnload")? },
                cu_module_get_function: unsafe { symbol(handle, "cuModuleGetFunction")? },
                cu_launch_kernel: unsafe { symbol(handle, "cuLaunchKernel")? },
                cu_ctx_synchronize: unsafe { symbol(handle, "cuCtxSynchronize")? },
                cu_get_error_string: unsafe { optional_symbol(handle, "cuGetErrorString") },
            })
        }

        fn init(&self) -> Result<(), String> {
            self.check(unsafe { (self.cu_init)(0) }, "cuInit")
        }

        fn device_count(&self) -> Result<usize, String> {
            let mut count = 0;
            self.check(
                unsafe { (self.cu_device_get_count)(&mut count) },
                "cuDeviceGetCount",
            )?;
            Ok(count.max(0) as usize)
        }

        fn first_device(&self) -> Result<CudaDevice, String> {
            let mut device = 0;
            self.check(
                unsafe { (self.cu_device_get)(&mut device, 0) },
                "cuDeviceGet(0)",
            )?;
            Ok(device)
        }

        fn check(&self, code: CudaResult, operation: &str) -> Result<(), String> {
            if code == CUDA_SUCCESS {
                return Ok(());
            }
            let message = self
                .error_string(code)
                .unwrap_or_else(|| format!("CUDA error code {code}"));
            Err(format!("{operation} failed: {message}"))
        }

        fn error_string(&self, code: CudaResult) -> Option<String> {
            let function = self.cu_get_error_string?;
            let mut raw = ptr::null();
            let lookup_code = unsafe { function(code, &mut raw) };
            if lookup_code != CUDA_SUCCESS || raw.is_null() {
                return None;
            }
            Some(
                unsafe { CStr::from_ptr(raw) }
                    .to_string_lossy()
                    .into_owned(),
            )
        }
    }

    struct CudaContextGuard<'a> {
        driver: &'a CudaDriver,
        context: CudaContext,
    }

    impl<'a> CudaContextGuard<'a> {
        fn create(driver: &'a CudaDriver) -> Result<Self, String> {
            if driver.device_count()? == 0 {
                return Err("CUDA backend was requested, but no CUDA devices were found.".into());
            }
            let device = driver.first_device()?;
            let mut context = ptr::null_mut();
            driver.check(
                unsafe { (driver.cu_ctx_create)(&mut context, 0, device) },
                "cuCtxCreate",
            )?;
            Ok(Self { driver, context })
        }
    }

    impl Drop for CudaContextGuard<'_> {
        fn drop(&mut self) {
            let _ = unsafe { (self.driver.cu_ctx_destroy)(self.context) };
        }
    }

    struct CudaModuleGuard<'a> {
        driver: &'a CudaDriver,
        module: CudaModule,
    }

    impl<'a> CudaModuleGuard<'a> {
        fn load(driver: &'a CudaDriver, ptx: &str) -> Result<Self, String> {
            let ptx = CString::new(ptx).expect("static PTX has no interior nul");
            let mut module = ptr::null_mut();
            driver.check(
                unsafe { (driver.cu_module_load_data)(&mut module, ptx.as_ptr() as *const c_void) },
                "cuModuleLoadData(genome_complexity)",
            )?;
            Ok(Self { driver, module })
        }

        fn function(&self, name: &str) -> Result<CudaFunction, String> {
            let function_name = CString::new(name).map_err(|error| error.to_string())?;
            let mut function = ptr::null_mut();
            self.driver.check(
                unsafe {
                    (self.driver.cu_module_get_function)(
                        &mut function,
                        self.module,
                        function_name.as_ptr(),
                    )
                },
                name,
            )?;
            Ok(function)
        }
    }

    impl Drop for CudaModuleGuard<'_> {
        fn drop(&mut self) {
            let _ = unsafe { (self.driver.cu_module_unload)(self.module) };
        }
    }

    struct DeviceBuffer<'a, T> {
        driver: &'a CudaDriver,
        ptr: CudaDevicePtr,
        len: usize,
        _marker: PhantomData<T>,
    }

    impl<'a, T: Copy> DeviceBuffer<'a, T> {
        fn new(driver: &'a CudaDriver, len: usize) -> Result<Self, String> {
            let mut ptr = 0;
            driver.check(
                unsafe { (driver.cu_mem_alloc)(&mut ptr, len * std::mem::size_of::<T>()) },
                "cuMemAlloc",
            )?;
            Ok(Self {
                driver,
                ptr,
                len,
                _marker: PhantomData,
            })
        }

        fn from_slice(driver: &'a CudaDriver, values: &[T]) -> Result<Self, String> {
            let buffer = Self::new(driver, values.len())?;
            driver.check(
                unsafe {
                    (driver.cu_memcpy_htod)(
                        buffer.ptr,
                        values.as_ptr() as *const c_void,
                        std::mem::size_of_val(values),
                    )
                },
                "cuMemcpyHtoD",
            )?;
            Ok(buffer)
        }

        fn device_ptr(&self) -> CudaDevicePtr {
            self.ptr
        }

        fn copy_to_host(&self) -> Result<Vec<T>, String> {
            let mut values = Vec::with_capacity(self.len);
            driver_copy_to_host(self.driver, self.ptr, values.spare_capacity_mut())?;
            unsafe {
                values.set_len(self.len);
            }
            Ok(values)
        }
    }

    impl<T> Drop for DeviceBuffer<'_, T> {
        fn drop(&mut self) {
            let _ = unsafe { (self.driver.cu_mem_free)(self.ptr) };
        }
    }

    fn driver_copy_to_host<T>(
        driver: &CudaDriver,
        ptr: CudaDevicePtr,
        destination: &mut [std::mem::MaybeUninit<T>],
    ) -> Result<(), String> {
        driver.check(
            unsafe {
                (driver.cu_memcpy_dtoh)(
                    destination.as_mut_ptr() as *mut c_void,
                    ptr,
                    std::mem::size_of_val(destination),
                )
            },
            "cuMemcpyDtoH",
        )
    }

    unsafe fn symbol<T>(handle: *mut c_void, name: &str) -> Result<T, String> {
        let symbol_name = CString::new(name).expect("static symbol name is valid");
        let raw = unsafe { dlsym(handle, symbol_name.as_ptr()) };
        if raw.is_null() {
            return Err(format!("Could not find CUDA symbol {name}."));
        }
        Ok(unsafe { std::mem::transmute_copy(&raw) })
    }

    unsafe fn optional_symbol<T>(handle: *mut c_void, name: &str) -> Option<T> {
        let symbol_name = CString::new(name).expect("static symbol name is valid");
        let raw = unsafe { dlsym(handle, symbol_name.as_ptr()) };
        if raw.is_null() {
            None
        } else {
            Some(unsafe { std::mem::transmute_copy(&raw) })
        }
    }

    const GENOME_COMPLEXITY_PTX: &str = r#"
.version 6.4
.target sm_52
.address_size 64

.visible .entry genome_complexity(
    .param .u64 hidden_neurons_ptr,
    .param .u64 hidden_layers_ptr,
    .param .u64 recurrent_connections_ptr,
    .param .u64 memory_cells_ptr,
    .param .u64 input_sizes_ptr,
    .param .u64 output_input_sizes_ptr,
    .param .u64 hebbian_ptr,
    .param .u64 normalization_ptr,
    .param .u64 kernel_memory_ptr,
    .param .u64 phase_encoding_ptr,
    .param .u64 second_derivative_ptr,
    .param .u64 output_ptr,
    .param .u32 len,
    .param .u32 output_dimensions
)
{
    .reg .pred %p<2>;
    .reg .b32 %r<64>;
    .reg .b64 %rd<64>;
    .reg .f64 %fd<32>;

    ld.param.u64 %rd1, [hidden_neurons_ptr];
    ld.param.u64 %rd2, [hidden_layers_ptr];
    ld.param.u64 %rd3, [recurrent_connections_ptr];
    ld.param.u64 %rd4, [memory_cells_ptr];
    ld.param.u64 %rd5, [input_sizes_ptr];
    ld.param.u64 %rd6, [output_input_sizes_ptr];
    ld.param.u64 %rd7, [hebbian_ptr];
    ld.param.u64 %rd8, [normalization_ptr];
    ld.param.u64 %rd9, [kernel_memory_ptr];
    ld.param.u64 %rd10, [phase_encoding_ptr];
    ld.param.u64 %rd11, [second_derivative_ptr];
    ld.param.u64 %rd12, [output_ptr];
    ld.param.u32 %r1, [len];
    ld.param.u32 %r2, [output_dimensions];

    mov.u32 %r3, %ctaid.x;
    mov.u32 %r4, %ntid.x;
    mov.u32 %r5, %tid.x;
    mad.lo.u32 %r6, %r3, %r4, %r5;
    setp.ge.u32 %p1, %r6, %r1;
    @%p1 bra DONE;

    mul.wide.u32 %rd20, %r6, 4;
    cvt.u64.u32 %rd21, %r6;
    mul.wide.u32 %rd22, %r6, 8;

    add.s64 %rd30, %rd1, %rd20;
    ld.global.u32 %r10, [%rd30];
    add.s64 %rd31, %rd2, %rd20;
    ld.global.u32 %r11, [%rd31];
    add.s64 %rd32, %rd3, %rd20;
    ld.global.u32 %r12, [%rd32];
    add.s64 %rd33, %rd4, %rd20;
    ld.global.u32 %r13, [%rd33];
    add.s64 %rd34, %rd5, %rd20;
    ld.global.u32 %r14, [%rd34];
    add.s64 %rd35, %rd6, %rd20;
    ld.global.u32 %r15, [%rd35];

    sub.u32 %r16, %r11, 1;
    mul.lo.u32 %r17, %r10, %r14;
    cvt.rn.f64.u32 %fd1, %r17;
    mov.f64 %fd0, %fd1;

    add.u32 %r18, %r10, 1;
    mul.lo.u32 %r19, %r16, %r10;
    mul.lo.u32 %r20, %r19, %r18;
    cvt.rn.f64.u32 %fd2, %r20;
    add.rn.f64 %fd0, %fd0, %fd2;

    mul.lo.u32 %r21, %r15, %r2;
    cvt.rn.f64.u32 %fd3, %r21;
    add.rn.f64 %fd0, %fd0, %fd3;

    mul.lo.u32 %r22, %r16, 3;
    cvt.rn.f64.u32 %fd4, %r22;
    add.rn.f64 %fd0, %fd0, %fd4;

    cvt.rn.f64.u32 %fd5, %r12;
    mov.u32 %r23, 2;
    cvt.rn.f64.u32 %fd6, %r23;
    div.rn.f64 %fd5, %fd5, %fd6;
    add.rn.f64 %fd0, %fd0, %fd5;

    mul.lo.u32 %r24, %r13, 2;
    cvt.rn.f64.u32 %fd7, %r24;
    add.rn.f64 %fd0, %fd0, %fd7;

    add.s64 %rd40, %rd7, %rd21;
    ld.global.u8 %r25, [%rd40];
    cvt.rn.f64.u32 %fd8, %r25;
    add.rn.f64 %fd0, %fd0, %fd8;

    add.s64 %rd41, %rd8, %rd21;
    ld.global.u8 %r26, [%rd41];
    cvt.rn.f64.u32 %fd9, %r26;
    add.rn.f64 %fd0, %fd0, %fd9;

    add.s64 %rd42, %rd9, %rd21;
    ld.global.u8 %r27, [%rd42];
    cvt.rn.f64.u32 %fd10, %r27;
    add.rn.f64 %fd0, %fd0, %fd10;

    add.s64 %rd43, %rd10, %rd21;
    ld.global.u8 %r28, [%rd43];
    cvt.rn.f64.u32 %fd11, %r28;
    add.rn.f64 %fd0, %fd0, %fd11;

    add.s64 %rd44, %rd11, %rd21;
    ld.global.u8 %r29, [%rd44];
    cvt.rn.f64.u32 %fd12, %r29;
    add.rn.f64 %fd0, %fd0, %fd12;

    add.s64 %rd50, %rd12, %rd22;
    st.global.f64 [%rd50], %fd0;

DONE:
    ret;
}
"#;
}

#[cfg(feature = "cuda")]
pub(crate) use enabled::{device_count, genome_complexities};

#[cfg(not(feature = "cuda"))]
pub(crate) fn genome_complexities(
    _population: &[crate::genome::Genome],
    _problem: &crate::problem::ProblemData,
) -> Result<Vec<f64>, String> {
    Err(
        "CUDA backend was requested, but this worker was not built with --features cuda."
            .to_string(),
    )
}
