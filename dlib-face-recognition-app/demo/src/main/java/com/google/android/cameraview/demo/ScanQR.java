package com.google.android.cameraview.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tzutalin.dlib.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScanQR extends AppCompatActivity implements View.OnClickListener {

    TextView name, email, dob, address;
    int BITMAP_QUALITY = 100;
    int MAX_IMAGE_SIZE = 500;
    String TAG = "AddPerson";
    private Bitmap bitmap;
    private File destination = null;
    private String imgPath = null;
    Button selectImage;
    //qr code scanner object
    private IntentIntegrator qrScan;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        //intializing scan object
        qrScan = new IntentIntegrator(this);
        findViewsById();
        qrScan.initiateScan();
    }


    private void findViewsById() {
        name = findViewById(R.id.scannedname);
        email = findViewById(R.id.scannedemail);

        dob = findViewById(R.id.scanneddob);
        //dob.setInputType(InputType.TYPE_NULL);

        address = findViewById(R.id.scannedaddress);
        selectImage = findViewById(R.id.selectimage);

        imageView = findViewById(R.id.profile_image);

        selectImage.setOnClickListener(ScanQR.this);

    }


    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                try {
                    //converting the data to json
                    JSONObject obj = new JSONObject(result.getContents());
                    //setting values to textviews
                    name.setText(obj.getString("name"));
                    email.setText(obj.getString("email"));
                    dob.setText(obj.getString("dob"));
                    address.setText(obj.getString("address"));
                    String base64Image = obj.getString("image");
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Bitmap scaleddecodedByte = AddPerson.scaleDown(decodedByte,MAX_IMAGE_SIZE,true);
                    imageView.setImageBitmap(decodedByte);
                    String targetPath = Constants.getDLibImageDirectoryPath() + "/" + name.getText().toString() + ".jpg";
                    destination = new File(targetPath);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    scaleddecodedByte.compress(Bitmap.CompressFormat.JPEG, BITMAP_QUALITY, bytes);
                    FileOutputStream fo;
                    try {
                        destination.createNewFile();
                        fo = new FileOutputStream(destination);
                        fo.write(bytes.toByteArray());
                        fo.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        //initiating the qr code scan
        Intent i = new Intent(ScanQR.this,MainActivity.class);
        startActivity(i);
    }

}


