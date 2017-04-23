package com.dunrite.tallyup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dunrite.tallyup.R;
import com.dunrite.tallyup.utility.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;
import shortbread.Shortcut;

/**
 * Activity for creation of a new poll
 */
@Shortcut(id = "new_poll", icon = R.drawable.ic_create_green_24dp, shortLabel = "New Poll")
public class CreateActivity extends FirebaseActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.questionText) EditText question;
    //@BindView(R.id.multiSelect) CheckBox multiSelect;
    @BindView(R.id.type_spinner) Spinner typeSpinner;
    @BindView(R.id.timeDurationButton) Button timeButton;
    @BindView(R.id.item0) EditText item0;
    @BindView(R.id.item1) EditText item1;
    @BindView(R.id.item2) EditText item2;
    @BindView(R.id.item3) EditText item3;
    @BindView(R.id.confirm_create_fab) FloatingActionButton fab;
    private String formattedQuestion;
    private String pollID;
    private long durationInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        durationInMillis = 600000;
        formatLengthButton(durationInMillis);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.choice_types, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(spinnerAdapter);
    }

    @OnClick(R.id.timeDurationButton)
    public void onClickTimeButton() {
        TimeDurationPickerDialog dialog = new TimeDurationPickerDialog(this, new TimeDurationPickerDialog.OnDurationSetListener() {
            @Override
            public void onDurationSet(TimeDurationPicker view, long duration) {
                durationInMillis = duration;
                formatLengthButton(durationInMillis);
            }
        }, durationInMillis);
        dialog.show();
    }

    @OnClick(R.id.confirm_create_fab)
    public void onClickFAB() {
        if (validQuestion()) {
            formattedQuestion = formatQuestion(question.getText().toString());
            signInToFirebase();
        }
    }

    private String formatQuestion(String s) {
        if(!s.endsWith("?")){
            s = s + "?";
        }
        return s;
    }

    private void formatLengthButton(long millis) {
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;
        String time = String.format("Length: %02dh %02dm %02ds", hour, minute, second);
        timeButton.setText(time);
    }

    private String millisFromNow(long millis) {
        Date now = Calendar.getInstance().getTime();
        now.setTime(now.getTime() + millis);
        return java.text.DateFormat.getDateTimeInstance().format(now);
    }

    private boolean validQuestion() {
        //TODO: validate form
        return true;
    }

    private HashMap<String, Object> constructChoice(EditText et) {
        HashMap<String, Object> temp = new HashMap<>();
        temp.put("Name", et.getText().toString());
        return temp;
    }

    @Override
    public void gatherDataFromFirebase(Task<AuthResult> task) {
        //Login successful
        Map<String, Object> newQuestion = new HashMap<>();
        Map<String, Object> i0 = constructChoice(item0);
        Map<String, Object> i1 = constructChoice(item1);
        if (!item2.getText().toString().equals("")) {
            Map<String, Object> i2 = constructChoice(item2);
            newQuestion.put("Item2", i2);
        }
        if (!item3.getText().toString().equals("")) {
            Map<String, Object> i3 = constructChoice(item3);
            newQuestion.put("Item3", i3);
        }
        newQuestion.put("Question", formattedQuestion);
        newQuestion.put("Item0", i0);
        newQuestion.put("Item1", i1);
        newQuestion.put("OwnerID", mAuth.getCurrentUser().getUid());
        newQuestion.put("Type", typeSpinner.getSelectedItem().toString());
        newQuestion.put("ExpireTime", millisFromNow(durationInMillis));
        mDatabase = FirebaseDatabase.getInstance().getReference("Polls");
        pollID = Utils.generateSaltString();
        mDatabase.child(pollID).updateChildren(newQuestion).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Open poll when created
                Intent i = new Intent(getApplicationContext(), PollActivity.class);
                i.putExtra("pollID", pollID);
                i.putExtra("pollQuestion", formattedQuestion);
                startActivity(i);
                finish();
            }
        });

        //login failure
        if (!task.isSuccessful()) {
            Log.w("auth", "signInAnonymously", task.getException());
            Toast.makeText(CreateActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
