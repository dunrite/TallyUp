package com.dunrite.tallyup.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dunrite.tallyup.R;
import com.dunrite.tallyup.utility.Utils;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;

/**
 *  Activity class containing everything partaining to Firebase
 */

public class FirebaseActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    public FirebaseAuth mAuth;
    public FirebaseAuth.AuthStateListener mAuthListener;
    public GoogleApiClient mGoogleApiClient;
    public DatabaseReference mDatabase;
    public GoogleSignInOptions gso;
    public static final String TAG = "GoogleActivity";
    public static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        mAuth = FirebaseAuth.getInstance();


    }

    public void signInToFirebase() {
        if (Utils.isOnline(getApplicationContext())) {
            if (mAuth.getCurrentUser() != null) {
                if (mAuth.getCurrentUser().isAnonymous()) {
                    Log.d("signInToFirebase", "Anon (First If)");
                    firebaseAuthAnonymous();
                } else {
                    Log.d("signInToFirebase", "Silent Google");
                    silentGoogleLogin();
                }
            } else {
                Log.d("signInToFirebase", "Anon (Second else)");
                firebaseAuthAnonymous();
            }
        } else {
            Snackbar.make(findViewById(R.id.activity_main), "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signInToFirebase();
                }
            }).show();
        }
    }

    public void silentGoogleLogin() {
        OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult != null) {
            handleGooglePendingResult(pendingResult);
        } else {

        }
    }

    private void handleGooglePendingResult(OptionalPendingResult<GoogleSignInResult> pendingResult) {
        if (pendingResult.isDone()) {
            // There's immediate result available.
            GoogleSignInResult signInResult = pendingResult.get();
            onSilentSignInCompleted(signInResult);
        } else {
            // There's no immediate result ready,  waits for the async callback.
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult signInResult) {
                    onSilentSignInCompleted(signInResult);
                }
            });
        }
    }

    private void onSilentSignInCompleted(GoogleSignInResult signInResult) {
        GoogleSignInAccount signInAccount = signInResult.getSignInAccount();
        if (signInAccount != null) {
            // you have a valid sign in account. Skip the login.
            Log.d("SilentSignIn", "VALID SIGN IN");
            firebaseAuthGoogle(signInAccount);
        } else {
            Log.d("SilentSignIn", "NOT A VALID SIGN IN");
            // you don't have a valid sign in account. Eventually display the login page again
        }
    }

    /**
     * Connect to the Firebase database with Google account
     * @param acct google account
     */
    public void firebaseAuthGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() == null) {
            mAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());
                            gatherDataFromFirebase(task);

                        }
                    });
        } else {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                            gatherDataFromFirebase(task);
                        }
                    });
        }

    }

    /**
     * Connect to the Firebase database with anonymous account
     */
    public void firebaseAuthAnonymous() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        gatherDataFromFirebase(task);
                    }
                });
    }

    public void gatherDataFromFirebase(Task<AuthResult> task) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
