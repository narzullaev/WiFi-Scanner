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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Main2Activity extends AppCompatActivity {

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 1;
    private int FINE_LOCATION_PERMISSION_CODE = 1;
    private static final String TAG = "Main2Activity";
    private Button scan;
    private TextView studentName;
    private TextView studentMatric;

    private WifiManager wifiManager;
    private StudentInfo studentInfo;

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private String studentID = "";
    int iter = 1;

    int forCount = 0;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // initializing Student ID EditText and Scan Button
        scan = findViewById(R.id.scan);
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        Button logout = findViewById(R.id.logout);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        studentName = findViewById(R.id.studentName);
        studentMatric = findViewById(R.id.studentId);

        FirebaseApp.initializeApp(this);
        //Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    // user is signed in
                    WiFiScanner();
                    mDatabase.child("users").child(user.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    studentInfo = dataSnapshot.getValue(StudentInfo.class);
                                    initUserData(studentInfo);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }else{
                    // user is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)   // Smart lock enables the phone to sort out
                                    // of automatically save users' credentials try to log them in
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);


                }
            }
        };

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance().signOut(Main2Activity.this);
                mFirebaseAuth.signOut();
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(studentName.getText().toString().equals("Name")){
                    Intent intent = new Intent(Main2Activity.this, StudentDetails.class);
                    startActivity(intent);
                }
            }
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    }


    @Override
    protected void onStart() {
        super.onStart();
        wifiStatus();
    }

    protected void initUserData(StudentInfo info){
        if(info != null){
            studentName.setText(info.getStudentName());
            studentMatric.setText(info.getStudentId());
            studentID = info.getStudentId();
        }

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        String defaultNameField = "Name";
        String defaultStudentIdField = "Matric";
        studentName.setText(defaultNameField);
        studentMatric.setText(defaultStudentIdField);
    }


    // Turning WiFi on if it is not on
    private void wifiStatus(){
        if(!wifiManager.isWifiEnabled()){
            toastMessage("Turning ON WiFi");
            wifiManager.setWifiEnabled(true);
        }
    }

    private void WiFiScanner(){
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!studentName.getText().toString().equals("Name")){
                    progressBar.setVisibility(View.VISIBLE);
                    if(ContextCompat.checkSelfPermission(Main2Activity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG, "Permission Granted");
                    }else requestLocationPermission();
                    toastMessage("Scanning WiFi...");

                    for (int i=1; i<6; i++){
                        try {
                            registerReceiver(broadcastReceiver(i), new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                            wifiManager.startScan();
                        }catch(IllegalArgumentException m){
                            // studentID.setError(m.getMessage());
                        }
                    }
                }else{
                    toastMessage("Please register first.");
                }


            }
        });

    }

    private BroadcastReceiver broadcastReceiver(final int num){

        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results = wifiManager.getScanResults();
                final int resultSize = results.size();
                forCount=0;
                unregisterReceiver(this);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                for (ScanResult scanResult: results){
                    //getting WiFi scan result for one object
                    String SSID = scanResult.SSID;
                    String BSSID = scanResult.BSSID;
                    String RSSI = Integer.toString(scanResult.level);
                    String frequency = Integer.toString(scanResult.frequency);

                    //getting current time and date
                    Date date = new Date();
                    String currentTimeDate = dateFormat.format(date);
                    String currentDate = dateFormat1.format(date);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(!studentID.isEmpty() && user != null){
                        // Creating Scan Info Object
                        ScanInfo scanInfo = new ScanInfo(BSSID, SSID, RSSI, frequency, currentTimeDate);
                        mDatabase.child("scanResults").child(currentDate).child(studentID).child(""+num).push().setValue(scanInfo, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Log.d(TAG, "Completed");
                                forCount++;
                                if (resultSize == forCount) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    toastMessage("Done");
                                    forCount = 0;
                                }


                            }

                        });
                    }


                }
                iter++;
            }
        };

    }


    private void requestLocationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to send scanned WiFi resluts to database")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.INTERNET }, FINE_LOCATION_PERMISSION_CODE);
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION }, FINE_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == FINE_LOCATION_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                toastMessage("Permission Granted");
            }else {
                toastMessage("Permission Denied");
            }
        }
    }

    public void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
