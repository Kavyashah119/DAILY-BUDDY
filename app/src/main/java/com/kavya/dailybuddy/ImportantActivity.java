package com.kavya.dailybuddy;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;

import static android.view.View.GONE;

public class ImportantActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter adapter;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_important);

        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.important_tasks);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userID");

        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext()); /* getting user ID */
        userId =account.getId();

        final Query query = firebaseFirestore.collection(userId).document("task").collection("imp");

        FirestoreRecyclerOptions<notes> options = new FirestoreRecyclerOptions.Builder<notes>().setQuery(query,notes.class).build();

        adapter = new FirestoreRecyclerAdapter<notes,notesViewHolder>(options) {

            @NonNull
            @Override
            public notesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
                return new ImportantActivity.notesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final notesViewHolder holder, int position, @NonNull final notes model) {

                final DocumentReference documentReference = firebaseFirestore.collection(userId).document("task").collection("imp").document(model.getTitle());
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.get("preference").equals("0")){
                            holder.impBtn.setBackground(getResources().getDrawable(R.drawable.ic_baseline_star_border_24));
                            documentReference.delete();
                        }else if(documentSnapshot.get("preference").equals("1")){
                            holder.impBtn.setBackground(getResources().getDrawable(R.drawable.ic_baseline_star_24));
                        }else {
                            firebaseFirestore.collection(userId).document("task").collection("imp").document(model.getTitle().toLowerCase()).delete();
                        }
                    }
                });

                holder.title.setText(model.getTitle());
                holder.des.setText(model.getDescription());
                holder.impBtn.setBackground(getResources().getDrawable(R.drawable.ic_baseline_star_24));

                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(model.getTime()==null || model.getTime().equals("") || model.getDate()==null || model.getDate().equals(""))
                        {
                            Log.d("TAG", "Time is null" );
                        }
                        else{
                            Log.d("TAG", model.getTime());
                            holder.reminderIcon.setVisibility(View.VISIBLE);
                        }

                        if(holder.editBtn.getVisibility() == GONE) {
                            TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                            holder.editBtn.setVisibility(View.VISIBLE);
                            holder.deleteBtn.setVisibility(View.VISIBLE);

                            if(model.getDate()!=null){
                                holder.duedate.setVisibility(View.VISIBLE);
                                holder.duedateText.setVisibility(View.VISIBLE);

                                if(model.getDate().equals("")){
                                    holder.duedateText.setText(R.string.no_duedate);
                                }else{
                                    holder.duedateText.setText(model.getDate());
                                }
                            }

                            if(model.getTime()!=null){
                                holder.duetime.setVisibility(View.VISIBLE);
                                holder.duetimeText.setVisibility(View.VISIBLE);

                                if(model.getTime().equals("")){
                                    holder.duetimeText.setText(R.string.no_duetime);
                                }else{
                                    holder.duetimeText.setText(model.getTime());
                                }
                            }
                        }
                        else{
                            holder.editBtn.setVisibility(GONE);
                            holder.deleteBtn.setVisibility(GONE);
                            holder.duedate.setVisibility(GONE);
                            holder.duedateText.setVisibility(GONE);
                            holder.duetime.setVisibility(GONE);
                            holder.duetimeText.setVisibility(GONE);
                        }
                    }
                });

                holder.editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ImportantActivity.this,EditOption.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("description", model.getDescription());
                        intent.putExtra("preference",model.getPreference());
                        intent.putExtra("duedate",model.getDate());
                        intent.putExtra("duetime",model.getTime());
                        finish();
                        startActivity(intent);
                    }
                });

                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(ImportantActivity.this);

                        builder.setTitle("Confirm Delete");
                        builder.setMessage("Are you sure you want to delete?");
                        builder.setCancelable(true);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).delete();
                                firebaseFirestore.collection(userId).document("task").collection("imp").document(model.getTitle().toLowerCase()).delete();
                                showSnackBar();
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                    public void showSnackBar(){
                        Snackbar snackbar = Snackbar.make(recyclerView,"This task has been deleted", BaseTransientBottomBar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).set(model);
                                        firebaseFirestore.collection(userId).document("task").collection("imp").document(model.getTitle().toLowerCase()).set(model);
                                    }
                                });
                        snackbar.show();
                    }
                });

                holder.completedBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            holder.completedBtn.setBackground(getResources().getDrawable(R.drawable.done));
                            model.preference="-1";
                            firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).update("preference",model.getPreference());
                            DocumentReference doc = firebaseFirestore.collection(userId).document("task").collection("imp").document(model.getTitle());
                            doc.delete();
                            doc.update("preference",model.preference);
                            Toast.makeText(ImportantActivity.this, "Task Completed!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            holder.completedBtn.setBackground(getResources().getDrawable(R.drawable.ic_baseline_radio_button));
                            model.preference="0";
                            firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).update("preference",model.getPreference());
                        }
                    }
                });

                holder.impBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        model.preference="0";
                        holder.impBtn.setBackground(getResources().getDrawable(R.drawable.ic_baseline_star_border_24));

                        DocumentReference doc = firebaseFirestore.collection(userId).document("task").collection("imp").document(model.getTitle());
                        doc.delete();
                        doc.update("preference",model.preference);

                        DocumentReference documentReference1 = firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle());
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("preference",model.preference);
                        documentReference1.update(hashMap);
                    }
                });
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private class notesViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        ToggleButton completedBtn;
        TextView title,des;
        ImageButton editBtn,deleteBtn;
        ImageView reminderIcon;
        ToggleButton impBtn;
        ImageButton duedate,duetime;
        TextView duedateText,duetimeText;

        public notesViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            completedBtn = itemView.findViewById(R.id.btn_radio);
            title = itemView.findViewById(R.id.txt_title);
            des = itemView.findViewById(R.id.txt_description);
            editBtn = itemView.findViewById(R.id.edit_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
            reminderIcon = itemView.findViewById(R.id.reminder_icon);
            impBtn = itemView.findViewById(R.id.important_icon);
            duedate = itemView.findViewById(R.id.due_date_icon);
            duedateText = itemView.findViewById(R.id.due_date_display);
            duetime = itemView.findViewById(R.id.due_time_icon);
            duetimeText = itemView.findViewById(R.id.due_time_display);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ImportantActivity.this,MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}