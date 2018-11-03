package com.example.opencv.opencv_app2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.cvtColor;

public class DetailActivity extends AppCompatActivity {
    ImageView imageview;
    Bitmap bmpInput, bmpOutput;
    Mat matInput, matOutput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Bundle extra = new Bundle();
        extra = getIntent().getExtras();

        imageview = (ImageView)findViewById(R.id.imageView);

        String photoPath = Environment.getExternalStorageDirectory() + "/frames/" + extra.getString("KEY_FILENAME");


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bmpInput = BitmapFactory.decodeFile(photoPath, options);

        imageview.setImageBitmap(bmpInput);
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
}
