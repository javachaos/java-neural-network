package com.neuralnetwork.shared.tests.training;

import com.neuralnetwork.shared.training.TrainSample;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TrainSampleTest {

    List<Double> emptyList = new ArrayList<>();
    List<Double> oneElement = new ArrayList<>() {{ add(1.0); }};
    List<Double> twoElement = new ArrayList<>() {{ add(1.0); add(2.0); }};

    @Test
    void createTrainSample_shouldNotBeNull() {
        Exception ex = assertThrows( IllegalArgumentException.class,
                () -> new TrainSample(null, null), "Inputs should not be null.");
        assertEquals("Inputs should not be null.", ex.getMessage());
        ex = assertThrows( IllegalArgumentException.class,
                () -> new TrainSample(oneElement, null), "Inputs should not be null.");
        assertEquals("Inputs should not be null.", ex.getMessage());
        ex = assertThrows( IllegalArgumentException.class,
                () -> new TrainSample(null, oneElement), "Inputs should not be null.");
        assertEquals("Inputs should not be null.", ex.getMessage());
    }

    @Test
    void createTrainSample_shouldNotBeEmpty() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> new TrainSample(emptyList, emptyList), "Input vectors should not be empty.");
        assertEquals("Input vectors should not be empty.", ex.getMessage());
        ex = assertThrows(IllegalArgumentException.class,
                () -> new TrainSample(emptyList, oneElement), "Input vectors should not be empty.");
        assertEquals("Input vectors should not be empty.", ex.getMessage());
        ex = assertThrows(IllegalArgumentException.class,
                () -> new TrainSample(oneElement, emptyList), "Input vectors should not be empty.");
        assertEquals("Input vectors should not be empty.", ex.getMessage());
    }

    @Test
    void createTrainSample_inputLengthsShouldBeEqual() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> new TrainSample(twoElement, oneElement), "Input vectors should be the same length.");
        assertEquals("Input vectors should be the same length.", ex.getMessage());
    }

    @Test
    void getOutputsShouldNotBeNull() {
        List<Double> oneElementTwo = new ArrayList<>() {{ add(2.0); }};
        assertNotNull(new TrainSample(oneElement, oneElement).getOutputs());
        assertEquals(new TrainSample(oneElementTwo, oneElement).getOutputs(), oneElement);
        assertNotEquals(new TrainSample(oneElement, oneElementTwo).getOutputs(), oneElement);
    }

    @Test
    void getInputsShouldNotBeNull() {
        List<Double> oneElementThree = new ArrayList<>() {{ add(3.0); }};
        assertNotNull(new TrainSample(oneElement, oneElement).getInputs());
        assertEquals(new TrainSample(oneElement, oneElementThree).getInputs(), oneElement);
        assertNotEquals(new TrainSample(oneElement, oneElementThree).getInputs(), oneElementThree);
    }
}
