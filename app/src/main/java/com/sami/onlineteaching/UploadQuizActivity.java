package com.sami.onlineteaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UploadQuizActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String randomId;
    private EditText inputQuestion, inputOption1, inputOption2, inputOption3, inputOption4;
    private long currentQuestion;
    private CheckBox checkboxOption1, checkboxOption2, checkboxOption3, checkboxOption4;
    private TextView textQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_quiz);

        Bundle b = getIntent().getExtras();

        assert b != null;
        randomId = b.getString("randomId");

        initViews();

    }

    private void initViews() {

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        textQuestion = findViewById(R.id.textQuestion);
        inputQuestion = findViewById(R.id.inputQuestion);
        inputOption1 = findViewById(R.id.inputOption1);
        inputOption2 = findViewById(R.id.inputOption2);
        inputOption3 = findViewById(R.id.inputOption3);
        inputOption4 = findViewById(R.id.inputOption4);
        checkboxOption1 = findViewById(R.id.checkboxOption1);
        checkboxOption2 = findViewById(R.id.checkboxOption2);
        checkboxOption3 = findViewById(R.id.checkboxOption3);
        checkboxOption4 = findViewById(R.id.checkboxOption4);

        rootRef.child("Topics").child(randomId).child("quiz").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentQuestion = dataSnapshot.getChildrenCount();
                currentQuestion++;
                textQuestion.setText("Question " + currentQuestion);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkboxOption1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkboxOption1.isChecked()){

                    checkboxOption2.setChecked(false);
                    checkboxOption3.setChecked(false);
                    checkboxOption4.setChecked(false);

                }
            }
        });

        checkboxOption2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkboxOption2.isChecked()){

                    checkboxOption1.setChecked(false);
                    checkboxOption3.setChecked(false);
                    checkboxOption4.setChecked(false);

                }
            }
        });

        checkboxOption3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkboxOption3.isChecked()){

                    checkboxOption1.setChecked(false);
                    checkboxOption2.setChecked(false);
                    checkboxOption4.setChecked(false);

                }
            }
        });

        checkboxOption4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkboxOption4.isChecked()){

                    checkboxOption1.setChecked(false);
                    checkboxOption2.setChecked(false);
                    checkboxOption3.setChecked(false);

                }
            }
        });

        findViewById(R.id.btnAddQuestion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuestion();
            }
        });

        findViewById(R.id.btnFinish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateToMainActivity();
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void addQuestion() {

        String answer;
        String question = inputQuestion.getText().toString();
        String option1 = inputOption1.getText().toString();
        String option2 = inputOption2.getText().toString();
        String option3 = inputOption3.getText().toString();
        String option4 = inputOption4.getText().toString();

        if (question.isEmpty()) {
            inputQuestion.setError("Question is required");
            inputQuestion.requestFocus();
            return;
        }

        if (option1.isEmpty()) {
            inputOption1.setError("Option 1 is required");
            inputOption1.requestFocus();
            return;
        }

        if (option2.isEmpty()) {
            inputOption2.setError("Option 2 is required");
            inputOption2.requestFocus();
            return;
        }

        if (option3.isEmpty()) {
            inputOption3.setError("Option 3 is required");
            inputOption3.requestFocus();
            return;
        }

        if (option4.isEmpty()) {
            inputOption4.setError("Option 4 is required");
            inputOption4.requestFocus();
            return;
        }

        String currentQuestionString = String.valueOf(currentQuestion);

        if(checkboxOption1.isChecked()){
            answer = option1;
        }else if(checkboxOption2.isChecked()){
            answer = option2;
        }else if(checkboxOption3.isChecked()){
            answer = option3;
        }else if(checkboxOption4.isChecked()){
            answer = option4;
        }else{
            Toast.makeText(this, "Please select correct option", Toast.LENGTH_LONG).show();
            return;
        }

        rootRef.child("Topics").child(randomId).child("quiz").child(currentQuestionString).child("answer").setValue(answer);
        rootRef.child("Topics").child(randomId).child("quiz").child(currentQuestionString).child("question").setValue(question);
        rootRef.child("Topics").child(randomId).child("quiz").child(currentQuestionString).child("option1").setValue(option1);
        rootRef.child("Topics").child(randomId).child("quiz").child(currentQuestionString).child("option2").setValue(option2);
        rootRef.child("Topics").child(randomId).child("quiz").child(currentQuestionString).child("option3").setValue(option3);
        rootRef.child("Topics").child(randomId).child("quiz").child(currentQuestionString).child("option4").setValue(option4);

        inputQuestion.setText("");
        inputQuestion.requestFocus();
        inputOption1.setText("");
        inputOption2.setText("");
        inputOption3.setText("");
        inputOption4.setText("");

        checkboxOption1.setChecked(false);
        checkboxOption2.setChecked(false);
        checkboxOption3.setChecked(false);
        checkboxOption4.setChecked(false);

        currentQuestion++;
        textQuestion.setText("Question " + currentQuestion);

        Toast.makeText(this, "Question added successfully", Toast.LENGTH_LONG).show();

        if(currentQuestion > 3){

            findViewById(R.id.btnFinish).setEnabled(true);

        }

    }

    private void updateToMainActivity() {

        finish();
        Intent intent = new Intent(UploadQuizActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

}
