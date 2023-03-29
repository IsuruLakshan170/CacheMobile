package com.example.cachemobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    Interpreter tflite;
    private static final String SERVER_ADDRESS = "141.145.200.6";
    private static final int SERVER_PORT = 8000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //--copy model and get predictions---
        //copyToInternalStorage("V");
         runModel("c");

        //---read csv and get model accuray---
        // readCsv("V");

        //---rename model---
        // renameModel("v");

        //---delete files---
//        deleteFiles("v");

        //---socket handling---
        new Thread(new Runnable() {
            @Override
            public void run() {
            //socketConnect("V");


    }

    //socket connect
    private void socketConnect(String v){

        try {
            //create socket and connect to server
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to server.");
            Log.i("Socket", "Connected to server.");
            //data recive untill recive  new line caractor
            BufferedReader BReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //print data recived for reader variable
            String MyUrl = BReader.readLine();
            System.out.println("recived data : " + MyUrl);
            Log.i("Socket", "recived data : " + MyUrl);

            //socket close
            socket.close();
            try {
                Log.i("Socket", "Try ");

                // MyUrl = "http://localhost:5000/download?ID=123";
                URL url = new URL(MyUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                Log.i("Socket", "Try ");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String fileName = "5MB.zip";
                    Log.i("Socket", "Input Stream");

                    InputStream inputStream = conn.getInputStream();

                    // Get the path to the internal storage directory
                    File directory = new File(getFilesDir(), "models");
                    directory.mkdirs();
                    Log.i("Socket", "models dir");

                    // Create a new file in the internal storage directory and copy the model data to it
                    File modelFile = new File(directory, "model.tflite");
                    OutputStream outputStream = new FileOutputStream(modelFile);

//                    FileOutputStream outputStream = new FileOutputStream(fileName);
                    byte[] buffer = new byte[1024];
                    int bytesRead = -1;
                    Log.i("Socket", "read....");
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        Log.i("Socket", "Writing....");

                    }

                    outputStream.close();
                    inputStream.close();
                    Log.i("Socket", "Save to internal storage");

                    System.out.println("File downloaded successfully.");
                } else {
                    Log.i("Socket", "Failed to download file. Response code: " + conn.getResponseCode());
                    System.out.println("Failed to download file. Response code: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (UnknownHostException e) {
            Log.i("Socket", "ERROR: Server not found.");
            System.err.println("ERROR: Server not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        }).start();

    }

    //close socket connect

    //model rename and delete files
     private void renameModel(String v){
         File modelFile = new File(getFilesDir() + "/models/receivedmodel.tflite");
         File newModelFile = new File(getFilesDir() + "/models/model.tflite");
         if(modelFile.exists()) {
             if(modelFile.renameTo(newModelFile)) {
                 Log.d("FileRename", "File renamed successfully.");
             } else {
                 Log.d("FileRename", "File rename failed.");
             }
         } else {
             Log.d("FileRename", "File not found.");
         }
     }

    private void deleteFiles(String v) {
        File receivedModelFile = new File(getFilesDir() + "/models/model.tflite");
        if (receivedModelFile.exists()) {
            if (receivedModelFile.delete()) {
                Log.d("FileDelete", "File deleted successfully.");
            } else {
                Log.d("FileDelete", "File delete failed.");
            }
        } else {
            Log.d("FileDelete", "File not found.");
        }

    }


    //close model rename  and delete files


    //http network

    private void downloadImage(String imageUrl) {
        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                String imageUrl = params[0];
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    return bitmap;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                if (result != null) {
                    saveImageToInternalStorage(result);
                    Log.i("DownloadImage", "Image downloaded successfully");
                } else {
                    Log.i("DownloadImage", "Failed to download image");
                }
            }
        }.execute(imageUrl);
    }

    private void saveImageToInternalStorage(Bitmap bitmap) {

        ContextWrapper cw = new ContextWrapper(getApplicationContext());

// Get the path to the "models" directory in internal storage
        File directory = new File(cw.getFilesDir(), "models");
        directory.mkdirs();

// Create a new file in the "models" directory and save the image to it
        File mypath = new File(directory, "image.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //http test network

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
        float[] x_data = {1, 1};


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

    //read csv file for asset
    private void readCsv(String v){
        // Load the CSV file from the assets folder
        String fileName = "dataset.csv";
        String[] data = loadDataFromAsset(this, fileName);
        Log.i("MyApp", "data set Loaded");
        // Preview the data in the console


// Split the first row into column names (if needed)
        String[] columnNames = data[0].split(",");

// Initialize the intData array
        int[][] intData = new int[data.length-1][columnNames.length];

// Convert the data into integers and store in the intData array
        for (int i = 1; i < data.length; i++) {
            String[] rowData = data[i].split(",");
            for (int j = 0; j < columnNames.length; j++) {
                intData[i-1][j] = Integer.parseInt(rowData[j]);
            }
        }
        loadModel("v");
        modelAccuracy(intData);


    }

    private String[] loadDataFromAsset(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        StringBuilder stringBuilder = new StringBuilder();

        try {
            // Open the CSV file as an InputStream and read its contents
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert the CSV data to an array of Strings
        return stringBuilder.toString().split("\n");
    }



//close read csv

    //model accuracy
    private void loadModel(String v){
        try {
            tflite = new Interpreter(loadFile());
            Log.i("MyApp", "Model load success fully");

        }catch (Exception ex){
            ex.printStackTrace();
            Log.i("MyApp", "Error");
            Log.i("MyApp", ex.toString());

        }
    }
    private void modelAccuracy(int[][] data){


        float[][] input_values = new float[data.length][2];
        int[][] realOutput = new int[data.length][1];

        for (int i = 0; i < data.length; i++) {
            input_values[i][0] = (float) data[i][0];
            input_values[i][1] = (float) data[i][1];
            realOutput[i][0] = data[i][2];
        }


// define input and output arrays
        float[][] x_np = new float[input_values.length][2];
        float[][] output = new float[1][7];

        int correctPredictions = 0;

// loop over input values
        for (int i = 0; i < input_values.length; i++) {
            // set input values
            x_np[i] = input_values[i];
            x_np[i][0] /= 12;
            x_np[i][1] /= 12;

            // run prediction
            tflite.run(x_np[i], output);

            // get predicted label for this input value
            int y_pred = argmax(output, 1)[0];
            int value = realOutput[i][0];

            if (y_pred == value) {
                correctPredictions++;
            }

            // print the predicted label
//            Log.i("MyApp", "Prediction for input " + i + ": " + y_pred);
//            Log.i("MyApp", "Real value for input " + i + ": " + value);
        }

        float accuracy = ((float) correctPredictions / input_values.length) * 100;
        Log.i("MyApp", "Model Accuracy: " + accuracy + "%");


    }


    //close model accuracy

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