package com.buczel.tirt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.videotransmitter.commons.dts.ImageDataDts;
import com.videotransmitter.commons.dts.NetAdresDs;
import com.videotransmitter.commons.sender.Sender;
import com.videotransmitter.commons.service.ImageDataSenderServce;
import com.videotransmitter.commons.service.impl.ImageDataSenderServceImpl;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by Reiz3N on 2016-03-13.
 */

public class SenderClass {
    public static final int PORT = 8004;

    public static String getIP() {
        return IP;
    }

    public static void setIP(String IP) {
        SenderClass.IP = IP;
    }

    private static String IP = "192.168.43.249";
    private static byte[] IMG_DATA_BYTE;
    private static int IMG_WIDTH = 600;
    private static int IMG_HEIGHT = 300;
    private static int IMG_LAYERS = 3;
    private static NetAdresDs netAdresDs;
    private static ImageDataDts imageDataDts;
    ImageDataSenderServce senderServce = new ImageDataSenderServceImpl();
    Sender sender;


    public SenderClass() {

        /* create adress configuration */
        netAdresDs = buildNetAdresDs();
        /* create data transport structure */
        imageDataDts = senderServce.createImageDataDts(IMG_WIDTH, IMG_HEIGHT, IMG_LAYERS);
        /* create and configure sender */
        sender = senderServce.createUDPSender(netAdresDs);
    }

    public void start(byte[] data) {

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        bitmap = getResizedBitmap(bitmap, IMG_WIDTH, IMG_HEIGHT);
        IMG_DATA_BYTE = readImage(bitmap);

        AsyncTask<Void, Void, Void> myTask = new UDPSender();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else
            myTask.execute();
    }


    public void testUDPSender() {


        for (int i = 0; i < 1; i++) {
            IMG_DATA_BYTE[0] = (byte) i;
			/* send Frame */
            senderServce.clearBufferAndFillData(imageDataDts, IMG_DATA_BYTE);
            //sender.send(imageDataDts);
//            Object[] objects = new Object[2];
//            objects[0] = sender;
//            objects[1] = imageDataDts;
            //new SendImage(sender).execute(imageDataDts);
            AsyncTask<Void, Void, Void> myTask = new SendImage();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Log.e("Sender", "Zium! nr: " + i);
            } else
                myTask.execute();

            waitTime(40);

        }
        //sender.stop();
    }

    private NetAdresDs buildNetAdresDs() {
        NetAdresDs ds = new NetAdresDs();
        ds.setIp(IP);
        ds.setPort(PORT);
        return ds;
    }


    private void waitTime(long time) {
        synchronized (this) {
            try {
                wait(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private byte[] readImage(Bitmap bitmap) {

        InputStream istr;
        Bitmap img = null;

        try {
            img = bitmap;
        } catch (Exception e) {
            Log.e("Error reading file", e.toString());
        }
        IMG_HEIGHT = img.getHeight();
        IMG_WIDTH = img.getWidth();
        IMG_LAYERS = 3;
        ByteBuffer buffer = ByteBuffer.allocate(IMG_HEIGHT * IMG_WIDTH * IMG_LAYERS);
        for (int i = 0; i < IMG_HEIGHT; i++) {
            for (int j = 0; j < IMG_WIDTH; j++) {
                int color = img.getPixel(j, i);
                buffer.put((byte) Color.blue(color));
                buffer.put((byte) Color.green(color));
                buffer.put((byte) Color.red(color));
            }
        }
        return buffer.array();
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private class SendImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            sender.send(imageDataDts);
            return null;
        }

    }

    private class UDPSender extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            testUDPSender();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e("Watek glowny", "KONIEC");
        }
    }


}
