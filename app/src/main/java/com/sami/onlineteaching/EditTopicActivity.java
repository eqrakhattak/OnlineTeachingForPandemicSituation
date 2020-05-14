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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditTopicActivity extends AppCompatActivity {

    private String topicId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private EditText inputTopicName, inputReferenceLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_topic);

        topicId = getIntent().getExtras().get("topic_id").toString();

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        inputTopicName = findViewById(R.id.inputTopicName);
        inputReferenceLinks = findViewById(R.id.inputReferenceLinks);

        findViewById(R.id.btnModifyTopic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyTopic();
            }
        });

        loadTopicInformation();

    }

    private void loadTopicInformation() {

        final FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            rootRef.child("Topics").child(topicId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild("name")){

                        String name = dataSnapshot.child("name").getValue().toString();
                        inputTopicName.setText(name);
                    }

                    if(dataSnapshot.hasChild("referenceLinks")){

                        String referenceLinks = dataSnapshot.child("referenceLinks").getValue().toString();
                        inputReferenceLinks.setText(referenceLinks);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void modifyTopic() {

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

        rootRef.child("Topics").child(topicId).child("name").setValue(name);
        rootRef.child("Topics").child(topicId).child("referenceLinks").setValue(referenceLinks)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(EditTopicActivity.this, "Topic modified", Toast.LENGTH_LONG).show();
                            updateToMainActivity();
                        }else{
                            Toast.makeText(EditTopicActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void updateToMainActivity() {

        finish();
        Intent intent = new Intent(EditTopicActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

}
