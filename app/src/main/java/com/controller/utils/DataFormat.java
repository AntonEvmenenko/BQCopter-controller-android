package com.controller.utils;

/*
struct Data {
    int16_t roll;
    int16_t pitch;
    int16_t yaw;
    int16_t checksum;
};
*/

public class DataFormat {
    public static final int BYTES_PER_PACKAGE = 16;

    private short roll = 0;
    private short pitch = 0;
    private short yaw = 0;

    private boolean error = false;

    public DataFormat(String s) {
        if (s.length() == BYTES_PER_PACKAGE) {
            short roll = 0, pitch = 0, yaw = 0, checksum = 0, tempChecksum = 0;
            try {
                roll = (short)Integer.parseInt(s.substring(0, 4), 16);
                pitch = (short)Integer.parseInt(s.substring(4, 8), 16);
                yaw = (short)Integer.parseInt(s.substring(8, 12), 16);
                checksum = (short)Integer.parseInt(s.substring(12, 16), 16);
                tempChecksum = (short)(roll + pitch + yaw);
                if (tempChecksum == checksum) {
                    this.roll = roll;
                    this.pitch = pitch;
                    this.yaw = yaw;
                } else {
                    error = true;
                }
            } catch (Exception e) {
                error = true;
            }
        } else {
            error = true;
        }
    }

    public boolean parsingError() {
        return error;
    }

    public int getRoll() {
        return roll;
    }

    public int getPitch() {
        return pitch;
    }

    public int getYaw() {
        return yaw;
    }
}
