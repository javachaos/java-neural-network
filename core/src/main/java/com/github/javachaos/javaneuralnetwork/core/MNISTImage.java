package com.github.javachaos.javaneuralnetwork.core;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Arrays;

public class MNISTImage {
    private final int rows;
    private final int cols;
    private final byte[] data;
    private String predictedValue;

    public MNISTImage(int rows, int cols, byte[] data, String predictedValue) {
        this.data = data;
        this.rows = rows;
        this.cols = cols;
        this.predictedValue = predictedValue;
    }

    public MNISTImage(int rows, int cols, byte[] data) {
        this(rows, cols, data, "UNKNOWN");
    }

    public byte[] getData() {
        return data;
    }

    public double[] getNormilized() {
        int[] imgInts = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            imgInts[i] = data[i];
        }
        double[] normal = new double[imgInts.length];
        int min = Arrays.stream(imgInts).min().orElseThrow();
        int max = Arrays.stream(imgInts).max().orElseThrow();
        for (int j = 0; j < normal.length; j++) {
            normal[j] = ((double) imgInts[j] - min) / (max - min * 1.0);
        }
        return normal;
    }

    public void setPredictedValue(String v) {
        this.predictedValue = v;
    }

    public BufferedImage getImage() {
        BufferedImage buff = new BufferedImage(rows, cols,
                BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buff.setRGB(i, j, data[i * rows + j]);
            }
        }
        RescaleOp op = new RescaleOp(-1.0f, 255.0f, null);
        return op.filter(buff, null);
    }

    /**
     * Convert 4 bytes to int.
     * @param b 4 byte array
     * @return signed int
     */
    private int bytesToInt(byte[] b) {
        int x;
        x = ((b[0] & 0xFF) << 24) |
                ((b[1] & 0xFF) << 16) |
                ((b[2] & 0xFF) << 8)  |
                ( b[3] & 0xFF);
        return x;
    }

    @Override
    public String toString() {
        return predictedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MNISTImage that = (MNISTImage) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
