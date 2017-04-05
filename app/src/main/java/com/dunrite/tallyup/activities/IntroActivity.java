package com.dunrite.tallyup.activities;

import android.content.Intent;
import android.os.Bundle;

import com.dunrite.tallyup.R;
import com.dunrite.tallyup.fragments.IntroSlide;
import com.dunrite.tallyup.utility.Utils;
import com.github.paolorotolo.appintro.AppIntro2;

/**
 * Activity that is used the first time a user opens the app
 */
public class IntroActivity extends AppIntro2{
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(IntroSlide.newInstance(R.layout.fragment_intro1));
        addSlide(IntroSlide.newInstance(R.layout.fragment_intro2));
        addSlide(IntroSlide.newInstance(R.layout.fragment_intro3));
    }

    @Override
    public void onDonePressed() {
        Utils.appHasLaunched(this);
        Intent intent = new Intent(this, MainActivity.class); //call Intro class
        startActivity(intent);
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }

    @Override
    public void onBackPressed() {
        //DOn't allow user to exit intro this way
    }
}
