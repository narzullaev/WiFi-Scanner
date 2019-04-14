package com.example.sardor.wifiscanner;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

public class Login extends AppCompatActivity {

    private MaterialEditText email;
    private MaterialEditText password;
    private FirebaseAuth mAuth;
    public AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog = new SpotsDialog.Builder().setContext(Login.this).build();
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailField);
        password = findViewById(R.id.password);

        Button loginButton = findViewById(R.id.loginButton);
        TextView signUp = findViewById(R.id.signUp);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.getText() != null && password.getText() != null) {
                    dialog.setMessage("Signing you in...");
                    dialog.show();

                    String uEmail = email.getText().toString();
                    String uPassword = password.getText().toString();

                    if (uEmail.isEmpty()) {
                        email.setError("Email is required");
                        email.requestFocus();
                        dialog.dismiss();
                        return;
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(uEmail).matches()) {
                        email.setError("Please enter valid email");
                        email.requestFocus();
                        dialog.dismiss();
                        return;
                    }

                    if (uPassword.isEmpty()) {
                        password.setError("Password is required");
                        password.requestFocus();
                        dialog.dismiss();
                        return;
                    }

                    if (uPassword.length() < 6) {
                        password.setError("Password must be at least 6 characters");
                        password.requestFocus();
                        dialog.dismiss();
                    }

                    mAuth.signInWithEmailAndPassword(uEmail, uPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Login.this, Main2Activity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        dialog.dismiss();
                                        makeMessage("Please register first");
                                    }
                                }
                            });




                }
            }
        });


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            Intent intent = new Intent(Login.this, Main2Activity.class);
            startActivity(intent);
            finish();
        }
    }

    public void makeMessage(String message) {
        Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show();
    }
}
