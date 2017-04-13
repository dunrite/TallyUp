package com.dunrite.tallyup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.dunrite.tallyup.R;
import com.dunrite.tallyup.fragments.IntroSlide;
import com.dunrite.tallyup.utility.Utils;
import com.github.paolorotolo.appintro.AppIntro2;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Activity that is used the first time a user opens the app
 */
public class IntroActivity extends AppIntro2 implements GoogleApiClient.OnConnectionFailedListener {
    public GoogleApiClient mGoogleApiClient;
    public static final int RC_SIGN_IN = 9001;
    public GoogleSignInOptions gso;

    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(IntroSlide.newInstance(R.layout.fragment_intro1));
        addSlide(IntroSlide.newInstance(R.layout.fragment_intro2));
        addSlide(IntroSlide.newInstance(R.layout.fragment_intro3));
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build GoogleApiClient with AppInvite API for receiving deep links AND for the firebase stuff
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
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

    public void launchGoogleSignInIntent() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                Intent intent = new Intent();
                intent.putExtra("googleSignInAccount", account);
                setResult(RC_SIGN_IN, intent);
                Utils.appHasLaunched(this);
                finish();
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }
}
