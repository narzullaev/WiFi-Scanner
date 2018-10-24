package com.example.sardor.wifiscanner;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Main2Activity extends AppCompatActivity {


    private int STORAGE_PERMISSION_CODE = 1;
    private static final String TAG = "Main2Activity";

    private EditText studentID;
    private Button scan;

    private WifiManager wifiManager;
    private DatabaseReference mDatabase;

    int forCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // initializing Student ID EditText and Scan Button
        studentID = findViewById(R.id.studentId);
        scan = findViewById(R.id.scan);


        //Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiStatus();
        checkForPermissions();
        WiFiScanner();


    }

    // Turning WiFi on if it is not on
    private void wifiStatus(){
        if(!wifiManager.isWifiEnabled()){
            toastMessage("Turning ON WiFi");
            wifiManager.setWifiEnabled(true);
        }
    }

    // testing permissions (optional)
    private void checkForPermissions(){
        if(ContextCompat.checkSelfPermission(Main2Activity.this,
                Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
            new AlertDialog.Builder(this)
                    .setTitle("Internet Permission")
                    .setMessage("This app uses Internet Access to send scanned WiFi logs to database")
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .create().show();
        }else{
            requestInternetPermission();
        }

        if(ContextCompat.checkSelfPermission(Main2Activity.this,
                Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED){
            new AlertDialog.Builder(this)
                    .setTitle("WiFi Permission")
                    .setMessage("This app uses WiFi Access to scan available access points.")
                    .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create().show();
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("WiFi Permission")
                    .setMessage("Please enable this permission manually from your settings")
                    .setPositiveButton("Alright", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create().show();
        }

    }



    private void WiFiScanner(){
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(Main2Activity.this,
                        Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "Permission Granted");
                }else requestInternetPermission();

                try {
                    if (studentID.getText().toString().isEmpty()) {
                        throw new IllegalArgumentException("Enter Your Student ID");
                    }

                    registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    wifiManager.startScan();
                }catch(IllegalArgumentException m){
                    studentID.setError(m.getMessage());
                }
            }
        });
    }

    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();
            final int resultSize = results.size();
            forCount=0;
            unregisterReceiver(this);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            toastMessage("Scanning WiFi...");

            for (ScanResult scanResult: results){
                //getting WiFi scan result for one object
                String ssid = scanResult.SSID;
                String bssid = scanResult.BSSID;
                String rssi = Integer.toString(scanResult.level);
                String frequency = Integer.toString(scanResult.frequency);

                //getting Student ID
                String uid = studentID.getText().toString();

                //getting current time and date
                Date date = new Date();
                String currentTimeDate = dateFormat.format(date);
                String currentDate = dateFormat1.format(date);


                // Creating Scan Info Object
                ScanInfo scanInfo = new ScanInfo(bssid, ssid, rssi, frequency, currentTimeDate);
                mDatabase.child("scanResults").child(currentDate).child(uid).push().setValue(scanInfo, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Log.d(TAG, "Completed");
                        forCount++;
                        toastMessage("Scanning Access point No: " + forCount);
                        toastMessage("Access Point No: " + forCount + " stored to database");
                        if (resultSize == forCount) {
                            toastMessage("Done");
                            forCount = 0;
                        }


                    }

                });

            }
            toastMessage(resultSize+ " access points detected");

        }
    };

    private void requestInternetPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to send scanned WiFi resluts to database")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.INTERNET }, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();

        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET }, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                toastMessage("Permission Granted");
            }else {
                toastMessage("Permission Denied");
            }
        }
    }




    public void toastMessage(String message){
        Toast.makeText(this, ""+message, Toast.LENGTH_SHORT).show();
    }
}
