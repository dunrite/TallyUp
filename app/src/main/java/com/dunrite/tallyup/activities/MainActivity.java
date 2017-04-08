package com.dunrite.tallyup.activities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dunrite.tallyup.Poll;
import com.dunrite.tallyup.R;
import com.dunrite.tallyup.RecyclerItemClickListener;
import com.dunrite.tallyup.adapters.UsersPollsAdapter;
import com.dunrite.tallyup.utility.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Main activity that shows active and closed polls for the user
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ArrayList<Poll> pollsList;
    private UsersPollsAdapter adapter;

    @BindView(R.id.fab_create) FloatingActionButton fabCreate;
    @BindView(R.id.usersPolls) RecyclerView usersPollsRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setRecentsStyle();

        mAuth = FirebaseAuth.getInstance();
        pollsList = new ArrayList<>();

        if(Utils.isFirstLaunch(this)) {
            Intent intent = new Intent(this, IntroActivity.class); //call Intro class
            startActivity(intent);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        signInToFirebase();
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

    public void setupRecyclerView() {
        adapter = new UsersPollsAdapter(pollsList, this);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        usersPollsRV.setLayoutManager(manager);
        usersPollsRV.setAdapter(adapter);
        usersPollsRV.addOnItemTouchListener(
                new RecyclerItemClickListener(this, usersPollsRV,
                        new RecyclerItemClickListener.OnItemClickListener(){
                            @Override
                            public void onItemClick(View view, int position){
                                configureIntent(adapter.getPositionInfo(position));
                            }
                            @Override
                            public void onItemLongClick(View view, int position) {

                            }

                        }));

    }

    public void configureIntent(Poll p) {
        Intent i = new Intent(this, PollActivity.class);
        i.putExtra("pollID", p.getId());
        i.putExtra("pollQuestion", p.getQuestion());
        startActivity(i);
    }

    public boolean userIsInPoll(Map<String, Object> poll) {
        //for ( poll.get("Voters").get())
        return mAuth.getCurrentUser().getUid().equals(poll.get("OwnerID"));
    }
    /**
     * Connect to the Firebase database
     */
    public void signInToFirebase() {
        if(Utils.isOnline(getApplicationContext())) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //Login successful
                            mDatabase = FirebaseDatabase.getInstance().getReference("Polls");

                            // Read from the database
                            mDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // This method is called once with the initial value(s) and again
                                    // whenever data at this location is updated.
                                    pollsList.clear();

                                    Map<String, Object> polls = (Map<String, Object>) dataSnapshot.getValue();
                                    //Log.d("polls", polls.toString());
                                    for (Map.Entry<String, Object> poll : polls.entrySet()) {
                                        Map<String, Object> attributes = (Map<String, Object>) poll.getValue();
                                        if(userIsInPoll(attributes)) {
                                            //Poll (String q, String t, Boolean m, Map<String, Integer> o) {
                                            pollsList.add(new Poll(poll.getKey(), attributes.get("Question").toString()));

                                        }
                                    }
                                    setupRecyclerView();
                                }

                                // Data listener cancelled
                                @Override
                                public void onCancelled(DatabaseError error) {
                                    // Failed to read value
                                    Log.w("data", "Failed to read value.", error.toException());
                                }
                            });

                            //login failure
                            if (!task.isSuccessful()) {
                                Log.w("auth", "signInAnonymously", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Snackbar.make(findViewById(R.id.activity_main), "No Internet Connection", Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signInToFirebase();
                }
            }).show();
        }
    }
}
