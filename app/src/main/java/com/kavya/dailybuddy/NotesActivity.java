package com.kavya.dailybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class NotesActivity extends AppCompatActivity {

    FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    FirebaseFirestore firebaseFirestore;
    String userId;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        firebaseFirestore = FirebaseFirestore.getInstance();

        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext()); /* getting user ID */
        userId = account.getId();

        recyclerView = findViewById(R.id.notesRecyclerView);

        Query query = firebaseFirestore.collection(userId).document("note").collection("notes").orderBy("datetime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>().setQuery(query,Note.class).build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Note,NotesViewHolder>(options) {

            @NonNull
            @Override
            public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note,parent,false);
                return new NotesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NotesViewHolder holder, int position, @NonNull final Note model) {

                holder.noteTitle.setText(model.getTitle());
                if(model.getSubtitle().trim().isEmpty()){
                    holder.noteSubTitle.setVisibility(View.GONE);
                }
                else{
                    holder.noteSubTitle.setText(model.getSubtitle());
                }
                holder.noteDateTime.setText(model.getDatetime());
                holder.noteText.setText(model.getText());

                holder.cardviewLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NotesActivity.this,CreateNoteActivity.class);
                        intent.putExtra("note_title",model.getTitle());
                        intent.putExtra("note_subtitle",model.getSubtitle());
                        intent.putExtra("note_text",model.getText());
                        //firebaseFirestore.collection(userId).document("note").collection("notes").document(model.getTitle()).delete();
                        finish();
                        startActivity(intent);
                    }
                });
            }
        };
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(firestoreRecyclerAdapter);

        FloatingActionButton fab_add = findViewById(R.id.fab_add_note);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotesActivity.this,CreateNoteActivity.class);
                startActivity(intent);
            }
        });
    }

    private class NotesViewHolder extends RecyclerView.ViewHolder {

        TextView noteTitle,noteSubTitle,noteDateTime,noteText;
        LinearLayout cardviewLayout;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.textTitle);
            noteSubTitle = itemView.findViewById(R.id.textSubTitle);
            noteDateTime = itemView.findViewById(R.id.textDateTimeDisplay);
            noteText = itemView.findViewById(R.id.textNote);
            cardviewLayout = itemView.findViewById(R.id.layout_note);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firestoreRecyclerAdapter.startListening();
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firestoreRecyclerAdapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NotesActivity.this,MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }
}