package com.ingenios.recognition;

import com.ingenios.recognition.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ChangeActivity extends Activity {

    private Intent intent;
    private Intent intentExit;

    private Button button;
    private Button button2;
    private TextToSpeech t1;
    private double vuelto;
    private String strVuelto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vuelto = getIntent().getExtras().getDouble("price")*-1;
        strVuelto = "+" + vuelto;
        Integer decimals = new Integer(strVuelto.substring(strVuelto.indexOf('.') + 1));
        strVuelto = (decimals == 0 ? strVuelto.substring(strVuelto.indexOf('+') + 1, strVuelto.indexOf('.')) : strVuelto.substring(strVuelto.indexOf('+') + 1));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        intent = new Intent(ChangeActivity.this, MainActivity.class);
        intentExit = new Intent(ChangeActivity.this, FinishActivity.class);

        TextView tvC = (TextView) findViewById(R.id.tvChange);
        String msg = getResources().getString(R.string.recibe_vuelto);
        strVuelto = msg.replace("N$", strVuelto + " " + getResources().getString(R.string.moneda_plural));

        tvC.setText(strVuelto);
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
                        if (vuelto > 0) {
                            msg = strVuelto;
                        }else {
                            msg = getResources().getString(R.string.no_recibe_vuelto);
                        }

                        ((TextView)findViewById(R.id.tvChange)).setText(msg);
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
                if (vuelto > 0) {
                    intent.putExtra("price", vuelto);
                    intent.putExtra("calledfrom", MainActivity.CALLED_FROM_CHANGE);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else{
                    intentExit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intentExit.putExtra("whocallme","FORCE-EXIT");
                    startActivity(intentExit);
                }
            }
        });

        button2 = (Button) findViewById(R.id.exit_button);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                intentExit.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentExit.putExtra("Exit me", true);
                startActivity(intentExit);
            }
        });
    }
}
