package com.kavya.dailybuddy;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.makeramen.roundedimageview.RoundedImageView;

public class NotesActivity extends AppCompatActivity {

    FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    FirebaseFirestore firebaseFirestore;
    String userId;
    RecyclerView recyclerView;
    SearchView search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        search = findViewById(R.id.inputSearch);

        firebaseFirestore = FirebaseFirestore.getInstance();

        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext()); /* getting user ID */
        userId = account.getId();

        recyclerView = findViewById(R.id.notesRecyclerView);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                noteSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                noteSearch(newText);
                return false;
            }
        });

        Query query = firebaseFirestore.collection(userId).document("note").collection("notes").orderBy("pinned", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>().setQuery(query,Note.class).build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Note,NotesViewHolder>(options) {

            @NonNull
            @Override
            public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note,parent,false);
                return new NotesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final NotesViewHolder holder, int position, @NonNull final Note model) {

                holder.noteTitle.setText(model.getTitle());
                if(model.getSubtitle().trim().isEmpty()){
                    holder.noteSubTitle.setVisibility(View.GONE);
                }
                else{
                    holder.noteSubTitle.setText(model.getSubtitle());
                }
                holder.noteDateTime.setText(model.getDatetime());
                holder.noteText.setText(model.getText());

                if(model.getPinned().equals("1")){
                    Log.d("MODEL","Pinned");
                    holder.notepinned.setVisibility(View.VISIBLE);
                    holder.notepinned.setColorFilter(Color.argb(255,135,206,235));
                }else{
                    Log.d("MODEL","Not Pinned");
                    holder.notepinned.setVisibility(View.GONE);
                }

                holder.cardviewLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NotesActivity.this,CreateNoteActivity.class);
                        intent.putExtra("note_title",model.getTitle());
                        intent.putExtra("note_subtitle",model.getSubtitle());
                        intent.putExtra("note_text",model.getText());
                        intent.putExtra("note_pinned",model.getPinned());
                        if(model.getColor() != null){
                            intent.putExtra("note_color",model.getColor());
                        }
                        if(model.getImagepath() != null){
                            intent.putExtra("note_img",model.getImagepath());
                        }
                        if(model.getWebURL() != null){
                            intent.putExtra("note_url",model.getWebURL());
                        }
                        //firebaseFirestore.collection(userId).document("note").collection("notes").document(model.getTitle()).delete();
                        finish();
                        startActivity(intent);
                    }
                });

                GradientDrawable gradientDrawable = (GradientDrawable)holder.cardviewLayout.getBackground();
                if(model.getColor()!=null){
                    gradientDrawable.setColor(Color.parseColor(model.getColor()));
                }else{
                    gradientDrawable.setColor(Color.parseColor("#333333"));
                }

                if(model.getImagepath() != null){
                    holder.imageView.setImageBitmap(BitmapFactory.decodeFile(model.getImagepath()));
                    holder.imageView.setVisibility(View.VISIBLE);
                }else{
                    holder.imageView.setVisibility(View.GONE);
                }

                if(model.getWebURL() != null){
                    holder.noteURL.setText(model.getWebURL());
                    holder.noteURL.setVisibility(View.VISIBLE);
                }else{
                    holder.noteURL.setVisibility(View.GONE);
                }

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

        TextView noteTitle,noteSubTitle,noteDateTime,noteText,noteURL;
        LinearLayout cardviewLayout;
        RoundedImageView imageView;
        ImageView notepinned;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.textTitle);
            noteSubTitle = itemView.findViewById(R.id.textSubTitle);
            noteDateTime = itemView.findViewById(R.id.textDateTimeDisplay);
            noteText = itemView.findViewById(R.id.textNote);
            noteText.setMovementMethod(new ScrollingMovementMethod());
            cardviewLayout = itemView.findViewById(R.id.layout_note);
            imageView = itemView.findViewById(R.id.imageNote);
            noteURL = itemView.findViewById(R.id.textURL);
            notepinned = itemView.findViewById(R.id.pinned_note);
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

    private void noteSearch(String s) {

        Query query = firebaseFirestore.collection(userId).document("note").collection("notes");

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>().setQuery(query.orderBy("title").startAt(s).endAt(s+"\uf8ff"),Note.class).build();

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

                try{
                    if(model.getPinned().equals("1")){
                        Log.d("MODEL","Pinned");
                        holder.notepinned.setVisibility(View.VISIBLE);
                        holder.notepinned.setColorFilter(Color.argb(255,135,206,235));
                    }else{
                        Log.d("MODEL","Not Pinned");
                        holder.notepinned.setVisibility(View.GONE);
                    }
                }catch(Exception e){
                    e.getMessage();
                }

                holder.cardviewLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NotesActivity.this,CreateNoteActivity.class);
                        intent.putExtra("note_title",model.getTitle());
                        intent.putExtra("note_subtitle",model.getSubtitle());
                        intent.putExtra("note_text",model.getText());
                        intent.putExtra("note_pinned",model.getPinned());
                        if(model.getColor() != null){
                            intent.putExtra("note_color",model.getColor());
                        }
                        if(model.getImagepath() != null){
                            intent.putExtra("note_img",model.getImagepath());
                        }
                        if(model.getWebURL() != null){
                            intent.putExtra("note_url",model.getWebURL());
                        }
                        //firebaseFirestore.collection(userId).document("note").collection("notes").document(model.getTitle()).delete();
                        finish();
                        startActivity(intent);
                    }
                });

                GradientDrawable gradientDrawable = (GradientDrawable)holder.cardviewLayout.getBackground();
                if(model.getColor()!=null){
                    gradientDrawable.setColor(Color.parseColor(model.getColor()));
                }else{
                    gradientDrawable.setColor(Color.parseColor("#333333"));
                }

                if(model.getImagepath() != null){
                    holder.imageView.setImageBitmap(BitmapFactory.decodeFile(model.getImagepath()));
                    holder.imageView.setVisibility(View.VISIBLE);
                }else{
                    holder.imageView.setVisibility(View.GONE);
                }

                if(model.getWebURL() != null){
                    holder.noteURL.setText(model.getWebURL());
                    holder.noteURL.setVisibility(View.VISIBLE);
                }else{
                    holder.noteURL.setVisibility(View.GONE);
                }
            }
        };
        firestoreRecyclerAdapter.startListening();
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(firestoreRecyclerAdapter);
    }
}