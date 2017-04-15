package com.dunrite.tallyup.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.dunrite.tallyup.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import shortbread.Shortcut;

/**
 * About activity
 */
public class AboutActivity extends AppCompatActivity {
    @BindView(R.id.versionNum) TextView versionNum;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.a_dunrite_app) ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String versionName = "";
        PackageInfo packageInfo;
        //Inserts the correct version number of the app
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionNum.setText(versionName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @OnClick(R.id.a_dunrite_app)
    public void onClickFab() {
        Uri uri = Uri.parse("http://www.dunriteapps.com/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri); 
        startActivity(intent);
    }
}