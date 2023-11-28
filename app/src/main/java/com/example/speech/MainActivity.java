package com.example.speech;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    ActivityResultLauncher<String> requestPermissionLauncher;
    private static final int VR_REQUEST=999;
    private ListView wordList;
    private final String LOG_TAG="SpeechRepeatActivity";
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech repeatTTS;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION=200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button speechBtn = findViewById(R.id.speech_btn);
        wordList = findViewById(R.id.word_list);


        ActivityCompat.requestPermissions(this, new String[]{"Manifest.permission.RECORD_AUDIO"},
                REQUEST_RECORD_AUDIO_PERMISSION);


        PackageManager packManager = getPackageManager();
        List<ResolveInfo> intActivities = packManager.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (intActivities.size()!=0)
        {
            speechBtn.setOnClickListener(this);
            speechBtn.setEnabled(true);
       }
        else{
            speechBtn.setEnabled(false);
            Toast.makeText(this, "Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }

        wordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView wordView = (TextView)view;
                String wordChosen = (String)wordView.getText();
                Log.v(LOG_TAG, "chosen:" + wordChosen);
                Toast.makeText(MainActivity.this, "You said:" + wordChosen,Toast.LENGTH_SHORT).show();
                repeatTTS.speak("Вы сказали: " + wordChosen, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

    }

    private void listenToSpeech() {
        Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a word");
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
        startActivityForResult(listenIntent, VR_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode ==VR_REQUEST && resultCode == RESULT_OK){
            ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_main, suggestedWords));
        }
        else{
            Toast.makeText(this, "ResultCode NotOk", Toast.LENGTH_LONG);
        }

        if (requestCode == MY_DATA_CHECK_CODE)
        {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
                repeatTTS = new TextToSpeech(this, this);
            else{
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS)
            repeatTTS.setLanguage(Locale.ROOT);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.speech_btn){
            listenToSpeech();
        }
    }
}