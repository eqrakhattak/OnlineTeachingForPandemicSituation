package com.sami.onlineteaching;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private static final int CHOOSE_IMAGE = 6117;
    private ImageView imageProfile;
    private EditText inputFullName, inputUsername;
    private Uri uriProfileImage;
    private String urlProfileImage;
    private ProgressBar barUploadImage;
    private String occupation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle b = getIntent().getExtras();

        occupation = b.getString("occupation");

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        imageProfile = findViewById(R.id.imageProfile);
        inputFullName = findViewById(R.id.inputFullName);
        inputUsername = findViewById(R.id.inputUsername);
        barUploadImage = findViewById(R.id.barUploadImage);

        loadUserInformation(occupation);

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        findViewById(R.id.btnSaveProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile(occupation);
            }
        });
    }

    private void imageChooser() {

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select profile image"), CHOOSE_IMAGE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uriProfileImage = data.getData();

            CropImage.activity(uriProfileImage)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                uriProfileImage = result.getUri();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                    imageProfile.setImageBitmap(bitmap);

                    uploadImageToFirebaseStorage();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE && result != null) {
                Toast.makeText(ProfileActivity.this, result.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    private void uploadImageToFirebaseStorage() {

        final StorageReference refProfileImage = FirebaseStorage.getInstance().getReference("profileImages/" + System.currentTimeMillis() + ".jpg");
        final FirebaseUser user = mAuth.getCurrentUser();

        if (uriProfileImage != null) {
            barUploadImage.setVisibility(View.VISIBLE);
            refProfileImage.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    barUploadImage.setVisibility(View.GONE);
                    refProfileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            urlProfileImage = uri.toString();

                            if(user != null) {

                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                        .setPhotoUri(Uri.parse(urlProfileImage))
                                        .build();

                                user.updateProfile(profile).addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ProfileActivity.this, "Uploaded!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    barUploadImage.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void saveProfile(final String occupation) {

        String fullName = inputFullName.getText().toString();
        String username = inputUsername.getText().toString();

        if (fullName.isEmpty()) {
            inputFullName.setError("Full name is required");
            inputFullName.requestFocus();
            return;
        }

        if (fullName.length() < 4) {
            inputFullName.setError("Full name can't be this small");
            inputFullName.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            inputUsername.setError("Username is required");
            inputUsername.requestFocus();
            return;
        }

        if (username.length() < 2) {
            inputUsername.setError("Username can't be this small");
            inputUsername.requestFocus();
            return;
        }

        String currentUserID = mAuth.getCurrentUser().getUid();

        rootRef.child(occupation).child(currentUserID).child("fullname").setValue(fullName);
        rootRef.child(occupation).child(currentUserID).child("username").setValue(username);

        rootRef.child(occupation).child(currentUserID).child("uid").setValue(currentUserID)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_LONG).show();

                            if(occupation.equals("Teachers")) updateToMainActivity();
                            else updateToMainActivityStudents();

                        }else{
                            Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            updateUI();
        }

    }

    private void loadUserInformation(String occupation) {

        final FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            if (user.getPhotoUrl() != null) {

                String photoUrl = user.getPhotoUrl().toString();

                Glide.with(this)
                        .load(photoUrl)
                        .into(imageProfile);
            }

            String currentUserID = user.getUid();

            rootRef.child(occupation).child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild("fullname")){
                        String fullName = dataSnapshot.child("fullname").getValue().toString();
                        inputFullName.setText(fullName);
                    }

                    if(dataSnapshot.hasChild("username")){
                        String phone = dataSnapshot.child("username").getValue().toString();
                        inputUsername.setText(phone);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void updateUI() {

        finish();
        Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void updateToMainActivity() {

        finish();
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void updateToMainActivityStudents() {

        finish();
        Intent intent = new Intent(ProfileActivity.this, MainActivityStudents.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

}