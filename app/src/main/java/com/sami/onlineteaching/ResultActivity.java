package com.sami.onlineteaching;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResultActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference studentRef;
    private TextView textTotal, textCorrect, textIncorrect, textPoints, textResult;
    private LottieAnimationView animationResult;
    private String receiverTopicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();

        receiverTopicId = intent.getExtras().get("visit_topic_id").toString();

        mAuth = FirebaseAuth.getInstance();
        studentRef = FirebaseDatabase.getInstance().getReference().child("Students").child(mAuth.getCurrentUser().getUid());

        textTotal = findViewById(R.id.textTotal);
        textCorrect = findViewById(R.id.textCorrect);
        textIncorrect = findViewById(R.id.textIncorrect);
        textPoints = findViewById(R.id.textPoints);
        textResult = findViewById(R.id.textResult);
        animationResult = findViewById(R.id.animationResult);

        String total = intent.getStringExtra("total");
        String correct = intent.getStringExtra("correct");
        String incorrect = intent.getStringExtra("incorrect");
        String points = intent.getStringExtra("points");

        textTotal.setText(total);
        textCorrect.setText(correct);
        textIncorrect.setText(incorrect);
        textPoints.setText(points);

        checkResult(total, correct);

    }

    private void checkResult(String total, String correct) {

        int totalInt = Integer.parseInt(total);
        int correctInt = Integer.parseInt(correct);
        int percentage = (correctInt * 100) / totalInt;

        if(percentage >= 50){

            passedResult();
        }else{

            failedResult();
        }

    }

    private void passedResult() {

        animationResult.setAnimation(R.raw.trophy);
        textResult.setText("Congratulations!");

        studentRef.child("completed").
                child(receiverTopicId).child("message").setValue("Topic Completed");

        increaseLevel();

        findViewById(R.id.btnLeaveFeedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                Intent intent = new Intent(ResultActivity.this, TopicDetailActivity.class);
                intent.putExtra("visit_topic_id", receiverTopicId);
                startActivity(intent);

            }
        });

    }

    private void failedResult() {

        animationResult.setAnimation(R.raw.failed);
        textResult.setText("Please try again!");

        findViewById(R.id.btnLeaveFeedback).setVisibility(View.GONE);

    }

    private void increaseLevel() {

        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String level = dataSnapshot.child("level").getValue().toString();
                int levelInt = Integer.parseInt(level);
                levelInt++;

                studentRef.child("level").setValue(String.valueOf(levelInt));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}