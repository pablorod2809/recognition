package com.ingenios.recognition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.ingenios.recognition.util.SystemUiHider;

import java.util.Locale;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FinishActivity extends Activity {

    private Intent intent;
    private Intent intentExit;
    private Button button;
    private Button button2;
    private TextToSpeech t1;
    private String whocallme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        whocallme = getIntent().getExtras().getString("whocallme");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_finish);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        intentExit = new Intent(FinishActivity.this, FirstActivity.class);
        addListenerOnButton();

    }

    @Override
    public void onResume() {
        super.onResume();
        initTTS();
    }

    private void initTTS (){
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    int result = t1.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "No soporta este lenguaje" + Locale.getDefault().toLanguageTag().toString());
                    } else{
                        String msg;
                        if (whocallme == "FORCE-EXIT") {
                           //Vengo de una cancelación de pantalla
                            msg = getResources().getString(R.string.otra_compra);
                            ((TextView)findViewById(R.id.tvTitle)).setVisibility(View.INVISIBLE);
                        }else {
                            msg = getResources().getString(R.string.fin) + ". " + getResources().getString(R.string.otra_compra);
                        }
                        t1.speak(msg , TextToSpeech.QUEUE_ADD, null);
                    }
                }
            }
        });
    }

    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.ok_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                intentExit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentExit);
            }
        });

        button2 = (Button) findViewById(R.id.no_button);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                intentExit.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentExit.putExtra("Exit me", true);
                startActivity(intentExit);
                finish();
            }
        });
    }
}
