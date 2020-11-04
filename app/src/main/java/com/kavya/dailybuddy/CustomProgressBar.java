package com.kavya.dailybuddy;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

public class CustomProgressBar extends Dialog {

    Activity activity;

    public CustomProgressBar(Activity act) {
        super(act);
        this.activity=act;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_custom_progress_bar);

        setCancelable(false);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }
}