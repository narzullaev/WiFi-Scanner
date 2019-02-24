package com.example.sardor.wifiscanner;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;
import com.rengwuxian.materialedittext.MaterialEditText;
public class StudentDetails extends AppCompatActivity {

    private MaterialEditText studentName;
    private MaterialEditText studentId;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        // Make the activity swipe to back
        Slidr.attach(this);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        studentId = findViewById(R.id.studentId);
        studentName = findViewById(R.id.studentName);
        Button update = findViewById(R.id.update);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(studentName.getText() != null && studentId.getText() != null){
                    progressBar.setVisibility(View.VISIBLE);
                    String name = studentName.getText().toString();
                    String id = studentId.getText().toString();
                    String UID = currentUser.getUid();
                    if(name.isEmpty()){
                        studentName.setError("Name is required");
                    }

                    if(id.isEmpty()){
                        studentId.setError("Student ID is required");
                    }

                    if(!name.isEmpty() && !id.isEmpty()){
                        writeToFirebase(name, id, UID);
                    }else{
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                }

            }
        });
    }

    private void writeToFirebase(String name, String id, String uid) {
        StudentInfo newStudentInfo = new StudentInfo(name,id);
        databaseReference.child("users").child(uid).setValue(newStudentInfo, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                makeMessage("Done!");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth != null){
            currentUser = mAuth.getCurrentUser();
            databaseReference.child("users").child(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            StudentInfo studentInfo = dataSnapshot.getValue(StudentInfo.class);
                            if(studentInfo != null){
                                studentName.setText(studentInfo.getStudentName());
                                studentId.setText(studentInfo.getStudentId());
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            makeMessage("Error: "+databaseError.getMessage());
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    public void makeMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
