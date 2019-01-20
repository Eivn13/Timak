package firebase.com.camera2_firebase;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import firebase.com.camera2_firebase.Helper.GraphicOverlay;
import firebase.com.camera2_firebase.Helper.RectOverlay;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {
    private static final String API_BASE_URL = "https://www.androidfacenet.tech";
    ImageView imageview;
    GraphicOverlay graphicOverlay;
    int i = 0;
    private TextureView textureView;
    public String detected_name = "Unknown";                    //Premenna do ktorej sa nastavi meno detekovanej tvare
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }
    public Bitmap cutedFace = null;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    EditText editText;
    int wasImageProcessed = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice=null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView)findViewById(R.id.textureView);
        assert textureView != null;

        textureView.setSurfaceTextureListener(textureListener);

        imageview = (ImageView)findViewById(R.id.imageView);

        graphicOverlay = (GraphicOverlay)findViewById(R.id.graphic_overlay);

        editText = (EditText) findViewById(R.id.edit_text);
    }

    public void addFaceToNet(View view)
    {
        String string = editText.getText().toString();
        if (!string.equals("")) {
            if (cutedFace != null && wasImageProcessed > 0 && string != null) {
                final Bitmap croppedBitmap = cutedFace;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
                byte[] byteArray = stream.toByteArray();
                String byte_string = Base64.encodeToString(byteArray,Base64.NO_WRAP);     //Vytvorenie stringu ktory sa bude posielat na server
                RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(API_BASE_URL).build();  //Retrofit 1.9
                ApiDefine api = restAdapter.create(ApiDefine.class);
                api.postImage(
                        string,
                        byte_string,
                        new Callback<Response>() {
                            @Override
                            public void success(Response response, Response response2) {
                                BufferedReader reader = null;
                                String result = "";
                                String tmp = "";
                                try{
                                    reader = new BufferedReader(new InputStreamReader(response.getBody().in()));
                                    while((tmp = reader.readLine()) != null) {
                                        result += tmp;
                                    }
                                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });


                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "face.txt");
                try {
                    file.createNewFile();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
                if(file.exists())
                {
                    String str = byteArray.toString();
                    try {
                        OutputStream fo = new FileOutputStream(file);
                        fo.write(byteArray);
                        fo.close();
                        Toast.makeText(this, "File maked", Toast.LENGTH_SHORT).show();
                    }
                    catch (IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                }
                new Thread(new Runnable() {
                    public void run() {
                        imageview.post(new Runnable() {
                            public void run() {
                                imageview.setImageBitmap(croppedBitmap);
                            }
                        });
                    }
                }).start();
                wasImageProcessed = 0;
                cutedFace = null;
            }
        }
    }

    public void trainImages(View view){
        boolean turnOn = true;
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(API_BASE_URL).build();
        ApiDefine api = restAdapter.create(ApiDefine.class);
        api.startTraining(
                turnOn,
                new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        BufferedReader reader = null;
                        String result = "";
                        String tmp = "";
                        try{
                            reader = new BufferedReader(new InputStreamReader(response.getBody().in()));
                            while((tmp = reader.readLine()) != null) {
                                result += tmp;
                            }
                            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void sendFoto(View view){
        if (cutedFace != null && wasImageProcessed > 0) {
            final Bitmap croppedBitmap = cutedFace;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
            byte[] byteArray = stream.toByteArray();
            String byte_string = Base64.encodeToString(byteArray, Base64.NO_WRAP);

            RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(API_BASE_URL).build();
            ApiDefine api = restAdapter.create(ApiDefine.class);
            api.recognize(
                    byte_string,
                    new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {
                            BufferedReader reader = null;
                            String result = "";
                            String tmp = "";
                            try {
                                reader = new BufferedReader(new InputStreamReader(response.getBody().in()));
                                while ((tmp = reader.readLine()) != null) {
                                    result += tmp;
                                }
                                detected_name = result;
                                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            new Thread(new Runnable() {
                public void run() {
                    imageview.post(new Runnable() {
                        public void run() {
                            imageview.setImageBitmap(croppedBitmap);
                        }
                    });
                }
            }).start();
            wasImageProcessed = 0;
            cutedFace = null;
        }
    }

    private void runFaceDetector(final Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .build();

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        processFaceResult(firebaseVisionFaces, bitmap);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void processFaceResult(List<FirebaseVisionFace> firebaseVisionFaces, Bitmap bitmap) {
        graphicOverlay.clear();
        for(FirebaseVisionFace face : firebaseVisionFaces)
        {
            Rect bounds = face.getBoundingBox();

            RectOverlay rect = new RectOverlay(graphicOverlay,bounds,detected_name);
            graphicOverlay.add(rect);
//            Toast.makeText(this, "H: "+(bitmap.getWidth() - face.getBoundingBox().right),Toast.LENGTH_SHORT).show();
            int left = face.getBoundingBox().left;
            int right = face.getBoundingBox().right;
            int top = face.getBoundingBox().top;
            int bottom = face.getBoundingBox().bottom;
            if (left < 0)
                left = 0;
            if (right < 0)
                right = 0;
            if (top < 0)
                top = 0;
            if (bottom < 0)
                bottom = 0;
            int width = right - left;
            int height = bottom - top;
            if (width < 0)
                width = bitmap.getWidth();
            if (height < 0)
                height = bitmap.getHeight();
            cutedFace = Bitmap.createBitmap(bitmap, left, top, width, height);
            wasImageProcessed++;
            break;
        }
    }
    private void processPicture() {
        if (i % 5 == 0) {
            final int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
            new Thread(new Runnable() {
                public void run() {
                    if (cameraDevice == null) {
                        return;
                    }
                    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    try {
                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
                        Size[] jpegSizes = null;
                        if (characteristics != null) {
                            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                        }

                        //volitelna velkost

                        int width = textureView.getWidth();
                        int height = textureView.getHeight();
                        if (jpegSizes != null && jpegSizes.length > 0) {
                            width = jpegSizes[0].getWidth();
                            height = jpegSizes[0].getHeight();
                        }
                        ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
                        List<Surface> outputSurface = new ArrayList<>(2);
                        outputSurface.add(reader.getSurface());
                        outputSurface.add(new Surface(textureView.getSurfaceTexture()));

                        final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        captureBuilder.addTarget(reader.getSurface());
                        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                        //kontrola orientacie zariadenia


                        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation) + 180);

                        ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                            @Override
                            public void onImageAvailable(ImageReader reader) {
                                Image image = null;
                                try {
                                    image = reader.acquireLatestImage();
                                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                    byte[] bytes = new byte[buffer.capacity()];
                                    buffer.get(bytes);
                                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    float cx = bitmap.getWidth() / 2f;
                                    float cy = bitmap.getHeight() / 2f;
                                    Matrix matrix = new Matrix();
                                    matrix.postScale(-1f, 1f, cx, cy);
                                    final Bitmap flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                                    new Thread(new Runnable() {
                                        public void run() {
                                            graphicOverlay.post(new Runnable() {
                                                public void run() {
                                                    runFaceDetector(flippedBitmap);
                                                }
                                            });
                                        }
                                    }).start();
                                    buffer.clear();
                                } finally {
                                    {
                                        if (image != null)
                                            image.close();
                                    }
                                }
                            }
                        };

                        reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                        final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                                createCameraPreview();
                            }
                        };

                        cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                try {
                                    session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                            }
                        }, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        i++;
    }

    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSessions = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{
                        android.Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallBack, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            processPicture();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            startBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() throws InterruptedException {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
}
