package com.github.javachaos.javaneuralnetwork.test;

import com.github.javachaos.javaneuralnetwork.core.MNISTDataReader;
import com.github.javachaos.javaneuralnetwork.core.MNISTImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class MNISTDataTest {
    private static final Logger LOGGER = LogManager.getLogger(MNISTDataTest.class);

    @Test
    void testMNISTImage() {
        MNISTDataReader dr = new MNISTDataReader("/train-images-idx3-ubyte");
        MNISTImage image = dr.getImages().get((int) (Math.random() * 60000));
        BufferedImage img = image.getImage();
        // Create an ImageIcon from the BufferedImage
        ImageIcon icon = new ImageIcon(img);
        // Show the dialog with the image
        JOptionPane.showMessageDialog(null, icon, "Image Dialog", JOptionPane.PLAIN_MESSAGE);
        LOGGER.debug("Normal Vector: {}", image.getNormilized());
    }
}
