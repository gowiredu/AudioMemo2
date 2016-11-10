package com.gowiredu.audiomemotest2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Movie;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.StorageMetadata;
import com.gowiredu.audiomemo.R;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.R.attr.data;
import static android.R.id.input;
import static android.R.id.list;
import static android.R.string.no;
import static android.bluetooth.BluetoothClass.Service.AUDIO;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

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
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private ArrayList<String> lines = new ArrayList<>(); // temporarily store names of files in directory
    //public ListView ResultsListView;
    public FloatingActionButton roundButton;
    private long tempFileName; // temporary file name
    private String tempString; // temporary string to store name of touched file (to look up for playback or deletion).
    String tempTextfield;
    ArrayList<String> audioListArrayList = new ArrayList<>();
    ArrayList<String> audioListArrayListUploaded = new ArrayList<>();


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
    int touchedPosition;
    LayoutInflater inflater;

    // expand/ collapse stuff
    boolean isExpanded = false;
    FrameLayout yellowCard;
    TextView full_textView;
    TextView preview_textView;
    ImageButton playButton;
    ImageButton uploadButton;
    ImageButton deleteButton;
    ImageButton renameButton;
    ImageView dropDown_icon;
    ImageView collapse_icon;
    SeekBar memoSeekBar;
    private long currentAudioLength;


    int prevTouchedPosition;
    int currentTouchedPosition;
    private int resetWidth;
    private int resetHeight;
    String theTouchedRecording;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private View view;

    // Navigation Drawer stuff
    private String[] navList = {"Device", "Uploaded"};

    NavigationView navigationView;
    int navCurrentSelected;
    private View navView;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    Menu m;
    int categoryCount;



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

    public static final String MyPREFERENCES = "MyPrefs" ;

    public static final String nameKey = "nameKey";
    public static final String countKey = "phoneKey";
    SharedPreferences sharedpreferences;
    public static String previewTextToPass = null;


    MenuItem myMoveGroupItem;
    // MenuItem myMoveGroupItem = navigationView.getMenu().findItem(R.id.submenu_1);  -- it also works!
    SubMenu subMenu;
    String categoryName;



    // add navigation drawer
    // make play button change to stop button on click
    // put upload button on far right (in place of current stop button)
    //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        view = findViewById(R.id.content); // needed to create Snackbar with permissionsGranted() method.

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        // OLD --> ResultsListView = (ListView) findViewById(R.id.ResultsListView);

        //inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View vi = inflater.inflate(R.layout.activity_main, null);


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


        //inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        //NAVIGATION DRAWER STUFF START
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);
        navCurrentSelected = 0;


        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String restoredText = prefs.getString(nameKey, null);

        if (restoredText != null) {
            Toast.makeText(MainActivity.this, restoredText, Toast.LENGTH_LONG).show();
            int restoredInt = prefs.getInt(countKey, 0);
            Toast.makeText(MainActivity.this, String.valueOf(restoredInt), Toast.LENGTH_LONG).show();

            categoryCount = restoredInt;

            myMoveGroupItem = navigationView.getMenu().getItem(2);
            // MenuItem myMoveGroupItem = navigationView.getMenu().findItem(R.id.submenu_1);  -- it also works!
            subMenu = myMoveGroupItem.getSubMenu();
            //subMenu.add(tempTextfield);
            subMenu.add(2, categoryCount, Menu.NONE, restoredText);
            //String name = prefs.getString("name", "No name defined");//"No name defined" is the default value.
            //int idName = prefs.getInt("idName", 0); //0 is the default value.
        }



        //NAVIGATION DRAWER STUFF END


        createFolderIfNoFolder(); // creates a folder for recordings if there is no folder yet (like the first time the app is launched after download).


        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        //mRecyclerView.setAdapter(new SampleRecycler());

        Log.i("R1", "Recyclerview found");


        Log.i("R2", "Layout Manager set");


        if (navCurrentSelected == 0)
        {
            mAdapter = new MyRecyclerViewAdapter(getDataSet());
        }
        Log.i("R3", "Dataset found");

        mRecyclerView.setAdapter(mAdapter);
        Log.i("R4", "Adapter set");


        //permissionsGranted(view); // check if permissions have been granted.


        //mRecyclerView.invalidate();
        //mAdapter.notifyItemRemoved(touchedPosition);

        //mAdapter.notifyItemChanged(touchedPosition);
        //mAdapter.notifyItemRemoved(touchedPosition);

        refreshRecyclerView();

        ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                //View vi = inflater.inflate(R.layout.recyclerview_item, null);


                touchedPosition = position; // record current position


                // if current position is not equal to prev position and prev position.getHeight = 750
                // prev position.getHeight = 168. Set is expanded to false

                TextView memoTitle = (TextView) view.findViewById(R.id.memo_title);
                tempString = memoTitle.getText().toString();

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


    public void expandCollapseLayout(View view) {
        //mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, mAdapter.getItemCount());

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


        // for resizing the card when its touched.
        CardView layout = (CardView) view.findViewById(R.id.card_view);

        // Gets the layout params that will allow you to resize the layout
        ViewGroup.LayoutParams params = layout.getLayoutParams();

        // Changes the height and width to the specified *pixels*
        params.height = layout.getHeight();
        resetHeight = layout.getHeight();
        params.width = layout.getWidth();
        resetWidth = layout.getWidth();


        //card = (CardView) view.findViewById(R.id.card_view);

        if (!isExpanded) // if card has not been expanded
        {
            params.height = 750;
            params.width = layout.getWidth();
            layout.setLayoutParams(params);

            //card.setLayoutParams(new LinearLayout.LayoutParams(CardView.LayoutParams.MATCH_PARENT, 500));
            //card.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 500));

            yellowCard.setVisibility(View.VISIBLE);
            full_textView.setVisibility(View.VISIBLE);
            preview_textView.setVisibility(View.INVISIBLE);
            playButton.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            renameButton.setVisibility(View.VISIBLE);

            collapse_icon.setVisibility(View.VISIBLE);
            dropDown_icon.setVisibility(View.INVISIBLE);
            memoSeekBar.setVisibility(View.VISIBLE);


            // if the user is currently in the "Uploaded" section...
            if (navCurrentSelected == 2)
            {
                // ...change the upload button to a download button when the card expands.
                uploadButton.setImageResource(R.drawable.ic_download_icon_menu);
            }


            renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    renameRecordingDialogTextField();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteAudioDialog();
                }
            });

            inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //View vi = inflater.inflate(R.layout.recyclerview_item, null); //recyclerview_item.xml is your file.

                    // if the user is in the "Devices" section,  set play button to appropriate local file.
                    if (navCurrentSelected == 0) {
                        playAudioFile();
                    }

                    // else if the user is in the "Uploaded" section, set play button to memo saved in the cloud
                    else if (navCurrentSelected == 1)
                    {
                        playAudioFile();
                    }

                    else if (navCurrentSelected == 2)
                    {
                        playAudioFileUploaded();
                    }

                    else if (navCurrentSelected == 3)
                    {
                        playAudioFile();
                    }
                }
            });


            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (navCurrentSelected != 2)
                    {
                        uploadAudio();
                    }
                    else
                    {
                        downloadAudio();
                    }
                }
            });


            //mRecyclerView.scrollToPosition(touchedPosition);

            isExpanded = true;

            populateTextBox(view);

            // get the name of the touched file and find the audio and text for it.
        } else {
            params.height = 168;
            params.width = layout.getWidth();
            layout.setLayoutParams(params);
            //card.setLayoutParams(new LinearLayout.LayoutParams(CardView.LayoutParams.MATCH_PARENT, 500));
            //card.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT));

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


            //pf.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            isExpanded = false;
        }
    }


    // create a folder when the application is run, if there is no folder.
    private void createFolderIfNoFolder() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d("SDCARD: ", "No SDCARD");
        } else {
            File folderFirstTimePersonalAudio = new File(Environment.getExternalStorageDirectory() + File.separator + "PersonalAudio");
            File folderFirstTimePersonalText = new File(Environment.getExternalStorageDirectory() + File.separator + "PersonalText");

            File folderFirstTimeWorkAudio = new File(Environment.getExternalStorageDirectory() + File.separator + "WorkAudio");
            File folderFirstTimeWorkText = new File(Environment.getExternalStorageDirectory() + File.separator + "WorkText");


            File folderUploadedFirstTime = new File(Environment.getExternalStorageDirectory() + File.separator + "MemosUploaded");
            File folderUploadedTextFileFirstTime = new File(Environment.getExternalStorageDirectory() + File.separator + "MemosUploadedURL");


            if (!(folderFirstTimePersonalAudio.exists())) {
                folderFirstTimePersonalAudio.mkdirs();
                Log.d("PERSONAL_AUDIO", "PERSONAL AUDIO Folder Created");
                Log.d("Audio Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalAudio");

                /*
                if ((folderAudioFirstTime.exists())) {
                    folderTextFirstTime.mkdirs();
                    Log.d("Folder", "Transcription Folder Created");
                    Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MediaRecorderText");
                }
                */
            }

            if (!(folderFirstTimePersonalText.exists())) {
                folderFirstTimePersonalText.mkdirs();
                Log.d("PERSONAL_TEXT", "PERSONAL TEXT Folder Created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText");
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




    // starts the recording
    private void startRecording() {
        /**
         * Start speech to text intent. This opens up Google Speech Recognition API dialog box to listen the speech input.
         * */

        Runnable r = new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        "Speak now...");
                //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 4000);
                //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4000);

                intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
                intent.putExtra("android.speech.extra.GET_AUDIO", true);


                try {
                    startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry! Speech recognition is not supported in this device.",
                            Toast.LENGTH_SHORT).show();
                }

        /*
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(opformats[curFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilePath());


        try {

            /*
            recorder.prepare();
            recorder.start();
            /*
            speech.startListening(recognizerIntent);
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
            intent.putExtra("android.speech.extra.GET_AUDIO", true);


            startActivityForResult(intent, );


            Log.i("RECORDING", "Recording started");

        } catch (Exception e) {
            e.printStackTrace();
        }
        */
            }
        };
        Thread deleteAudioFileThread = new Thread(r);
        deleteAudioFileThread.run();

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

                    String directory_audioFile = null;
                    // write transcription to text file
                    String directory_textFile = null;

                    if (navCurrentSelected == 0)
                    {
                        directory_audioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalAudio";
                        directory_textFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText";

                    }
                    else if (navCurrentSelected == 1)
                    {
                        directory_audioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkAudio";
                        directory_textFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText";
                    }

                    else if (navCurrentSelected == 3)
                    {
                        directory_audioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Audio";
                        directory_textFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text";
                    }


                    try {
                        // get audio extras
                        Uri audioUri = data.getData();
                        ContentResolver contentResolver = getContentResolver();

                        String audioFileName = Long.toString(tempFileName) + fileExtension[curFormat];

                        File file = new File(directory_audioFile, audioFileName);
                        InputStream filestream = contentResolver.openInputStream(audioUri);
                        OutputStream out = new FileOutputStream(file);


                        // RUNNABLE HERE
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


                    String textFileName = Long.toString(tempFileName) + fileExtension[curFormat] + ".txt";
                    File outputFile = new File(directory_textFile, textFileName);

                    //String test = result_text.get(0);

                    try {
                        FileOutputStream stream = new FileOutputStream(outputFile);
                        stream.write(result_text.get(0).getBytes());
                        stream.close();

                        // convert a "Long" to a "String" (current milliseconds to a String, for giving files temporary names)
                        long number = tempFileName;
                        String numberAsString = Long.toString(number);

                        // add audio to list of audio files on the screen along with the selected audio format extension.
                        audioListArrayList.add(0, numberAsString + fileExtension[curFormat]);

                        // refresh the screen and lists the new file
                        refreshRecyclerView();
                        mAdapter.notifyItemRangeChanged(touchedPosition, audioListArrayList.size());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /*
                    try {
                        FileOutputStream fos = new FileOutputStream(outputFile);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(test);
                        fos.close();

                        // convert a "Long" to a "String"
                        long number = tempFileName;
                        String numberAsString = Long.toString(number);
                        lines.add(numberAsString + fileExtension[curFormat]);

                        // refresh the ListView
                        refreshListView();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    */


                }
                break;
            }
        }
    }


    // Updates the ArrayList and refreshes the ListView to reflect the changes.
    /*
    private void listDirectoryFiles() {
        // convert a "Long" to a "String"
        long number = tempFileName;
        String numberAsString = Long.toString(number);
        lines.add(numberAsString + fileExtension[curFormat]);

        // refresh the ListView
        refreshRecyclerView();
    }
    */


    // refreshes the ListView
    private void refreshRecyclerView() {
        // refresh the listView
        mAdapter.notifyDataSetChanged();


    }











    /*
    private void longPressDialogBox() {
        // create a dialog box asking the user if they would like to rename the recording.
        android.app.AlertDialog.Builder sendReportBox = new android.app.AlertDialog.Builder(this);
        sendReportBox.setTitle(tempString);
        sendReportBox.setMessage("What would you like to do with this memo?");
        sendReportBox.setCancelable(true);

        sendReportBox.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity.this, "Delete selected", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        deleteAudioDialog();
                    }
                });

        sendReportBox.setNegativeButton(
                "Rename",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity.this, "Rename selected", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        renameRecordingDialogTextField();
                    }
                });

        sendReportBox.setNeutralButton("Upload",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        //context.startActivity(new Intent(context, Setup.class));
                        //dialog.cancel();
                    }
                });

        android.app.AlertDialog dialogReportBox = sendReportBox.create();
        dialogReportBox.show();
        dialogReportBox.getButton(dialogReportBox.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialogReportBox.getButton(dialogReportBox.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY);
        dialogReportBox.getButton(dialogReportBox.BUTTON_NEUTRAL).setTextColor(Color.BLUE);

    }
    */


    // dialog box with TextField to take new name of file.
    private void renameRecordingDialogTextField() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle(tempString);

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Enter New Title Here");

        builder.setView(input);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); // show keyboard

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tempTextfield = input.getText().toString();
                renameRecording();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); // hide keyboard

            }
        });


        //builder.show();

        android.app.AlertDialog dialogReportBox = builder.create();
        dialogReportBox.show();
        dialogReportBox.getButton(dialogReportBox.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialogReportBox.getButton(dialogReportBox.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }


    // ** CHECK FOR DUPLICATES. IF USER TRIES TO NAME FILE WITH A NAME THAT ALREADY EXISTS, STOP THEM.**

    // searches for the file to be renamed in the folder and renames it
    private void renameRecording() {
        // save name of file in temporary string (taken care of with global variable "tempString"
        // go through the folder looking for a file that matches temporary string

        Log.i("tempTextfield", tempTextfield);

        String renameRecordingFilePath = null;
        String renameTextFilePath = null;

        if (navCurrentSelected == 0)
        {
            renameRecordingFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalAudio";
            renameTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText";
        }

        else if (navCurrentSelected == 1)
        {
            renameRecordingFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkAudio";
            renameTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText";
        }

        else if (navCurrentSelected == 3)
        {
            renameRecordingFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Audio";
            renameTextFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text";
        }


        Log.d("AUDIOFILES", "Current Audio Path: " + renameRecordingFilePath);
        Log.d("TEXTFILES", "Current Text Path: " + renameTextFilePath);


        File f1 = new File(renameRecordingFilePath);
        File audio_files[] = f1.listFiles();
        Log.d("AudioFiles", "Size: " + audio_files.length); // Log for current number of audio files in the folder
        File rename_audio = new File(renameRecordingFilePath, tempTextfield + fileExtension[curFormat]);


        File f2 = new File(renameTextFilePath);
        File text_files[] = f2.listFiles();
        Log.d("TextFiles", "Size: " + text_files.length); // Log for current number of text files in the folder
        File rename_textFile = new File(renameTextFilePath, tempTextfield + fileExtension[curFormat] + ".txt");


        // go through list of audiofiles, find and rename the right one.
        for (int i = 0; i < audio_files.length; i++) {
            if (audio_files[i].getName().equals(tempString)) {

                //renameTo goes here.
                boolean renamed = audio_files[i].renameTo(rename_audio);


                // refresh listView now (reflect changes on the screen)


            }
        }


        // go through list of textfiles, find and rename the right one.
        for (int i = 0; i < text_files.length; i++) {
            if (text_files[i].getName().equals(tempString + ".txt")) {

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
                //mAdapter.notifyItemRemoved(touchedPosition);

                refreshRecyclerView();


                // look for a file named that
                // reset the textbox to the contents of that file


                // finish everything before refreshing ListView (rename audio and text files.

            }
        }


    }


    // dialog box asking "Would you like to delete this memo?"
    private void deleteAudioDialog() {
        //AlertDialog.Builder build = new AlertDialog.Builder(this);

        android.app.AlertDialog.Builder sendReportBox = new android.app.AlertDialog.Builder(MainActivity.this);
        sendReportBox.setTitle("Delete Memo");
        sendReportBox.setMessage("Are you sure you want to delete this memo?");
        sendReportBox.setIcon(android.R.drawable.ic_dialog_alert);
        sendReportBox.setCancelable(true);


        sendReportBox.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Toast.makeText(MainActivity.this, "Delete selected", Toast.LENGTH_LONG).show();
                        deleteAudioFile();
                        dialog.cancel();
                    }
                });

        sendReportBox.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Toast.makeText(MainActivity.this, "Cancel selected", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog dialogReportBox = sendReportBox.create();
        dialogReportBox.show();
        dialogReportBox.getButton(dialogReportBox.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialogReportBox.getButton(dialogReportBox.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

    }


    // called if "Yes" is selected after "deleteAudioDialog"
    private void deleteAudioFile() {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                String deleteRecordingFilePath = null;
                String deleteTextFilePath = null;

                if (navCurrentSelected == 0)
                {
                    deleteRecordingFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalAudio";
                    deleteTextFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalText";
                }

                else if (navCurrentSelected == 1)
                {
                    deleteRecordingFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkAudio";
                    deleteTextFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkText";
                }

                else if (navCurrentSelected == 3)
                {
                    deleteRecordingFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Audio";
                    deleteTextFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Text";
                }

                Log.d("Files", "Path: " + deleteRecordingFilePath);
                Log.d("Files", "Path: " + deleteTextFilePath);


                File f1 = new File(deleteRecordingFilePath);
                File audio_files[] = f1.listFiles();


                Log.d("Files", "Size: " + audio_files.length); // current number of audio files in the folder


                File f2 = new File(deleteTextFilePath);
                File text_files[] = f2.listFiles();


                Log.d("Files", "Size: " + text_files.length); // current number of text files in the folder


                for (int i = 0; i < audio_files.length; i++) {
                    if (audio_files[i].getName().equals(tempString)) {
                        boolean deleted = audio_files[i].delete();

                    }
                }


                for (int i = 0; i < text_files.length; i++) {
                    if (text_files[i].getName().equals(tempString + ".txt")) {
                        boolean deleted = text_files[i].delete();


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
        };
        Thread deleteAudioFileThread = new Thread(r);
        deleteAudioFileThread.run();
    }


    public void onPause() {
        super.onPause();
        Log.i("PAUSE", "onPause() called");
        //stopRecording();
    }


    public void onStop() {
        super.onStop();
        Log.i("STOP", "onStop() called");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        //stopRecording();
    }


    // NOT USED. MOVED STUFF TO START RECORDING
    public void speechToTextServiceStart() {
        /*
        speech.setRecognitionListener(MainActivity.this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        */
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);


        /*
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                Locale.getDefault());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        };
        mSpeechRecognizer.setRecognitionListener(listener);
        */

    }


























    /*
    // handle result of speech recognition
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // the resulting text is in the getExtras:
        Bundle bundle = data.getExtras();
        ArrayList<String> matches = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
        // the recording url is in getData:
        Uri audioUri = data.getData();
        ContentResolver contentResolver = getContentResolver();

        try {
            InputStream filestream = contentResolver.openInputStream(audioUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // TODO: read audio file from inputstream
    }
    */


    // NO LONGER A PART OF THE CODE. WILL DELETE LATER.
    // stops the recording and frees the MediaRecorder
    private void stopRecording() {
        /*
        if (recorder != null)
        {

            if (speech != null);
            {
                speech.destroy();
                speech.stopListening();

            }


            //Toast.makeText(this, "Memo recorded successfully", Toast.LENGTH_SHORT).show();
            //speech.stopListening();
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        */

        // CREATE TEXT FILE OF RECORDING AND SAVE TO "MEDIARECORDERTEXT"
        File filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MediaRecorderText");

        String textFileName = Long.toString(tempFileName) + fileExtension[curFormat] + ".txt";
        File outputFile = new File(filePath, textFileName);

        // write global ArrayList "transcriptBuilder" to txt file
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(transcriptionBuilder);
            fos.close();
            //transcriptionBuilder.clear();


        } catch (Exception e) {
            e.printStackTrace();
        }


        // GET AUDIO FILE BACK FROM API


        Log.i("MICROPHONE", "Recording stopped");
        //listDirectoryFiles();

        /*
        if (m != null)
        {
            m.stop();
            m.reset();
            m.release();
            m = null;
            Log.i("MEDIAPLAYER", "Audio stopped");
        }*/

    }


    private void writeTranscriptionToTextFile() {


    }


    private void playAudioFileUploaded()
    {

        final Uri[] myUri = {null};

        Runnable r = new Runnable() {
            @Override
            public void run() {
                int lineCount = 0;

                try {

                    // get touched position
                    // open URL file
                    // read specific line URL and use that as the audio DataSource

                    //File folderUploadedTextFileFirstTime = new File(Environment.getExternalStorageDirectory() + File.separator + "MediaRecorderUploadedURL");

                    String sdcard = Environment.getExternalStorageDirectory() + File.separator + "MemosUploadedURL";

                    File file = new File(sdcard, "Uploaded Files URL.txt");


                    StringBuilder text = new StringBuilder();

                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        if (lineCount == touchedPosition) {
                            text.append(line);
                            text.append('\n');

                            // convert string to URL and set as play source
                            Log.i("THE_URL", line);
                            Log.i("THE_LINE_NO", String.valueOf(lineCount));
                            Log.i("TOUCHED POSITION CONF.", String.valueOf(touchedPosition));

                            myUri[0] = Uri.parse(line);
                        } else {
                            lineCount++;
                        }

                    }
                    br.close();

                    //Toast.makeText(MainActivity.this, String.valueOf(lineCount),Toast.LENGTH_LONG).show();


                    //memoSeekBar.setProgress(0);
                    //mDuration = m.getDuration();


                    // seekbar stuff goes here
                    //memoSeekBar1.setMax(mDuration);

                    //Handler mHandler = new Handler();
                    //inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    //final View vi = inflater.inflate(R.layout.recyclerview_item, null); //recyclerview_item.xml is your file.




            /*
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (m.isPlaying())
                    {
                        int currentPosition = 0;
                        int total = m.getDuration();
                        memoSeekBar1.setMax(total);

                        while (m.isPlaying() && currentPosition < total)
                        {
                            try {
                                Thread.sleep(1000);
                                //Log.i("SEEKBAR", Integer.toString(currentPosition));
                                currentPosition = m.getCurrentPosition();

                            } catch (Exception e) {
                                return;
                            }
                            memoSeekBar1.setProgress(currentPosition);
                            Log.i("SEEKBAR_MOVED", "MOVED TO " + currentPosition);
                        }
                    }
                }
            };
            //mHandler.postDelayed(r, 10);
            Thread playerThread = new Thread(r);
            playerThread.run();
            */

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        Thread playerThread = new Thread(r);
        playerThread.run();


        //final SeekBar memoSeekBar1 = new SeekBar(this);


        // start other thread here

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
                            //View inflater
                            // use that to find the seekbar


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

                    Log.i("PLAY_CLOUD_AUDIO", "Playing Cloud Audio");

                    // check if MediaPlayer (audio) is done.
                    m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer m) {
                            playButton.setImageResource(R.drawable.ic_play_icon);
                            m.pause();
                            //m.stop();
                            //m.reset();
                            //m.release();
                            //memoSeekBar.setProgress(0);
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

    // for playing the selected audio file in the ListView
    private void playAudioFile() {
        // Runnable for MediaPlayer
        int mDuration;
        //final SeekBar memoSeekBar1 = new SeekBar(this);

        try {

            final MediaPlayer m = new MediaPlayer();

            try {

                if (navCurrentSelected == 0)
                {
                    m.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalAudio" + File.separator + tempString);
                }

                else if (navCurrentSelected == 1)
                {
                    m.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkAudio" + File.separator + tempString);
                }

                else if (navCurrentSelected == 3)
                {
                    m.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Audio" + File.separator + tempString);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                m.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //memoSeekBar.setProgress(0);
            //mDuration = m.getDuration();


            // seekbar stuff goes here
            //memoSeekBar1.setMax(mDuration);

            //Handler mHandler = new Handler();
            //inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //final View vi = inflater.inflate(R.layout.recyclerview_item, null); //recyclerview_item.xml is your file.

            m.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {
                    final Handler seekHandler = new Handler();
                    playButton.setImageResource(R.drawable.ic_stop_icon);
                    //View inflater
                    // use that to find the seekbar


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




            /*
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (m.isPlaying())
                    {
                        int currentPosition = 0;
                        int total = m.getDuration();
                        memoSeekBar1.setMax(total);

                        while (m.isPlaying() && currentPosition < total)
                        {
                            try {
                                Thread.sleep(1000);
                                //Log.i("SEEKBAR", Integer.toString(currentPosition));
                                currentPosition = m.getCurrentPosition();

                            } catch (Exception e) {
                                return;
                            }
                            memoSeekBar1.setProgress(currentPosition);
                            Log.i("SEEKBAR_MOVED", "MOVED TO " + currentPosition);
                        }
                    }
                }
            };
            //mHandler.postDelayed(r, 10);
            Thread playerThread = new Thread(r);
            playerThread.run();
            */


            Log.i("PLAY_AUDIO", "Playing Audio");

            // check if MediaPlayer (audio) is done.
            m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer m) {
                    playButton.setImageResource(R.drawable.ic_play_icon);
                    m.pause();
                    //m.stop();
                    //m.reset();
                    //m.release();
                    //memoSeekBar.setProgress(0);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // get the file path
    private String getFilePath() {
        tempFileName = System.currentTimeMillis();
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        File folder = new File(filePath, "MediaRecorderSample"); // create folder for files
        Log.i("FILEPATH_SET", folder.toString());

        // if "MediaRecorderSample" folder does not exist, create one...
        if (!folder.exists()) {
            folder.mkdirs();
        }

        //...and add the audio file.
        return (folder.getAbsolutePath() + "/" + tempFileName + fileExtension[curFormat]);
    }


























    /*
    // for choosing a recording format (MP3 or 3GPP)
    private void formatDialogBox()
    {
        Log.i("FORMAT", "Format called");
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        String formats[] = {"MP3", "3GPP"};

        build.setTitle("Choose a format");
        build.setSingleChoiceItems(formats, curFormat, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                curFormat = which;
                dialog.dismiss();
            }
        });
        build.show();
    }
    */


    // **** RECYCLERVIEW STUFF FROM HERE ONWARD ****


    private void seekBarUpdate(final MediaPlayer m) {
        //inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View vi = inflater.inflate(R.layout.recyclerview_item, null); //recyclerview_item.xml is your file.


        /*
        final Handler mHandler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(m.isPlaying()){
                    int mCurrentPosition = m.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                //mHandler.postDelayed(this, 1000);
            }
        });
        */
        /*
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if (!(amoungToupdate * memoSeekBar.getProgress() >= duration))
                        {
                            int p = memoSeekBar.getProgress();
                            p += 1;
                            memoSeekBar.setProgress(p);
                        }
                    }
                });
            };
        }, amoungToupdate);
        */
    }


    // same as listDirectoryFilesStart()
    private ArrayList<String> getDataSet() {

        try {

            String filePath = null;
            String filePath2 = null;


            if (navCurrentSelected == 0)
            {
                filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalAudio"; // a folder in directory called "MediaRecorderSample"
                filePath2 = Environment.getExternalStorageDirectory().getPath() + File.separator + "PersonalText";
            }

            else if (navCurrentSelected == 1)
            {
                filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkAudio"; // a folder in directory called "MediaRecorderSample"
                filePath2 = Environment.getExternalStorageDirectory().getPath() + File.separator + "WorkText";

            }

            else if (navCurrentSelected == 3)
            {
                filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Audio"; // a folder in directory called "MediaRecorderSample"
                filePath2 = Environment.getExternalStorageDirectory().getPath() + File.separator + categoryName + "Text";
            }
            Log.d("Files", "Path: " + filePath); // put file path in Log (to be sure of file path)

            File f = new File(filePath);
            File f2 = new File(filePath2);


            File file[] = f.listFiles(); // array of files
            Log.d("Files", "Size: " + file.length); // current number of files in the folder

            File file2[] = f2.listFiles(); // array of files
            Log.d("Files", "Size: " + file2.length); // current number of files in the folder


            // populate items in RecyclerView
            for (int index = 0; index < file.length; index++) {
                String textForTextView = file[index].getName() + ".txt";

                inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View vi = inflater.inflate(R.layout.recyclerview_item, null); //recyclerview_item.xml is your file.


                audioListArrayList.add(file[index].getName());
                TextView memoTitle = (TextView) vi.findViewById(R.id.memo_title); //get a reference to the textview on the recyclerview_item.xml file.
                memoTitle.setText(file[index].getName());


                StringBuilder text = new StringBuilder();


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

                TextView memoPreview = (TextView) vi.findViewById(R.id.memo_preview);

                String preview = "testing";

                previewTextToPass = text.toString();

                //String test = "test";
                memoPreview.setText(previewTextToPass);
                Log.i("PREVIEW", text.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return audioListArrayList;
    }
    //Log.i("COUNT", "Number of files set: " + count);


    /*
     for (int index = 0; index < file2.length; index++)
            {
                TextView memoPreview = (TextView)vi.findViewById(R.id.memo_preview);
                StringBuilder text = new StringBuilder();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(filePath2 + File.separator + file2[index]));
                    String line;

                    while ((line = br.readLine()) != null)
                    {
                        text.append(line);
                    }

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                memoPreview.setText(text);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return audioListArrayList;
    }
     */


    public void playAudio(View view) {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                final MediaPlayer m = new MediaPlayer();

                try {
                    m.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MediaRecorderSample" + File.separator + tempString);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    m.prepare();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //audio_progress.setMax(m.getDuration());
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        m.start();
                        Log.i("PLAY_AUDIO", "Playing Audio");

                        // check if audio is done. If it is, release the mediaplayer automatically.
                        m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer m) {
                                m.stop();
                                m.reset();
                                m.release();

                                Log.i("ONCOMPLETION_AUDIO", "Audio Completed");
                            }
                        });
                    }
                });


                /*
                stopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (m.isPlaying())
                        {
                            m.stop();
                            m.reset();
                            m.release();

                            Log.i("STOPPED_AUDIO", "Audio Stopped");
                        }
                    }
                });
                */

                //playButton.setEnabled(false);


                // check if MediaPlayer (audio) is done.
            }
        };
        Thread playerThread = new Thread(r);
        playerThread.run();
    }


    public void populateTextBox(final View view) {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                StringBuilder text = new StringBuilder();

                File filePath;

                TextView fullTranscription = (TextView) view.findViewById(R.id.transcription_full);




                if (navCurrentSelected == 0)
                {
                    filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText" + File.separator + tempString + ".txt");

                    try {
                        BufferedReader br = new BufferedReader(new FileReader(filePath));
                        String line;

                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            //text.append('\n');
                        }
                        fullTranscription.setText(text);

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }





                else if (navCurrentSelected == 1)
                {
                    filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText" + File.separator + tempString + ".txt");

                    try {
                        BufferedReader br = new BufferedReader(new FileReader(filePath));
                        String line;

                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            //text.append('\n');
                        }
                        fullTranscription.setText(text);

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }





                else if (navCurrentSelected == 2) // UPLOADED SECTION SELECTED
                {
                    // fullTranscription.setText("Unable to display uploaded memos. Please download memo to view.");

                    // read txt file
                    // turn string into URL
                    // use that URL to find file and populate yellow textview

                    URL textURL;
                    URLConnection conn;

                    filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MemosUploadedURL" + File.separator + "Uploaded Files URL.txt");

                    try {
                        // Create a URL for the desired page

                        BufferedReader br = new BufferedReader(new FileReader(filePath));
                        String line;

                        int lineCount = 0;

                        while ((line = br.readLine()) != null) {
                            //touched position check here

                            if (lineCount == touchedPosition)
                            {
                                //text.append(line);
                                //text.append('\n');

                                // convert string to URL and set as textfile source
                                textURL = new URL(line);
                                conn = textURL.openConnection();
                                conn.connect();



                                // open url with text file
                                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                String str;

                                while ((str = in.readLine()) != null)
                                {
                                    // str is one line of text; readLine() strips the newline character(s)
                                    text.append(str);

                                }
                                //text.append('\n');

                            }

                            else
                            {
                                lineCount++;
                            }

                        }


                        fullTranscription.setText(text.toString());
                        Log.i("FULL_TRANSCRIPTION_TEXT", text.toString());


                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                else if (navCurrentSelected == 3)
                {
                    filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text" + File.separator + tempString + ".txt");

                    try {
                        BufferedReader br = new BufferedReader(new FileReader(filePath));
                        String line;

                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            //text.append('\n');
                        }
                        fullTranscription.setText(text);

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

            }
        };
        Thread playerThread = new Thread(r);
        playerThread.run();

        //Log.i("TEXTFILES", filePath.listFiles());

    }

    public void populateTextBoxUploaded()
    {
        // access online txt file. Read contents into
    }

    public void refreshTextBoxAfterRename(final View view) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String mp3ExtensionAdd = tempTextfield + ".mp3";

                File filePath = null;

                if (navCurrentSelected == 0)
                {
                    filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText" + File.separator + mp3ExtensionAdd + ".txt");
                }

                else if (navCurrentSelected == 1)
                {
                    filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText" + File.separator + mp3ExtensionAdd + ".txt");

                }

                else if (navCurrentSelected == 3)
                {
                    filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text" + File.separator + mp3ExtensionAdd + ".txt");

                }

                StringBuilder text = new StringBuilder();


                try {
                    BufferedReader br = new BufferedReader(new FileReader(filePath));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        //text.append('\n');
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Couldn't display memo", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                //TextView memoTitleRename = (TextView) view.findViewById(R.id.memo_title);
                TextView fullTranscription = (TextView) view.findViewById(R.id.transcription_full);

                //memoTitleRename.setText(mp3ExtensionAdd);
                fullTranscription.setText(text);
            }
        };
        Thread playerThread = new Thread(r);
        playerThread.run();

        //Log.i("TEXTFILES", filePath.listFiles());

    }

    // find both files (DONE)
    // rename them using the String tempTextField (DONE)
    // look for a file mp3Extension add
    // if found, open that file and read its contents into the yellow textbox
    // set memoTitle to the mp3ExtensionAdd


    private void permissionsGranted(View vi) {
        //inflater = (LayoutInflater)MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View vi = inflater.inflate(R.layout.activity_main, null);

        if (checkPermission()) {

            //Snackbar.make(vi, "Permission already granted.", Snackbar.LENGTH_LONG).show();
            //Toast.makeText(MainActivity.this, "Permissions already granted", Toast.LENGTH_LONG).show();
        } else {
            Snackbar.make(vi, "Please allow permissions.", Snackbar.LENGTH_LONG).show();
            //Toast.makeText(MainActivity.this, "Please request permission.", Toast.LENGTH_LONG).show();
        }

        if (!checkPermission()) {

            requestPermission();

        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);


        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.i("NAV_ID", String.valueOf(id));

        if (id == R.id.nav_personal) {

            if (navCurrentSelected == 0)
            {
                Toast.makeText(MainActivity.this, "'Personal' Already Selected", Toast.LENGTH_SHORT).show();
            }

            else
            {
                navCurrentSelected = 0;

                audioListArrayList.clear();

                mAdapter = new MyRecyclerViewAdapter(getDataSet());
                mRecyclerView.setAdapter(mAdapter);

                // Handle the camera action
                mRecyclerView.invalidate();
                refreshRecyclerView();
            }

        }

        else if (id == R.id.nav_work)
        {
            if (navCurrentSelected == 1)
            {
                Toast.makeText(MainActivity.this, "'Work' Already Selected", Toast.LENGTH_SHORT).show();
            }

            else
            {
                navCurrentSelected = 1;

                audioListArrayList.clear();

                mAdapter = new MyRecyclerViewAdapter(getDataSet());
                mRecyclerView.setAdapter(mAdapter);

                // Handle the camera action
                mRecyclerView.invalidate();
                refreshRecyclerView();

                // CLEAR ARRAYLIST HERE
            }
        }

        else if (id == R.id.nav_uploaded) {

            if (navCurrentSelected == 2)
            {
                Toast.makeText(MainActivity.this, "'Uploaded' Already Selected", Toast.LENGTH_SHORT).show();
            }

            else
            {
                navCurrentSelected = 2;

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

            /*
            Menu m = navigationView.getMenu();

            for (int i = 0; i < m.size(); i++)
            {
                Log.i("MENU", m.getItem(i).toString());
            }

            m.add(R.id.nav_view, Menu.NONE, 1, "Item2");
            */

            // dialog box to add new category
            addNewCategory();

            /*
            Random r = new Random();
            int i = r.nextInt(100);
            MenuItem myMoveGroupItem = navigationView.getMenu().getItem(2);
            // MenuItem myMoveGroupItem = navigationView.getMenu().findItem(R.id.submenu_1);  -- it also works!
            SubMenu subMenu = myMoveGroupItem.getSubMenu();
            subMenu.add("Item "+ i);
            */

            /*
            for (int in = 0; in < subMenu.size(); in++)
            {
                Log.i("MENU", subMenu.getItem(in).toString());
            }
            */


            //m = navView.getMenu;
            //MenuItem mi = m.getItem(m.size()-1);
            //mi.setTitle(mi.getTitle());
        }

        else if (id % 10 == 0)
        {
            navCurrentSelected = 3;

            categoryName = item.toString();
            // get name of that category
            Toast.makeText(MainActivity.this, item.toString(), Toast.LENGTH_LONG).show();

            navigationView.getMenu().getItem(2).setChecked(true);


            audioListArrayList.clear();

            mAdapter = new MyRecyclerViewAdapter(getDataSet());
            mRecyclerView.setAdapter(mAdapter);

            // Handle the camera action
            mRecyclerView.invalidate();
            refreshRecyclerView();

            //navigationView.getMenu().getItem(2)



            // get name of category.
            // attach "audio" to it and use that to find a folder.
            // do the same with text.
        }
        /*
        else if (id == R.id.nav_share) {

        }
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }






    public void addNewCategory()
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {

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
                        tempTextfield = input.getText().toString();

                /*
                Random r = new Random();
                int i = r.nextInt(1000000000);
                */

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



                        // save navigation id to SharedPreferences
                        // save name to SharedPreferences
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); // hide keyboard

                    }
                });


                //builder.show();

                android.app.AlertDialog dialogReportBox = builder.create();
                dialogReportBox.show();
                dialogReportBox.getButton(dialogReportBox.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialogReportBox.getButton(dialogReportBox.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        };
        Thread addNewCategoryThread = new Thread(r);
        addNewCategoryThread.run();
    }






    public int getVariable()
    {
        return myVariable;
    }







    public void createFolderNewCategory(String newCategoryName)
    {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d("SDCARD: ", "No SDCARD");
        } else {
            File folderFirstTimeNewCategoryAudio = new File(Environment.getExternalStorageDirectory() + File.separator + tempTextfield + "Audio");
            File folderFirstTimeNewCategoryText = new File(Environment.getExternalStorageDirectory() + File.separator + tempTextfield + "Text");


            if (!(folderFirstTimeNewCategoryAudio.exists())) {
                folderFirstTimeNewCategoryAudio.mkdirs();
                Log.d("NEW_CATEGORY_AUDIO", tempTextfield + "AUDIO Folder Created");
                Log.d("Audio Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + tempTextfield + "Audio");

                /*
                if ((folderAudioFirstTime.exists())) {
                    folderTextFirstTime.mkdirs();
                    Log.d("Folder", "Transcription Folder Created");
                    Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MediaRecorderText");
                }
                */
            }

            if (!(folderFirstTimeNewCategoryText.exists())) {
                folderFirstTimeNewCategoryText.mkdirs();
                Log.d("NEW_CATEGORY_TEXT", tempTextfield + "TEXT Folder Created");
                Log.d("Folder Directory", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + tempTextfield + "Text");
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





    private void uploadAudio()
    {
        android.app.AlertDialog.Builder sendReportBox = new android.app.AlertDialog.Builder(MainActivity.this);
        sendReportBox.setTitle("Upload Memo");
        sendReportBox.setMessage("Are you sure you want to upload this memo?");
        sendReportBox.setCancelable(true);


        sendReportBox.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();


                        String audioToUploadFilePath = null;
                        String textToUploadFilePath = null;


                        if (navCurrentSelected == 0)
                        {
                            audioToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalAudio" + File.separator + tempString;
                            textToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PersonalText" + File.separator + tempString + ".txt";

                        }

                        else if (navCurrentSelected == 1)
                        {
                            audioToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkAudio" + File.separator + tempString;
                            textToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WorkText" + File.separator + tempString + ".txt";

                        }

                        else if (navCurrentSelected == 3)
                        {
                            audioToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Audio" + File.separator + tempString;
                            textToUploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + categoryName + "Text" + File.separator + tempString + ".txt";

                        }


                        //go through the arraylist until you find a file tha
                        // call delete on both files after



                        mProgress = new ProgressDialog(MainActivity.this);
                        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        mProgress.setCancelable(false);
                        mProgress.setTitle("Please Wait..");
                        mProgress.setMessage("Uploading...");

                        mProgress.show();


                        StorageReference filepathAudio = mStorage.child("UploadedAudio").child(tempString);

                        StorageReference filepathText = mStorage.child("UploadedText").child(tempString + ".txt");


                        StorageMetadata metadataAudio = new StorageMetadata.Builder()
                                .setCustomMetadata(tempString, null)
                                .build();

                        StorageMetadata metadataText = new StorageMetadata.Builder()
                                .setCustomMetadata(tempString + ".txt", null)
                                .build();


                        //mFileName is the directory of the file that needs to be uploaded
                        final Uri uriAudio = Uri.fromFile(new File(audioToUploadFilePath));
                        Uri uriText = Uri.fromFile(new File(textToUploadFilePath));

                        filepathAudio.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final String fileUriAudio = taskSnapshot.getDownloadUrl().toString();
                                Log.i("URI_TEST", fileUriAudio);

                                Runnable r = new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            File folderUploadedTextFile = new File(Environment.getExternalStorageDirectory() + File.separator + "MemosUploaded");
                                            if (!folderUploadedTextFile.exists()) {
                                                folderUploadedTextFile.mkdirs();
                                            }

                                            File folderUploadedTextFileFirstTime = new File(Environment.getExternalStorageDirectory() + File.separator + "MemosUploadedURL");
                                            if (!folderUploadedTextFileFirstTime.exists()) {
                                                folderUploadedTextFileFirstTime.mkdirs();
                                            }

                                            File uploadedTxt = new File(folderUploadedTextFile, "Uploaded Files.txt");
                                            FileWriter writer1 = new FileWriter(uploadedTxt, true);
                                            writer1.append(tempString);
                                            writer1.append("\n");
                                            writer1.flush();
                                            writer1.close();
                                            Toast.makeText(MainActivity.this, "Saved to Uploaded Files", Toast.LENGTH_LONG).show();

                                            File uploadedAudioURL = new File(folderUploadedTextFileFirstTime, "Uploaded Files URL.txt");
                                            FileWriter writer2 = new FileWriter(uploadedAudioURL);
                                            writer2.append(fileUriAudio);
                                            writer2.flush();
                                            writer2.close();
                                            Toast.makeText(MainActivity.this, "Saved to Uploaded Files URL", Toast.LENGTH_LONG).show();

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


                        /*
                        filepathAudio.updateMetadata(metadataAudio)
                                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        // Updated metadata is in storageMetadata
                                        Toast.makeText(MainActivity.this, "Audio Metadata added.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Toast.makeText(MainActivity.this, "Unable to write audio metadata.", Toast.LENGTH_LONG).show();
                                    }
                                });



                        filepathAudio.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                Log.i("METADATA_TEST_AUDIO", storageMetadata.toString());
                            }
                        });
                        */


                        filepathText.putFile(uriText).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                final String fileUriText = taskSnapshot.getDownloadUrl().toString();

                                Toast.makeText(MainActivity.this, "Memo Uploaded", Toast.LENGTH_SHORT).show();
                                Log.i("URI_AUDIO", uriAudio.toString());
                                mProgress.dismiss();
                                deleteAudioFile();

                                File folderUploadedTextFileFirstTime = new File(Environment.getExternalStorageDirectory() + File.separator + "MemosUploadedURL");

                                try {
                                    File uploadedTextURL = new File(folderUploadedTextFileFirstTime, "Uploaded Files Transcript URLs.txt");
                                    FileWriter writer3 = new FileWriter(uploadedTextURL, true);
                                    writer3.append(fileUriText);
                                    writer3.append("\n");
                                    writer3.flush();
                                    writer3.close();
                                    Toast.makeText(MainActivity.this, "Saved to Uploaded Files Transcript URLs.txt", Toast.LENGTH_LONG).show();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        /*

                        filepathText.updateMetadata(metadataText)
                                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        // Updated metadata is in storageMetadata
                                        Toast.makeText(MainActivity.this, "Text Metadata added.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Toast.makeText(MainActivity.this, "Unable to write text metadata.", Toast.LENGTH_LONG).show();
                                    }
                                });

                        filepathText.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                Log.i("METADATA_TEST_TEXT", storageMetadata.toString());
                            }
                        });

                        Log.i("textMetadata", filepathAudio.getName());
                        */


                        // textfile to save audio file names
                        // find files in database with contents of txt file.
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




    private ArrayList<String> getDataSetUploaded() {
        // refresh adapter with stuff in Firebase

        // check txt file with names of uploaded files.
        // set memoTitle to the file names in the txt file.
        // when the user expands a card, the URL to play the audio should be completed with tempString (the name of the recording the user touched)


        // open the file with the uploaded file names. Load them into the adapter arraylist.
        // refresh the adapter with those contents.

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {

                    File folderUploadedTextFileFirstTime = new File(Environment.getExternalStorageDirectory() + File.separator + "MemosUploaded");

                    File textFile = new File(folderUploadedTextFileFirstTime, "Uploaded Files.txt");

                    BufferedReader br = new BufferedReader(new FileReader(textFile));
                    String line;

                    while ((line = br.readLine()) != null) {
                        audioListArrayListUploaded.add(line);
                    }
                    br.close();

                    Log.i("Uploaded Files", audioListArrayListUploaded.toString());


                    // now load the contents (filenames) into memotitle

                    for (int i = 0; i < audioListArrayListUploaded.size(); i++)
                    {
                        View vi = inflater.inflate(R.layout.recyclerview_item, null); //recyclerview_item.xml is your file.
                        TextView memoTitle = (TextView) vi.findViewById(R.id.memo_title); //get a reference to the textview on the recyclerview_item.xml file.
                        memoTitle.setText(audioListArrayListUploaded.get(i));
                        memoTitle.setTextColor(Color.BLUE);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread getDataSetUploadedThread = new Thread(r);
        getDataSetUploadedThread.run();

        return audioListArrayListUploaded;
    }



    public void downloadAudio()
    {
        Toast.makeText(MainActivity.this, "Download Button Touched", Toast.LENGTH_LONG).show();

        // go through text file with filenames
        // find which line the filename was found on

        // open file with URL of audio to "Personal". Download that file.
        // open file with URL of text to "Personal". Download that file.

        // Or use dialog box to allow user to select where they want file to be downloaded.

        //StorageReference filepath = mStorage.child("UploadedAudio").child("new_audio.3gp");

        /*
        StorageReference filepathAudio = mStorage.child("UploadedAudio").child(tempString);

        StorageReference filepathText = mStorage.child("UploadedText").child(tempString + ".txt");

        //File localFile = File.createTempFile("audioFileDownloadTest", "3gp");
        File localFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "therecording2.3gp");


        filepath.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Download Complete", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, "Sorry. Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }



    // ABILITY TO APPEND TO TEXT FILES USED TO FIND FILES, NOT OVERWRITE.
    // ADD ABILITY TO CREATE MULTIPLE CATEGORIES (NOT JUST ONE, LIKE RIGHT NOW).
    // ADD IN LOCATION STUFF.
    // ADD TRANSCRIPTION PREVIEW FOR MEMOS.


}
