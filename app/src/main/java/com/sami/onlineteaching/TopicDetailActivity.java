package com.sami.onlineteaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TopicDetailActivity extends AppCompatActivity {

    private DatabaseReference topicsRef, usersRef;
    private FirebaseAuth mAuth;
    private String receiverTopicId;
    private TextView textActualTopicName, textActualReference;
    private EditText inputFeedback;
    private RecyclerView recyclerFeedback;
    private Button btnStartQuiz, btnPost;
    private String fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);

        receiverTopicId = getIntent().getExtras().get("visit_topic_id").toString();

        mAuth = FirebaseAuth.getInstance();

        topicsRef = FirebaseDatabase.getInstance().getReference().child("Topics");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Students");

        textActualTopicName = findViewById(R.id.textActualTopicName);
        textActualReference = findViewById(R.id.textActualReference);
        inputFeedback = findViewById(R.id.inputFeedback);
        recyclerFeedback = findViewById(R.id.recyclerFeedback);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);
        btnPost = findViewById(R.id.btnPost);

        recyclerFeedback.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        retrieveTopicInfo();
        retrieveComment();
        checkTopicCompleted();

    }

    private void checkTopicCompleted() {

        usersRef.child(mAuth.getCurrentUser().getUid()).child("completed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                btnStartQuiz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        finish();
                        Intent intent = new Intent(TopicDetailActivity.this, QuizStartActivity.class);
                        intent.putExtra("visit_topic_id", receiverTopicId);
                        startActivity(intent);

                    }
                });

                btnPost.setEnabled(false);
                btnPost.setText("You have to complete the course first");
                inputFeedback.setEnabled(false);

                for (DataSnapshot item_snapshot : dataSnapshot.getChildren()) {

                    if (item_snapshot.getKey().equals(receiverTopicId)) {

                        btnStartQuiz.setEnabled(false);
                        btnStartQuiz.setText("You have already finished this topic");
                        inputFeedback.setEnabled(true);
                        btnPost.setEnabled(true);
                        btnPost.setText("Post");

                        btnPost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                initializeComment();

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void retrieveTopicInfo() {

        topicsRef.child(receiverTopicId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String referenceLinks = dataSnapshot.child("referenceLinks").getValue().toString();

                    textActualTopicName.setText(name);
                    textActualReference.setText(referenceLinks);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void retrieveComment(){ ;

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Feedbacks>()
                        .setQuery(topicsRef.child(receiverTopicId).child("feedbacks"), Feedbacks.class)
                        .build();

        FirebaseRecyclerAdapter<Feedbacks, TopicDetailActivity.ProductsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Feedbacks, TopicDetailActivity.ProductsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final TopicDetailActivity.ProductsViewHolder holder, final int position, @NonNull Feedbacks model) {

                        final String feedbackIDs = getRef(position).getKey();

                        usersRef.child(mAuth.getCurrentUser().getUid()).child("feedbacks").child(receiverTopicId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()) {

                                    for (DataSnapshot item_snapshot : dataSnapshot.getChildren()) {

                                        if (item_snapshot.getKey().equals(feedbackIDs)) {

                                            holder.imageFeedbackDelete.setVisibility(View.VISIBLE);

                                        }
                                    }

                                    holder.imageFeedbackDelete.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            deleteComment(feedbackIDs);

                                        }
                                    });

                                }

                                topicsRef.child(receiverTopicId).child("feedbacks").child(feedbackIDs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.exists()) {

                                            String name = dataSnapshot.child("name").getValue().toString();
                                            String feedback = dataSnapshot.child("feedback").getValue().toString();

                                            holder.textPersonName.setText(name);
                                            holder.textFeedbackString.setText(feedback);

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public TopicDetailActivity.ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_feedbacks, parent, false);
                        TopicDetailActivity.ProductsViewHolder productsViewHolder = new TopicDetailActivity.ProductsViewHolder(view);
                        return productsViewHolder;

                    }
                };

        recyclerFeedback.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ProductsViewHolder extends RecyclerView.ViewHolder{

        TextView textPersonName, textFeedbackString;
        ImageView imageFeedbackDelete;

        public ProductsViewHolder(@NonNull View itemView) {
            super(itemView);

            textPersonName = itemView.findViewById(R.id.textPersonName);
            textFeedbackString = itemView.findViewById(R.id.textFeedbackString);
            imageFeedbackDelete = itemView.findViewById(R.id.imageFeedbackDelete);

        }
    }

    private void initializeComment() {

        final String feedback = inputFeedback.getText().toString();

        if (feedback.isEmpty()) {
            inputFeedback.setError("Feedback field is empty");
            inputFeedback.requestFocus();
            return;
        }

        usersRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fullName = dataSnapshot.child("fullname").getValue().toString();

                postComment(feedback);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void postComment(final String feedback) {

        String rand = generateRandomId();

        topicsRef.child(receiverTopicId).child("feedbacks").child(rand).child("uid").setValue(mAuth.getCurrentUser().getUid());
        topicsRef.child(receiverTopicId).child("feedbacks").child(rand).child("name").setValue(fullName);
        topicsRef.child(receiverTopicId).child("feedbacks").child(rand).child("feedback").setValue(feedback).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(TopicDetailActivity.this, "Feedback posted", Toast.LENGTH_LONG).show();
                    inputFeedback.setText("");
                }else{
                    Toast.makeText(TopicDetailActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }

            }
        });

        usersRef.child(mAuth.getCurrentUser().getUid()).child("feedbacks").child(receiverTopicId).child(rand).child("feedback").setValue("Feedback posted").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(!task.isSuccessful()){
                    Toast.makeText(TopicDetailActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void deleteComment(final String feedbackIDs) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(TopicDetailActivity.this);
        builder1.setMessage("Do you really want to delete this feedback?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        usersRef.child(mAuth.getCurrentUser().getUid()).child("feedbacks").child(receiverTopicId).child(feedbackIDs).removeValue();
                        topicsRef.child(receiverTopicId).child("feedbacks").child(feedbackIDs).removeValue();

                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

    private String generateRandomId() {

        long inSeconds = System.currentTimeMillis() / 10000;
        return String.valueOf(999999999 - inSeconds);
    }

}
