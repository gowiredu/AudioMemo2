package com.gowiredu.audiomemotest2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gowiredu.audiomemo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static android.R.attr.lines;
import static com.gowiredu.audiomemo.R.id.audio_progress;

public class TestActivity extends AppCompatActivity {

    private TextView memoTitle;
    private TextView fullTranscription;
    SeekBar audio_progress;
    Button playButton;
    Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        audio_progress = (SeekBar) findViewById(R.id.audio_progress);
        fullTranscription = (TextView) findViewById(R.id.transcription_full);
        playButton = (Button) findViewById(R.id.play_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        memoTitle = (TextView) findViewById(R.id.memo_title);

        Intent intent = getIntent();
        final String sentFromPrevActivity = intent.getStringExtra("memo_title");

        memoTitle.setText(sentFromPrevActivity);

        populateTextBox();

        //lookForMatchingTxt(textForTextView, textFileArray, filePath);

        stopButton.setEnabled(false);
        playButton.setEnabled(true);


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the name of recording at the current index (the contents of textView1)
                // pass the contents of textView1 to the global variable tempString
                // call "playAudio"
                // Runnable for MediaPlayer

                playAudio();


                /*
                final Handler mHandler = new Handler();

                // Update Seekbar on UI thread
                TestActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(m != null){
                            int mCurrentPosition = m.getCurrentPosition() / 1000;
                            audio_progress.setProgress(mCurrentPosition);
                        }
                        mHandler.postDelayed(this, 1000);
                    }
                });
                */
            }
        });

    }

    // get name of file from header
    // go through the "MediaRecorderText" folder and look for a file like that








    public void playAudio()
    {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                MediaPlayer m = new MediaPlayer();

                try {
                    m.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator + "MediaRecorderSample" + File.separator + memoTitle.getText());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    m.prepare();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //audio_progress.setMax(m.getDuration());
                m.start();
                playButton.setEnabled(false);
                Log.i("PLAY_AUDIO", "Playing Audio");



                // check if MediaPlayer (audio) is done.
                m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer m) {
                        m.stop();
                        m.reset();
                        m.release();
                        playButton.setEnabled(true);
                        stopButton.setEnabled(false);

                        Log.i("ONCOMPLETION_AUDIO", "Audio Completed");
                    }
                });
            }
        };
        Thread playerThread = new Thread(r);
        playerThread.run();
    }









    public void populateTextBox()
    {
        String textForTextView = memoTitle.getText().toString() + ".txt";

        File filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MediaRecorderText" + File.separator + textForTextView);


        //Log.i("TEXTFILES", filePath.listFiles());

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
        fullTranscription.setText(text);
    }

}
