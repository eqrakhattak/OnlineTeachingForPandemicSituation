package com.sami.onlineteaching;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyTopicsFragment extends Fragment {

    private View myTopicsView;
    private FirebaseAuth mAuth;
    private DatabaseReference myTopicsRef, topicsRef, usersRef;
    private RecyclerView recyclerMyTopics;

    public MyTopicsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myTopicsView = inflater.inflate(R.layout.fragment_my_topics, container, false);

        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        myTopicsRef = FirebaseDatabase.getInstance().getReference().child("Teachers").child(uid).child("topics");
        topicsRef = FirebaseDatabase.getInstance().getReference().child("Topics");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Teachers");

        recyclerMyTopics = myTopicsView.findViewById(R.id.recyclerMyTopics);
        recyclerMyTopics.setLayoutManager(new LinearLayoutManager(getContext()));

        myTopicsView.findViewById(R.id.btnUploadTopic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getContext(), UploadTopicActivity.class));

            }
        });

        return myTopicsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<MyTopics>()
                        .setQuery(myTopicsRef, MyTopics.class)
                        .build();

        FirebaseRecyclerAdapter<MyTopics, ProductsViewHolder> adapter =
                new FirebaseRecyclerAdapter<MyTopics, ProductsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ProductsViewHolder holder, final int position, @NonNull MyTopics model) {

                        String topicIDs = getRef(position).getKey();

                        topicsRef.child(topicIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists() && dataSnapshot.child("name").exists()) {

                                    String name = dataSnapshot.child("name").getValue().toString();

                                    holder.textTopicName.setText(name);

                                    holder.imageEdit.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            String idToEditTopic = getRef(position).getKey();

                                            editTopic(idToEditTopic);

                                        }
                                    });

                                    holder.imageAdd.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            String idToAddQuestion = getRef(position).getKey();

                                            addQuestion(idToAddQuestion);

                                        }
                                    });

                                    holder.imageDelete.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            String idToDeleteTopic = getRef(position).getKey();

                                            deleteTopic(idToDeleteTopic);

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_my_topics, parent, false);
                        ProductsViewHolder productsViewHolder = new ProductsViewHolder(view);
                        return productsViewHolder;

                    }
                };

        recyclerMyTopics.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ProductsViewHolder extends RecyclerView.ViewHolder{

        TextView textTopicName;
        ImageView imageEdit, imageAdd, imageDelete;

        public ProductsViewHolder(@NonNull View itemView) {
            super(itemView);

            textTopicName = itemView.findViewById(R.id.textTopicName);
            imageEdit = itemView.findViewById(R.id.imageEdit);
            imageAdd = itemView.findViewById(R.id.imageAdd);
            imageDelete = itemView.findViewById(R.id.imageDelete);

        }
    }

    private void editTopic(String idToEditTopic) {

        Intent intent = new Intent(getContext(), EditTopicActivity.class);
        intent.putExtra("topic_id", idToEditTopic);
        startActivity(intent);

    }

    private void addQuestion(String idToAddQuestion) {

        Intent intent = new Intent(getContext(), UploadQuizActivity.class);
        intent.putExtra("randomId", idToAddQuestion);
        startActivity(intent);

    }

    private void deleteTopic(final String idToDeleteTopic) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setMessage("Do you really want to delete this topic?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                topicsRef.child(idToDeleteTopic).removeValue();
                                myTopicsRef.child(idToDeleteTopic).removeValue();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

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
}
