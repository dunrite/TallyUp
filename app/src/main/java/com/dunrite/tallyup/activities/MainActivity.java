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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dunrite.tallyup.ArrayListAnySize;
import com.dunrite.tallyup.Poll;
import com.dunrite.tallyup.PollItem;
import com.dunrite.tallyup.R;
import com.dunrite.tallyup.RecyclerItemClickListener;
import com.dunrite.tallyup.adapters.UsersPollsAdapter;
import com.dunrite.tallyup.utility.Utils;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
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
import java.util.Collections;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Main activity that shows active and closed polls for the user
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ArrayList<Poll> pollsList;
    private UsersPollsAdapter adapter;
    private GoogleApiClient mGoogleApiClient;

    @BindView(R.id.fab_create) FloatingActionButton fabCreate;
    @BindView(R.id.usersPolls) RecyclerView usersPollsRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setRecentsStyle();

        // Build GoogleApiClient with AppInvite API for receiving deep links
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        mAuth = FirebaseAuth.getInstance();
        pollsList = new ArrayList<>();

        if(Utils.isFirstLaunch(this)) {
            Intent intent = new Intent(this, IntroActivity.class); //call Intro class
            startActivity(intent);
        }

        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found.
        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    // Extract deep link from Intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    Log.d("TallyUp.MainActivity", "Deep Link: " + deepLink);
                                    // Handle the deep link. For example, open the linked
                                    // content, or apply promotional credit to the user's
                                    // account.
                                    String id = deepLink.substring(deepLink.lastIndexOf("/"));
                                    Poll p = new Poll(id, "test");
                                    configureIntent(p);
                                } else {
                                    Log.d("TallyUp.PollActivity", "getInvitation: no deep link found.");
                                }
                            }
                        });

    }

    @Override
    public void onStart() {
        super.onStart();
        signInToFirebase();
    }

    /**
     * Inflate the overflow menu in the actionbar
     * @param menu the menu
     * @return inflated
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handle the overflow menu in the actionbar
     * @param item the selected item
     * @return super call
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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

    /**
     * Checks if user is in the poll or not
     * @param attributes contents of poll
     * @return is in poll
     */
    public boolean userIsInPoll(Map<String, Object> attributes) {
        String uid = mAuth.getCurrentUser().getUid();
        //check if user is owner
        if (uid.equals(attributes.get("OwnerID"))) {
            return true;
        }
        //Check if user is a voter, but not owner
        for (Map.Entry<String, Object> things : attributes.entrySet()) {
            if (things.getKey().equals("Voters")) {
                Map<String, Object> thing = (Map<String, Object>) things.getValue();
                if (thing.containsKey(uid))
                    return true;
            }
        }
        return false;
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

                                    for (Map.Entry<String, Object> poll : polls.entrySet()) {
                                        Map<String, Object> attributes = (Map<String, Object>) poll.getValue();
                                        // attributes = ExpireTime, Item0, Item1 etc
                                        if(userIsInPoll(attributes)) {
                                            ArrayListAnySize<PollItem> pollItems = new ArrayListAnySize<>();
                                            //Get the PollItems to later put in the Poll
                                            for (Map.Entry<String, Object> item : attributes.entrySet()) {
                                                if (item.getKey().startsWith("Item")) {
                                                    Map<String, Object> attr = (Map<String, Object>) item.getValue();
                                                    PollItem pi = new PollItem(attr.get("Name").toString(), 0);
                                                    pollItems.add(Character.getNumericValue(item.getKey().charAt(4)), pi);
                                                }
                                            }
                                            pollItems.removeAll(Collections.singleton(null)); //get rid of null crap
                                            //Tally votes for each PollItem
                                            for (Map.Entry<String, Object> item : attributes.entrySet()) {
                                                if (item.getKey().equals("Voters")) {
                                                    Map<String, Object> attr = (Map<String, Object>) item.getValue();
                                                    for (Map.Entry<String, Object> vote : attr.entrySet()) {
                                                        pollItems.get(Integer.parseInt(vote.getValue().toString())).addVote();
                                                    }
                                                }
                                            }
                                            pollsList.add(new Poll(poll.getKey(), attributes.get("Question").toString(), "type", false, pollItems));
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
