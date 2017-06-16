package com.ingenios.recognition;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ingenios.recognition.util.MyCameraJavaView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2  {
    private static final String TAG = "Ingenios::recognition";
    public static final int CALLED_FROM_BUY = 1;
    public static final int CALLED_FROM_CHANGE = 2;

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
   // private CameraBridgeViewBase mOpenCvCameraView;
    //private JavaCameraView mOpenCvCameraView;
    private MyCameraJavaView mOpenCvCameraView;
    private RotateAnimation animation;
    private TextToSpeech t1;

    // Estas variables se usan para la imagen original y el template de busqueda.
    Mat mRgba;     //Imagen que se procesa
    Mat mTempRgba; //Imagen que viene de la camara
    Mat[] mKPTemplates;
    ImageButton button;
    ImageButton btnClose;

    //Billetes leidos
    int valor;
    String logValor;
    double lectura;
    String posible;

    //valores de entrada y navegaciòn
    double precio;
    int calledfrom;
    private Intent intentNext;

    LinkedList<DMatch> good_matches;

    //-------------- Funciones de la clase.
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    Object o = Comunicador.getObjeto();
                    mKPTemplates = (Mat[])o;
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    //-------------- Eventos de la actividad, con configuraciones por default para OPENCV
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        precio = getIntent().getExtras().getDouble("price");
        calledfrom = getIntent().getExtras().getInt("calledfrom");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (MyCameraJavaView) findViewById(R.id.show_camera_activity_java_surface_view);
        mOpenCvCameraView.SetCaptureFormat(CvType.CV_8UC1);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        TextView tvP = (TextView) findViewById(R.id.tvPrecio);
        tvP.setText("$ " + precio);
        hideBill(0);


        button = (ImageButton) findViewById(R.id.shoot);
        btnClose = (ImageButton) findViewById(R.id.btnClose);

        animation = new RotateAnimation(-90.0f, -90.0f,RotateAnimation.RELATIVE_TO_SELF, 0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f);
        animation.setFillAfter(true);
        animation.setDuration(0);
        //((LinearLayout) findViewById(R.id.linearLayout)).setAnimation(animation);

        addListenerOnButton();

        Log.i(TAG, "called onCreate");
    }

    @Override
    public void onPause() {
        Log.i(TAG, "called onPause");
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        initTTS(R.string.lee_billete);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    //--------------- Interface del listener del opencv

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC1);
        mOpenCvCameraView.setFlashMode(this, 1);
        //mOpenCvCameraView.setFocusMode(this, 1);
        //mOpenCvCameraView.setCameraIndex(1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }


    //Accion que se ejecuta al apretar la imagen.
    public void addListenerOnButton() {


   /*     button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mRgba = mTempRgba;
                executeRecognition();
            }
        });
*/


        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        mOpenCvCameraView.setFlashMode(MainActivity.this, 4);
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        mRgba = mTempRgba;
                        executeRecognition();
                        mOpenCvCameraView.setFlashMode(MainActivity.this, 0);
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        btnClose.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentNext = new Intent(MainActivity.this, FinishActivity.class);
                intentNext.putExtra("whocallme", "FORCE-EXIT");
                intentNext.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentNext);
            }
        });

    }

    private void executeRecognition(){
        try {
            valor = 0;
            posible = "";
            lectura = 0;
           // Log.i("CAMERA.ATT", mOpenCvCameraView.getResolution().height + " - " + mOpenCvCameraView.getResolution().width);

            FeatureDetector myFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);
            MatOfKeyPoint keypoints = new MatOfKeyPoint();
            myFeatureDetector.detect(mRgba, keypoints);

            DescriptorExtractor Extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
            Mat escenaDescriptors = new Mat();

            Extractor.compute(mRgba, keypoints, escenaDescriptors);

            MatOfDMatch matches = new MatOfDMatch();
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

            //------------ Analizo todos los templates de billetes cargados ----------------
            for (int i = 0; i < mKPTemplates.length; i++) {
                //Log.i("Distacia","Template --->" + mKPTemplates[i]);
                Mat objectDescriptors = new Mat();
                objectDescriptors = mKPTemplates[i];
                matcher.match(objectDescriptors, escenaDescriptors, matches);
                matches = sortedKMatches(matches, 0, 10, i);
                if (lectura > 70) //Si uno supera el 70% no analizo los siguientes.
                    i = mKPTemplates.length;

            }
            //------------------------------------------------------------------------------

            if (valor > 0) {
                //Muestro el billete reconocido en pantalla
                TextView tv = (TextView) findViewById(R.id.tvMessage);
                TextView tv3 = (TextView) findViewById(R.id.tvPrecio);
                tv.setText(logValor);
                tv.setVisibility(View.VISIBLE);
                //Resto el valor reconocido al valor actual
                precio -= valor;
                precio = Double.parseDouble(new DecimalFormat("#.##").format(precio));
                if (precio > 0) {
                    String strPrecio = "+" + precio;
                    Integer decimals = new Integer(strPrecio.substring(strPrecio.indexOf('.') + 1));
                    strPrecio = (decimals == 0 ? strPrecio.substring(strPrecio.indexOf('+') + 1, strPrecio.indexOf('.')) : strPrecio);
                    tv3.setText("$ " + precio);
                    if (precio == 1)
                        initTTS(getResources().getString(R.string.falta_singular) + " " + strPrecio + " " + getResources().getString(R.string.moneda_singular));
                    else
                        initTTS(getResources().getString(R.string.falta_plural) + " " + strPrecio + " " + getResources().getString(R.string.moneda_plural));
                }
                tv3.setTextColor(getResources().getColor(R.color.priceGreen));

            } else {
                TextView tv2 = (TextView) findViewById(R.id.tvPosibles);
                tv2.setBackgroundColor(Color.argb(107, 142, 142, 142));
                tv2.setText(posible);
                initTTS(R.string.no_pude_reconocer);
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        hideBill(2000);
        //Llamo a actividad si ya termine de leer
        if (precio <= 0) {
            callForm();
        }
    }

    //Invoca a la proxima actividad en funcion de donde viene
    private void callForm() {
        intentNext = new Intent(MainActivity.this, FinishActivity.class);
        if (calledfrom == MainActivity.CALLED_FROM_CHANGE){
            intentNext.putExtra("whocallme", "FORCE-EXIT");
            intentNext = new Intent(MainActivity.this, FinishActivity.class);
        } else
            intentNext = new Intent(MainActivity.this, ChangeActivity.class);

        intentNext.putExtra("price", new Double(precio));
        intentNext.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentNext);
    }

    //Ocultar valor billete luego de 2 segundos.
    public void hideBill(int pMilis){
        CountDownTimer cdTimer = new CountDownTimer(pMilis,pMilis) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                ((TextView) findViewById(R.id.tvMessage)).setText("");
                ((TextView) findViewById(R.id.tvPosibles)).setText("");
                ((TextView) findViewById(R.id.tvPosibles)).setBackgroundColor(Color.argb(0, 0, 0, 0));
            }
        };
       cdTimer.start();
    }

    //Lectura de frase con el idioma por defecto.
    private void initTTS (final int idMsg){
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    int result = t1.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "No soporta este lenguaje" + Locale.getDefault().toLanguageTag().toString());
                    } else{
                        t1.speak(getResources().getString(idMsg), TextToSpeech.QUEUE_ADD, null);
                    }
                }
            }
        });
    }
    private void initTTS (final String charMsg){
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    int result = t1.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "No soporta este lenguaje" + Locale.getDefault().toLanguageTag().toString());
                    } else{
                        t1.speak(charMsg, TextToSpeech.QUEUE_ADD, null);
                    }
                }
            }
        });
    }

    public MatOfDMatch sortedKMatches(MatOfDMatch matches, int start, int end, int v){
        List<DMatch> list = matches.toList();
        Collections.sort(list, new Comparator<DMatch>() {
            int qty=0;
            @Override
            public int compare(final DMatch object1, final DMatch object2) {
                  //Log.i("Distacia",qty + ")"+object1.distance + " - " + object2.distance);
                  return (int)(object1.distance - object2.distance);
            }
        });

        if(list.size()<end){
            Log.i(TAG,"Only found "+list.size()+" matches. Can't return "+end);
            end = list.size();
        }
        List<DMatch> subllist = list.subList(start, end);

        //Obtengo un promedio aritmetico de la distancia entre los 10 primeros descriptores
        double value = 0;
        for (int x=0; x<end; x++){
            value += subllist.get(x).distance;
        }
        value =  100 - (value / end);


        if (value > 60)
         if (value >= 65 && this.lectura < value) {
            this.valor = valueFormIndex(v);
            this.logValor = "$ " + this.valor;
            this.lectura = Double.parseDouble(new DecimalFormat("#.##").format(value));
            this.posible = "%" + value;
         } else {
            this.posible += valueFormIndex(v) + " (%" + value + ") -";
         }
        else {
           // this.posible += valueFormIndex(v) + " (%" + value + ") -";
        }

        MatOfDMatch result = new MatOfDMatch();
        result.fromList(subllist);
        return result;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame){
   //     if (mOpenCvCameraView.isShutterPhoto()){
           // mRgba = inputFrame.rgba();
   //         mOpenCvCameraView.finishCapture();
            Log.i("CAMERA.FINISHCAP","Termine captura");
   //     }
        mTempRgba = inputFrame.rgba();
        //Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGBA2RGB);
        return mTempRgba;
    }


    private int valueFormIndex(int pIndex){
      // a partir del indice del arreglo devuelvo el valor del billete
      return  (pIndex > 1 ? (pIndex > 5 ? (pIndex > 9 ? (pIndex > 11 ? (pIndex > 15 ? 100:50):20):10):5):2);

    }

}
