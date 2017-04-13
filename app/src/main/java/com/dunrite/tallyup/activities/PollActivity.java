package com.dunrite.tallyup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.dunrite.tallyup.ArrayListAnySize;
import com.dunrite.tallyup.PollItem;
import com.dunrite.tallyup.R;
import com.dunrite.tallyup.adapters.PollChoiceAdapter;
import com.dunrite.tallyup.utility.Utils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
/**
 * Activity that displays a poll
 */
public class PollActivity extends FirebaseActivity {
    private String pollID;
    private int selectedItem; //If the user previously participated, we need to remember their choice
    private ArrayListAnySize<PollItem> pollItems;
    private PollChoiceAdapter adapter;

    @BindView(R.id.questionText) TextView questionText;
    @BindView(R.id.recyclerview_choices) RecyclerView choicesRV;
    @BindView(R.id.main_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pollItems = new ArrayListAnySize<>();
        selectedItem = -1;

        Intent intent = getIntent();
        if (intent.hasExtra("pollID"))
            pollID = intent.getStringExtra("pollID");
        if (intent.hasExtra("pollQuestion"))
            questionText.setText(intent.getStringExtra("pollQuestion"));

        Log.d("PollAcivity.onCreate", "PollID:" + pollID);

    }


    @Override
    public void onStart() {
        super.onStart();
        signInToFirebase();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.poll, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.poll_id_share) {
            Utils.buildDeepLink(this, pollID);
        }
        return super.onOptionsItemSelected(item);
    }


    public void launchShareIntent(String shareURL) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Take my poll in TallyUp.\n" + shareURL);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    public void setupRecyclerView() {
        adapter = new PollChoiceAdapter(pollItems, selectedItem, this);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        choicesRV.setLayoutManager(manager);
        choicesRV.setAdapter(adapter);
    }

    public void updateChoice(int prev, int curr) {
        mDatabase.child("Voters").child(mAuth.getCurrentUser().getUid()).setValue(curr);
       // mDatabase.child("Item" + (prev + 1)).child("Votes").
    }

    @Override
    public void gatherDataFromFirebase(Task<AuthResult> task) {
        //Login successful
        mDatabase = FirebaseDatabase.getInstance().getReference("Polls/" + pollID);

        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value(s) and again
                // whenever data at this location is updated.
                pollItems.clear();

                Map<String, Object> items = (Map<String, Object>) dataSnapshot.getValue();
                //Log.d("items", items.toString());
                for (Map.Entry<String, Object> item : items.entrySet()) {
                    if (item.getKey().startsWith("Item")) {
                        Map<String, Object> attributes = (Map<String, Object>) item.getValue();
                        PollItem pi = new PollItem(attributes.get("Name").toString(), 0);
                        pollItems.add(Character.getNumericValue(item.getKey().charAt(4)), pi);
                    }
                    if (item.getKey().equals("Question")) {
                        questionText.setText(item.getValue().toString());
                    }
                }
                pollItems.removeAll(Collections.singleton(null)); //get rid of null crap
                //Determine which choice is selected by user and count all the votes
                for (Map.Entry<String, Object> item : items.entrySet()) {
                    if (item.getKey().equals("Voters")) {
                        Map<String, Object> attributes = (Map<String, Object>) item.getValue();
                        for (Map.Entry<String, Object> vote : attributes.entrySet()) {
                            if (vote.getKey().equals(mAuth.getCurrentUser().getUid()))
                                selectedItem = Integer.parseInt(vote.getValue().toString());
                            pollItems.get(Integer.parseInt(vote.getValue().toString())).addVote();
                        }
                    }
                }
                setupRecyclerView();
            }

            // Data listener cancelled
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("data", "Failed to read values.", error.toException());
            }
        });

        //login failure
        if (!task.isSuccessful()) {
            Log.w("auth", "signInAnonymously", task.getException());
            Toast.makeText(PollActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
