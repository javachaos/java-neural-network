/*******************************************************************************
 * Copyright (c) 2013 Fred .
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU
 * Public License v3.0 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred  - initial API and implementation
 ******************************************************************************/
package com.github.javachaos.javaneuralnetwork.opencl.node;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jocl.CL.*;

/**
 * Main class.
 * 
 *
 */
public final class Main {

    /**
     * Unused Ctor.
     */
    private Main() {
    }

    /**
     * Logger instance.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(Main.class);

    /**
     * The number of memory objects to be created.
     */
    private static final int NUM_DATA = 3;

    /**
     * Number of data points.
     */
    private static final int N = 10000000;

    /**
     * The source code of the OpenCL program to execute.
     */
    private static final String PROGRAM_SOURCE =
        "__kernel void "
        + "sampleKernel(__global const float *a,"
        + "             __global const float *b,"
        + "             __global float *c)"
        + "{"
        + "    int gid = get_global_id(0);"
        + "    c[gid] = a[gid] * b[gid] * a[gid];"
        + "}";

    /**
     * The entry point of this sample.
     *
     * @param args Not used
     */
    public static void main(final String[] args) {
        // Create input- and output data
        float[] srcArrayA = new float[N];
        float[] srcArrayB = new float[N];
        float[] dstArray = new float[N];
        for (int i = 0; i < N; i++) {
            srcArrayA[i] = i;
            srcArrayB[i] = i;
        }
        Pointer srcA = Pointer.to(srcArrayA);
        Pointer srcB = Pointer.to(srcArrayB);
        Pointer dst = Pointer.to(dstArray);

        // The platform, device type and device number
        // that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Obtain the number of platforms
        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int[] numDevicesArray = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id[] devices = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);

        // Create a command-queue for the selected device
        cl_command_queue commandQueue =
                clCreateCommandQueueWithProperties(context, device, null, null);

        // Allocate the memory objects for the input- and output data
        cl_mem[] memObjects = new cl_mem[NUM_DATA];
        memObjects[0] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_float * (long) N, srcA, null);
        memObjects[1] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_float * (long) N, srcB, null);
        memObjects[2] = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_float * (long) N, null, null);

        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context,
            1, new String[] {PROGRAM_SOURCE} , null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);
        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "sampleKernel", null);
        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0,
            Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1,
            Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2,
            Sizeof.cl_mem, Pointer.to(memObjects[2]));
        // Set the work-item dimensions
        long[] globalWorkSize = new long[]{N};
        long[] localWorkSize = new long[]{1};

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            globalWorkSize, localWorkSize, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
                (long) N * Sizeof.cl_float, dst, 0, null, null);

        // Release kernel, program, and memory objects
        clReleaseMemObject(memObjects[0]);
        clReleaseMemObject(memObjects[1]);
        clReleaseMemObject(memObjects[2]);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
        float first = dstArray[0];
        float last = dstArray[dstArray.length - 1];
        LOGGER.info("{} {}", first, last);
    }
}
