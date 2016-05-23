package com.buczel.tirt;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;


//import com.google.android.gms.ads.*;

public class VideoStream extends Activity
        implements CameraView.CameraReadyCallback {


    private final int PictureWidth = 480;
    private final int PictureHeight = 360;
    private final int MediaBlockNumber = 3;
    private final int MediaBlockSize = 1024 * 512;
    private final int StreamingInterval = 100;

    private OverlayView overlayView = null;
    private CameraView cameraView = null;
    private AudioRecord audioCapture = null;

    ExecutorService executor = Executors.newFixedThreadPool(3);
    VideoEncodingTask videoTask = new VideoEncodingTask();

    private ReentrantLock previewLock = new ReentrantLock();
    boolean inProcessing = false;
    private boolean isSending = false;

    byte[] yuvFrame = new byte[1920 * 1280 * 2];

    MediaBlock[] mediaBlocks = new MediaBlock[MediaBlockNumber];
    int mediaWriteIndex = 0;
    int mediaReadIndex = 0;

    Handler streamingHandler;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    SenderClass sender;


    //
    //  Activiity's event handler
    //
    @Override
    public void onCreate(Bundle savedInstanceState) {

        // application setting
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // load and setup GUI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleSending);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isSending = true;
                } else {
                    isSending = false;
                }
            }
        });

        sender = new SenderClass();

        // init  camera
        for (int i = 0; i < MediaBlockNumber; i++) {
            mediaBlocks[i] = new MediaBlock(MediaBlockSize);
        }
        resetMediaBuffer();

        initCamera();


        streamingHandler = new Handler();
        streamingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doStreaming();
            }
        }, StreamingInterval);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (cameraView != null) {
            previewLock.lock();
            cameraView.StopPreview();
            cameraView.Release();
            previewLock.unlock();
            cameraView = null;
        }

        finish();
        //System.exit(0);
    }

    @Override
    public void onBackPressed() {
        isSending = false;
        super.onBackPressed();
    }

    //
    //  Interface implementation
    //
    public void onCameraReady() {
        cameraView.StopPreview();
        cameraView.setupCamera(PictureWidth, PictureHeight, 4, 25.0, previewCb);

        cameraView.StartPreview();
    }


    //  Internal help functions
    private void initCamera() {
        SurfaceView cameraSurface = (SurfaceView) findViewById(R.id.surface_camera);
        cameraView = new CameraView(cameraSurface);
        cameraView.setCameraReadyCallback(this);

        overlayView = (OverlayView) findViewById(R.id.surface_overlay);
    }


    private void resetMediaBuffer() {
        synchronized (VideoStream.this) {
            for (int i = 1; i < MediaBlockNumber; i++) {
                mediaBlocks[i].reset();
            }
            mediaWriteIndex = 0;
            mediaReadIndex = 0;
        }
    }

    private void doStreaming() {
        synchronized (VideoStream.this) {

            MediaBlock targetBlock = mediaBlocks[mediaReadIndex];
            if (targetBlock.flag == 1) {
                targetBlock.reset();
                mediaReadIndex++;
                if (mediaReadIndex >= MediaBlockNumber) {
                    mediaReadIndex = 0;
                }
            }
        }

        streamingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doStreaming();
            }
        }, StreamingInterval);

    }



    //  Internal help class and object definment
    private PreviewCallback previewCb = new PreviewCallback() {
        public void onPreviewFrame(byte[] frame, Camera camera) {
            previewLock.lock();

            doVideoEncode(frame);
            camera.addCallbackBuffer(frame);
            previewLock.unlock();

            try {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();
                YuvImage image = new YuvImage(frame, parameters.getPreviewFormat(), size.width, size.height, null);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                image.compressToJpeg( new Rect(0, 0, image.getWidth(), image.getHeight()), 90, bos);

                //we put stream into array and put it into Sender
                if(isSending)
                sender.start(bos.toByteArray());

            } catch (Exception e) {
                Toast toast = Toast
                        .makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    private void doVideoEncode(byte[] frame) {
        if (inProcessing == true) {
            return;
        }
        inProcessing = true;

        if(cameraView==null) return;

        int picWidth = cameraView.Width();
        int picHeight = cameraView.Height();
        int size = picWidth * picHeight + picWidth * picHeight / 2;
        System.arraycopy(frame, 0, yuvFrame, 0, size);

        executor.execute(videoTask);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class VideoEncodingTask implements Runnable {
        private byte[] videoHeader = new byte[8];

        public VideoEncodingTask() {
            videoHeader[0] = (byte) 0x19;
            videoHeader[1] = (byte) 0x79;
        }

        public void run() {
            MediaBlock currentBlock = mediaBlocks[mediaWriteIndex];
            if (currentBlock.flag == 1) {
                inProcessing = false;
                return;
            }
            inProcessing = false;
        }
    }




}

