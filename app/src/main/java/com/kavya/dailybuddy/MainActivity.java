package com.kavya.dailybuddy;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    EditText edt_title,edt_description;
    FloatingActionButton floatingActionButton;
    FirebaseFirestore firebaseFirestore;

    RecyclerView recyclerView;
    FirestoreRecyclerAdapter adapter;
    String userId;

    String title, description;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    CoordinatorLayout coordinatorLayout;
    Toolbar toolbar;
    TextView textView;

    String username;
    Uri userphoto;
    TextView name_of_person;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)                                              /* On create method */
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edt_title = findViewById(R.id.edt_title);
        edt_description = findViewById(R.id.edt_description);
        floatingActionButton = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        toolbar = findViewById(R.id.toolbar);
        textView = findViewById(R.id.person_name);

        drawerLayout.closeDrawers();

        setSupportActionBar(toolbar);
        navigationView.bringToFront();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(MainActivity.this);

        firebaseFirestore = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.listTodo);
        title = edt_title.getText().toString();
        description = edt_description.getText().toString();

        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext()); /* getting user ID */
        userId =account.getId();
        username = account.getDisplayName();
        userphoto = account.getPhotoUrl();

        View headerview = navigationView.getHeaderView(0);
        name_of_person = headerview.findViewById(R.id.person_name);
        name_of_person.setText(username);

        mImageView = headerview.findViewById(R.id.person_photo);
        Picasso.with(this).load(userphoto).into(mImageView);

        floatingActionButton.setOnClickListener(new View.OnClickListener()
       {
            @Override
            public void onClick(View v) {

                if(edt_title.getText().toString().isEmpty())
                {
                    int ecolor = R.color.errorColor;
                    String estring = "Please enter a title";
                    ForegroundColorSpan fgcspan = new ForegroundColorSpan(getResources().getColor(ecolor));
                    SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
                    ssbuilder.setSpan(fgcspan, 0, estring.length(), 0);
                    edt_title.requestFocus();
                    edt_title.setError(ssbuilder);
                    return;
                }

                DocumentReference documentReference;
                documentReference = firebaseFirestore.collection(userId).document("task").collection("tasks").document(edt_title.getText().toString());

                Map<String, Object> task = new HashMap<>();
                task.put("title",edt_title.getText().toString());
                task.put("description",edt_description.getText().toString());
                task.put("date", null);
                task.put("time", null);

                documentReference.set(task);
            }
        });

        Query query = firebaseFirestore.collection(userId).document("task").collection("tasks");

        FirestoreRecyclerOptions<notes> options = new FirestoreRecyclerOptions.Builder<notes>().setQuery(query,notes.class).build();

        adapter = new FirestoreRecyclerAdapter<notes, notesViewHolder>(options) {                   /* adapter code */

            @NonNull
            @Override
            public notesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
                return new notesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final notesViewHolder holder, final int position, @NonNull final notes model) {

                holder.reminder.setVisibility(GONE);
                holder.delete_button.setVisibility(GONE);
                holder.edit_button.setVisibility(GONE);

                holder.note_title.setText(model.getTitle());
                holder.note_description.setText(model.getDescription());

                if(model.getTime()==null || model.getTime().equals(""))
                {
                    Log.d("TAG", "Time is null" );
                }
                else{
                    Log.d("TAG", model.getTime());
                    holder.reminder.setVisibility(View.VISIBLE);
                }

                holder.mcardview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    if(holder.edit_button.getVisibility() == GONE) {
                        TransitionManager.beginDelayedTransition(holder.mcardview, new AutoTransition());
                        holder.edit_button.setVisibility(View.VISIBLE);
                        holder.delete_button.setVisibility(View.VISIBLE);

                        if(model.getDate()!=null){
                            holder.date_icon.setVisibility(View.VISIBLE);
                            holder.date_display.setVisibility(View.VISIBLE);
                            holder.date_display.setText(model.getDate());
                        }

                        if(model.getTime()!=null){
                            holder.time_icon.setVisibility(View.VISIBLE);
                            holder.time_display.setVisibility(View.VISIBLE);
                            holder.time_display.setText(model.getTime());
                        }
                    }
                    else{
                        holder.edit_button.setVisibility(GONE);
                        holder.delete_button.setVisibility(GONE);
                        holder.date_icon.setVisibility(GONE);
                        holder.date_display.setVisibility(GONE);
                        holder.time_icon.setVisibility(GONE);
                        holder.time_display.setVisibility(GONE);
                    }

                    }
                });

                holder.radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            holder.note_title.setPaintFlags(holder.note_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).delete();
                    }
                });

                holder.edit_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this,EditOption.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("description", model.getDescription());
                        finish();
                        startActivity(intent);
                    }
                });

                holder.delete_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        builder.setTitle("Confirm Delete");
                        builder.setMessage("Are you sure you want to delete?");
                        builder.setCancelable(true);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).delete();
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
                                    }
                                });
                        snackbar.show();
                    }
                });
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.getDrawerLockMode(drawerLayout);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.Notes:
            {
                drawerLayout.closeDrawers();
                finish();
                startActivity(new Intent(MainActivity.this,NotesActivity.class));
                Toast.makeText(this, "Inside notes", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.favourites:
            {
                Toast.makeText(this, "Inside fav", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.about_app:
            {
                Toast.makeText(this, "Inside app", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.profile:
            {
                Toast.makeText(this, "Inside pro", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return true;
    }

    private  class  notesViewHolder extends RecyclerView.ViewHolder                                /* View Holder */
    {
        TextView note_title, note_description;
        CardView mcardview;
        ImageButton edit_button;
        ImageButton delete_button;
        ImageView reminder;
        ImageButton date_icon,time_icon;
        TextView date_display,time_display;
        RadioButton radioButton;

        public notesViewHolder(@NonNull View itemView) {
        super(itemView);
            note_title = itemView.findViewById(R.id.txt_title);
            note_description = itemView.findViewById(R.id.txt_description);
            mcardview = itemView.findViewById(R.id.card_view);
            edit_button = itemView.findViewById(R.id.edit_btn);
            delete_button = itemView.findViewById(R.id.delete_btn);
            reminder = itemView.findViewById(R.id.reminder_icon);
            date_icon = itemView.findViewById(R.id.due_date_icon);
            time_icon = itemView.findViewById(R.id.due_time_icon);
            date_display = itemView.findViewById(R.id.due_date_display);
            time_display = itemView.findViewById(R.id.due_time_display);
            radioButton = itemView.findViewById(R.id.btn_radio);
        }
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
