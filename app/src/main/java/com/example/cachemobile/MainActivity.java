package com.example.cachemobile;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        copyToInternalStorage("V");
        runModel("c");
        //test data type
        dType("V");
    }

    //copy from asset to internal storage
    private void copyToInternalStorage(String v){
        Log.i("MyApp", "copyToInternalStorage function start");
        try {
            // Open the model file from the assets folder
            InputStream inputStream = getAssets().open("model.tflite");
            Log.i("MyApp", "Read from assets");

            // Get the path to the internal storage directory
            File directory = new File(getFilesDir(), "models");
            directory.mkdirs();

            // Create a new file in the internal storage directory and copy the model data to it
            File modelFile = new File(directory, "model.tflite");
            OutputStream outputStream = new FileOutputStream(modelFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            Log.i("MyApp", "Save to internal storage");

            // Now the model file is copied to the internal storage directory and can be accessed from there
            //check existancy
            File directory1 = new File(getFilesDir(), "models");
            File modelFile1 = new File(directory1, "model.tflite");

            if (modelFile1.exists()) {
                Log.i("MyApp", "Successfully load from internal storage");

                // The model file was successfully copied to the internal storage directory
                // You can now load the model from this file using the code I provided in my previous response
            } else {
                Log.i("MyApp", "Failed to load from internal storage");
                // The model file was not copied to the internal storage directory
                // Handle the error
            }

        } catch (IOException e) {
            // Handle the error
            Log.i("MyApp", "Failed to savte internal storage");
            Log.i("MyApp", e.toString());
        }

    }
    private MappedByteBuffer loadFile() throws IOException {
        // Get the path to the model file in the internal storage directory
        File directory = new File(getFilesDir(), "models");
        File modelFile = new File(directory, "model.tflite");

        // Open the model file as a FileInputStream
        FileInputStream inputStream = new FileInputStream(modelFile);

        // Get a FileChannel from the FileInputStream
        FileChannel fileChannel = inputStream.getChannel();

        // Get the start offset and declared length of the model file
        long startOffset = 0;
        long declareLength = modelFile.length();

        // Map the model file to a MappedByteBuffer
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength);

        // Close the input stream and file channel
        inputStream.close();
        fileChannel.close();

        // Return the MappedByteBuffer
        return mappedByteBuffer;
    }

    //close copy

    //model
    private void runModel(String v){
        try {
            tflite = new Interpreter(loadFile());
            Log.i("MyApp", "Model load success fully");
            predict("v");
        }catch (Exception ex){
            ex.printStackTrace();
            Log.i("MyApp", "Error");
            Log.i("MyApp", ex.toString());

        }
    }

//    private MappedByteBuffer loadFile() throws IOException {
//        AssetFileDescriptor fileDescriptor=this.getAssets().openFd("model.tflite");
//        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
//        FileChannel fileChannel=inputStream.getChannel();
//        long startOffset=fileDescriptor.getStartOffset();
//        long declareLength=fileDescriptor.getDeclaredLength();
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
//    }

    private void predict(String v){
        float[] x_data = {10, 1};


        float[][] x_np = new float[1][2];
        x_np[0] = x_data;
        x_np[0][0] /= 12;
        x_np[0][1] /= 12;


        float[][] output = new float[1][7];
        tflite.run(x_np, output);
        int[] y_pred_model_1 = argmax(output, 1);

        Log.i("MyApp", "Output: " + Arrays.toString(y_pred_model_1));

    }

    public static int[] argmax(float[][] array, int axis) {
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            float max = array[i][0];
            int index = 0;
            for (int j = 1; j < array[i].length; j++) {
                if (array[i][j] > max) {
                    max = array[i][j];
                    index = j;
                }
            }
            result[i] = index;
        }
        return result;
    }

    //close model

    //check data type
    private void dType(String v){
        try {
            InputStream inputStream = getAssets().open("model.tflite");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            String result = byteArrayOutputStream.toString("UTF-8");
            Log.i("MyApp", "Data type : "+result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //close data type

    /*
    The internal storage of an Android app is considered to be relatively secure because it is private to the app and
     inaccessible to other apps by default. Other apps cannot read or modify the files stored in the internal storage
      of your app, and the files stored in the internal storage are deleted when the user uninstalls the app.
     */
    /*
    By default, files stored in the internal storage of an Android app are not visible to the user or accessible through
     file managers or other apps on the device. This is because the internal storage is private to the app and other apps
      do not have permission to access it.
     */

    /*
    If the size of the model is small and the model does not change frequently, you can save it in the app's asset folder
     or in the internal storage of the app. The internal storage is a private storage that is only accessible to the app,
      and it is a good option for storing sensitive data like models.

    If the size of the model is large and you want to reduce the load time of the model, you can consider using the cache memory
     of the app. Cache memory is a fast memory that is used to store frequently accessed data. However, it is important to note
      that the cache memory is not a persistent storage, which means that data in the cache memory can be deleted by the system
      at any time to free up space.
     */
}