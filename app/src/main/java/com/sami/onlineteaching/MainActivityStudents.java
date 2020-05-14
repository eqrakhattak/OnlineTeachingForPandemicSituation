package com.sami.onlineteaching;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivityStudents extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private LottieAnimationView animationSwipe;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private TextView textTarget, textLevel, textUsername;
    private RecyclerView recyclerAllTopics;
    private DatabaseReference topicsRef, usersRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_students);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        animationSwipe = findViewById(R.id.animationSwipe);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        textTarget = findViewById(R.id.textTarget);
        textLevel = findViewById(R.id.textLevel);
        textUsername = findViewById(R.id.textUsername);

        topicsRef = FirebaseDatabase.getInstance().getReference().child("Topics");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Teachers");

        recyclerAllTopics = findViewById(R.id.recyclerAllTopics);
        recyclerAllTopics.setLayoutManager(new LinearLayoutManager(MainActivityStudents.this));

        findViewById(R.id.imageProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile();
            }
        });

        findViewById(R.id.imageLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {

                    animationSwipe.setAnimation(R.raw.swipedown);
                    animationSwipe.playAnimation();

                } else {
                    animationSwipe.setAnimation(R.raw.swipeup);
                    animationSwipe.playAnimation();
                }
            }
        });
    }

    private void verifyUserExistence(FirebaseUser currentUser) {

        if (currentUser != null) {
            String currentUserID = currentUser.getUid();
            rootRef.child("Students").child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.child("username").exists()) {
                        updateToProfileActivity();
                    } else {

                        String username = dataSnapshot.child("username").getValue().toString();
                        textUsername.setText(username);

                        String level = dataSnapshot.child("level").getValue().toString();
                        textLevel.setText(level);

                        String target = dataSnapshot.child("target").getValue().toString();
                        textTarget.setText(target);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void updateUI(FirebaseUser currentUser) {

        if (currentUser == null) {
            finish();
            Intent intent = new Intent(MainActivityStudents.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void updateToProfileActivity() {

        finish();
        Intent intent = new Intent(MainActivityStudents.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("occupation", "Students");
        startActivity(intent);
    }

    private void openProfile() {

        Intent intent = new Intent(MainActivityStudents.this, ProfileActivity.class);
        intent.putExtra("occupation", "Students");
        startActivity(intent);
    }

    private void logout() {

        FirebaseAuth.getInstance().signOut();
        updateUI(null);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        verifyUserExistence(currentUser);

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<MyTopics>()
                        .setQuery(topicsRef, MyTopics.class)
                        .build();

        FirebaseRecyclerAdapter<MyTopics, MainActivityStudents.ProductsViewHolder> adapter =
                new FirebaseRecyclerAdapter<MyTopics, MainActivityStudents.ProductsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final MainActivityStudents.ProductsViewHolder holder, final int position, @NonNull MyTopics model) {

                        String topicIDs = getRef(position).getKey();

                        topicsRef.child(topicIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists() && dataSnapshot.child("name").exists()) {

                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String teacherUid = dataSnapshot.child("uid").getValue().toString();

                                    holder.textTopicName.setText(name);

                                    usersRef.child(teacherUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            String teacherName = dataSnapshot.child("fullname").getValue().toString();
                                            holder.textTeacherName.setText("Uploaded by: " + teacherName);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        String visitTopicId = getRef(position).getKey();

                                        Intent intent = new Intent(MainActivityStudents.this, TopicDetailActivity.class);
                                        intent.putExtra("visit_topic_id", visitTopicId);
                                        startActivity(intent);

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
                    public MainActivityStudents.ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_all_topics, parent, false);
                        MainActivityStudents.ProductsViewHolder productsViewHolder = new MainActivityStudents.ProductsViewHolder(view);
                        return productsViewHolder;

                    }
                };

        recyclerAllTopics.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ProductsViewHolder extends RecyclerView.ViewHolder {

        TextView textTopicName, textTeacherName;

        public ProductsViewHolder(@NonNull View itemView) {
            super(itemView);

            textTopicName = itemView.findViewById(R.id.textTopicName);
            textTeacherName = itemView.findViewById(R.id.textTeacherName);
        }
    }
}
