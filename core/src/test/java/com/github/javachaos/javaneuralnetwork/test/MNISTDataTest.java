package com.github.javachaos.javaneuralnetwork.test;

import com.github.javachaos.javaneuralnetwork.core.MNISTImage;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MNISTDataTest {

    @Test
    void testMNISTImage() {
        MNISTImage image = new MNISTImage(2, 2, new byte[]{0, 1, 2, 3});
        BufferedImage img = image.getImage();
        assertEquals(2, img.getWidth());
        assertEquals(2, img.getHeight());
        assertArrayEquals(new double[]{0.0, 1.0 / 3.0, 2.0 / 3.0, 1.0},
                image.getNormilized(), 0.0000001);
    }
}
