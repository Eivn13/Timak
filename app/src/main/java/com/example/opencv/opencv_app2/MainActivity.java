package com.example.opencv.opencv_app2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    // Used to load the 'native-lib' library on application startup.
    static {

    }
    Mat mRgba, imgGray, imgCanny, mmat;
    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status)
            {
                case BaseLoaderCallback.SUCCESS:{
                    javacameraview.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };
    JavaCameraView javacameraview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        javacameraview = (JavaCameraView)findViewById(R.id.java_camera_view);
        javacameraview.setCameraIndex(1);
        javacameraview.setVisibility(SurfaceView.VISIBLE);
        javacameraview.setCvCameraViewListener(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(javacameraview!=null)
        {
            javacameraview.disableView();
        }
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(javacameraview!=null)
        {
            javacameraview.disableView();
        }
    }
    protected void onResume()
    {
        super.onResume();
        if(OpenCVLoader.initDebug())
        {
            Log.d(TAG, "OpenCv Loaded");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else
        {
            Log.d(TAG, "OpenCv not Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallBack);
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mmat = new Mat(height, width, CvType.CV_8UC4);
        imgGray = new Mat(height, width,  CvType.CV_8UC1);
        imgCanny = new Mat(height, width,  CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
//        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.Canny(imgGray, imgCanny, 50, 150);
        return mRgba;
    }
    public void onClickBTN(View view)
    {
        String filename = saveImage(mRgba);
        Intent intent = new Intent(this, DetailActivity.class);
        Bundle extra = new Bundle();
        extra.putString("KEY_FILENAME",filename);
        intent.putExtras(extra);
        startActivity(intent);
    }
    public String saveImage(Mat subImg)
    {
        Bitmap bmp = null;

        try
        {
            bmp = Bitmap.createBitmap(subImg.cols(), subImg.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(subImg, bmp);
        }
        catch (CvException e)
        {
            Log.d(TAG, e.getMessage());
        }

        subImg.release();

        FileOutputStream out = null;
        String filename = UUID.randomUUID().toString() + ".png";
        String filename_ret = "";
        File sd = new File(Environment.getExternalStorageDirectory() + "/frames");
        boolean success = true;
        if(!sd.exists())
        {
            success = sd.mkdir();
        }
        if(success)
        {
            File dest = new File(sd, filename);
            try
            {
                out = new FileOutputStream(dest);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }
            finally {
                try
                {
                    if(out != null)
                    {
                        out.close();
                        filename_ret = filename;
                        Log.d(TAG, "IMG saved");
                    }
                }
                catch (IOException e)
                {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
        return filename_ret;
    }
}
