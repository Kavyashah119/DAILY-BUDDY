package com.kavya.dailybuddy;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.text.DateFormat;
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
    LinearLayout layoutMiscellaneous;
    BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    String selectedNoteColor;
    View viewSubtitleIndicator;
    ImageView imageColor1,imageColor2,imageColor3,imageColor4,imageColor5;
    private ImageView imageNote;
    private String selectedImagePath;
    ImageView pin_imageview;
    ImageView delete;

    String pinned = "0";

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private TextView textWebURL;
    private LinearLayout layoutWebURL;
    private AlertDialog dialogAddURL;

    DocumentReference doc;
    Map<String, Object> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        firebaseFirestore = FirebaseFirestore.getInstance();

        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext()); /* getting user ID */
        userId = account.getId();

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputNoteSubTitle);
        inputNoteText = findViewById(R.id.inputNote);
        inputTextDateTime = findViewById(R.id.textDateTime);
        imgSave = findViewById(R.id.imageSave);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        imageNote = findViewById(R.id.imageNote);
        textWebURL = findViewById(R.id.textWebURL);
        layoutWebURL = findViewById(R.id.layoutWebURL);
        pin_imageview = findViewById(R.id.pin);
        delete = findViewById(R.id.delete);

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        final Intent intent = getIntent();
        final String title_from_intent = intent.getStringExtra("note_title");
        String subtitle_from_intent = intent.getStringExtra("note_subtitle");
        final String text_from_intent = intent.getStringExtra("note_text");
        final String pinned_from_intent = intent.getStringExtra("note_pinned");

        inputNoteTitle.setText(title_from_intent);
        inputNoteSubtitle.setText(subtitle_from_intent);
        inputNoteText.setText(text_from_intent);

        imageColor1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
        imageColor2 = layoutMiscellaneous.findViewById(R.id.imageColor2);
        imageColor3 = layoutMiscellaneous.findViewById(R.id.imageColor3);
        imageColor4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
        imageColor5 = layoutMiscellaneous.findViewById(R.id.imageColor5);

        try{
            final String color_from_intent = intent.getStringExtra("note_color");
            //GradientDrawable gradientDrawableColor = (GradientDrawable)viewSubtitleIndicator.getBackground();
            //gradientDrawableColor.setColor(Color.parseColor(color_from_intent));
            selectedNoteColor=color_from_intent;
            setSubtitleIndicatorColor();
         }
        catch (Exception e){
            Log.d("TAG",e.getMessage());
        }

        try{
            final String img_from_intent = intent.getStringExtra("note_img");
            if(!(img_from_intent.equals(""))){
                imageNote.setImageBitmap(BitmapFactory.decodeFile(img_from_intent));
                imageNote.setVisibility(View.VISIBLE);
            }

        }catch(Exception e){
            Log.d("TAG",e.getMessage());
        }

        try {
            final String url_from_intent = intent.getStringExtra("note_url");
            if(!(url_from_intent.equals(""))){
                textWebURL.setText(url_from_intent);
                layoutWebURL.setVisibility(View.VISIBLE);
            }

        }catch(Exception e){
            Log.d("TAG",e.getMessage());
        }

        try{
            if(pinned_from_intent.equals("0")){
                pin_imageview.setColorFilter(Color.argb(255,164, 164, 164));
            }else if(pinned_from_intent.equals("1")){
                pin_imageview.setColorFilter(Color.argb(255,135,206,235));
            }
        }catch (Exception e){
            Log.d("TAG",e.getMessage());
        }

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
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

        pin_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    doc = firebaseFirestore.collection(userId).document("note").collection("notes").document(title_from_intent);
                    map = new HashMap<>();
                }
                catch (Exception e)
                {
                   doc = firebaseFirestore.collection(userId).document("note").collection("notes").document(inputNoteTitle.getText().toString());
                   map = new HashMap<>();
                   map.put("title",inputNoteTitle.getText().toString());
                   map.put("pinned",pinned);
                   doc.set(map);
                }

                doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.get("pinned").equals("0")){
                            pin_imageview.setColorFilter(Color.argb(255,135,206,235));
                            pinned="1";
                            map.put("pinned",pinned);
                            doc.update(map);
                            Toast.makeText(CreateNoteActivity.this, "This note is pinned", Toast.LENGTH_SHORT).show();
                        }else{
                            pin_imageview.setColorFilter(Color.argb(255, 164, 164, 164));
                            pinned="0";
                           map.put("pinned",pinned);
                           doc.update(map);
                            Toast.makeText(CreateNoteActivity.this, "This note is not pinned", Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG",e.getMessage());
                    }
                });
            }
        });

        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if(!(title_from_intent.equals(null)))
                        firebaseFirestore.collection(userId).document("note").collection("notes").document(title_from_intent);
                }
                catch (Exception e)
                {
                    Log.d("TAG", e.getMessage());
                }

                documentReference = firebaseFirestore.collection(userId).document("note").collection("notes").document(inputNoteTitle.getText().toString());
                final Map<String,Object> note_map = new HashMap<>();

                note_map.put("title",inputNoteTitle.getText().toString());
                note_map.put("subtitle",inputNoteSubtitle.getText().toString());
                note_map.put("text",inputNoteText.getText().toString());
                note_map.put("datetime",inputTextDateTime.getText().toString());
                note_map.put("color",selectedNoteColor);
                note_map.put("imagepath",selectedImagePath);
                note_map.put("webURL",textWebURL.getText().toString());
                note_map.put("pinned",pinned);

                documentReference.set(note_map);

                Toast.makeText(CreateNoteActivity.this, "Note saved", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CreateNoteActivity.this,NotesActivity.class);
                finish();
                startActivity(intent);
            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);

                builder.setTitle("Confirm Delete");
                builder.setMessage("Are you sure you want to delete?");
                builder.setCancelable(true);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            firebaseFirestore.collection(userId).document("note").collection("notes").document(title_from_intent).delete();
                            Intent intent1 = new Intent(CreateNoteActivity.this,NotesActivity.class);
                            finish();
                            startActivity(intent1);
                        }catch (Exception e){
                            firebaseFirestore.collection(userId).document("note").collection("notes").document(inputNoteTitle.getText().toString()).delete();
                            Intent intent1 = new Intent(CreateNoteActivity.this,NotesActivity.class);
                            finish();
                            startActivity(intent1);
                        }
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
        });

        layoutMiscellaneous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetBehavior.getState()!=BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        setSubtitleIndicatorColor();

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#333333";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FDBE3B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#3A52Fc";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else{
                    selectImage();
                }
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });
    }

    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable)viewSubtitleIndicator.getBackground();
        //gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
        try{
            gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            Log.d("PHOTO","if in select img");
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("PHOTO","Inside on activity result");
        super.onActivityResult(requestCode, resultCode, data);
            Log.d("PHOTO","URI here");
            if(data != null){
                Log.d("PHOTO","URI there");
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try{
                        Log.d("PHOTO","Try");
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);

                    }catch(Exception e){
                        Log.d("PHOTO","Catch");
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        private String getPathFromUri(Uri contentUri){
        String filepath;
            Cursor cursor = getContentResolver()
                    .query(contentUri,null,null,null,null);
            if(cursor == null){
                filepath = contentUri.getPath();
            }else{
                cursor.moveToFirst();
                int index = cursor.getColumnIndex("_data");
                filepath = cursor.getString(index);
                cursor.close();
            }
            return  filepath;
        }

        private void showAddURLDialog(){
           if(dialogAddURL == null){
               AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
               View view = LayoutInflater.from(this).inflate(
                       R.layout.layout_add_url,
                       (ViewGroup)findViewById(R.id.layoutAddUrlContainer)
               );
               builder.setView(view);

               dialogAddURL = builder.create();
               if(dialogAddURL.getWindow()!=null){
                   dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
               }

               final EditText inputURL = view.findViewById(R.id.inputURL);
               inputURL.requestFocus();

               view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       if(inputURL.getText().toString().trim().isEmpty()){
                           Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                       }else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                           Toast.makeText(CreateNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                       }else{
                           textWebURL.setText(inputURL.getText().toString());
                           layoutWebURL.setVisibility(View.VISIBLE);
                           dialogAddURL.dismiss();
                       }
                   }
               });

               view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       dialogAddURL.dismiss();
                   }
               });
           }
           dialogAddURL.show();
        }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(CreateNoteActivity.this,NotesActivity.class));
    }
}