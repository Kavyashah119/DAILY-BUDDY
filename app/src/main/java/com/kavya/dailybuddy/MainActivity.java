package com.kavya.dailybuddy;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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

    String username,usermail;
    Uri userphoto;
    TextView name_of_person,email_of_person;
    ImageView mImageView;

    String preference="0";
    int count=0;

    ImageView titleMic,desMic;

    GoogleSignInClient googleSignInClient;

    RelativeLayout relativeLayout;

    TextView impTextView;

    static int temp=0;
    ImageView noTasks;
    TextView textWinning;

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
        titleMic = findViewById(R.id.title_mic);
        desMic = findViewById(R.id.des_mic);
        relativeLayout = findViewById(R.id.relative_layout);
        noTasks = findViewById(R.id.no_tasks_img);
        textWinning = findViewById(R.id.text_winning);

        impTextView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.Important));
        impTextView.setText("0");
        impTextView.setGravity(Gravity.CENTER_VERTICAL);
        impTextView.setTypeface(null, Typeface.BOLD);
        impTextView.setTextColor(getResources().getColor(R.color.colorNoteColor5));

        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) /* Configure Google SignIn */
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(MainActivity.this,gso);

        drawerLayout.closeDrawers();

        setSupportActionBar(toolbar);
        navigationView.bringToFront();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorNoteColor2));
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
        usermail = account.getEmail();

        View headerview = navigationView.getHeaderView(0);
        name_of_person = headerview.findViewById(R.id.person_name);
        name_of_person.setText(username);
        email_of_person = headerview.findViewById(R.id.person_email);
        email_of_person.setText(usermail);

        mImageView = headerview.findViewById(R.id.person_photo);
        Picasso.with(this).load(userphoto).into(mImageView);

        titleMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordSpeechTitle();
            }
        });

        desMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordSpeechDescription();
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener()
       {
            @Override
            public void onClick(View v) {

                if(edt_title.getText().toString().isEmpty())
                {
                    int ecolor = R.color.colorNoteColor2;
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
                task.put("preference","0");

                documentReference.set(task).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("TAG", "task added successfully");
                    }
                });

                edt_title.getText().clear();
                edt_description.getText().clear();
            }
        });

        final Query query = firebaseFirestore.collection(userId).document("task").collection("tasks").orderBy("preference", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<notes> options = new FirestoreRecyclerOptions.Builder<notes>().setQuery(query,notes.class).build();

        adapter = new FirestoreRecyclerAdapter<notes, notesViewHolder>(options) {                   /* adapter code */

            @NonNull
            @Override
            public notesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
                return new notesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final notesViewHolder holder, int position, @NonNull final notes model) {

                temp = adapter.getItemCount();
                Log.d("DELETE", temp +"on bind");
                holder.reminder.setVisibility(GONE);
                holder.delete_button.setVisibility(GONE);
                holder.edit_button.setVisibility(GONE);

                holder.note_title.setText(model.getTitle());
                holder.note_description.setText(model.getDescription());

                //holder.radioButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_radio_button));
                holder.radioButton.setBackgroundResource(R.drawable.ic_baseline_radio_button);

               if(model.getTime()==null || model.getTime().equals("") || model.getDate()==null || model.getDate().equals(""))
                {
                    Log.d("TAG", "Time is null" );
                }
                else{
                    Log.d("TAG", model.getTime());
                    holder.reminder.setVisibility(View.VISIBLE);
                }

               /* if(holder.reminder.getVisibility()==View.VISIBLE){
                    String message = "This is a demo notification";
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this,"DailyBuddy")
                            .setSmallIcon(R.drawable.kavyalogo)
                            .setContentTitle("New Notification")
                            .setContentText(message)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);

                    NotificationManager notificationManager = (NotificationManager)getSystemService(
                            Context.NOTIFICATION_SERVICE
                    );
                    notificationManager.notify(0,builder.build());
                } */

                final DocumentReference imp = firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase());
                imp.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d("SNAPSHOT","INSIDE");

                        try{
                            if(documentSnapshot.get("preference").equals("0")){
                                Log.d("TOGGLE","Starting pref 0");
                                holder.toggleButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_star_border_24));
                               // holder.radioButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_radio_button));
                                holder.radioButton.setBackgroundResource(R.drawable.ic_baseline_radio_button);
                            }
                            else if(documentSnapshot.get("preference").equals("1")){
                                Log.d("TOGGLE","Starting pref 1");
                                holder.toggleButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_star_24));
                               // holder.radioButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_radio_button));
                                holder.radioButton.setBackgroundResource(R.drawable.ic_baseline_radio_button);
                             }
                            else if(documentSnapshot.get("preference").equals("-1")){
                                holder.toggleButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_star_border_24));
                                holder.note_title.setPaintFlags(holder.note_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                              //  holder.radioButton.setBackground(getResources().getDrawable(R.drawable.done));
                                holder.radioButton.setBackgroundResource(R.drawable.done);
                                holder.toggleButton.setClickable(false);

                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        try{
                            if(documentSnapshot.get("preference").equals("1")){
                                count++;
                                initializeCountDrawer(count);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

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

                            if(model.getDate().equals("")){
                                holder.date_display.setText(R.string.no_duedate);
                            }else{
                                holder.date_display.setText(model.getDate());
                            }
                        }

                        if(model.getTime()!=null){
                            holder.time_icon.setVisibility(View.VISIBLE);
                            holder.time_display.setVisibility(View.VISIBLE);

                            if(model.getTime().equals("")){
                                holder.time_display.setText(R.string.no_duetime);
                            }else{
                                holder.time_display.setText(model.getTime());
                            }
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

                holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            holder.radioButton.setBackground(getResources().getDrawable(R.drawable.done));
                            preference="-1";
                            model.preference="-1";
                            firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).update("preference",model.getPreference());
                            firebaseFirestore.collection(userId).document("task").collection("imp").document(model.getTitle().toLowerCase()).update("preference",model.getPreference());
                            holder.note_title.setPaintFlags(holder.note_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                            count--;
                            initializeCountDrawer(count);

//                            holder.radioButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_radio_button_checked));
                            }
                        else{
                          //  holder.radioButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_radio_button));
                            holder.radioButton.setBackgroundResource(R.drawable.ic_baseline_radio_button);
                            preference="0";
                            model.preference="0";
                            firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).update("preference",model.getPreference());

                        }
                    }
                });

                holder.note_title.setPaintFlags(0);
              //  holder.radioButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_radio_button));
                holder.radioButton.setBackgroundResource(R.drawable.ic_baseline_radio_button);

//                holder.radioButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                            holder.note_title.setPaintFlags(holder.note_title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                            model.preference="-1";
//                            holder.toggleButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_star_border_24));
//
//                        firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).update("preference",model.getPreference());
//                           // firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).delete();
//                    }
//                });

                holder.edit_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this,EditOption.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("description", model.getDescription());
                        intent.putExtra("preference",model.getPreference());
                        intent.putExtra("duedate",model.getDate());
                        intent.putExtra("duetime",model.getTime());
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
                                if(model.getPreference().equals("1")){
                                    count--;
                                    initializeCountDrawer(count);
                                }
                                temp--;
                                Log.d("DELETE", temp +"after deletion");

                                if(temp==0)
                                {
                                    noTasks.setVisibility(View.VISIBLE);
                                    textWinning.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    noTasks.setVisibility(GONE);
                                    textWinning.setVisibility(GONE);
                                }
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
                                        temp++;
                                        Log.d("DELETE", temp +"after undo");
                                        firebaseFirestore.collection(userId).document("task").collection("tasks").document(model.getTitle().toLowerCase()).set(model);
                                        firebaseFirestore.collection(userId).document("task").collection("imp").document(model.getTitle().toLowerCase()).set(model);
                                    }
                                });
                        snackbar.show();
                    }
                });

                if(temp == 0)
                {
                    noTasks.setVisibility(View.VISIBLE);
                    textWinning.setVisibility(View.VISIBLE);
                }else{
                    noTasks.setVisibility(View.GONE);
                    textWinning.setVisibility(View.GONE);
                }

                final HashMap<String,Object> imp_map = new HashMap<>();

                holder.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            Log.d("TOGGLE","Is Checked");
                            holder.toggleButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_star_24));
                            preference="1";

                            DocumentReference dr = firebaseFirestore.collection(userId).document("task").collection("imp").document(model.getTitle());
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("title",model.getTitle());
                            map.put("description",model.getDescription());
                            map.put("preference",preference);
                            map.put("date",model.getDate());
                            map.put("time",model.getTime());
                            dr.set(map, SetOptions.merge());

                            imp_map.put("preference",preference);
                            imp.update(imp_map);
                        }
                        else{
                            Log.d("TOGGLE","Is Not Checked");
                            holder.toggleButton.setBackground(getResources().getDrawable(R.drawable.ic_baseline_star_border_24));
                            preference="0";
                            DocumentReference dr = firebaseFirestore.collection(userId).document("task").collection("imp").document(model.getTitle());
                            dr.delete();
                            imp_map.put("preference",preference);
                            imp.update(imp_map);
                            count--;
                            initializeCountDrawer(count);
                        }
                    }
                });

            }
        };
        if(temp==0)
        {
            noTasks.setVisibility(View.VISIBLE);
            textWinning.setVisibility(View.VISIBLE);
        }
        else
        {
            noTasks.setVisibility(GONE);
            textWinning.setVisibility(GONE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initializeCountDrawer(int count) {
        impTextView.setGravity(Gravity.CENTER_VERTICAL);
        impTextView.setTypeface(null, Typeface.BOLD);
        impTextView.setTextColor(getResources().getColor(R.color.colorNoteColor5));

        impTextView.setText(""+count);
    }

    private void recordSpeechTitle() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,"en-US");
        try{
            startActivityForResult(intent,1);

        }catch (Exception e){
            Toast.makeText(this, "Your device doesnot support speech record", Toast.LENGTH_SHORT).show();
        }
    }

    private void recordSpeechDescription() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,"en-US");
        try{
            startActivityForResult(intent,2);

        }catch (Exception e){
            Toast.makeText(this, "Your device doesnot support speech record", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!= null){
            ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            edt_title.setText(text.get(0));
        } else if(requestCode==2 && resultCode==RESULT_OK && data!= null){
            ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            edt_description.setText(text.get(0));
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            finish();
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
            case R.id.Important:
            {
                Toast.makeText(this, "Inside fav", Toast.LENGTH_SHORT).show();
                 Intent intent = new Intent(MainActivity.this,ImportantActivity.class);
                 intent.putExtra("userID",userId);
                 finish();
                 startActivity(intent);
                 return true;
            }
            case R.id.logout:
            {
                googleSignInClient.signOut();
                finish();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
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
        ToggleButton radioButton;
        ToggleButton toggleButton;

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
            toggleButton = itemView.findViewById(R.id.important_icon);
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
