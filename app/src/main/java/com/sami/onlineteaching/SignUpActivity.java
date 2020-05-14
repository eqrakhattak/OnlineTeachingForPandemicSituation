package com.sami.onlineteaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private EditText inputEmail, inputPassword;
    private SwitchMaterial btnSwitch;
    private ProgressBar barSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnSwitch = findViewById(R.id.btnSwitch);
        barSignUp = findViewById(R.id.barSignUp);

        findViewById(R.id.btnSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.btnSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEmailPasswordAccount();
            }
        });
    }

    private void createEmailPasswordAccount() {

        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (email.isEmpty()) {
            inputEmail.setError("Email is required");
            inputEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("Email is not valid");
            inputEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            inputPassword.setError("Password is required");
            inputPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            inputPassword.setError("Password is weak");
            inputPassword.requestFocus();
            return;
        }

        barSignUp.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            final FirebaseUser user = mAuth.getCurrentUser();
                            String currentUserId = user.getUid();

                            if(btnSwitch.isChecked()){

                                registerTeacher(currentUserId);
                            }else{

                                registerStudent(currentUserId);
                            }

                        } else {

                            barSignUp.setVisibility(View.GONE);

                            // If sign in fails, display a message to the user.
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(SignUpActivity.this, "User already registered",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void registerTeacher(String currentUserId) {

        final FirebaseUser user = mAuth.getCurrentUser();

        rootRef.child("Teachers").child(currentUserId).child("occupation").setValue("teacher")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(SignUpActivity.this, "Profile created", Toast.LENGTH_LONG).show();
                            teachersUpdateUI(user);
                        }else{
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void registerStudent(String currentUserId) {

        final FirebaseUser user = mAuth.getCurrentUser();

        rootRef.child("Students").child(currentUserId).child("level").setValue("1");
        rootRef.child("Students").child(currentUserId).child("target").setValue("1");
        rootRef.child("Students").child(currentUserId).child("occupation").setValue("student")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(SignUpActivity.this, "Profile created", Toast.LENGTH_LONG).show();
                            studentsUpdateUI(user);
                        }else{
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void teachersUpdateUI(FirebaseUser currentUser) {

        if(currentUser != null){
            barSignUp.setVisibility(View.GONE);
            finish();
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void studentsUpdateUI(FirebaseUser currentUser) {

        if(currentUser != null){
            barSignUp.setVisibility(View.GONE);
            finish();
            Intent intent = new Intent(SignUpActivity.this, MainActivityStudents.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
