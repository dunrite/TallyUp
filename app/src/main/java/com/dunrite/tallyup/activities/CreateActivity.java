package com.dunrite.tallyup.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.dunrite.tallyup.R;
import com.dunrite.tallyup.utility.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity for creation of a new poll
 */
public class CreateActivity extends AppCompatActivity {
    @BindView(R.id.questionText) EditText question;
    @BindView(R.id.multiSelect) CheckBox multiSelect;
    @BindView(R.id.item0) EditText item0;
    @BindView(R.id.item1) EditText item1;
    @BindView(R.id.item2) EditText item2;
    @BindView(R.id.item3) EditText item3;
    @BindView(R.id.confirm_create_fab) FloatingActionButton fab;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String formattedQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.confirm_create_fab)
    public void onClickFAB() {
        if (validQuestion()) {
            formattedQuestion = formatQuestion(question.getText().toString());

            pushQuestionToFirebase(formattedQuestion);
        }
    }

    private String formatQuestion(String s) {
        if(!s.endsWith("?")){
            s = s + "?";
        }
        return s;
    }

    private boolean validQuestion() {
        //TODO: validate form
        return true;
    }

    private void pushQuestionToFirebase(final String q) {
        if(Utils.isOnline(getApplicationContext())) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //Login successful
                            mDatabase = FirebaseDatabase.getInstance().getReference("Polls");
                            mDatabase.child(Utils.generateSaltString()).child("Question").setValue(q);


                            //login failure
                            if (!task.isSuccessful()) {
                                Log.w("auth", "signInAnonymously", task.getException());
                                Toast.makeText(CreateActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Snackbar.make(findViewById(R.id.activity_main), "No Internet Connection", Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pushQuestionToFirebase(formattedQuestion);
                }
            }).show();
        }
    }
}
