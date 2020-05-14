package com.sami.onlineteaching;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class AllStudentsFragment extends Fragment {

    private View allStudentsView;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private RecyclerView recyclerAllStudents;

    public AllStudentsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        allStudentsView = inflater.inflate(R.layout.fragment_all_students, container, false);

        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Students");

        recyclerAllStudents = allStudentsView.findViewById(R.id.recyclerAllStudents);
        recyclerAllStudents.setLayoutManager(new LinearLayoutManager(getContext()));

        return allStudentsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Students>()
                        .setQuery(usersRef, Students.class)
                        .build();

        FirebaseRecyclerAdapter<Students, AllStudentsFragment.ProductsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Students, AllStudentsFragment.ProductsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final AllStudentsFragment.ProductsViewHolder holder, final int position, @NonNull Students model) {

                        final String studentIDs = getRef(position).getKey();

                        usersRef.child(studentIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()) {

                                    String name = dataSnapshot.child("fullname").getValue().toString();
                                    String username = dataSnapshot.child("username").getValue().toString();
                                    String level = dataSnapshot.child("level").getValue().toString();
                                    String target = dataSnapshot.child("target").getValue().toString();

                                    holder.textStudentName.setText(name);
                                    holder.textStudentUsername.setText(username);
                                    holder.textStudentLevel.setText("Level: " + level);
                                    holder.textStudentTarget.setText("Target: " + target);

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            getTargetInput(studentIDs);
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
                    public AllStudentsFragment.ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_all_students, parent, false);
                        AllStudentsFragment.ProductsViewHolder productsViewHolder = new AllStudentsFragment.ProductsViewHolder(view);
                        return productsViewHolder;

                    }
                };

        recyclerAllStudents.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ProductsViewHolder extends RecyclerView.ViewHolder{

        TextView textStudentName, textStudentUsername, textStudentLevel, textStudentTarget;

        public ProductsViewHolder(@NonNull View itemView) {
            super(itemView);

            textStudentName = itemView.findViewById(R.id.textStudentName);
            textStudentUsername = itemView.findViewById(R.id.textStudentUsername);
            textStudentLevel = itemView.findViewById(R.id.textStudentLevel);
            textStudentTarget = itemView.findViewById(R.id.textStudentTarget);

        }
    }

    private void getTargetInput(final String studentID){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set target");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String textTarget = input.getText().toString();
                updateTarget(textTarget, studentID);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void updateTarget(String textTarget, String studentID){

        usersRef.child(studentID).child("target").setValue(textTarget);

    }

}
