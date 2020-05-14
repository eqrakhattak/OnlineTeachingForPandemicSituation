package com.sami.onlineteaching;

import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QuizStartActivity extends AppCompatActivity {

    private DatabaseReference topicsRef, usersRef;
    private FirebaseAuth mAuth;
    private String receiverTopicId;

    private Button b1, b2, b3, b4;
    private TextView t1_question, timerTxt;
    private int total = 0;
    private int correct = 0;
    private int points = 0;
    private int wrong = 0;
    private long maxVragen = 0;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_start);

        receiverTopicId = getIntent().getExtras().get("visit_topic_id").toString();

        mAuth = FirebaseAuth.getInstance();

        topicsRef = FirebaseDatabase.getInstance().getReference().child("Topics");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Students");

        b1 = findViewById(R.id.buton1);
        b2 = findViewById(R.id.buton2);
        b3 = findViewById(R.id.buton3);
        b4 = findViewById(R.id.buton4);

        t1_question = (TextView) findViewById(R.id.questionsTxt);
        timerTxt = (TextView) findViewById(R.id.timerTxt);
        updateQuestion();

        reverseTimer(30, timerTxt);
    }


    public void updateQuestion() {
        b1.setEnabled(true);
        b2.setEnabled(true);
        b3.setEnabled(true);
        b4.setEnabled(true);

        topicsRef.child(receiverTopicId).child("quiz").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    maxVragen = (dataSnapshot.getChildrenCount());
                }
                total++;

                if (total > maxVragen) {
                    finish();
                    countDownTimer.cancel();
                    total--;
                    Intent i = new Intent(QuizStartActivity.this, ResultActivity.class);
                    i.putExtra("total", String.valueOf(total));
                    i.putExtra("correct", String.valueOf(correct));
                    i.putExtra("incorrect", String.valueOf(wrong));
                    i.putExtra("points", String.valueOf(points));
                    i.putExtra("visit_topic_id", receiverTopicId);
                    startActivity(i);
                }
                else {

                    topicsRef.child(receiverTopicId).child("quiz").child(String.valueOf(total)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                final Question question = dataSnapshot.getValue(Question.class);
                                t1_question.setText(question.getQuestion());

                                b1.setText(question.getOption1());
                                b2.setText(question.getOption2());
                                b3.setText(question.getOption3());
                                b4.setText(question.getOption4());

                                b1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        b1.setEnabled(false);
                                        b2.setEnabled(false);
                                        b3.setEnabled(false);
                                        b4.setEnabled(false);
                                        if (b1.getText().toString().equals(question.getAnswer())) {
                                            b1.setBackgroundColor(Color.GREEN);

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    correct++;
                                                    points = points + 15;
                                                    b1.setBackgroundColor(Color.parseColor("#009688"));
                                                    updateQuestion();
                                                }
                                            }, 1500);
                                        } else {
                                            // answer is wrong ... we will find the correct answer, and make it green
                                            wrong++;
                                            points = points - 5;
                                            b1.setBackgroundColor(Color.RED);

                                            if (b2.getText().toString().equals(question.getAnswer())) {
                                                b2.setBackgroundColor(Color.GREEN);
                                            } else if (b3.getText().toString().equals(question.getAnswer())) {
                                                b3.setBackgroundColor(Color.GREEN);
                                            } else if (b4.getText().toString().equals(question.getAnswer())) {
                                                b4.setBackgroundColor(Color.GREEN);
                                            }

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    b1.setBackgroundColor(Color.parseColor("#009688"));
                                                    b2.setBackgroundColor(Color.parseColor("#009688"));
                                                    b3.setBackgroundColor(Color.parseColor("#009688"));
                                                    b4.setBackgroundColor(Color.parseColor("#009688"));
                                                    updateQuestion();
                                                }
                                            }, 1500);


                                        }
                                    }
                                });

                                b2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        b1.setEnabled(false);
                                        b2.setEnabled(false);
                                        b3.setEnabled(false);
                                        b4.setEnabled(false);
                                        if (b2.getText().toString().equals(question.getAnswer())) {
                                            b2.setBackgroundColor(Color.GREEN);

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    correct++;
                                                    points = points + 15;
                                                    b2.setBackgroundColor(Color.parseColor("#009688"));
                                                    updateQuestion();
                                                }
                                            }, 1500);
                                        } else {
                                            // answer is wrong ... we will find the correct answer, and make it green
                                            wrong++;
                                            points = points - 5;
                                            b2.setBackgroundColor(Color.RED);

                                            if (b1.getText().toString().equals(question.getAnswer())) {
                                                b1.setBackgroundColor(Color.GREEN);
                                            } else if (b3.getText().toString().equals(question.getAnswer())) {
                                                b3.setBackgroundColor(Color.GREEN);
                                            } else if (b4.getText().toString().equals(question.getAnswer())) {
                                                b4.setBackgroundColor(Color.GREEN);
                                            }

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    b1.setBackgroundColor(Color.parseColor("#009688"));
                                                    b2.setBackgroundColor(Color.parseColor("#009688"));
                                                    b3.setBackgroundColor(Color.parseColor("#009688"));
                                                    b4.setBackgroundColor(Color.parseColor("#009688"));
                                                    updateQuestion();
                                                }
                                            }, 1500);


                                        }
                                    }
                                });

                                b3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        b1.setEnabled(false);
                                        b2.setEnabled(false);
                                        b3.setEnabled(false);
                                        b4.setEnabled(false);
                                        if (b3.getText().toString().equals(question.getAnswer())) {
                                            b3.setBackgroundColor(Color.GREEN);

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    correct++;
                                                    points = points + 15;
                                                    b3.setBackgroundColor(Color.parseColor("#009688"));
                                                    updateQuestion();
                                                }
                                            }, 1500);
                                        } else {
                                            // answer is wrong ... we will find the correct answer, and make it green
                                            wrong++;
                                            points = points - 5;
                                            b3.setBackgroundColor(Color.RED);

                                            if (b1.getText().toString().equals(question.getAnswer())) {
                                                b1.setBackgroundColor(Color.GREEN);
                                            } else if (b2.getText().toString().equals(question.getAnswer())) {
                                                b2.setBackgroundColor(Color.GREEN);
                                            } else if (b4.getText().toString().equals(question.getAnswer())) {
                                                b4.setBackgroundColor(Color.GREEN);
                                            }

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    b1.setBackgroundColor(Color.parseColor("#009688"));
                                                    b2.setBackgroundColor(Color.parseColor("#009688"));
                                                    b3.setBackgroundColor(Color.parseColor("#009688"));
                                                    b4.setBackgroundColor(Color.parseColor("#009688"));
                                                    updateQuestion();
                                                }
                                            }, 1500);


                                        }
                                    }
                                });

                                b4.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        b1.setEnabled(false);
                                        b2.setEnabled(false);
                                        b3.setEnabled(false);
                                        b4.setEnabled(false);
                                        if (b4.getText().toString().equals(question.getAnswer())) {
                                            b4.setBackgroundColor(Color.GREEN);

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    correct++;
                                                    points = points + 15;
                                                    b4.setBackgroundColor(Color.parseColor("#009688"));
                                                    updateQuestion();
                                                }
                                            }, 1500);
                                        } else {
                                            // answer is wrong ... we will find the correct answer, and make it green
                                            wrong++;
                                            points = points - 5;
                                            b4.setBackgroundColor(Color.RED);

                                            if (b1.getText().toString().equals(question.getAnswer())) {
                                                b1.setBackgroundColor(Color.GREEN);
                                            } else if (b2.getText().toString().equals(question.getAnswer())) {
                                                b2.setBackgroundColor(Color.GREEN);
                                            } else if (b3.getText().toString().equals(question.getAnswer())) {
                                                b3.setBackgroundColor(Color.GREEN);
                                            }

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    b1.setBackgroundColor(Color.parseColor("#009688"));
                                                    b2.setBackgroundColor(Color.parseColor("#009688"));
                                                    b3.setBackgroundColor(Color.parseColor("#009688"));
                                                    b4.setBackgroundColor(Color.parseColor("#009688"));
                                                    updateQuestion();
                                                }
                                            }, 1500);
                                        }
                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void reverseTimer(int seconds, final TextView tv) {

        countDownTimer = new CountDownTimer(seconds * 1000 + 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                tv.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            }


            @Override
            public void onFinish() {
                tv.setText("Completed");
                Intent myIntent = new Intent(QuizStartActivity.this, ResultActivity.class);
                myIntent.putExtra("total", String.valueOf(total));
                myIntent.putExtra("correct", String.valueOf(correct));
                myIntent.putExtra("incorrect", String.valueOf(wrong));
                myIntent.putExtra("points", String.valueOf(points));
                myIntent.putExtra("visit_topic_id", receiverTopicId);
                startActivity(myIntent);
            }
        }.start();
    }

}
