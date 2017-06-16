package com.ingenios.recognition;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by pablorodriguez on 1/6/17.
 */
public class Bills implements CameraBridgeViewBase.CvCameraViewListener2 {
    private Mat [] mTemplates;
    private Mat [] mKPTemplates;
    private File cacheDir;
    private AssetManager assetManager;
    private BaseLoaderCallback mLoaderCallback;

    public Bills(Context ctx){
        mTemplates = new Mat[20];
        mKPTemplates = new Mat[20];
        assetManager = ctx.getAssets();
        initDir();
        mLoaderCallback = new BaseLoaderCallback(ctx) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                    {
                        Log.i("PP", "OpenCV loaded successfully");
                        mTemplates = new Mat[20];
                        mKPTemplates = new Mat[20];
                        initialize();
                        // mOpenCvCameraView.enableView();
                    } break;
                    default:
                    {
                        super.onManagerConnected(status);
                    } break;
                }
            }
        };
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, ctx, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public Mat[] getTemplates(){
           return mTemplates;
    }

    public Mat[] getKPTemplates(){
        return mKPTemplates;
    }

    private void initialize(){
        FeatureDetector myFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor Extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        int i = 0;
        //billetes de 2 ---> index 0 al 1
        addBill(i++, "002ARS_F1.jpg",myFeatureDetector, Extractor);
        addBill(i++, "002ARS_B1.jpg",myFeatureDetector, Extractor);
        //billetes de 5 ---> index 2 al 5
        addBill(i++, "005ARS_F1.jpg",myFeatureDetector, Extractor);
        addBill(i++, "005ARS_B1.jpg",myFeatureDetector, Extractor);
        addBill(i++, "005ARS_F2.jpg",myFeatureDetector, Extractor);
        addBill(i++, "005ARS_B2.jpg",myFeatureDetector, Extractor);
        //billetes de 10 ---> index 6 al 9
        addBill(i++, "010ARS_F1.jpg",myFeatureDetector, Extractor);
        addBill(i++, "010ARS_B1.jpg",myFeatureDetector, Extractor);
        addBill(i++, "010ARS_F2.jpg",myFeatureDetector, Extractor);
        addBill(i++, "010ARS_B2.jpg",myFeatureDetector, Extractor);
        //billetes de 20 ---> index 10 al 11
        addBill(i++, "020ARS_F1.jpg",myFeatureDetector, Extractor);
        addBill(i++, "020ARS_B1.jpg",myFeatureDetector, Extractor);
        //billetes de 50 ---> index 12 al 15
        addBill(i++, "050ARS_F1.jpg",myFeatureDetector, Extractor);
        addBill(i++, "050ARS_B1.jpg",myFeatureDetector, Extractor);
        addBill(i++, "050ARS_F2.jpg",myFeatureDetector, Extractor);
        addBill(i++, "050ARS_B2.jpg",myFeatureDetector, Extractor);
        //billetes de 100 ---> index 16 al 20
        addBill(i++, "100ARS_F1.jpg",myFeatureDetector, Extractor);
        addBill(i++, "100ARS_B1.jpg",myFeatureDetector, Extractor);
        addBill(i++, "100ARS_F2.jpg",myFeatureDetector, Extractor);
        addBill(i++, "100ARS_B2.jpg",myFeatureDetector, Extractor);
        //billete de 200 ---> index
        //billete de 500 ---> index

    }
    private void addBill(int index, String pFile, FeatureDetector myFeatureDetector, DescriptorExtractor Extractor){
        Mat objectDescriptors = new Mat();
        MatOfKeyPoint objectkeypoints = new MatOfKeyPoint();
        try {
            InputStream input = assetManager.open(pFile);
            String pathfile = this.getFileAbsPath(pFile, input);
            mTemplates[index] = Imgcodecs.imread(pathfile, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED); //100ms
            //Imgproc.cvtColor(mTemplates[index], mTemplates[index], Imgproc.COLOR_RGBA2RGB); //Si tiene alpha se lo saco.
            myFeatureDetector.detect(mTemplates[index], objectkeypoints);             //100 ms
            Extractor.compute(mTemplates[index], objectkeypoints, objectDescriptors); //200 ms
            mKPTemplates[index] =objectDescriptors;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileAbsPath(String fileName, InputStream input) {
        File file;
        try {
            file = new File(cacheDir, fileName);
            try {
                OutputStream output = new FileOutputStream(file);
                try {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                } finally {
                    output.close();
                }
                input.close();
            } catch (Exception e) {
                e.printStackTrace(); // handle exception, define IOException and others
            }
        } finally {
        }
        return file.getAbsolutePath();
    }
    private void initDir() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(
                    android.os.Environment.getExternalStorageDirectory(),
                    "money");

            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return null;
    }

}
