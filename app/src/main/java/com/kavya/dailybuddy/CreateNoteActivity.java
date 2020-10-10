package com.kavya.dailybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle,inputNoteSubtitle,inputNoteText;
    private TextView inputTextDateTime;
    String userId;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    ImageView imgSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        final Intent intent = getIntent();
        final String title_from_intent = intent.getStringExtra("note_title");
        String subtitle_from_intent = intent.getStringExtra("note_subtitle");
        final String text_from_intent = intent.getStringExtra("note_text");

        firebaseFirestore = FirebaseFirestore.getInstance();

        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext()); /* getting user ID */
        userId = account.getId();

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubTitle);
        inputNoteText = findViewById(R.id.inputNote);
        inputTextDateTime = findViewById(R.id.textDateTime);
        imgSave = findViewById(R.id.imageSave);

        inputNoteTitle.setText(title_from_intent);
        inputNoteSubtitle.setText(subtitle_from_intent);
        inputNoteText.setText(text_from_intent);

        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        Calendar sCalendar = Calendar.getInstance();
        String dayLongName = sCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

        inputTextDateTime.setText(dayLongName+" , "+currentDateTimeString);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(CreateNoteActivity.this,NotesActivity.class));
            }
        });

        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if(!(title_from_intent.equals(null)))
                        firebaseFirestore.collection(userId).document("note").collection("notes").document(title_from_intent).delete();
                }
                catch (Exception e)
                {
                    Log.d("TAG", e.getMessage());
                }

                documentReference = firebaseFirestore.collection(userId).document("note").collection("notes").document(inputNoteTitle.getText().toString());

                Map<String,Object> note_map = new HashMap<>();
                note_map.put("title",inputNoteTitle.getText().toString());
                note_map.put("subtitle",inputNoteSubtitle.getText().toString());
                note_map.put("text",inputNoteText.getText().toString());
                note_map.put("datetime",inputTextDateTime.getText().toString());

                documentReference.set(note_map);

                Toast.makeText(CreateNoteActivity.this, "Note saved", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CreateNoteActivity.this,NotesActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(CreateNoteActivity.this,NotesActivity.class));
    }
}