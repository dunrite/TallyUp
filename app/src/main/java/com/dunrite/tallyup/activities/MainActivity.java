package com.dunrite.tallyup.activities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dunrite.tallyup.R;
import com.dunrite.tallyup.RecyclerItemClickListener;
import com.dunrite.tallyup.adapters.UsersPollsAdapter;
import com.dunrite.tallyup.pojo.ArrayListAnySize;
import com.dunrite.tallyup.pojo.Poll;
import com.dunrite.tallyup.pojo.PollItem;
import com.dunrite.tallyup.utility.Utils;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static com.dunrite.tallyup.R.menu.main;

/**
 * Main activity that shows active and closed polls for the user
 */
public class MainActivity extends FirebaseActivity {
    private ArrayList<Poll> pollsList;
    private UsersPollsAdapter adapter;

    @BindView(R.id.fab_create)
    FloatingActionButton fabCreate;
    @BindView(R.id.usersPolls)
    RecyclerView usersPollsRV;
    @BindView(R.id.activity_main)
    CoordinatorLayout mainView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.your_poll)
    TextView yourPolls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setRecentsStyle();
        pollsList = new ArrayList<>();
        checkForDeepLink();

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    if (user.getEmail() != null) {
                        Snackbar.make(mainView,
                                "Logged in as " + user.getEmail(), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(mainView,
                                "Logged in as " + user.getUid().substring(0,5), Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        // [END auth_state_listener]
        Log.d("MainActivity.onCreate", "isComingFromIntroSignIn: " + Utils.isComingFromIntroSignIn(this));
        if (Utils.isFirstLaunch(this)) {
            Intent intent = new Intent(this, IntroActivity.class); //call Intro class
            startActivityForResult(intent, RC_SIGN_IN);
        } else if (!Utils.isComingFromIntroSignIn(this)){
            signInToFirebase();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.action_sign_in).setVisible(true);
        menu.findItem(R.id.action_sign_out).setVisible(false);
        try {
            if(mAuth.getCurrentUser().getEmail()!= null) {
                menu.findItem(R.id.action_sign_out).setVisible(true);
                menu.findItem(R.id.action_sign_in).setVisible(false);
            }
        } catch(Exception e){
            Log.d(TAG, "null object refernce because we are already signed out");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Inflate the overflow menu in the actionbar
     *
     * @param menu the menu
     * @return inflated
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(main, menu);
        return true;
    }

    /**
     * Handle the overflow menu in the actionbar
     *
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
        } else if (id == R.id.action_sign_in) {
            launchGoogleSignInIntent();
            invalidateOptionsMenu();
        } else if (id == R.id.action_sign_out) {
            FirebaseAuth.getInstance().signOut();
            invalidateOptionsMenu();
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

    /**
     * Check if app was launched from a deep link and launch
     */
    public void checkForDeepLink() {
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

    /**
     * Set up the RecyclerView
     */
    public void setupRecyclerView() {
        adapter = new UsersPollsAdapter(pollsList, this);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        usersPollsRV.setLayoutManager(manager);
        usersPollsRV.setAdapter(adapter);
        usersPollsRV.addOnItemTouchListener(
                new RecyclerItemClickListener(this, usersPollsRV,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                view.findViewById(R.id.end_button).setVisibility(View.GONE);
                                view.findViewById(R.id.leave_button).setVisibility(View.GONE);
                                configureIntent(adapter.getPositionInfo(position));
                            }

                            @Override
                            public void onItemLongClick(View view, int position) {
                                if (adapter.getPositionInfo(position).getOwnerID().equals(mAuth.getCurrentUser().getUid()))
                                    view.findViewById(R.id.end_button).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.leave_button).setVisibility(View.VISIBLE);
                            }

                        }));
        progressBar.setVisibility(View.INVISIBLE);
        yourPolls.setVisibility(View.VISIBLE);


    }

    public void configureIntent(Poll p) {
        Intent i = new Intent(this, PollActivity.class);
        i.putExtra("pollID", p.getId());
        i.putExtra("pollQuestion", p.getQuestion());
        startActivity(i);
    }

    /**
     * Checks if user is in the poll or not
     *
     * @param attributes contents of poll
     * @return is in poll
     */
    public boolean userIsInPoll(Map<String, Object> attributes) {
        String uid = mAuth.getCurrentUser().getUid();
        if (!uid.equals("")) {
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
        }
        return false;
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void launchGoogleSignInIntent() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                firebaseAuthGoogle(account);
                new MaterialTapTargetPrompt.Builder(MainActivity.this)
                        .setTarget(findViewById(R.id.fab_create))
                        .setPrimaryText("Create your first poll!")
                        .setSecondaryText("Tap the pencil to create your first poll")
                        .setBackgroundColourFromRes(R.color.colorPrimaryDark)
                        .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
                        {
                            @Override
                            public void onHidePrompt(MotionEvent event, boolean tappedTarget)
                            {
                                //Do something such as storing a value so that this prompt is never shown again
                            }

                            @Override
                            public void onHidePromptComplete()
                            {

                            }
                        })
                        .show();
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    @Override
    public void gatherDataFromFirebase(Task<AuthResult> task) {
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
                    if (userIsInPoll(attributes)) {
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
                        pollsList.add(new Poll(poll.getKey(),
                                attributes.get("Question").toString(),
                                attributes.get("Type").toString(),
                                attributes.get("ExpireTime").toString(),
                                attributes.get("OwnerID").toString(), pollItems));
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
            Toast.makeText(MainActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
        Utils.cameFromIntroSignIn(this, false);
    }


}
