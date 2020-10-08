package com.kavya.dailybuddy;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class EditOption extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;

    ImageView calendar_img,time_img;
    EditText title_edttext,des_edttxt,date,time;
    FloatingActionButton floatingActionButton;
    private int mDate,mMonth,mYear;
    private int mHour,mMinute;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_option);

        Intent intent = getIntent();

        firebaseFirestore = FirebaseFirestore.getInstance();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext()); /* getting user ID */
        userId =account.getId();


        final String edit_title = intent.getStringExtra("title");
        final String edit_description = intent.getStringExtra("description");

        calendar_img = findViewById(R.id.calendar);
        date = findViewById(R.id.pick_a_date);
        time_img = findViewById(R.id.time_imgview);
        time = findViewById(R.id.select_time);
        title_edttext = findViewById(R.id.title_edt);
        des_edttxt = findViewById(R.id.des_edt);
        floatingActionButton = findViewById(R.id.fab_done);

        title_edttext.setText(edit_title);
        des_edttxt.setText(edit_description);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cal = Calendar.getInstance();
                mDate = cal.get(Calendar.DATE);
                mMonth = cal.get(Calendar.MONTH);
                mYear = cal.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(EditOption.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                    }
                },mYear,mMonth,mDate);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
                datePickerDialog.show();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(EditOption.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mHour = hourOfDay;
                        mMinute = minute;
                        String str_time = mHour+":"+mMinute;
                        SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");
                        try {
                            Date mDate = f24Hours.parse(str_time);
                            SimpleDateFormat f12Hours = new SimpleDateFormat("hh:mm aa");
                            time.setText(f12Hours.format(mDate));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },12,0,false);
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timePickerDialog.updateTime(mHour,mMinute);
                timePickerDialog.show();
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String d = date.getText().toString();
                String t = time.getText().toString();

                DocumentReference documentReference = firebaseFirestore.collection(userId).document("task").collection("tasks").document(edit_title);
                HashMap<String, Object> save_changes = new HashMap<>();
                save_changes.put("title", edit_title);
                save_changes.put("description", des_edttxt.getText().toString());
                if(d == null)
                    save_changes.put("date", null);
                else
                    save_changes.put("date", date.getText().toString());
                if(t == null)
                    save_changes.put("time", null);
                else
                    save_changes.put("time", time.getText().toString());

                documentReference.set(save_changes).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditOption.this, "Changes done!", Toast.LENGTH_SHORT).show();
                        Intent intent1 = new Intent(EditOption.this, MainActivity.class);
                        finish();
                        startActivity(intent1);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditOption.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

               /*documentReference.set(save_changes).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditOption.this, "Changes done!", Toast.LENGTH_SHORT).show();
                        documentReference.get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.contains("time")) {
                                            Toast.makeText(EditOption.this, "CONTAINS TIME", Toast.LENGTH_LONG).show();
                                            containsTime = true;
                                        } else {
                                            Toast.makeText(EditOption.this, "DOESNOT CONTAIN TIME", Toast.LENGTH_SHORT).show();
                                        }

                                        intent1.putExtra("flag_for_time",containsTime);
                                        intent1.putExtra("document_name",edit_title);
                                        startActivity(intent1);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditOption.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                });*/


            }
        });
    }

}

