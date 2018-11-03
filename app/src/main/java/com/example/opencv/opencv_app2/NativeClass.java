package com.example.opencv.opencv_app2;

public class NativeClass {
    public native static String getMessage();
    public native static void LandmarkDetection(long addrInput, long addrOutput);
}
