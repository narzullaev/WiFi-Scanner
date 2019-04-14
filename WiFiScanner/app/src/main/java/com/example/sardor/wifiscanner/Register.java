package com.example.sardor.wifiscanner;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

public class Register extends AppCompatActivity {

    private DatabaseReference databaseReference;
    public AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        dialog = new SpotsDialog.Builder().setContext(Register.this).build();

        final MaterialEditText name = findViewById(R.id.studentName);
        final MaterialEditText email = findViewById(R.id.emailField);
        Button register = findViewById(R.id.registerButton);
        TextView questionLogin = findViewById(R.id.question);
        final MaterialEditText studentID = findViewById(R.id.studentId);
        final MaterialEditText password = findViewById(R.id.password);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setMessage("Loading...");
                dialog.show();

                if (name.getText() != null && email.getText() != null &&
                        studentID.getText() != null && password.getText() != null) {
                    final String studentName = name.getText().toString();
                    String studentEmail = email.getText().toString();
                    final String studentId = studentID.getText().toString();
                    String passwordField = password.getText().toString();

                    if(!Patterns.EMAIL_ADDRESS.matcher(studentEmail).matches()){
                        email.setError("Please enter valid email");
                        email.requestFocus();
                        dialog.dismiss();
                        return;
                    }

                    if(passwordField.isEmpty()){
                        password.setError("Password is required");
                        password.requestFocus();
                        dialog.dismiss();
                        return;
                    }

                    if(passwordField.length() < 6){
                        password.setError("Password must be at least 6 characters");
                        password.requestFocus();
                        dialog.dismiss();
                        return;
                    }



                    mAuth.createUserWithEmailAndPassword(studentEmail, passwordField)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        StudentInfo studentInfo = new StudentInfo(studentName, studentId);
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if(user != null){
                                            String UID = user.getUid();
                                            writeToFirebase(studentName, studentId, UID);
                                        }
                                    }else{
                                        if (task.getException()instanceof FirebaseAuthUserCollisionException){
                                            makeMessage("This user is already active");
                                        }else{
                                            if(task.getException() != null){
                                                makeMessage(task.getException().getMessage());
                                            }
                                        }
                                    }
                                }
                            }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            dialog.dismiss();
                            Intent intent = new Intent(Register.this, Main2Activity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        });

        questionLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            }
        });


    }

    private void writeToFirebase(String name, String id, String uid) {
        StudentInfo newStudentInfo = new StudentInfo(name,id);
        databaseReference.child("users").child(uid).setValue(newStudentInfo, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                makeMessage("User Registered Successfully");
                dialog.dismiss();
            }
        });
    }

    public void makeMessage(String message) {
        Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show();
    }
}

