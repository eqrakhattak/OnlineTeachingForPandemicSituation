package com.sami.onlineteaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UploadTopicActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private EditText inputTopicName, inputReferenceLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_topic);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        inputTopicName = findViewById(R.id.inputTopicName);
        inputReferenceLinks = findViewById(R.id.inputReferenceLinks);

        findViewById(R.id.btnAddTopic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTopic();
            }
        });

    }

    private void addTopic() {

        String uid = mAuth.getCurrentUser().getUid();
        final String randomId = generateRandomId();
        String name = inputTopicName.getText().toString();
        String referenceLinks = inputReferenceLinks.getText().toString();

        if (name.isEmpty()) {
            inputTopicName.setError("Topic name is required");
            inputTopicName.requestFocus();
            return;
        }

        if (name.length() < 7) {
            inputTopicName.setError("Topic name can't be this small");
            inputTopicName.requestFocus();
            return;
        }

        if (referenceLinks.isEmpty()) {
            inputReferenceLinks.setError("Reference links are required");
            inputReferenceLinks.requestFocus();
            return;
        }

        if (referenceLinks.length() < 7) {
            inputReferenceLinks.setError("Reference links can't be this small");
            inputReferenceLinks.requestFocus();
            return;
        }

        rootRef.child("Topics").child(randomId).child("name").setValue(name);
        rootRef.child("Topics").child(randomId).child("referenceLinks").setValue(referenceLinks);
        rootRef.child("Topics").child(randomId).child("uid").setValue(uid)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(UploadTopicActivity.this, "Topic added", Toast.LENGTH_LONG).show();
                            addToTeachersTree(randomId);
                        }else{
                            Toast.makeText(UploadTopicActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void addToTeachersTree(String randomId) {

        rootRef.child("Teachers").child(mAuth.getCurrentUser().getUid()).child("topics").child(randomId).child("message").setValue("Topic Added");
        updateToUploadQuizActivity(randomId);

    }

    private String generateRandomId() {

        long inSeconds = System.currentTimeMillis() / 10000;
        long negative = 999999999 - inSeconds;
        return String.valueOf(negative);

    }

    private void updateToUploadQuizActivity(String randomId) {

        finish();
        Intent intent = new Intent(UploadTopicActivity.this, UploadQuizActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("randomId", randomId);
        startActivity(intent);

    }

}
