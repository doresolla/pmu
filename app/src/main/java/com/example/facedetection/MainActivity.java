package com.example.facedetection;

import static android.content.ContentValues.TAG;

import static java.security.AccessController.getContext;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    public ImageView imageView;
    public Button detectBtn, selectBtn;

    public FloatingActionButton camera;

    private static int RESULT_LOAD_IMAGE = 1;
    private static int REQUEST_TAKE_PHOTO = 2;
    String path;
    File directory;

    ActivityResultLauncher<String> requestPermissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        selectBtn = findViewById(R.id.selectImgBtn);
        camera = findViewById(R.id.cameraBtn);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, RESULT_LOAD_IMAGE);
            }
        });

        detectBtn = findViewById(R.id.detectFaceBtn);
        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap inputImage = BitmapFactory.decodeFile(path);
                Paint paint = new Paint();
                paint.setStrokeWidth(10);
                paint.setColor(Color.YELLOW);
                paint.setStyle(Paint.Style.STROKE);
                Bitmap finalImage = Bitmap.createBitmap(
                        inputImage.getWidth(),
                        inputImage.getHeight(),
                        Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(finalImage);
                canvas.drawBitmap(inputImage, 0, 0, null);
                FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .setMode(FaceDetector.ACCURATE_MODE)
                        .build();
                if (!faceDetector.isOperational()) {
                    Toast.makeText(MainActivity.this, "Детектор пока не готов", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    Frame frame = new Frame.Builder().setBitmap(inputImage).build();
                    SparseArray<Face> faces = faceDetector.detect(frame);

                    for (int i = 0; i < faces.size(); i++) {
                        Face face = faces.valueAt(i);
                        float x1 = face.getPosition().x;
                        float y1 = face.getPosition().y;
                        float x2 = x1 + face.getWidth();
                        float y2 = y1 + face.getHeight();
                        RectF rect = new RectF(x1, y1, x2, y2);
                        canvas.drawRect(rect, paint);
                        for (Landmark landmark : face.getLandmarks()) {
                            int circleX = (int) landmark.getPosition().x;
                            int circleY = (int) landmark.getPosition().y;
                            canvas.drawCircle(circleX, circleY, 10, paint);
                        }
                    }
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), finalImage));
                    faceDetector.release();
                }

            }
        });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->
        {
            if (isGranted) {
                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveFullImage();
                    }
                });
            }
            else{
                Toast.makeText(MainActivity.this, "Can't continue without the required permissions", Toast.LENGTH_LONG).show();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNecessaryPermissionsAndDoSomething();
            }
        });

    }

    public void getNecessaryPermissionsAndDoSomething() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //check first if you have the permission or not
            //if you don't then launch permission request dialog
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }else saveFullImage(); //continue your work naturally
    }


    private File createImageFile() throws IOException {
        directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "JPEG_" + timestamp + "_";
        File img = null;
        try {
            img = File.createTempFile(fileName, ".jpg", directory);
        } catch (Exception ex) {
            Log.v(TAG, "Can't create file to take pic");
            Toast.makeText(this, "Please check SD card", Toast.LENGTH_LONG);
        }

        path = img.getAbsolutePath();
        Toast.makeText(this, "mCurrentPhotoPath = " + path, Toast.LENGTH_LONG);

        Log.i("Info", "--путь:" + fileName);
        return img;
    }

    private void saveFullImage() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photofile = null;
            try {
                photofile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(MainActivity.this, "error while creating", Toast.LENGTH_LONG).show();
            }
            if (photofile != null) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.example.homefolder.example.provider",
                        photofile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
            }
           }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImg = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImg, filePath, null, null, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(filePath[0]));
            cursor.close();
            Toast.makeText(MainActivity.this, "path = " + path, Toast.LENGTH_LONG);
            imageView.setImageDrawable(BitmapDrawable.createFromPath(path));
        }
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            try {
                galleryAddPic();
                setPic();

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "ResultForActivity", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

}