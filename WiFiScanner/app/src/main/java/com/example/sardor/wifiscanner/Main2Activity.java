package com.example.sardor.wifiscanner;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class Main2Activity extends AppCompatActivity {

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "Main2Activity";
    public android.app.AlertDialog dialog;
    public android.app.AlertDialog dialog1;
    private int FINE_LOCATION_PERMISSION_CODE = 1;
    private Button presentButton;
    private Button absentButton;
    private TextView studentName;
    private TextView studentMatric;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private List<Double> locationAccuracy;
    private WifiManager wifiManager;
    private StudentInfo studentInfo;
    private CoordinatorLayout coordinatorLayout;
    private double longitude;
    private double latitude;

    private Spinner spinner;

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String studentID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        dialog = new SpotsDialog.Builder().setContext(Main2Activity.this).build();


        // initializing Student ID EditText and Scan Button
        presentButton = findViewById(R.id.presentBTN);
        absentButton = findViewById(R.id.absentBTN);
        locationAccuracy = new ArrayList<>();
        spinner = findViewById(R.id.spinner);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        studentName = findViewById(R.id.studentName);
        studentMatric = findViewById(R.id.studentId);


        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        FirebaseApp.initializeApp(this);
        //Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
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
                } else {
                    // user is signed out
                    firebaseAuth.signOut();
                    Intent intent = new Intent(Main2Activity.this, Login.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        initSpinner();

        initLocation();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void initSpinner(){
        List<String> lectures = new ArrayList<>();
        lectures.add("Select Subject");
        lectures.add("SKJ1013 JAVA LECTURE");
        lectures.add("SKJ1013 JAVA LABS");
        lectures.add("SKJ1033 DEC LECTURE");
        lectures.add("SKJ1043 DEC LABS");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lectures);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                // sign out
                toastMessage("Signed out");
                mFirebaseAuth.signOut();
                Intent intent = new Intent(Main2Activity.this, Login.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                toastMessage("Signed in!");
            } else if (resultCode == RESULT_CANCELED) {
                toastMessage("Sign in canceled");
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        wifiStatus();

    }

    protected void initUserData(StudentInfo info) {
        if (info != null) {
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
        locationAccuracy.clear();
        studentName.setText(defaultNameField);
        studentMatric.setText(defaultStudentIdField);
    }

    // Turning WiFi on if it is not on
    private void wifiStatus() {
        if (!wifiManager.isWifiEnabled()) {
            toastMessage("Turning ON WiFi");
            wifiManager.setWifiEnabled(true);
        }
    }

    private void initLocation(){
        dialog.setMessage("Please wait. Initializing...");
        dialog.show();
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                double acc = location.getAccuracy();
                locationAccuracy.add(acc);
                if (locationAccuracy.size()>5){
                    dialog.dismiss();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        scanLocation();
    }

    private void WiFiScanner() {
        presentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(spinner.getSelectedItemId()!=0){
                    final String className = spinner.getSelectedItem().toString();
                    dialog1 = new SpotsDialog.Builder().setContext(Main2Activity.this).build();
                    dialog1.setMessage("Scanning...");
                    dialog1.show();
                    writeScannerResult("Present", className);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            writeScannerResult("Present", className);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    writeScannerResult("Present", className);

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            writeScannerResult("Present", className);
                                            dialog1.dismiss();
                                            toastMessage("done");
                                        }
                                    }, 4000);
                                }
                            },4000);
                        }
                    }, 4000);
                }

            }
        });


        absentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(spinner.getSelectedItemId()!=0){
                    final String className = spinner.getSelectedItem().toString();
                    dialog1 = new SpotsDialog.Builder().setContext(Main2Activity.this).build();
                    dialog1.setMessage("Scanning...");
                    dialog1.show();
                    writeScannerResult("Absent", className);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            writeScannerResult("Absent", className);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    writeScannerResult("Absent", className);

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            writeScannerResult("Absent", className);
                                            dialog1.dismiss();
                                        }
                                    }, 4000);
                                }
                            },4000);
                        }
                    }, 4000);
                }

            }
        });

    }


    private void writeScannerResult(final String status, String className) {
        if (!studentName.getText().toString().equals("Name")) {
            if (wifiManager.isWifiEnabled()) {
                if (ContextCompat.checkSelfPermission(Main2Activity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission Granted");
                } else requestLocationPermission();
                try {
                    double minAcc = Collections.min(locationAccuracy);
                    registerReceiver(broadcastReceiver(minAcc, status, className), new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    wifiManager.startScan();
                    dialog.dismiss();

                } catch (IllegalArgumentException m) {
                    // studentID.setError(m.getMessage());
                    dialog.dismiss();
                }


            } else {
                dialog.dismiss();
                toastMessage("Turn on your WiFi");
            }
        } else {
            dialog.dismiss();
            toastMessage("Please register first.");
        }


    }

    private BroadcastReceiver broadcastReceiver(final double minAcc,  final String status, final String className) {

        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                List<ScanResult> results = wifiManager.getScanResults();
                unregisterReceiver(this);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                for (ScanResult scanResult : results) {
                    //getting WiFi presentButton result for one object
                    String SSID = scanResult.SSID;
                    String BSSID = scanResult.BSSID;
                    String RSSI = Integer.toString(scanResult.level);
                    String frequency = Integer.toString(scanResult.frequency);

                    //getting current time and date
                    Date date = new Date();
                    String currentTimeDate = dateFormat.format(date);
                    String currentDate = dateFormat1.format(date);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (!studentID.isEmpty() && user != null) {
                        // Creating Scan Info Object
                        ScanInfo scanInfo = new ScanInfo(BSSID, SSID, RSSI, frequency, currentTimeDate, latitude, longitude, minAcc);
                        mDatabase.child(className).child(currentDate).child(status)
                                .child(studentID).child(BSSID)
                                .setValue(scanInfo, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        Log.d(TAG, "Scan Results are Completed");
                                    }

                                });
                    }


                }

            }
        };

    }


    private void requestLocationPermission() {
        dialog.dismiss();
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to send scanned WiFi resluts to database")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.INTERNET}, FINE_LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toastMessage("Permission Granted");
            } else {
                toastMessage("Permission Denied");
            }
        }
    }

    private void scanLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates("gps", 2000, 0, locationListener);
    }

    public void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
