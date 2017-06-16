package com.ingenios.recognition;

import com.ingenios.recognition.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FirstActivity extends Activity {
    private Intent intent;
    private Button button;
    private Button button2;
    private ImageButton btnMic;
    private EditText precio;
    private TextToSpeech t1;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_first);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if( getIntent().getBooleanExtra("Exit me", false)){
            finish();
        }

        intent = new Intent(FirstActivity.this, MainActivity.class);

        precio = (EditText) findViewById(R.id.edtPrice);
        precio.requestFocus();


        addListenerOnButton();

        if(precio.requestFocus()){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }


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
                        if (precio.getText().toString().isEmpty())
                           t1.speak(getResources().getString(R.string.ingresa_importe) + " " + getResources().getString(R.string.por_favor), TextToSpeech.QUEUE_ADD, null);
                    }
                }
            }
        });
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.dime_importe),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String[] arr = result.get(0).split(" ");
                    String rsta = "";
                    double val;
                    for ( String ss : arr) {
                        try {
                            val = Double.parseDouble(ss);
                            if (((int)val) == val)
                                rsta += (int)val;
                            else
                                rsta += val;
                        } catch(NumberFormatException nfe) {
                            if ((ss.equals("con") || ss.equals("punto") || ss.equals("y"))) {
                                if (rsta.indexOf(".") <= 0)
                                    rsta += '.';
                            } else{
                                if (ss.equals("centavos") && rsta.indexOf(".") <= 0){
                                    rsta = "0." + rsta;
                                }
                            }

                        }
                    }
                    precio.setText(rsta);
                    if (!rsta.isEmpty()) {
                        //TODO: poner espera
                        button.callOnClick();
                    }
                }
                break;
            }

        }
    }

    public void addListenerOnButton() {
        button = (Button) findViewById(R.id.ok_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (arg0 != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(arg0.getWindowToken(), 0);
                }
                intent.putExtra("price", new Double(precio.getText().toString()));
                intent.putExtra("calledfrom", MainActivity.CALLED_FROM_BUY);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        button2 = (Button) findViewById(R.id.exit_button);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        btnMic = (ImageButton) findViewById(R.id.imageButton);
        btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                promptSpeechInput();
            }
        });

    }


}
