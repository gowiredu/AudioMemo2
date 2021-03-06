package com.gowiredu.audiomemotest2;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gowiredu.audiomemo.R;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;


import android.os.Handler;
import android.widget.ViewSwitcher;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.R.attr.id;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private ArrayList<String> lines = new ArrayList<>(); // temporarily store names of files in directory
    public FloatingActionButton roundButton;
    private long tempFileName; // temporary file name
    private StringBuilder tempString; // temporary string to store name of touched file (to look up for playback or deletion).
    public String tempTextfield;
    public StringBuilder tempFileNameOnline;
    public ArrayList<String> audioListArrayList = new ArrayList<>();
    public ArrayList<String> audioListArrayListUploaded = new ArrayList<>();


    // speech to text
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String transcriptionBuilder;
    private String LOG_TAG = "VoiceRecActivity";
    private final int SPEECH_RECOGNITION_CODE = 1;

    // RECYCLERVIEW STUFF
    private RecyclerView mRecyclerView;
    //private CardView mCardView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public int touchedPosition;
    public static LayoutInflater inflater;

    // expand / collapse stuff
    boolean isExpanded = false;
    public TextView memoTitle;
    public FrameLayout yellowCard;
    public TextView full_textView;
    public TextView preview_textView;
    public ImageButton playButton;
    public ImageButton uploadButton;
    public ImageButton deleteButton;
    public ImageButton renameButton;
    public ImageView dropDown_icon;
    public ImageView collapse_icon;
    public SeekBar memoSeekBar;
    public TextView location_textView;
    private long currentAudioLength;


    public String theTouchedRecording;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private View view; // for MainActivity (not memo card)

    // Navigation Drawer stuff
    private String[] navList = {"Device", "Uploaded"};

    public NavigationView navigationView;
    public static int navCurrentSelected;
    private View navView;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    public Menu m;
    public int categoryCount;


    //FIREBASE STUFF
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    public ProgressDialog mProgress;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "AUTHLISTENER";


    //private Button start, stop, format;
    //private MediaRecorder recorder = null;
    final private int opformats[] = {MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP};

    final private String fileExtension[] = {".mp3", ".3gpp"};
    private int curFormat = 0;
    private int myVariable = 0;

    public static final String MyPREFERENCES = "MyPrefs";

    public static final String nameKey = "nameKey";
    public static final String countKey = "phoneKey";
    public SharedPreferences sharedpreferences;
    public static StringBuilder previewTextToPass;



    public MenuItem myMoveGroupItem;
    public SubMenu subMenu;
    public String categoryName;

    public int changeRoundButtonColorFlag = 0;


    // for editText (editing a saved Transcript) and memoTitle_editText
    public ViewSwitcher switcher;
    public String editTextString; // temporary store for what is typed into the editText. Will be appended to the textfile when the user hits "Enter"
    public String previousTextStorage; // store what was in the textfile before the user started editing. Will be restored if user touches the back button
    // ...or anywhere else on the screen along with a Toast that says "Edit Cancelled"
    public int onBackPressedCheck; // for knowing the difference between an app exit and an "Edit" exit (cancels the edit) when the back button is pressed.
    //... alternates between 0 and 1.




    // Location stuff
    AppLocationService appLocationService;
    String theLocationAddress;

    LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        tempString = new StringBuilder();
        tempFileNameOnline = new StringBuilder();
        previewTextToPass = new StringBuilder();

        switcher = (ViewSwitcher) findViewById(R.id.view_switcher); // to switch view to the editText for editing a memo transcription.

        view = findViewById(R.id.content); // needed for permissionsGranted() to create Snackbar.

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        appLocationService = new AppLocationService(
                MainActivity.this);


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);



        // FIREBASE STUFF STARTS
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

        mStorage = FirebaseStorage.getInstance().getReference();
        // FIREBASE STUFF ENDS.





        // NAVIGATION DRAWER STUFF STARTS
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navCurrentSelected = 0;


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        changeNavHeaderImage(); // set navigation drawer header.


        //View test = (View) findViewById(R.id.header_image);
        //test.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nav_header_cloud));
        //linearLayout = (LinearLayout) view.findViewById(R.id.header_image);
        //linearLayout.setBackgroundResource(R.drawable.nav_header_cloud); //or whatever your image is






        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String restoredText = prefs.getString(nameKey, null);

        if (restoredText != null) {
            int restoredInt = prefs.getInt(countKey, 0);

            categoryCount = restoredInt;

            myMoveGroupItem = navigationView.getMenu().getItem(2);
            subMenu = myMoveGroupItem.getSubMenu();
            subMenu.add(2, categoryCount, Menu.NONE, restoredText);
        }

        //NAVIGATION DRAWER STUFF ENDS


        createFolderIfNoFolder(); // creates a folder for recordings if there is no folder yet (like the first time the app is launched after download).


        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);



        // START WITH "Personal" SELECTED AND LOAD THE CONTENTS of "Personal"
        if (navCurrentSelected == 0) {
            mAdapter = new MyRecyclerViewAdapter(getDataSet());
        }

        mRecyclerView.setAdapter(mAdapter);

        //navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setCheckedItem(R.id.nav_personal);




        // SHOW CONTENTS OF "Personal" ON THE USER'S SCREEN NOW.
        refreshRecyclerView();

        ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View view) {

                if (tempString != null)
                {
                    tempString.delete(0, tempString.length());
                }

                touchedPosition = position; // record current position


                // if current position is not equal to prev position and prev position.getHeight = 750
                // prev position.getHeight = 168. Set is expanded to false

                TextView memoTitle = (TextView) view.findViewById(R.id.memo_title);
                tempString.append(memoTitle.getText().toString());
                //Toast.makeText(MainActivity.this, tempString.toString(), Toast.LENGTH_LONG).show();

                expandCollapseLayout(view); // expands (or collapses0 the touched cardview


                // check if any other ones are expanded.
                // go through the entire cardview
                // if any of their sizes equal 750
                // reset every one but the one at the current touchedPosition to 168
                // expand the one at the touchedPosition


                String touchedRecording = memoTitle.getText().toString(); // save the title of the recording that was touched
                theTouchedRecording = touchedRecording;
                // **THIS** Toast.makeText(MainActivity.this, touchedRecording, Toast.LENGTH_LONG).show(); // display title of recording touched on the screen










                /*
                Log.d("Click Item", String.valueOf(position));

                //View vi = inflater.inflate(R.layout.recyclerview_item, null);
                TextView memoTitle = (TextView) view.findViewById(R.id.memo_title);
                //Toast.makeText(this, memoTitle.getText().toString(), Toast.LENGTH_LONG).show();
                Log.i("Test", memoTitle.getText().toString());

                String touchedRecording = memoTitle.getText().toString(); // save the title of the recording that was touched
                Toast.makeText(MainActivity.this, touchedRecording, Toast.LENGTH_LONG).show(); // display title of recording touched on the screen


                // get current position
                // look for file in direc


                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                intent.putExtra("memo_title", touchedRecording); // pass saved title of recording to next activity to use as header.
                startActivity(intent);
                */
            }
        });



        // FOR HIDING THE RECORD BUTTON WHEN THE USER IS SCROLLING.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && roundButton.isShown())
                    roundButton.hide();
            }

            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    roundButton.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });







        /*
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View vi = inflater.inflate(R.layout.recyclerview_item, null);

                card = (CardView) vi.findViewById(R.id.card_view);

                TextView memoTitle = (TextView) vi.findViewById(R.id.memo_title);

                yellowCard = (FrameLayout) vi.findViewById(R.id.mtf_card);
                full_textView = (TextView) vi.findViewById(R.id.transcription_full);
                playButton = (Button) vi.findViewById(R.id.play_button);
                stopButton = (Button) vi.findViewById(R.id.stop_button);

                if (!isExpanded) // if card has not been expanded
                {
                    yellowCard.setVisibility(View.VISIBLE); // expand the card
                    full_textView.setVisibility(View.VISIBLE); // expand the card
                    playButton.setVisibility(View.VISIBLE); // expand the card
                    stopButton.setVisibility(View.VISIBLE); // expand the card

                    isExpanded = true;
                }
                else
                {
                    // else, collapse the card
                    yellowCard.setVisibility(View.GONE);
                    full_textView.setVisibility(View.GONE);
                    playButton.setVisibility(View.GONE);
                    stopButton.setVisibility(View.GONE);
                    //pf.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    isExpanded = false;
                }


                String touchedRecording = memoTitle.getText().toString(); // save the title of the recording that was touched
                Toast.makeText(MainActivity.this, touchedRecording, Toast.LENGTH_LONG).show(); // display title of recording touched on the screen

            }
        });
        */


        // ***THIS LONGCLICK WORKS***
        /*
        ((MyRecyclerViewAdapter) mAdapter).setOnItemLongClickListener(new MyRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                TextView memoTitle = (TextView) view.findViewById(R.id.memo_title);
                Log.d("LONGPRESS", memoTitle.getText().toString()); // log stuff. Just to see if right file was IDd by app.
                touchedPosition = position;
                tempString = memoTitle.getText().toString();
                longPressDialogBox();

            }
        });
        */


        //RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        //mRecyclerView.addItemDecoration(itemDecoration);


        // Code to Add an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).deleteItem(index);




        /*
        start = (Button) findViewById(R.id.startbtn);
        stop = (Button) findViewById(R.id.stopbtn);
        format = (Button) findViewById(R.id.formatbtn);

        stop.setEnabled(false);
        format.setEnabled(true);
        */

        //speechToTextServiceStart();
        //mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        //RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        //mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);


        // *** RECYCLERVIEW STUFF START ***
        // when a card is touched


        // The full transcription
        // the yellow card itself
        // the play button
        // the stop button

        // record button
        roundButton = (FloatingActionButton) findViewById(R.id.fab_button);

        // animate record button on startup (pops up from nothingness)
        ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1, 50, 50); // 50, 50 is so it animates from the center outward.
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        anim.setFillEnabled(true);
        anim.setDuration(500);
        anim.setInterpolator(new OvershootInterpolator());
        roundButton.startAnimation(anim);







        // RECORD BUTTON
        roundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roundButtonClicked(v);

            }
        });







        memoTitle = (TextView) findViewById(R.id.memo_title);
        yellowCard = (FrameLayout) findViewById(R.id.mtf_card);
        full_textView = (TextView) findViewById(R.id.transcription_full);
        preview_textView = (TextView) findViewById(R.id.memo_preview);
        playButton = (ImageButton) findViewById(R.id.play_button);
        uploadButton = (ImageButton) findViewById(R.id.upload_button);
        deleteButton = (ImageButton) findViewById(R.id.delete_button);
        renameButton = (ImageButton) findViewById(R.id.rename_button);
        dropDown_icon = (ImageView) findViewById(R.id.dropdown_arrow);
        collapse_icon = (ImageView) findViewById(R.id.collapse_arrow);
        memoSeekBar = (SeekBar) findViewById(R.id.memo_seekBar);
        location_textView = (TextView) findViewById(R.id.location_textView);


        // Save default card size for resetting if needed
        //View vi = inflater.inflate(R.layout.recyclerview_item, null);
        //CardView layout = (CardView)vi.findViewById(R.id.card_view);
        //resetWidth = vi.getWidth();
        //resetHeight = vi.getHeight();


    }


                    /*
        ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(LOG_TAG, "Position" + position);

                touchedPosition = position;

                View vi = inflater.inflate(R.layout.recyclerview_item, null); //log.xml is your file.


                TextView memoTitle = (TextView)vi.findViewById(R.id.memo_title);
                String touchedRecording = memoTitle.getText().toString(); // save the title of the recording that was touched
                Toast.makeText(MainActivity.this, touchedRecording, Toast.LENGTH_LONG).show(); // display title of recording touched on the screen




                // get current position
                // look for file in direc


                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                intent.putExtra("memo_title", touchedRecording); // pass saved title of recording to next activity to use as header.
                startActivity(intent);

                // ADD ANIMATION LATER
            }
        });
        */


    //((MyRecyclerViewAdapter) mAdapter).deleteItem(position);
    //mAdapter.notifyDataSetChanged();

    // when a card is long-pressed
        /*
        ((MyRecyclerViewAdapter) mAdapter).setOnItemLongClickListener(new MyRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(final int position, View v) {

                Log.i(LOG_TAG, " Longclicked on Item " + position);

                longPressDialogBox();
            }
        });
        */

    // *** RECYCLERVIEW STUFF ENDS ***










    // FOR EXPANDING AND COLLAPSING THE MEMO CARD.
    public void expandCollapseLayout(View view) {
        //mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, mAdapter.getItemCount());

        memoTitle = (TextView) view.findViewById(R.id.memo_title);
        yellowCard = (FrameLayout) view.findViewById(R.id.mtf_card);
        full_textView = (TextView) view.findViewById(R.id.transcription_full);
        preview_textView = (TextView) view.findViewById(R.id.memo_preview);
        playButton = (ImageButton) view.findViewById(R.id.play_button);
        uploadButton = (ImageButton) view.findViewById(R.id.upload_button);
        deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
        renameButton = (ImageButton) view.findViewById(R.id.rename_button);

        dropDown_icon = (ImageView) view.findViewById(R.id.dropdown_arrow);
        collapse_icon = (ImageView) view.findViewById(R.id.collapse_arrow);
        memoSeekBar = (SeekBar) view.findViewById(R.id.memo_seekBar);

        switcher = (ViewSwitcher) view.findViewById(R.id.view_switcher);
        //switcher2 = (ViewSwitcher) view.findViewById(R.id.memoTitle_ViewSwitcher);
        location_textView = (TextView) view.findViewById(R.id.location_textView);





        // for resizing the card when its touched.
        CardView layout = (CardView) view.findViewById(R.id.card_view);

        // Gets the layout params that will allow you to resize the layout
        ViewGroup.LayoutParams params = layout.getLayoutParams();

        // Changes the height and width to the specified *pixels*
        params.height = layout.getHeight();
        params.width = layout.getWidth();


        //card = (CardView) view.findViewById(R.id.card_view);

        if (!isExpanded) // if card has been expanded
        {
            params.height = 750;
            params.width = layout.getWidth();
            layout.setLayoutParams(params);


            yellowCard.setVisibility(View.VISIBLE);
            full_textView.setVisibility(View.VISIBLE);
            preview_textView.setVisibility(View.INVISIBLE);
            playButton.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            renameButton.setVisibility(View.VISIBLE);

            collapse_icon.setVisibility(View.INVISIBLE);
            dropDown_icon.setVisibility(View.INVISIBLE);
            memoSeekBar.setVisibility(View.VISIBLE);

            switcher.setVisibility(View.VISIBLE);
            location_textView.setVisibility(View.VISIBLE);


            // onClickListener for when yellow card is touched. So it will turn into editable text for user.
            yellowCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextViewClicked(view);
                }
            });



            // if the user is currently in the "Uploaded" section...
            if (navCurrentSelected == 2) {
                // ...change the upload button to a download button when the card expands.
                uploadButton.setImageResource(R.drawable.ic_download_icon_menu);
            }


            renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    renameButtonTouched();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (navCurrentSelected == 2)
                    {
                        deleteAudioFileUploadedDialog();
                    }
                    else
                    {
                        deleteAudioDialog();
                    }
                }
            });

            inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // if the user is in the "Devices" section,  set play button to appropriate local file.
                    if (navCurrentSelected == 0) {
                        playAudioFile();
                    }

                    // else if the user is in the "Uploaded" section, set play button to memo saved in the cloud
                    else if (navCurrentSelected == 1) {
                        playAudioFile();
                    }

                    else if (navCurrentSelected == 2) {
                        playAudioFileUploaded();
                    }

                    else if (navCurrentSelected == 3) {
                        playAudioFile();
                    }
                }
            });


            // onClickListener for Upload button (download button, if the user is in the "Uploaded" section).
            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (navCurrentSelected != 2) {
                        uploadAudio();
                    } else {
                        downloadAudioDialog();
                    }
                }
            });



            isExpanded = true;

            populateTextBox(view);


        }



        else
        {
            params.height = 168;
            params.width = layout.getWidth();
            layout.setLayoutParams(params);

            // else, collapse the card
            yellowCard.setVisibility(View.GONE);
            full_textView.setVisibility(View.GONE);
            preview_textView.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
            uploadButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            renameButton.setVisibility(View.GONE);

            collapse_icon.setVisibility(View.INVISIBLE);
            dropDown_icon.setVisibility(View.VISIBLE);
            memoSeekBar.setVisibility(View.GONE);

            switcher.setVisibility(View.GONE);
            location_textView.setVisibility(View.GONE);


            isExpanded = false;
        }
    }










    // CREATE A FOLDER WHEN THE APPLICATION IS RUN, IF THERE IS NO FOLDER OR IF THIS IS THE FIRST TIME THE USER IS USING THE APP.
    private void createFolderIfNoFolder() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d("SDCARD: ", "No SDCARD");
        } else {
            File folderFirstTimePersonalAudio = new File(Environment.getExternalStorageDirectory() + File.separator + "PersonalAudio");
            File folderFirstTimePersonalText = new File(Environment.getExternalStorageDirectory() + File.separator + "PersonalText");
            File folderFirstTimePersonalLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalLocations");



            File folderFirstTimeWorkAudio = new File(Environment.getExternalStorageDirectory() + File.separator + "WorkAudio");
            File folderFirstTimeWorkText = new File(Environment.getExternalStorageDirectory() + File.separator + "WorkText");
            File folderFirstTimeWorkLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkLocations");


            File folderUploadedFirstTime = new File(Environment.getExternalStorageDirectory() + File.separator + "MemosUploaded");
            File folderUploadedTextFileFirstTime = new File(Environment.getExternalStorageDirectory() + File.separator + "MemosUploadedURL");







            // CREATE APPROPRIATE FOLDERS IF THEY DON'T EXIST.
            if (!(folderFirstTimePersonalAudio.exists())) {
                folderFirstTimePersonalAudio.mkdirs();
                Log.d("PERSONAL_AUDIO", "PERSONAL AUDIO Folder Created");
                Log.d("Audio Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalAudio");

            }

            if (!(folderFirstTimePersonalText.exists())) {
                folderFirstTimePersonalText.mkdirs();
                Log.d("PERSONAL_TEXT", "PERSONAL TEXT Folder Created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText");
            }


            if (!(folderFirstTimePersonalLocation.exists())) {
                folderFirstTimePersonalLocation.mkdirs();
                Log.d("PERSONAL_LOCATION", "PERSONAL LOCATION Folder Created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalLocations");
            }








            if (!(folderFirstTimeWorkAudio.exists())) {
                folderFirstTimeWorkAudio.mkdirs();
                Log.d("WORK_AUDIO", "WORK AUDIO Folder Created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkAudio");
            }


            if (!(folderFirstTimeWorkText.exists())) {
                folderFirstTimeWorkText.mkdirs();
                Log.d("WORK_TEXT", "WORK TEXT Folder created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory() + File.separator + "WorkText");
            }

            if (!(folderFirstTimeWorkLocation.exists())) {
                folderFirstTimeWorkLocation.mkdirs();
                Log.d("WORK_LOCATION", "WORK LOCATION Folder created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkLocations");
            }







            if (!(folderUploadedFirstTime.exists())) {
                folderUploadedFirstTime.mkdirs();
                Log.d("UPLOADED_FOLDER", "UPLOADED FOLDER Created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MemosUploaded");
            }


            if (!(folderUploadedTextFileFirstTime.exists())) {
                folderUploadedTextFileFirstTime.mkdirs();
                Log.d("UPLOADED_TEXTFILE", "UPLOADED TEXTFILE FOLDER created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory() + File.separator + "MemosUploadedURL");
            }
        }
    }








    // CALLED WHEN THE USER TOUCHES THE RECORD BUTTON.
    private void startRecording() {
        /**
         * Start speech to text intent. This opens up Google Speech Recognition API dialog box to listen the speech input.
         * */

        Runnable gpsRunnable = new Runnable() {
            @Override
            public void run() {
                // try to get the user's current location (long., lat. for use later to convert to an address).
                try {
                    AppLocationService gps = new AppLocationService(MainActivity.this);

                    // check if gps enabled. Boolean.
                    if (gps.canGetLocation()) {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();

                        // GET THE ADDRESS OF THE USER'S CURRENT LOCATION. IF NO ADDRESS, LAT and LONG WILL BE STORED INSTEAD.
                        LocationAddress locationAddress = new LocationAddress();
                        locationAddress.getAddressFromLocation(latitude, longitude, MainActivity.this, new GeocoderHandler());
                    } else {
                        // can't get location
                        // GPS or Network is not enabled
                        // Ask user to enable GPS/network in settings
                        gps.showSettingsAlert();
                    }

                } catch (Exception e) {
                    Log.i("LOCATION_RECORDING", "Unable to get current location");
                    e.printStackTrace();

                }
            }
        };
        Thread gpsThread = new Thread(gpsRunnable);
        gpsThread.run();




        Runnable r = new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        "Speak now...");

                intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
                intent.putExtra("android.speech.extra.GET_AUDIO", true);


                try {
                    startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry! Speech recognition is not supported in this device.",
                            Toast.LENGTH_SHORT).show();
                }


            }
        };
        Thread speechToTextThread = new Thread(r);
        speechToTextThread.run();

    }







    /**
     * Callback for speech recognition activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        permissionsGranted(view);

        switch (requestCode) {
            case SPEECH_RECOGNITION_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.i("RESULTS_TEXT", result_text.get(0));

                    previewTextToPass.delete(0, previewTextToPass.length());
                    previewTextToPass.append(result_text.get(0));

                    // directory for audio file.
                    String directory_audioFile = null;

                    // directory for transcription text file.
                    String directory_textFile = null;

                    // directory for storing location of memos.
                    String directory_location = null;


                    // PICK A DIRECTORY BASED ON THE CURRENT CATEGORY THE USER IS IN.
                    if (navCurrentSelected == 0) {
                        directory_audioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalAudio";
                        directory_textFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText";
                        directory_location = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalLocations";

                    }

                    else if (navCurrentSelected == 1) {
                        directory_audioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkAudio";
                        directory_textFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText";
                        directory_location = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkLocations";
                    }

                    else if (navCurrentSelected == 3) {
                        directory_audioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Audio";
                        directory_textFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text";
                        directory_location = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Locations";
                    }

                    // if user is in "Uploaded" section.
                    else if (navCurrentSelected == 2)
                    {
                        directory_audioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TempAudio";
                        directory_textFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TempText";
                        directory_location = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TempLocations";

                        File tempAudioFolder = new File(directory_audioFile);
                        File tempTextFolder = new File(directory_textFile);
                        File tempLocationsFolder = new File(directory_location);

                        if (!(tempAudioFolder.exists()))
                        {
                            tempAudioFolder.mkdirs();
                            Log.i("TEMP_AUDIO_FOLDER", "TempAudio created");
                        }

                        if (!(tempTextFolder.exists()))
                        {
                            tempTextFolder.mkdirs();
                            Log.i("TEMP_TEXT_FOLDER", "TempText created");
                        }

                        if (!(tempLocationsFolder.exists()))
                        {
                            tempLocationsFolder.mkdirs();
                            Log.i("TEMP_LOCATIONS_FOLDER", "TempLocations created");
                        }

                    }


                    // GET AUDIO FROM SPEECH-TO-TEXT SERVICES AND SAVE IN APPROPRIATE LOCATION.
                    try {

                        Uri audioUri = data.getData();
                        ContentResolver contentResolver = getContentResolver();

                        String audioFileName = Long.toString(tempFileName) + fileExtension[curFormat];

                        if (navCurrentSelected == 2)
                        {
                            tempFileNameOnline.delete(0, tempFileNameOnline.length());
                            tempFileNameOnline.append(audioFileName); // (for recording straight to the database)
                        }

                        File file = new File(directory_audioFile, audioFileName);
                        InputStream filestream = contentResolver.openInputStream(audioUri);
                        OutputStream out = new FileOutputStream(file);


                        try {

                            byte[] buffer = new byte[4 * 1024]; // or other buffer size
                            int read;

                            while ((read = filestream.read(buffer)) != -1) {
                                out.write(buffer, 0, read);
                            }
                            out.flush();
                            out.close();

                        } finally {
                            out.close();
                            filestream.close();
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    // SAVE TRANSCRIPTION TO TEXT FILES AND SAVE IN APPROPRIATE FOLDER

                    String textFileName = Long.toString(tempFileName) + fileExtension[curFormat] + ".txt";
                    File outputFile = new File(directory_textFile, textFileName);



                    try {
                        FileOutputStream stream = new FileOutputStream(outputFile);
                        stream.write(result_text.get(0).getBytes());
                        stream.close();


                        // convert a "Long" to a "String" (current milliseconds to a String, for giving files temporary names)
                        long number = tempFileName;
                        String numberAsString = Long.toString(number);


                        // add audio to list of audio files on the screen along with the selected audio format extension.

                        if (navCurrentSelected != 2)
                        {
                            audioListArrayList.add(0, numberAsString + fileExtension[curFormat]);
                        }

                        else
                        {
                            audioListArrayListUploaded.add(0, numberAsString + fileExtension[curFormat]);
                        }


                        // refresh the screen and lists the new file
                        refreshRecyclerView();
                        mAdapter.notifyItemRangeChanged(touchedPosition, audioListArrayList.size());


                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                    String memoLocationFile = Long.toString(tempFileName) + fileExtension[curFormat] + ".txt";
                    File outputFileLocation = new File(directory_location, memoLocationFile);


                    // GET THE USER'S CURRENT LOCATION AND SAVE IT TO A TEXT FILE.
                    try {

                        FileOutputStream fOut = new FileOutputStream(outputFileLocation); // outputFileLocation is random name + ".txt" saved in a "Locations" folder.
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        myOutWriter.append(theLocationAddress); // write the location info to the file.
                        myOutWriter.close();
                        fOut.close();



                        Log.i("SAVED_LOCATION", theLocationAddress);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("APPEND_TO_TEXTFILE", "File write failed: " + e.toString());
                    }


                    if (navCurrentSelected == 2)
                    {
                        try {
                            uploadAudioYesSelected();


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
                break;
            }
        }
    }








    // refreshes the view (reflect changes on the screen)
    private void refreshRecyclerView() {
        mAdapter.notifyDataSetChanged();


    }











    // dialog box with TextField to take new name of file.
    private void renameRecordingDialogTextField() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle(tempString.toString());

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Enter New Title Here");

        input.setSingleLine(true);
        input.setMaxLines(1);
        input.setLines(1);


        builder.setView(input);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); // show keyboard

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tempTextfield = input.getText().toString().trim();
                renameRecording();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); // hide keyboard
                dialog.cancel();
            }
        });



        final android.app.AlertDialog dialogReportBox = builder.create();
        dialogReportBox.show();
        dialogReportBox.getButton(dialogReportBox.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialogReportBox.getButton(dialogReportBox.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    // check if given file name by user is empty. If it is, prevent the user from saving the file and ask them to enter a name.
                    if (input.getText().toString().trim().length() == 0)
                    {
                        Toast.makeText(MainActivity.this, "Please enter a new name", Toast.LENGTH_LONG).show();
                    }

                    else
                    {
                        dialogReportBox.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    }

                    return true;
                }
                return false;
            }
        });
    }









    // searches for the file to be renamed in the folder and renames it
    private void renameRecording() {
        // save name of file in temporary string (taken care of with global variable "tempString"
        // go through the folder looking for a file that matches temporary string

        Log.i("tempTextfield", tempTextfield);

        String renameRecordingFilePath = null;
        String renameTextFilePath = null;
        String renameLocationTextFilePath = null;


        if (navCurrentSelected == 0)
        {
            renameRecordingFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalAudio";
            renameTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText";
            renameLocationTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalLocations";

        }

        else if (navCurrentSelected == 1)
        {
            renameRecordingFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkAudio";
            renameTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText";
            renameLocationTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkLocations";

        }

        else if (navCurrentSelected == 3)
        {
            renameRecordingFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Audio";
            renameTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text";
            renameLocationTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Locations";

        }


        Log.d("AUDIOFILES", "Current Audio Path: " + renameRecordingFilePath);
        Log.d("TEXTFILES", "Current Text Path: " + renameTextFilePath);
        Log.d("LOCATIONFILES", "Current LocationInfo Path: " + renameLocationTextFilePath);


        File f1 = new File(renameRecordingFilePath);
        File audio_files[] = f1.listFiles();
        Log.d("AudioFiles", "Size: " + audio_files.length); // Log for current number of audio files in the folder
        File rename_audio = new File(renameRecordingFilePath, tempTextfield + fileExtension[curFormat]);


        File f2 = new File(renameTextFilePath);
        File text_files[] = f2.listFiles();
        Log.d("TextFiles", "Size: " + text_files.length); // Log for current number of text files in the folder
        File rename_textFile = new File(renameTextFilePath, tempTextfield + fileExtension[curFormat] + ".txt");


        File f3 = new File(renameLocationTextFilePath);
        File location_files[] = f3.listFiles();
        Log.d("LocationTextFiles", "Size: " + text_files.length); // Log for current number of location text files in the folder
        File rename_locationTextFile = new File(renameLocationTextFilePath, tempTextfield + fileExtension[curFormat] + ".txt");


        // go through list of audiofiles, find and rename the right one.
        for (int i = 0; i < audio_files.length; i++)
        {
            if (audio_files[i].getName().equals(tempString.toString()))
            {

                //renameTo goes here.
                boolean renamed = audio_files[i].renameTo(rename_audio);


            }
        }







        // go through list of location text files and rename the appropriate one.
        for (int i = 0; i < location_files.length; i++)
        {
            if (location_files[i].getName().equals(tempString.toString() + ".txt"))
            {

                //renameTo goes here.
                boolean renamed = location_files[i].renameTo(rename_locationTextFile);



            }
        }






        // go through list of textfiles, find and rename the right one.
        for (int i = 0; i < text_files.length; i++)
        {
            if (text_files[i].getName().equals(tempString.toString() + ".txt"))
            {
                //renameTo goes here.
                boolean renamed = text_files[i].renameTo(rename_textFile);

                // remap path to textfile and reset textbox.
                inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View vi = inflater.inflate(R.layout.recyclerview_item, null); //recyclerview_item.xml is your file.

                // get file name (memoTitle)


                refreshTextBoxAfterRename(vi);
                Log.i("TEMPTEXTFIELD2", tempTextfield);

                audioListArrayList.set(i, tempTextfield + ".mp3");
                mRecyclerView.invalidate();
                mAdapter.notifyItemChanged(touchedPosition);

                refreshRecyclerView();


            }
        }


    }









    // dialog box asking "Would you like to delete this memo?"
    private void deleteAudioDialog()
    {

        android.app.AlertDialog.Builder sendReportBox = new android.app.AlertDialog.Builder(MainActivity.this);
        sendReportBox.setTitle("Delete Memo");
        sendReportBox.setMessage("Are you sure you want to delete this memo?");
        sendReportBox.setIcon(android.R.drawable.ic_dialog_alert);
        sendReportBox.setCancelable(true);


        sendReportBox.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAudioFile();
                        dialog.cancel();
                    }
                });

        sendReportBox.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog dialogReportBox = sendReportBox.create();
        dialogReportBox.show();
        dialogReportBox.getButton(dialogReportBox.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialogReportBox.getButton(dialogReportBox.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

    }






    // called if "Yes" is selected after "deleteAudioDialog"
    private void deleteAudioFile()
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run() {

                String deleteRecordingFilePath = null;
                String deleteTextFilePath = null;
                String deleteLocationInfoPath = null;


                if (navCurrentSelected == 0) {
                    deleteRecordingFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalAudio";
                    deleteTextFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalText";
                    deleteLocationInfoPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalLocations";

                }

                else if (navCurrentSelected == 1) {
                    deleteRecordingFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkAudio";
                    deleteTextFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkText";
                    deleteLocationInfoPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkLocations";

                }


                else if (navCurrentSelected == 2)
                {
                    deleteRecordingFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "TempAudio";
                    deleteTextFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "TempText";
                    deleteLocationInfoPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "TempLocations";
                }

                else {
                    deleteRecordingFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Audio";
                    deleteTextFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Text";
                    deleteLocationInfoPath = Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Locations";

                }


                Log.d("Audio Files", "Path: " + deleteRecordingFilePath);
                Log.d("Transcript Files", "Path: " + deleteTextFilePath);
                Log.d("Location Files", "Path: " + deleteLocationInfoPath);


                File f1 = new File(deleteRecordingFilePath);
                File audio_files[] = f1.listFiles();


                Log.d("Files", "Size: " + audio_files.length); // current number of audio files in the folder


                File f2 = new File(deleteTextFilePath);
                File text_files[] = f2.listFiles();


                Log.d("Files", "Size: " + text_files.length); // current number of text files in the folder

                File f3 = new File(deleteLocationInfoPath);
                File location_files[] = f3.listFiles();


                //Log.d("Files", "Size: " + location_files.length); // current number of text files in the folder


                // find and delete appropriate audio file.


                // if user is anywhere else but the upload section, delete files in the folder (after uploading)
                if (navCurrentSelected != 2)
                {
                    for (int i = 0; i < audio_files.length; i++) {
                        if (audio_files[i].getName().equals(tempString.toString())) {
                            boolean deleted = audio_files[i].delete();

                        }
                    }


                    // find and delete appropriate transcription text file.
                    for (int i = 0; i < text_files.length; i++) {
                        if (text_files[i].getName().equals(tempString.toString() + ".txt")) {
                            boolean deleted = text_files[i].delete();

                        }
                    }


                    // find and delete appropriate location text file then refresh the view.
                    for (int i = 0; i < location_files.length; i++) {
                        if (location_files[i].getName().equals(tempString.toString() + ".txt")) {
                            boolean deleted = location_files[i].delete();

                            //lines.remove(i);
                            ((MyRecyclerViewAdapter) mAdapter).deleteItem(touchedPosition);
                            //mRecyclerView.removeViewAt(position);
                            mAdapter.notifyItemRemoved(touchedPosition);
                            //mAdapter.notifyItemChanged(touchedPosition);
                            mAdapter.notifyItemRangeChanged(touchedPosition, audioListArrayList.size());
                            refreshRecyclerView();

                        }
                    }
                }

                // else, if user recorded from the "Uploaded" section, delete the whole folder after upload.
                else
                {
                    f1.delete();
                    f2.delete();
                    f3.delete();
                }
            }
        };

        Thread deleteAudioFileThread = new Thread(r);
        deleteAudioFileThread.run();
    }








    public void onPause() {
        super.onPause();
        Log.i("PAUSE", "onPause() called");
        //stopRecording();
    }








    // disconnect from online memo service if user closes the app.
    public void onStop() {
        super.onStop();
        Log.i("STOP", "onStop() called");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }








    // play uploaded audio file
    private void playAudioFileUploaded() {

        final Uri[] myUri = {null};

        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {


                    String folderUploadedMemoAudioURL = Environment.getExternalStorageDirectory() + File.separator + "MemosUploadedURL";

                    File uploadedMemoAudioURL = new File(folderUploadedMemoAudioURL, tempString.toString() + ".txt");

                    //File file = new File(sdcard, "Uploaded Files URL.txt");


                    StringBuilder text = new StringBuilder();

                    BufferedReader br = new BufferedReader(new FileReader(uploadedMemoAudioURL));
                    String line;

                    while ((line = br.readLine()) != null)
                    {
                        text.append(line);

                        // convert string to URL and set as play source
                        Log.i("THE_URL", line);

                        myUri[0] = Uri.parse(line);

                    }
                    br.close();



                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        Thread playerThread = new Thread(r);
        playerThread.run();






        // set audio source to URL
        Runnable s = new Runnable() {
            @Override
            public void run() {
                final MediaPlayer m = new MediaPlayer();


                try {
                    final String URL = myUri[0].toString();
                    m.setDataSource(URL);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                try {
                    m.prepare();

                    m.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(final MediaPlayer mp) {
                            final Handler seekHandler = new Handler();
                            playButton.setImageResource(R.drawable.ic_stop_icon);



                            final SeekBar seekBar = (SeekBar) findViewById(R.id.memo_seekBar);
                            seekBar.setMax(mp.getDuration());

                            mp.start();

                            // for the Seekbar.
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mp.isPlaying() && mp.getCurrentPosition() != mp.getDuration()) {
                                        seekBar.setProgress(mp.getCurrentPosition());
                                    }
                                    seekHandler.postDelayed(this, 10);
                                }
                            });
                        }
                    });

                    Log.i("PLAY_CLOUD_AUDIO", "Playing Cloud Audio");

                    // check if MediaPlayer (audio) is done.
                    m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer m) {
                            playButton.setImageResource(R.drawable.ic_play_icon);
                            m.pause();
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        Thread playerThread2 = new Thread(s);
        playerThread2.run();
    }

    // for playing locally stored audio file.
    private void playAudioFile() {

        try {

            final MediaPlayer m = new MediaPlayer();

            try {

                // if "Personal" is selected
                if (navCurrentSelected == 0) {
                    m.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalAudio" + File.separator + tempString.toString());
                }

                // if "Work" is selected
                else if (navCurrentSelected == 1) {
                    m.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkAudio" + File.separator + tempString.toString());
                }

                // if "Create New Category" is selected
                else if (navCurrentSelected == 3) {
                    m.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Audio" + File.separator + tempString.toString());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                m.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }


            m.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {
                    final Handler seekHandler = new Handler();
                    playButton.setImageResource(R.drawable.ic_stop_icon);

                    final SeekBar seekBar = (SeekBar) findViewById(R.id.memo_seekBar);
                    seekBar.setMax(mp.getDuration());

                    mp.start();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mp.isPlaying() && mp.getCurrentPosition() != mp.getDuration()) {
                                seekBar.setProgress(mp.getCurrentPosition());
                            }
                            seekHandler.postDelayed(this, 10);
                        }
                    });
                }
            });





            Log.i("PLAY_AUDIO", "Playing Audio");

            // check if MediaPlayer (audio) is done.
            m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer m) {
                    playButton.setImageResource(R.drawable.ic_play_icon);
                    m.pause();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }











    // get the name of the touched recording (tempString StringBuilder)
    // append that name to the MemosUploaded file
    // upload the file.


    // get and display files (for every category except "Uploaded")
    private ArrayList<String> getDataSet() {

        try {

            String filePath = null;
            String filePath2 = null;
            String filePath3 = null;



            if (navCurrentSelected == 0) {
                filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalAudio";
                filePath2 = Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalText";
                filePath3 = Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalLocations";

            } else if (navCurrentSelected == 1) {
                filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkAudio";
                filePath2 = Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkText";
                filePath3 = Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkLocations";


            } else if (navCurrentSelected == 3) {
                filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Audio";
                filePath2 = Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Text";
                filePath3 = Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Locations";

            }
            Log.d("Files", "Path: " + filePath); // put file path in Log (to be sure of file path)

            File f = new File(filePath);
            File f2 = new File(filePath2);
            File f3 = new File(filePath3);



            File file[] = f.listFiles(); // array of files
            Log.d("Files", "Size: " + file.length); // current number of files in the folder

            File file2[] = f2.listFiles(); // array of files
            Log.d("Files", "Size: " + file2.length); // current number of files in the folder

            File file3[] = f3.listFiles(); // array of files
            Log.d("Files", "Size: " + file3.length); // current number of files in the folder


            // populate items in RecyclerView
            for (int index = 0; index < file.length; index++)
            {
                previewTextToPass.delete(0, previewTextToPass.length());
                String textForTextView = file[index].getName() + ".txt"; // text file to read from (when populating yellow transcription box)

                inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View vi = inflater.inflate(R.layout.recyclerview_item, null); //recyclerview_item.xml is your file.


                audioListArrayList.add(file[index].getName());
                TextView memoTitle = (TextView) vi.findViewById(R.id.memo_title); //get a reference to the textview on the recyclerview_item.xml file.
                TextView memoPreview = (TextView) vi.findViewById(R.id.memo_preview);


                memoTitle.setText(file[index].getName()); // set title of memo with name in text file.


                StringBuilder text = new StringBuilder(); // StringBuilder for audio transcription.


                try {
                    BufferedReader br = new BufferedReader(new FileReader(filePath2 + File.separator + textForTextView));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                    }

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                String preview = "testing";

                //previewTextToPassArrayList.add(text.toString());
                //previewTextToPass.clear();
                previewTextToPass.append(text);

                //String test = "test";
                memoPreview.setText(preview);
                Log.i("PREVIEW", previewTextToPass.toString());

                // create the arraylist of
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return audioListArrayList;
    }




    // populate transcript text box and location info
    public void populateTextBox(final View view) {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                final StringBuilder transcriptionStringBuilder = new StringBuilder();
                final StringBuilder locationInfoStringBuilder = new StringBuilder();


                File filePathText;
                File filePathLocation;


                final TextView fullTranscription = (TextView) view.findViewById(R.id.transcription_full);
                final TextView locationTextView = (TextView) view.findViewById(R.id.location_textView); // textview for location info








                // else if "Personal" is selected
                if (navCurrentSelected == 0) {
                    filePathText = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText" + File.separator + tempString.toString() + ".txt");
                    filePathLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalLocations" + File.separator + tempString.toString() + ".txt");


                    // try to load contents of transcription file
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(filePathText));
                        String line;

                        while ((line = br.readLine()) != null) {
                            transcriptionStringBuilder.append(line);
                            //text.append('\n');
                        }
                        fullTranscription.setText(transcriptionStringBuilder);

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                        fullTranscription.setText("Unable to display memo");
                        e.printStackTrace();
                    }



                    // try to load contents of locations file
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(filePathLocation));
                        String line;

                        while ((line = br.readLine()) != null) {
                            locationInfoStringBuilder.append(line);
                        }

                        locationTextView.setText(locationInfoStringBuilder);

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't display location info", Toast.LENGTH_LONG).show();
                        locationTextView.setText("Location Unavailable");
                        e.printStackTrace();
                    }

                }







                // else if "Work" is selected
                else if (navCurrentSelected == 1) {
                    filePathText = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText" + File.separator + tempString.toString() + ".txt");
                    filePathLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkLocations" + File.separator + tempString.toString() + ".txt");


                    // try to load contents of transcription text file
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(filePathText));
                        String line;

                        while ((line = br.readLine()) != null) {
                            transcriptionStringBuilder.append(line);
                        }
                        fullTranscription.setText(transcriptionStringBuilder);

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                        fullTranscription.setText("Unable to display memo");
                        e.printStackTrace();
                    }



                    // try to load contents of locations file
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(filePathLocation));
                        String line;

                        while ((line = br.readLine()) != null) {
                            locationInfoStringBuilder.append(line);
                        }

                        locationTextView.setText(locationInfoStringBuilder);

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't display location info", Toast.LENGTH_LONG).show();
                        locationTextView.setText("Location Unavailable");
                        e.printStackTrace();
                    }

                }








                // else if "Uploaded" is selected
                else if (navCurrentSelected == 2) // UPLOADED SECTION SELECTED
                {



                    // attempt to get and load the contents of the transcription file in the database.
                    try {

                        Runnable getOnlineTranscription = new Runnable() {
                            @Override
                            public void run() {
                                File folderUploadedTranscriptFile = new File(Environment.getExternalStorageDirectory() + File.separator + "MemoTranscriptsUploadedURL");


                                try {
                                    BufferedReader br = new BufferedReader(new FileReader(folderUploadedTranscriptFile + File.separator + tempString.toString() + ".txt"));
                                    String line;

                                    // read the URL the user will be going to from the appropriate file.
                                    while ((line = br.readLine()) != null) {
                                        transcriptionStringBuilder.append(line);
                                    }

                                    // set the URL of the transcription text file as the URL in the text file.
                                    URL url = new URL(transcriptionStringBuilder.toString());
                                    Scanner s = new Scanner(url.openStream());
                                    String aLine = null; // the contents of the online transcription file will go here.

                                    // read the contents of the text file in the database.
                                    while (s.hasNextLine()) {
                                        aLine = s.nextLine();
                                        Log.i("TRANSCRIPT_ONLINE", aLine);
                                    }

                                    fullTranscription.setText(aLine);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        Thread getOnlineTranscriptionThread = new Thread(getOnlineTranscription);
                        getOnlineTranscriptionThread.run();

                    } catch(Exception e) {
                        fullTranscription.setText("Unable to display memo");
                        e.printStackTrace();
                    }




                    // attempt to get the location file from the database.
                    try {

                        Runnable getOnlineLocation = new Runnable() {
                            @Override
                            public void run() {
                                File folderUploadedTranscriptFile = new File(Environment.getExternalStorageDirectory() + File.separator + "MemoLocationsUploadedURL");


                                try {
                                    BufferedReader br = new BufferedReader(new FileReader(folderUploadedTranscriptFile + File.separator + tempString.toString() + ".txt"));
                                    String line;

                                    // read the URL the user will be going to from the appropriate file.
                                    while ((line = br.readLine()) != null) {
                                        locationInfoStringBuilder.append(line);
                                    }

                                    // set the URL of the transcription text file as the URL in the text file.
                                    URL url = new URL(locationInfoStringBuilder.toString());
                                    Scanner s = new Scanner(url.openStream());
                                    String aLine = null; // the contents of the online transcription file will go here.

                                    // read the contents of the text file in the database.
                                    while (s.hasNextLine()) {
                                        aLine = s.nextLine();
                                        Log.i("LOCATION_INFO_ONLINE", aLine);
                                    }

                                    locationTextView.setText(aLine);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        Thread getOnlineLocationThread = new Thread(getOnlineLocation);
                        getOnlineLocationThread.run();

                    } catch(Exception e) {
                        e.printStackTrace();
                        locationTextView.setText("Unable to display location info");
                    }
                }








                else if (navCurrentSelected == 3)
                {
                    filePathText = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text" + File.separator + tempString.toString() + ".txt");
                    filePathLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Locations" + File.separator + tempString.toString() + ".txt");


                    // try to load contents of transcription text file
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(filePathText));
                        String line;

                        while ((line = br.readLine()) != null) {
                            transcriptionStringBuilder.append(line);
                        }
                        fullTranscription.setText(transcriptionStringBuilder);

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                        fullTranscription.setText("Unable to display memo");
                        e.printStackTrace();
                    }



                    // try to load contents of locations file
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(filePathLocation));
                        String line;

                        while ((line = br.readLine()) != null) {
                            locationInfoStringBuilder.append(line);
                        }

                        locationTextView.setText(locationInfoStringBuilder);

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't display location info", Toast.LENGTH_LONG).show();
                        locationTextView.setText("Location Unavailable");
                        e.printStackTrace();
                    }
                }

            }
        };
        Thread playerThread = new Thread(r);
        playerThread.run();


    }








    // refresh the yellow textbox after the memo has been renamed.
    public void refreshTextBoxAfterRename(final View view)
    {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String mp3ExtensionAdd = tempTextfield + ".mp3";

                File filePath = null;

                if (navCurrentSelected == 0) {
                    filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText" + File.separator + mp3ExtensionAdd + ".txt");
                } else if (navCurrentSelected == 1) {
                    filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText" + File.separator + mp3ExtensionAdd + ".txt");

                } else if (navCurrentSelected == 3) {
                    filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text" + File.separator + mp3ExtensionAdd + ".txt");

                }

                StringBuilder text = new StringBuilder();


                try {
                    BufferedReader br = new BufferedReader(new FileReader(filePath));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                TextView fullTranscription = (TextView) view.findViewById(R.id.transcription_full);

                fullTranscription.setText(text);
            }
        };
        Thread playerThread = new Thread(r);
        playerThread.run();


    }



    private void permissionsGranted(View vi)
    {

        if (checkPermission()) {

        } else {
            Snackbar.make(vi, "Please allow permissions.", Snackbar.LENGTH_LONG).show();
        }

        if (!checkPermission()) {

            requestPermission();

        }
    }

    // handles permissions for API 23 and up
    private boolean checkPermission()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);


        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission()
    {

        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean audioAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean locationAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;


                    if (audioAccepted && storageAccepted && locationAccepted) {
                        //Snackbar.make(view, "Permissions Granted", Snackbar.LENGTH_LONG).show();
                        Toast.makeText(MainActivity.this, "Permissions Granted", Toast.LENGTH_LONG).show();
                    } else {

                        Snackbar.make(view, "Permissions Denied. Cannot access microphone, storage, and location", Snackbar.LENGTH_LONG).show();

                        //Toast.makeText(MainActivity.this, "Permissions Denied. Cannot access microphone and storage.", Toast.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                                showMessageOKCancel("All permissions are needed for Audio Memo to function properly",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener)
    {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    // if back button is pressed while Navigation Drawer is opened
    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    // what to put in the Navigation Drawer when the app starts up
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    // when an item in the Navigation Drawer is selected.
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.i("NAV_ID", String.valueOf(id));


        // else if "Personal" is selected
        if (id == R.id.nav_personal) {

            if (tempFileNameOnline != null)
            {
                tempFileNameOnline.delete(0, tempFileNameOnline.length());
            }

            if (changeRoundButtonColorFlag == 1) {
                changeRoundButtonColorFlag = 0;
                changeRoundButtonColor();
            }

            if (navCurrentSelected == 0) {
                Toast.makeText(MainActivity.this, "'Personal' Already Selected", Toast.LENGTH_SHORT).show();
            }

            else {
                navCurrentSelected = 0;

                changeNavHeaderImage(); // change the navigation drawer header image.

                audioListArrayList.clear();

                mAdapter = new MyRecyclerViewAdapter(getDataSet());
                mRecyclerView.setAdapter(mAdapter);

                // Handle the camera action
                mRecyclerView.invalidate();
                refreshRecyclerView();
            }

        }

        // else if "Work" is selected
        else if (id == R.id.nav_work)
        {

            if (tempFileNameOnline != null)
            {
                tempFileNameOnline.delete(0, tempFileNameOnline.length());
            }

            if (changeRoundButtonColorFlag == 1)
            {
                changeRoundButtonColorFlag = 0;
                changeRoundButtonColor();
            }

            if (navCurrentSelected == 1) {
                Toast.makeText(MainActivity.this, "'Work' Already Selected", Toast.LENGTH_SHORT).show();
            } else {
                navCurrentSelected = 1;

                changeNavHeaderImage(); // change the navigation drawer header image.

                audioListArrayList.clear();

                mAdapter = new MyRecyclerViewAdapter(getDataSet());
                mRecyclerView.setAdapter(mAdapter);

                // Handle the camera action
                mRecyclerView.invalidate();
                refreshRecyclerView();

            }
        }

        // else if "Uploaded" is selected
        else if (id == R.id.nav_uploaded)
        {

            if (tempFileNameOnline != null)
            {
                tempFileNameOnline.delete(0, tempFileNameOnline.length());
            }

            if (changeRoundButtonColorFlag == 0) {
                changeRoundButtonColorFlag = 1;
                changeRoundButtonColor();
            }

            if (navCurrentSelected == 2) {
                Toast.makeText(MainActivity.this, "'Uploaded' Already Selected", Toast.LENGTH_SHORT).show();
            } else {

                navCurrentSelected = 2;

                changeNavHeaderImage();

                audioListArrayListUploaded.clear();

                mAdapter = new MyRecyclerViewAdapter(getDataSetUploaded());

                mRecyclerView.setAdapter(mAdapter);

                mRecyclerView.invalidate();
                refreshRecyclerView();

                // pass navCurrentSelected to MyRecyclerViewAdapter here

                // pass navCurrentSelected to MyRecyclerViewAdapter so app will know to change memo title color to blue (since it's uploaded).
                myVariable = navCurrentSelected;
                getVariable();

            }

        }

        else if (id == R.id.nav_add)
        {

            if (tempFileNameOnline != null)
            {
                tempFileNameOnline.delete(0, tempFileNameOnline.length());
            }


            // dialog box to add new category
            addNewCategory();

        }

        else if (id % 10 == 0)
        {

            if (tempFileNameOnline != null)
            {
                tempFileNameOnline.delete(0, tempFileNameOnline.length());
            }

            if (changeRoundButtonColorFlag == 1)
            {
                changeRoundButtonColorFlag = 0;
                changeRoundButtonColor();
            }

            navCurrentSelected = 3;

            changeNavHeaderImage(); // change the navigation drawer header image.


            categoryName = item.toString();
            // get name of that category
            //Toast.makeText(MainActivity.this, item.toString(), Toast.LENGTH_LONG).show();

            navigationView.getMenu().getItem(2).setChecked(true);


            audioListArrayList.clear();

            mAdapter = new MyRecyclerViewAdapter(getDataSet());
            mRecyclerView.setAdapter(mAdapter);

            // Handle the camera action
            mRecyclerView.invalidate();
            refreshRecyclerView();

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    // for when "Uploaded" is selected.
    private void changeRoundButtonColor() {

        if (changeRoundButtonColorFlag == 1) {
            ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1, 50, 50); // 50, 50 is so it animates from the center outward.
            anim.setFillBefore(true);
            anim.setFillAfter(true);
            anim.setFillEnabled(true);
            anim.setDuration(500);
            anim.setInterpolator(new OvershootInterpolator());

            //anim.setStartOffset(3000); // to delay animation (if needed for some reason)
            roundButton.startAnimation(anim);
            roundButton.setBackgroundTintList(ColorStateList.valueOf(Color.CYAN));
        } else {
            ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1, 50, 50); // 50, 50 is so it animates from the center outward.
            anim.setFillBefore(true);
            anim.setFillAfter(true);
            anim.setFillEnabled(true);
            anim.setDuration(500);
            anim.setInterpolator(new OvershootInterpolator());

            //anim.setStartOffset(3000); // to delay animation (if needed for some reason)
            roundButton.startAnimation(anim);
            //roundButton.setBackgroundTintList(ColorStateList.valueOf(R.color.yellow));
            roundButton.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.yellow));

        }


    }


    public void addNewCategory() {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                builder.setTitle("New Category");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                input.setHint("Enter New Category Name Here");

                builder.setView(input);

                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); // show keyboard

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tempTextfield = input.getText().toString().trim();


                        categoryCount += 10; // save this to SharedPreferences every time so IDs are not replicated.

                        myMoveGroupItem = navigationView.getMenu().getItem(2);
                        // MenuItem myMoveGroupItem = navigationView.getMenu().findItem(R.id.submenu_1);  -- it also works!
                        subMenu = myMoveGroupItem.getSubMenu();
                        //subMenu.add(tempTextfield);
                        subMenu.add(2, categoryCount, Menu.NONE, tempTextfield);


                        // create a folder named after that file, one for audio and the other for text.
                        createFolderNewCategory(tempTextfield);


                        SharedPreferences.Editor editor = sharedpreferences.edit();

                        editor.putString(nameKey, tempTextfield);
                        editor.putInt(countKey, categoryCount);
                        editor.apply();

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); // hide keyboard

                    }
                });


                android.app.AlertDialog dialogReportBox = builder.create();
                dialogReportBox.show();
                dialogReportBox.getButton(dialogReportBox.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialogReportBox.getButton(dialogReportBox.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        };
        Thread addNewCategoryThread = new Thread(r);
        addNewCategoryThread.run();
    }


    public int getVariable() {
        return myVariable;
    }


    public void createFolderNewCategory(String newCategoryName) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d("SDCARD: ", "No SDCARD");
        } else {
            File folderFirstTimeNewCategoryAudio = new File(Environment.getExternalStorageDirectory() + File.separator + tempTextfield + "Audio");
            File folderFirstTimeNewCategoryText = new File(Environment.getExternalStorageDirectory() + File.separator + tempTextfield + "Text");
            File folderFirstTimeNewCategoryLocations = new File(Environment.getExternalStorageDirectory() + File.separator + tempTextfield + "Locations");



            if (!(folderFirstTimeNewCategoryAudio.exists())) {
                folderFirstTimeNewCategoryAudio.mkdirs();
                Log.d("NEW_CATEGORY_AUDIO", tempTextfield + "AUDIO Folder Created");
                Log.d("Audio Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + tempTextfield + "Audio");

            }

            if (!(folderFirstTimeNewCategoryText.exists())) {
                folderFirstTimeNewCategoryText.mkdirs();
                Log.d("NEW_CATEGORY_TEXT", tempTextfield + "TEXT Folder Created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + tempTextfield + "Text");
            }


            if (!(folderFirstTimeNewCategoryLocations.exists())) {
                folderFirstTimeNewCategoryLocations.mkdirs();
                Log.d("NEW_CATEGORY_TEXT", tempTextfield + "LOCATIONS Folder Created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + tempTextfield + "Locations");
            }

        }

    }


    public void roundButtonClicked(View view) {
        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Log.i("ROUNDBUTTON", "RECORD BUTTON TOUCHED");
        Toast.makeText(MainActivity.this, "Recording...", Toast.LENGTH_LONG).show();
        tempFileName = System.currentTimeMillis(); // get current milliseconds
        vb.vibrate(50); // vibrate when record button is touched
        permissionsGranted(view); // check if permissions have been granted (API 23 (Marshmallow) and up)

        startRecording();
    }


    private void uploadAudio() {
        android.app.AlertDialog.Builder sendReportBox = new android.app.AlertDialog.Builder(MainActivity.this);
        sendReportBox.setTitle("Upload Memo");
        sendReportBox.setMessage("Are you sure you want to upload this memo?");
        sendReportBox.setCancelable(true);


        sendReportBox.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                        uploadAudioYesSelected();

                    }
                });


        sendReportBox.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        android.app.AlertDialog dialogReportBox = sendReportBox.create();
        dialogReportBox.show();
        dialogReportBox.getButton(dialogReportBox.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        dialogReportBox.getButton(dialogReportBox.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

    }





    public void uploadAudioYesSelected()
    {
        String audioToUploadFilePath;
        String textToUploadFilePath;
        String locationToUploadFilePath;



        if (navCurrentSelected == 0)
        {
            audioToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalAudio" + File.separator + tempString.toString();
            textToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText" + File.separator + tempString.toString() + ".txt";
            locationToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalLocations" + File.separator + tempString.toString() + ".txt";
        }

        else if (navCurrentSelected == 1)
        {
            audioToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkAudio" + File.separator + tempString.toString();
            textToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText" + File.separator + tempString.toString() + ".txt";
            locationToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkLocations" + File.separator + tempString.toString() + ".txt";
        }

        else if (navCurrentSelected == 3)
        {
            audioToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Audio" + File.separator + tempString.toString();
            textToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text" + File.separator + tempString.toString() + ".txt";
            locationToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Locations" + File.separator + tempString.toString() + ".txt";
        }

        else
        {
            audioToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TempAudio" + File.separator + tempFileNameOnline.toString();
            textToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TempText" + File.separator + tempFileNameOnline.toString() + ".txt";
            locationToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TempLocations" + File.separator + tempFileNameOnline.toString() + ".txt";
        }



        mProgress = new ProgressDialog(MainActivity.this);
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.setCancelable(false);
        mProgress.setTitle("Please Wait..");
        mProgress.setMessage("Uploading...");

        mProgress.show();

        StorageReference filepathAudio;
        StorageReference filepathText;
        StorageReference filepathLocation;

        if (navCurrentSelected == 2)
        {
            filepathAudio = mStorage.child("UploadedAudio").child(tempFileNameOnline.toString());

            filepathText = mStorage.child("UploadedText").child(tempFileNameOnline.toString() + ".txt");

            filepathLocation = mStorage.child("UploadedLocations").child(tempFileNameOnline.toString() + ".txt");
        }

        else
        {
            filepathAudio = mStorage.child("UploadedAudio").child(tempString.toString());

            filepathText = mStorage.child("UploadedText").child(tempString.toString() + ".txt");

            filepathLocation = mStorage.child("UploadedLocations").child(tempString.toString() + ".txt");
        }



        // directories of the file that needs to be uploaded
        final Uri uriAudio = Uri.fromFile(new File(audioToUploadFilePath));
        Uri uriText = Uri.fromFile(new File(textToUploadFilePath));
        Uri uriLocation = Uri.fromFile(new File(locationToUploadFilePath));


        // UPLOAD THE AUDIO FILE.
        filepathAudio.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final String fileUriAudio = taskSnapshot.getDownloadUrl().toString();
                Log.i("URI_TEST", fileUriAudio);

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {

                            // save the memo name to this directory (for access to the audio file)
                            File folderUploadedMemoAudio = new File(Environment.getExternalStorageDirectory() + File.separator + "MemosUploaded");
                            if (!folderUploadedMemoAudio.exists()) {
                                folderUploadedMemoAudio.mkdirs();
                            }

                            // save the memo URL to this directory (for access to the audio file)
                            File folderUploadedMemoAudioURL = new File(Environment.getExternalStorageDirectory() + File.separator + "MemosUploadedURL");
                            if (!folderUploadedMemoAudioURL.exists()) {
                                folderUploadedMemoAudioURL.mkdirs();
                            }








                            // if user is not in the "Uploaded" section...
                            if (navCurrentSelected != 2)
                            {

                                // write memo name to text file (filename + ".txt") in "MemosUploaded" folder
                                File uploadedMemoAudio = new File(folderUploadedMemoAudio, tempString.toString() + ".txt");
                                FileWriter writer1 = new FileWriter(uploadedMemoAudio, true);
                                writer1.append(tempString.toString());
                                //writer1.append("\n");
                                writer1.flush();
                                writer1.close();


                                // write memo audio URL to text file (filename + ".txt") in "MemosUploadedURL" folder
                                File uploadedMemoAudioURL = new File(folderUploadedMemoAudioURL, tempString.toString() + ".txt");
                                FileWriter writer2 = new FileWriter(uploadedMemoAudioURL, true);
                                writer2.append(fileUriAudio);
                                //writer2.append("\n");
                                writer2.flush();
                                writer2.close();
                            }

                            else
                            {

                                // write memo name to text file (filename + ".txt") in "MemosUploaded" folder
                                File uploadedMemoAudio = new File(folderUploadedMemoAudio, tempFileNameOnline.toString() + ".txt");
                                FileWriter writer1 = new FileWriter(uploadedMemoAudio, true);
                                writer1.append(tempFileNameOnline);
                                //writer1.append("\n");
                                writer1.flush();
                                writer1.close();


                                // write memo audio URL to text file (filename + ".txt") in "MemosUploadedURL" folder
                                File uploadedMemoAudioURL = new File(folderUploadedMemoAudioURL, tempFileNameOnline.toString() + ".txt");
                                FileWriter writer2 = new FileWriter(uploadedMemoAudioURL, true);
                                writer2.append(fileUriAudio);
                                //writer2.append("\n");
                                writer2.flush();
                                writer2.close();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // create a folder with a txt file called UploadedMemos (MediaRecorderUploaded)
                };
                Thread uploadAudioFileThread = new Thread(r);
                uploadAudioFileThread.run();

            }
        });





        // UPLOAD THE LOCATION FILE.
        filepathLocation.putFile(uriLocation).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final String fileUriLocation = taskSnapshot.getDownloadUrl().toString();

                Log.i("URI_AUDIO", uriAudio.toString());

                deleteAudioFile();

                // directory for URL to location text files.
                File folderUploadedLocationsFile = new File(Environment.getExternalStorageDirectory() + File.separator + "MemoLocationsUploadedURL");
                if (!folderUploadedLocationsFile.exists()) {
                    folderUploadedLocationsFile.mkdirs();
                }


                // if user is not in the "Uploaded" section...
                if (navCurrentSelected != 2)
                {
                    try {
                        File uploadedLocationsFileURL = new File(folderUploadedLocationsFile, tempString.toString() + ".txt");
                        FileWriter writer3 = new FileWriter(uploadedLocationsFileURL, true);
                        writer3.append(fileUriLocation);
                        writer3.flush();
                        writer3.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        mProgress.dismiss();
                    }
                }

                else
                {
                    try {
                        File uploadedLocationsFileURL = new File(folderUploadedLocationsFile, tempFileNameOnline.toString() + ".txt");
                        FileWriter writer3 = new FileWriter(uploadedLocationsFileURL, true);
                        writer3.append(fileUriLocation);
                        writer3.flush();
                        writer3.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        mProgress.dismiss();
                    }
                }
            }
        });


        // UPLOAD THE TRANSCRIPTION FILE.
        filepathText.putFile(uriText).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final String fileUriText = taskSnapshot.getDownloadUrl().toString();

                Toast.makeText(MainActivity.this, "Memo Uploaded", Toast.LENGTH_SHORT).show();
                Log.i("URI_AUDIO", uriAudio.toString());
                deleteAudioFile();

                File folderUploadedTranscriptFile = new File(Environment.getExternalStorageDirectory() + File.separator + "MemoTranscriptsUploadedURL");
                if (!folderUploadedTranscriptFile.exists()) {
                    folderUploadedTranscriptFile.mkdirs();
                }


                // if user is not in the "Uploaded" section...
                if (navCurrentSelected != 2)
                {
                    try {
                        File uploadedTranscriptFileURL = new File(folderUploadedTranscriptFile, tempString.toString() + ".txt");
                        FileWriter writer3 = new FileWriter(uploadedTranscriptFileURL, true);
                        writer3.append(fileUriText);
                        //writer3.append("\n");
                        writer3.flush();
                        writer3.close();
                        //tempFileNameOnline.delete(0, tempFileNameOnline.length());
                        mProgress.dismiss();

                    } catch (IOException e) {
                        mProgress.dismiss();
                        //tempFileNameOnline.delete(0, tempFileNameOnline.length());
                        e.printStackTrace();
                    }
                }



                else
                {
                    try {
                        File uploadedTranscriptFileURL = new File(folderUploadedTranscriptFile, tempFileNameOnline.toString() + ".txt");
                        FileWriter writer3 = new FileWriter(uploadedTranscriptFileURL, true);
                        writer3.append(fileUriText);
                        //writer3.append("\n");
                        writer3.flush();
                        writer3.close();
                        //tempFileNameOnline.delete(0, tempFileNameOnline.length());
                        mProgress.dismiss();

                    } catch (IOException e) {
                        mProgress.dismiss();
                        //tempFileNameOnline.delete(0, tempFileNameOnline.length());
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private ArrayList<String> getDataSetUploaded() {

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {


                    String folderUploadedMemoAudio = Environment.getExternalStorageDirectory() + File.separator + "MemosUploaded";



                    // list files in "MemosUploaded" folder.
                    File audioFileUploaded = new File(folderUploadedMemoAudio);
                    File audio_filesUploaded[] = audioFileUploaded.listFiles();
                    Log.d("AudioFilesUploaded", "Size: " + audio_filesUploaded.length); // Log for current number of transcript files in the "MemosUploaded" folder.


                    // go through listed files in the folder ("MemosUploaded") and add those filenames to the audioListArrayList
                    for (int i = 0; i < audio_filesUploaded.length; i++)
                    {

                        // read memo titles from text files.
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(folderUploadedMemoAudio + File.separator + audio_filesUploaded[i].getName()));
                            String line;

                            while ((line = br.readLine()) != null)
                            {
                                audioListArrayListUploaded.add(line);
                            }
                            br.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }





                    // just to log how many transcript files exist
                    File transcriptTextFile = new File(folderUploadedMemoAudio);
                    File transcript_textFiles[] = transcriptTextFile.listFiles();
                    Log.d("AudioFiles", "Size: " + transcript_textFiles.length); // Log for current number of transcript files in the






                    Log.i("Uploaded Files", audioListArrayListUploaded.toString());


                    // now load the contents (filenames) into memotitle


                    for (int i = 0; i < audioListArrayListUploaded.size(); i++) {
                        View vi = inflater.inflate(R.layout.recyclerview_item, null);
                        TextView memoTitle = (TextView) vi.findViewById(R.id.memo_title); //get a reference to the textview on the recyclerview_item.xml file.
                        memoTitle.setText(audioListArrayListUploaded.get(i));

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread getDataSetUploadedThread = new Thread(r);
        getDataSetUploadedThread.run();

        return audioListArrayListUploaded;
    }


    public void downloadAudioDialog()
    {
        android.app.AlertDialog.Builder sendReportBox = new android.app.AlertDialog.Builder(MainActivity.this);
        sendReportBox.setTitle("Download Memo");
        sendReportBox.setMessage("Are you sure you want to download this memo?");
        //sendReportBox.setIcon(android.R.drawable.ic_dialog_alert);
        sendReportBox.setCancelable(true);


        sendReportBox.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        downloadAudio();
                        dialog.cancel();
                    }
                });

        sendReportBox.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog dialogReportBox = sendReportBox.create();
        dialogReportBox.show();
        dialogReportBox.getButton(dialogReportBox.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.forest_green));
        dialogReportBox.getButton(dialogReportBox.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

    }


    // TO DO. DOWNLOAD TO "PERSONAL"
    public void downloadAudio() {
        //Toast.makeText(MainActivity.this, "Download Button Touched", Toast.LENGTH_LONG).show();

        String downloadAudioToThisFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalAudio";
        String downloadTextToThisFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText";
        String downloadLocationsToThisFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalLocations";



        File localFolderForAudio = new File (downloadAudioToThisFilePath, tempString.toString());
        File localFolderForText = new File (downloadTextToThisFilePath, tempString.toString() + ".txt");
        File localFolderForLocations = new File (downloadLocationsToThisFilePath, tempString.toString() +".txt");



        mProgress = new ProgressDialog(MainActivity.this);
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.setCancelable(false);
        mProgress.setTitle("Please Wait..");
        mProgress.setMessage("Downloading...");

        mProgress.show();


        StorageReference filepathAudio = mStorage.child("UploadedAudio/"+ tempString.toString()); // online file path for the audio file.

        StorageReference filepathText = mStorage.child("UploadedText/" + tempString.toString() + ".txt"); // online path for the text file.

        StorageReference filepathLocation = mStorage.child("UploadedLocations/" + tempString.toString() + ".txt"); // online path for the location file.


        String audioToDownloadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MemosUploadedURL" + File.separator + tempString + ".txt";



        /*
        try {
            localFolderForAudio = File.createTempFile(tempString.toString(), null, downloadAudioToThisFilePath);
            localFolderForText = File.createTempFile(tempString.toString(), null, downloadTextToThisFilePath);
            localFolderForLocations = File.createTempFile(tempString.toString(), null, downloadLocationsToThisFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
        */


        // download audio file to "PersonalAudio" folder.
        filepathAudio.getFile(localFolderForAudio).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //Toast.makeText(MainActivity.this, "Audio Download Complete", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, "Unable to download audio.", Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
        });


        // download transcript file to "PersonalText" folder.
        filepathText.getFile(localFolderForText).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //Toast.makeText(MainActivity.this, "Transcript Download Complete", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, "Unable to download transcript.", Toast.LENGTH_LONG).show();
                mProgress.dismiss();
            }
        });


        // download location file to "PersonalLocations" folder.
        filepathLocation.getFile(localFolderForLocations).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Memo Downloaded", Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, "Unable to download location file.", Toast.LENGTH_LONG).show();
                mProgress.dismiss();
            }
        });



        mProgress.dismiss();

        ((MyRecyclerViewAdapter) mAdapter).deleteItem(touchedPosition);
        refreshRecyclerView();




    }



    public void TextViewClicked(View v)
    {

        switcher.showNext();

        onBackPressedCheck = 1;
        Log.i("ONBACKPRESSED_CHECK", String.valueOf(onBackPressedCheck));

        final TextView myTV = (TextView) switcher.findViewById(R.id.transcription_full);
        final EditText editText = (EditText) findViewById(R.id.hidden_edit_view);

        // focus on editText (for editing. Keyboard and cursor show)
        editText.requestFocus();

        // save the initial contents of the yellow box (transcription box) in case user cancels the edit.
        previousTextStorage = String.valueOf(full_textView.getText());

        final InputMethodManager[] imm = {(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)};
        imm[0].showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

        // make the initial contents of the editText the contents of the yellow box (transcription box)
        editText.setText(String.valueOf(full_textView.getText()));


        // key listener for ENTER and BACK buttons of user's keyboard
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

                // if the ENTER button is pressed while editing. Changes will be saved.
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    // Perform action on key press


                    editTextString = editText.getText().toString();



                    appendToTextFile();

                    refreshTextAfterBoxEdit();

                    Toast.makeText(MainActivity.this, "Changes Saved", Toast.LENGTH_LONG).show();

                    imm[0] = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm[0].hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    Log.i("ONBACKPRESSED_CHECK", String.valueOf(onBackPressedCheck));

                    return true;
                }

                // if the BACK button is pressed while editing. Changes will be cancelled and the original text restored.
                if ((keyCode == KeyEvent.KEYCODE_BACK))
                {
                    if (onBackPressedCheck == 1)
                    {
                        // set text to what it previously was. Reload same text file into the box.
                        //textView.setText(previousTextStorage);
                        switcher.showPrevious();
                        onBackPressedCheck = 0;
                        Toast.makeText(MainActivity.this, "Edit Cancelled", Toast.LENGTH_SHORT).show();


                        return true;
                    }
                }

                return false;
            }
        });
    }









    public void renameButtonTouched()
    {
        android.app.AlertDialog.Builder renameButtonDialog = new android.app.AlertDialog.Builder(MainActivity.this);
        renameButtonDialog.setTitle("Edit");
        renameButtonDialog.setMessage("Edit Memo Title or Transcript?");
        //sendReportBox.setIcon(android.R.drawable.ic_dialog_alert);
        renameButtonDialog.setCancelable(true);


        renameButtonDialog.setPositiveButton(
                "Transcript",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Toast.makeText(MainActivity.this, "Delete selected", Toast.LENGTH_LONG).show();
                        TextViewClicked(view);
                        dialog.cancel();
                    }
                });

        renameButtonDialog.setNegativeButton(
                "Memo Title",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Toast.makeText(MainActivity.this, "Cancel selected", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        renameRecordingDialogTextField();
                    }
                });

        android.app.AlertDialog dialogRenameButton = renameButtonDialog.create();
        dialogRenameButton.show();
        dialogRenameButton.getButton(dialogRenameButton.BUTTON_POSITIVE).setTextColor(Color.GRAY);
        dialogRenameButton.getButton(dialogRenameButton.BUTTON_NEGATIVE).setTextColor(Color.GRAY);

    }






    // for when user is done editing the transcript (the one in the yellow box).
    public void appendToTextFile()
    {
        Log.i("SEARCHING_FOR_FILE", tempString.toString() + ".txt");

        String textFileToAppend = tempString.toString() + ".txt";


        String appendTextFilePath = null;

        if (navCurrentSelected == 0)
        {
            appendTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText" + File.separator + textFileToAppend;
        }

        else if (navCurrentSelected == 1)
        {
            appendTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText" + File.separator + textFileToAppend;
        }

        else if (navCurrentSelected == 3)
        {
            appendTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text" + File.separator + textFileToAppend;
        }



        FileOutputStream fop = null;
        File file;



        try {

            file = new File(appendTextFilePath);
            fop = new FileOutputStream(file);

            // if file doesn't exists for any reason, then create it first.
            if (!file.exists())
            {
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = editTextString.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

            System.out.println("Done");
            Log.i("APPENDED_STRING", editTextString);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("APPEND_TO_TEXTFILE", "File write failed: " + e.toString());

        } finally {
            try {
                if (fop != null)
                {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }







    public void refreshTextAfterBoxEdit()
    {
        switcher.showPrevious();

        String textFileToAppend = tempString.toString() + ".txt";


        String appendTextFilePath = null;

        if (navCurrentSelected == 0)
        {
            appendTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText" + File.separator + textFileToAppend;
        }

        else if (navCurrentSelected == 1)
        {
            appendTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText" + File.separator + textFileToAppend;
        }

        else if (navCurrentSelected == 3)
        {
            appendTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text" + File.separator + textFileToAppend;
        }



        StringBuilder text = new StringBuilder();


        try {
            BufferedReader br = new BufferedReader(new FileReader(appendTextFilePath));
            String line;

            while ((line = br.readLine()) != null)
            {
                text.append(line);
            }

            TextView fullTranscription = (TextView) switcher.findViewById(R.id.transcription_full);

            fullTranscription.setText(text);

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Couldn't display changes", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }










    // if user touches any other place that is not the editText (while editing the memo transcript)
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        View view = getCurrentFocus();

        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit."))
        {
            onBackPressedCheck = 0;
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);

            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];

            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
            {
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
                switcher.showPrevious();
                Toast.makeText(MainActivity.this, "Edit Cancelled", Toast.LENGTH_SHORT).show();
            }
        }

        return super.dispatchTouchEvent(ev);
    }










    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    theLocationAddress = bundle.getString("address");
                    break;
                default:
                    theLocationAddress = "Location Unavailable";
            }
        }
    }




    public void changeNavHeaderImage()
    {
        if (navCurrentSelected == 0) {
            View hView = navigationView.getHeaderView(0);
            hView.setBackgroundResource(R.drawable.nav_header_personal);
            TextView nav_headerTextView_category = (TextView) hView.findViewById(R.id.nav_headerTextView);
            //nav_headerTextView_category.setText("Personal");
        }

        else if (navCurrentSelected == 1) {
            View hView = navigationView.getHeaderView(0);
            hView.setBackgroundResource(R.drawable.nav_header_work);
            TextView nav_headerTextView_category = (TextView) hView.findViewById(R.id.nav_headerTextView);
            //nav_headerTextView_category.setText("Work");
        }

        else if (navCurrentSelected == 2) {
            View hView = navigationView.getHeaderView(0);
            hView.setBackgroundResource(R.drawable.nav_header_cloud);
            TextView nav_headerTextView_category = (TextView) hView.findViewById(R.id.nav_headerTextView);
            //nav_headerTextView_category.setText("Uploaded");
        }

        else
        {
            View hView = navigationView.getHeaderView(0);
            hView.setBackgroundResource(R.drawable.nav_header_other);
            TextView nav_headerTextView_category = (TextView) hView.findViewById(R.id.nav_headerTextView);
            //nav_headerTextView_category.setText("Other");
        }


    }

    public void deleteAudioFileUploadedDialog()
    {
        android.app.AlertDialog.Builder sendReportBox = new android.app.AlertDialog.Builder(MainActivity.this);
        sendReportBox.setTitle("Delete Uploaded Memo");
        sendReportBox.setMessage("Are you sure you want to delete this uploaded memo?");
        sendReportBox.setIcon(android.R.drawable.ic_dialog_alert);
        sendReportBox.setCancelable(true);


        sendReportBox.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAudioFileUploaded();
                        dialog.cancel();
                    }
                });

        sendReportBox.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog dialogReportBox = sendReportBox.create();
        dialogReportBox.show();
        dialogReportBox.getButton(dialogReportBox.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialogReportBox.getButton(dialogReportBox.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

    }

    public void deleteAudioFileUploaded()
    {
        mProgress = new ProgressDialog(MainActivity.this);
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.setCancelable(false);
        mProgress.setTitle("Please Wait..");
        mProgress.setMessage("Deleting...");

        mProgress.show();


        StorageReference filepathAudio = mStorage.child("UploadedAudio/"+ tempString.toString()); // online file path for the audio file.

        StorageReference filepathText = mStorage.child("UploadedText/" + tempString.toString() + ".txt"); // online path for the text file.

        StorageReference filepathLocation = mStorage.child("UploadedLocations/" + tempString.toString() + ".txt"); // online path for the location file.


        String audioToDownloadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MemosUploadedURL" + File.separator + tempString + ".txt";





        // delete audio file from the cloud
        filepathAudio.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(MainActivity.this, "Audio File Deleted", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Toast.makeText(MainActivity.this, "Unable to delete audio", Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
        });






        // delete transcript file from the cloud
        filepathText.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(MainActivity.this, "Transcription Deleted", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Toast.makeText(MainActivity.this, "Unable to delete transcript", Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
        });






        // delete location file from the cloud
        filepathLocation.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(MainActivity.this, "Location File Deleted", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Toast.makeText(MainActivity.this, "Unable to delete location file", Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
        });



        String deleteRecordingFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "MemosUploaded";



        String memosUploaded = Environment.getExternalStorageDirectory().getPath() + File.separator + "MemosUploaded";

        String memosUploadedURL = Environment.getExternalStorageDirectory().getPath() + File.separator + "MemosUploadedURL";
        String memoTranscriptsUploadedURL = Environment.getExternalStorageDirectory().getPath() + File.separator + "MemoTranscriptsUploadedURL";
        String memoLocationsUploadedURL = Environment.getExternalStorageDirectory().getPath() + File.separator + "MemoLocationsUploadedURL";


        File f1 = new File(memosUploaded);
        File memoNameUploaded[] = f1.listFiles();

        File f2 = new File(memosUploadedURL);
        File memoAudioURL[] = f2.listFiles();

        File f3 = new File(memoTranscriptsUploadedURL);
        File memoTranscriptURL[] = f3.listFiles();

        File f4 = new File(memoLocationsUploadedURL);
        File memoLocationURL[] = f4.listFiles();


        for (int i = 0; i < memoNameUploaded.length; i++) {
            if (memoNameUploaded[i].getName().equals(tempString.toString() + ".txt")) {
                boolean deleted = memoNameUploaded[i].delete();

            }
        }

        for (int i = 0; i < memoAudioURL.length; i++) {
            if (memoAudioURL[i].getName().equals(tempString.toString() + ".txt")) {
                boolean deleted = memoAudioURL[i].delete();
            }
        }

        for (int i = 0; i < memoTranscriptURL.length; i++) {
            if (memoTranscriptURL[i].getName().equals(tempString.toString() + ".txt")) {
                boolean deleted = memoTranscriptURL[i].delete();
            }
        }

        for (int i = 0; i < memoLocationURL.length; i++) {
            if (memoLocationURL[i].getName().equals(tempString.toString() + ".txt")) {
                boolean deleted = memoLocationURL[i].delete();
            }
        }




        mProgress.dismiss();

        ((MyRecyclerViewAdapter) mAdapter).deleteItem(touchedPosition);
        refreshRecyclerView();

        // go to MemosUploaded, delete file named tempString.toString()
        // go to MemosUploadedURL, delete file named tempString.toString() + ".txt"

        // go to MemoTranscriptsUploadedURL, delete tempString.toString + ".txt"
        // go to MemoLocationsUploadedURL, delete tempString.toString + ".txt"


    }




}
