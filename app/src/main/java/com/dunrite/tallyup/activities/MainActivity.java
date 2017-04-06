package com.dunrite.tallyup.activities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.dunrite.tallyup.R;
import com.dunrite.tallyup.utility.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Main activity that shows active and closed polls for the user
 */
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.fab_create) FloatingActionButton fabCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setRecentsStyle();

        if(Utils.isFirstLaunch(this)) {
            Intent intent = new Intent(this, IntroActivity.class); //call Intro class
            startActivity(intent);
        }
    }

    @OnClick(R.id.fab_create)
    public void onClickFab() {
        Intent intent = new Intent(this, CreateActivity.class); //call Intro class
        startActivity(intent);
    }

    /**
     * Sets custom white logo on recent app menu card
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setRecentsStyle() {
        Bitmap recentsIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_white);
        String title = "TallyUp";
        int color = ContextCompat.getColor(this, R.color.colorPrimary);

        ActivityManager.TaskDescription description =
                new ActivityManager.TaskDescription(title, recentsIcon, color);
        this.setTaskDescription(description);
    }
}
