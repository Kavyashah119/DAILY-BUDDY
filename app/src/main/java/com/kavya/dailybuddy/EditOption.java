package com.kavya.dailybuddy;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
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
import androidx.core.app.NotificationCompat;

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
import java.util.Locale;

public class EditOption extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;
    ImageView calendar_img,time_img;
    EditText title_edttext,des_edttxt,date,time,set_reminder;
    FloatingActionButton floatingActionButton;
    private int mDate,mMonth,mYear;
    private int mHour,mMinute;
    String userId;

    String date_time = "";
    String time_date="";
    int year;
    int month;
    int day;

    int hour;
    int min;

    String final_notify= "";

    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_option);

        final Intent intent = getIntent();

        firebaseFirestore = FirebaseFirestore.getInstance();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext()); /* getting user ID */
        userId =account.getId();

        final String edit_title = intent.getStringExtra("title");
        final String edit_description = intent.getStringExtra("description");
        final String edit_preference = intent.getStringExtra("preference");
        final String edit_date = intent.getStringExtra("duedate");
        final String edit_time = intent.getStringExtra("duetime");

        calendar_img = findViewById(R.id.calendar);
        date = findViewById(R.id.pick_a_date);
        time_img = findViewById(R.id.time_imgview);
        time = findViewById(R.id.select_time);
        title_edttext = findViewById(R.id.title_edt);
        des_edttxt = findViewById(R.id.des_edt);
        floatingActionButton = findViewById(R.id.fab_done);
        set_reminder = findViewById(R.id.set_reminder);

        title_edttext.setText(edit_title);
        des_edttxt.setText(edit_description);
        date.setText(edit_date);
        time.setText(edit_time);

        final Calendar myCalendar = Calendar. getInstance () ;

        set_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
                /*final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet (DatePicker view , int year , int monthOfYear , int dayOfMonth) {
                        myCalendar .set(Calendar. YEAR , year) ;
                        myCalendar .set(Calendar. MONTH , monthOfYear) ;
                        myCalendar .set(Calendar. DAY_OF_MONTH , dayOfMonth) ;
                        updateLabel() ;
                    }
                }; */
            }

            private void selectDate() {
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditOption.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                myCalendar .set(Calendar. YEAR , year) ;
                                myCalendar .set(Calendar. MONTH , monthOfYear) ;
                                myCalendar .set(Calendar. DAY_OF_MONTH , dayOfMonth) ;
                                updateLabel();

                                //date_time = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                                //selectTime();
                            }
                        }, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
                datePickerDialog.show();
            }
            private void updateLabel () {
                String myFormat = "dd/MM/yy" ; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat , Locale. getDefault ()) ;
                Date date = myCalendar.getTime();
                set_reminder.setText(sdf.format(date));
                scheduleNotification(getNotification(set_reminder.getText().toString()) ,date.getTime()) ;
            }

            private void selectTime(){
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                min = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(EditOption.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                hour = hourOfDay;
                                min = minute;
                                time_date = hourOfDay+":"+minute;

                                final_notify = date_time+" "+time_date;
                                set_reminder.setText(final_notify);

                            }

                        }, hour, min, false);
                timePickerDialog.show();
            }
        });

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
                save_changes.put("preference",edit_preference);

                DocumentReference doc = firebaseFirestore.collection(userId).document("task").collection("imp").document(edit_title);
                HashMap<String, Object> map = new HashMap<>();
                map.put("title", edit_title);
                map.put("description", des_edttxt.getText().toString());
                if(d == null)
                    map.put("date", null);
                else
                    map.put("date", date.getText().toString());
                if(t == null)
                    map.put("time", null);
                else
                    map.put("time", time.getText().toString());
                map.put("preference",edit_preference);

                doc.set(map);

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

            }
        });
    }

    private void scheduleNotification (Notification notification , long delay) {
        Intent notificationIntent = new Intent( EditOption.this,ReminderBroadcast.class) ;
        notificationIntent.putExtra(ReminderBroadcast.NOTIFICATION_ID , 1 ) ;
        notificationIntent.putExtra(ReminderBroadcast. NOTIFICATION , notification) ;
        PendingIntent pendingIntent = PendingIntent. getBroadcast ( this, 0 , notificationIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context. ALARM_SERVICE ) ;
        assert alarmManager != null;
        alarmManager.set(AlarmManager. ELAPSED_REALTIME_WAKEUP ,delay , pendingIntent) ;
    }

    private Notification getNotification (String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( this,default_notification_channel_id ) ;
        builder.setContentTitle("Scheduled Notification") ;
        builder.setContentText(content) ;
        builder.setSmallIcon(R.drawable.ic_launcher_foreground) ;
        builder.setAutoCancel(true) ;
        builder.setChannelId(NOTIFICATION_CHANNEL_ID) ;
        return builder.build() ;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EditOption.this,MainActivity.class);
        finish();
        startActivity(intent);
    }
}

