package com.example.opencv.opencv_app2;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.cvtColor;

public class DetailActivity extends AppCompatActivity {
    ImageView imageview;
    Bitmap bmpInput, bmpOutput;
    Mat matInput, matOutput;
    static{
        System.loadLibrary("MyLibs");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Bundle extra = new Bundle();
        extra = getIntent().getExtras();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        imageview = (ImageView)findViewById(R.id.imageView);

        String photoPath = Environment.getExternalStorageDirectory() + "/frames/" + extra.getString("KEY_FILENAME");


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bmpInput = BitmapFactory.decodeFile(photoPath, options);

        imageview.setImageBitmap(bmpInput);

        matInput = convertBitMap2Mat(bmpInput);
        matOutput = new Mat(matInput.rows(),matInput.cols(), CvType.CV_8UC3);
    }

    Mat convertBitMap2Mat(Bitmap rgbaImage)
    {
        Mat rgbaMat = new Mat(rgbaImage.getHeight(), rgbaImage.getWidth(), CvType.CV_8UC4);
        Bitmap bmp32 = rgbaImage.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, rgbaMat);

        Mat rgbMat = new Mat(rgbaImage.getHeight(), rgbaImage.getWidth(), CvType.CV_8UC3);
        cvtColor(rgbaMat, rgbMat, Imgproc.COLOR_RGB2BGR, 3);
        return rgbaMat;
    }

    public void proccess_img(View view)
    {
        boolean bol = OpenCVLoader.initDebug();
        if(bol) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            NativeClass.LandmarkDetection(matInput.getNativeObjAddr(), matOutput.getNativeObjAddr());
                            bmpOutput = convertMat2Bitmap(matOutput);
                            imageview.setImageBitmap(bmpOutput);
                        }
                    });
                }
            };
            thread.start();
        }
        else
        {
            Toast.makeText(DetailActivity.this,"Nenacitalo OpenCv", Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap convertMat2Bitmap(Mat img){
        int width=img.width();
        int height=img.height();
        Bitmap bmp;
        bmp=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Mat tmp;
        tmp=img.channels()==1?new Mat(width,height, CvType.CV_8UC1,new Scalar(1)):new Mat(width,height,CvType.CV_8UC3,new Scalar(3));
        try{
            if(img.channels()==3){
                Imgproc.cvtColor(img,tmp,Imgproc.COLOR_RGB2BGRA);
            }else if(img.channels()==1){
                Imgproc.cvtColor(img,tmp,Imgproc.COLOR_GRAY2BGRA);
            }
            Utils.matToBitmap(tmp,bmp);
        }catch(Exception e){
            Log.d("Exception",e.getMessage());
        }
        return bmp;
    }
}
