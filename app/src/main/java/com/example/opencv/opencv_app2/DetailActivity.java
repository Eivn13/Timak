package com.example.opencv.opencv_app2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.opencv.core.Mat;

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
}
