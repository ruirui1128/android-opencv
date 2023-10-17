package com.mind.open.camera;

/**
 * create by Rui on 2023-07-06
 * desc:
 */
public class PreviewData {

    public PreviewData(byte[] data, int format, int width, int height) {
        this.data = data;
        this.format = format;
        this.width = width;
        this.height = height;
    }

    private byte[] data;
    private int format;
    private int width;
    private int height;


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }
}
