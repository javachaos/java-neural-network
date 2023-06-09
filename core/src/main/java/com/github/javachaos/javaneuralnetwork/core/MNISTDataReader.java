package com.github.javachaos.javaneuralnetwork.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MNISTDataReader {
    private int magic;
    private int numImages;
    private int numRows;
    private int numCols;
    private byte[] data;
    private List<MNISTImage> images;

    public MNISTDataReader() {
        parseData();
    }

    private void parseData() {
        byte[] allBytes = readRawFile();
        byte[] magic =     new byte[4];
        byte[] numImages = new byte[4];
        byte[] numRows =   new byte[4];
        byte[] numCols =   new byte[4];
        this.data = new byte[allBytes.length - 16];
        this.images = new ArrayList<>();

        System.arraycopy(allBytes, 0,  magic,     0, 4);
        System.arraycopy(allBytes, 4,  numImages, 0, 4);
        System.arraycopy(allBytes, 8,  numRows,   0, 4);
        System.arraycopy(allBytes, 12, numCols,   0, 4);
        System.arraycopy(allBytes, 16, data, 0, allBytes.length - 16);
        this.magic = bytesToInt(magic);
        this.numImages = bytesToInt(numImages);
        this.numRows = bytesToInt(numRows);
        this.numCols = bytesToInt(numCols);
        for (int i = 0; i < data.length; i+=(this.numCols * this.numRows)) {
            byte[] img = new byte[this.numCols * this.numRows];
            System.arraycopy(data, i, img, 0, img.length);
            this.images.add(new MNISTImage(this.numRows, this.numCols, img));
        }
    }

    private byte[] readRawFile() {
        try (BufferedInputStream bin = new BufferedInputStream(
                new URL(Constants.MNIST_URL).openStream())) {
            return decompressGzipToBytes(bin);
        } catch (IOException | NoSuchAlgorithmException fnf) {
            fnf.printStackTrace();
        }
        return new byte[0];
    }

    private byte[] decompressGzipToBytes(BufferedInputStream source) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(source)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }
        }
        if (checkHash(output.toByteArray())) {
            return output.toByteArray();
        }
        return new byte[0];
    }

    private boolean checkHash(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(input);
        return bytesToHex(encodedhash).equals(Constants.MNIST_SHA256);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


    public List<MNISTImage> getImages() {
        return images;
    }

    public int getMagicNumber() {
        return magic;
    }

    public int getNumImages() {
        return numImages;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public byte[] getData() {
        return data;
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
}
