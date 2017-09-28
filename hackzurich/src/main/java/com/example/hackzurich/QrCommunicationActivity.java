package com.example.hackzurich;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.example.hackzurich.blockchain.BlockChainCollection;

public class QrCommunicationActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback, QRCodeReaderView.OnQRCodeReadListener, CommunicationLayer {

    private static final int MY_PERMISSION_REQUEST_CAMERA = 0;
    private static final String TAG = "QrCommunicationActivity";

    public static final int EXCHANGE_BLOCKS = 1;

    private ViewGroup mainLayout;

    private TextView resultTextView;
    private ImageView qrImage;
    private QRCodeReaderView qrCodeReaderView;
    private Communicator communicator;

    private QRCodeHelper qrGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_communication);
        Intent intent = getIntent();
        int commType = intent.getIntExtra(CommunicationLayer.TYPE_MESSAGE, 0);

        if (commType == CommunicationLayer.SENDER) {
            Log.d(TAG, "Starting sender communicator");
            long amount = (long) (Float.parseFloat(intent.getStringExtra("value").toString()) * 100);
            communicator = new SenderCommunicator(this, amount);
        } else {
            Log.d(TAG, "Starting receiver communicator");
            communicator = new ReceiverCommunicator(this);
        }

        mainLayout = (ViewGroup) findViewById(R.id.main_layout);
        qrGenerator = QRCodeHelper.newInstance();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (qrCodeReaderView != null) {
            qrCodeReaderView.startCamera();
        }
        communicator.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
        }
        communicator.onEnd();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mainLayout, "Camera permission was granted.", Snackbar.LENGTH_SHORT).show();
            initQRCodeReaderView();
        } else {
            Snackbar.make(mainLayout, "Camera permission request was denied.", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed
    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        qrGenerator.setContent(text);
        communicator.onMessageReceived(text);
        Log.d(TAG, text);
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Snackbar.make(mainLayout, "Camera access is required to display the camera preview.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(QrCommunicationActivity.this, new String[]{
                            Manifest.permission.CAMERA
                    }, MY_PERMISSION_REQUEST_CAMERA);
                }
            }).show();
        } else {
            Snackbar.make(mainLayout, "Permission is not available. Requesting camera permission.",
                    Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA
            }, MY_PERMISSION_REQUEST_CAMERA);
        }
    }

    private void initQRCodeReaderView() {
        View content = getLayoutInflater().inflate(R.layout.content_communication, mainLayout, true);

        qrCodeReaderView = (QRCodeReaderView) content.findViewById(R.id.qrdecoderview);
        qrImage = (ImageView) content.findViewById(R.id.qrImage);

        qrCodeReaderView.setAutofocusInterval(1000L);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setBackCamera();
        qrCodeReaderView.startCamera();
    }

    @Override
    public void sendString(String text) {
        qrGenerator.setContent(text);
        Bitmap b = qrGenerator.generate();
        qrImage.setImageBitmap(b);
    }

    @Override
    public void errorOccured() {
        //TODO: Show error message, and stuff
        Intent intent = new Intent();
        setResult(QrCommunicationActivity.RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void finishedTransaction() {
        Intent intent = new Intent();
        setResult(QrCommunicationActivity.RESULT_OK, intent);
        finish();
    }

    @Override
    public BlockChainCollection getBCCollection() {
        return MainActivity.blockChainCollection;
    }


}
