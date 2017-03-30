package com.androidvideoconverter.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.androidvideoconverter.app.R;
import com.androidvideoconverter.app.comm_utils.FileUtils;
import com.androidvideoconverter.app.comm_utils.HttpUtils;
import com.androidvideoconverter.app.comm_utils.RequestSender;
import com.androidvideoconverter.app.events.EndUploadEvent;
import com.androidvideoconverter.app.events.ErrorMsgEvent;
import com.androidvideoconverter.app.events.StartLiveUploadEvent;
import com.androidvideoconverter.app.events.StreamletAckEvent;
import com.androidvideoconverter.app.mediarecorder.CustomMediaRecorder;
import com.androidvideoconverter.app.parsevideo.VideoParser;
import com.androidvideoconverter.app.uploader.SendingQueue;
import com.androidvideoconverter.app.uploader.UploadEngine;
import com.androidvideoconverter.app.uploader.UploadStreamlet;
import com.squareup.otto.Subscribe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.androidvideoconverter.app.events.EventBusProvider.getEventBus;

/*
 *Shows the camera preview on screen.
 * Caprure video using CustomMediaRecorder.
 * Use the record button to toggle recording on.
 * Recording continues until stopped.
 * The output is a video-only MP4 file.
 * Segment video into 3s streamlets.
 * Upload video to the server.
 * Handle resumable upload.
 */
public class MainActivity extends Activity {

    private static final String TAG = "RecordVideo";
    public static final String VIDEO_TEST_FILE_NAME = "videocapture_example.mp4";
    public static final String VIDEO_TEST_FOLDER = "/RecordedVideoTest/";
    public static final String LIVE_VIDEO_FOLDER = "/LiveVideo/";
    private static final File STORAGE_PATH_VoD = new File(Environment.getExternalStorageDirectory(), VIDEO_TEST_FOLDER);
    private static final File STORAGE_PATH_LIVE = new File(Environment.getExternalStorageDirectory(), LIVE_VIDEO_FOLDER);

    private String serverURL = "localhost";

    // main functional buttons
    private Button mSegmentBtn;
    private Button mUploadBtn;
    private Button mStartBtn;
    private Button mStopBtn;
    private Button mLifeVideo;
    private ProgressDialog barProgressDialog;
    private VideoView mVideoView;

    // custom parser to segement video into 3s streamlets
    private VideoParser mVideoParser;
    //total number of streamlets after segmentation
    private int numberOfSegments;
    //current streamlet to upload
    private Integer stremletNo;
    //custom media recorder
    private CustomMediaRecorder mediaRecorder;
    //current streamlet index for live capturing using MediaRecorder
    private int index=0;

    //uploading
    private RequestSender mRequestSender;
    public static boolean mIsLive;
    private UploadEngine mUploadEngine;

    //server id for uploading
    private String mLiveSvid;
    private boolean isSegmentationDone;
    private String serverID;
    private boolean isDoneUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //register EventBus
        getEventBus().register(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(com.androidvideoconverter.app.R.layout.activity_main);

        mUploadBtn = (Button) findViewById(com.androidvideoconverter.app.R.id.uploadVideoButton);
        mSegmentBtn = (Button) findViewById(R.id.btnSegmentVideo);
        mStartBtn = (Button) findViewById(R.id.mStartBtn);
        mStopBtn = (Button) findViewById(R.id.mStopBtn);
        mLifeVideo = (Button) findViewById(R.id.lifeVideoBtn);
        mVideoView = (VideoView) this.findViewById(R.id.videoView);

        mediaRecorder = new CustomMediaRecorder();
        mRequestSender = new RequestSender(getString(R.string.client_id), serverURL);

        //initialisation
        mIsLive = false;
        isDoneUpload=false;
        index = 0;
        isSegmentationDone = false;

        // set all listeners
        FileUtils.cleanUpFolder(LIVE_VIDEO_FOLDER);
        FileUtils.cleanUpFolder(VIDEO_TEST_FOLDER);
        startRecording();
        stopRecording();
        parseVideo();
        uploadVideo();
        // live capturing using MediaRecorder
        liveVideoStart();
    }

    private void startRecording() {
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtils.cleanUpFolder(VIDEO_TEST_FOLDER);
                isSegmentationDone = false;
                mediaRecorder.initRecorder(mVideoView,false,1280, 720, VIDEO_TEST_FILE_NAME, VIDEO_TEST_FOLDER);
                mediaRecorder.prepare();
                mediaRecorder.getmRecorder().start();
            }
        });
    }

    private void stopRecording() {

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaRecorder.getmRecorder() != null) {
                    mediaRecorder.getmRecorder().setOnErrorListener(null);
                    mediaRecorder.getmRecorder().setOnInfoListener(null);
                    try {
                        mediaRecorder.getmRecorder().stop();
                        String last_path = STORAGE_PATH_LIVE +String.format("/life_segment_%s.mp4", index);
                        UploadStreamlet lastStreamlet = new UploadStreamlet(mLiveSvid,index,last_path);
                        SendingQueue.add(lastStreamlet);
                        mUploadEngine.setLiveEnded(true);
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Got IllegalStateException in stopRecording");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    releaseMediaRecorder();
                }
            }
        });
    }
    private void parseVideo() {
        mSegmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((new File(Environment.getExternalStorageDirectory() + VIDEO_TEST_FOLDER).listFiles().length) != 0) {
                    if (!isSegmentationDone) {
                        mVideoParser = new VideoParser(Environment.getExternalStorageDirectory() + VIDEO_TEST_FOLDER, VIDEO_TEST_FILE_NAME);
                        mVideoParser.execute();
                        isSegmentationDone = true;
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Record video first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.releaseRecorder();
            mediaRecorder.releaseCamera();
        }
    }

    private void uploadVideo() {
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSegmentationDone) {
                    if (HttpUtils.isNetworkAvailable(getApplicationContext())) {
                        Log.v("MainActivity:", "upload button clicked");
                        isDoneUpload = false;
                        String base_url = serverURL;
                        numberOfSegments = new File(Environment.getExternalStorageDirectory() + VIDEO_TEST_FOLDER).listFiles().length - 1;
                        showProgressDialog(numberOfSegments);
                        RequestSender sender = new RequestSender(getString(R.string.client_id), base_url);
                        sender.send_new_upload("vod", numberOfSegments);
                    } else {
                        Toast.makeText(getApplicationContext(), "no internet connection", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Segment video first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Subscribe
    public void onErrorMsgEvent(ErrorMsgEvent event) {
        if(barProgressDialog!=null) {
            barProgressDialog.dismiss();
        }
        if(event.get_err_string()==null || !isDoneUpload){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Connection Lost. Resume upload.")
                    .setCancelable(false)
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    RequestSender sender = new RequestSender(getString(R.string.client_id), serverURL);
                                    if(serverID!=null) {
                                        showProgressDialog(numberOfSegments);
                                        sender.send_resume_upload(serverID);
                                    }else{
                                        showProgressDialog(numberOfSegments);
                                        sender.send_new_upload("vod", numberOfSegments);
                                    }
                                    dialog.cancel();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });
            builder.create();
            builder.show();
            return;
        }
        Toast.makeText(MainActivity.this, event.get_err_string(), Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onStreamletAckEvent(StreamletAckEvent event) {
        serverID = event.get_svid();
        stremletNo = Integer.valueOf(event.getStreamlet_no().toString());
        numberOfSegments = new File(Environment.getExternalStorageDirectory() + VIDEO_TEST_FOLDER).listFiles().length - 1;
        if (stremletNo < numberOfSegments) {
            RequestSender sender = new RequestSender(getString(R.string.client_id), serverURL);
            if (HttpUtils.isNetworkAvailable(getApplicationContext())) {
                sender.send_upload_streamlet(serverID, stremletNo, STORAGE_PATH_VoD + "/segment_" + stremletNo + ".mp4");
                barProgressDialog.setProgress(stremletNo);
            }else{
                Toast.makeText(getApplicationContext(), "no internet connection", Toast.LENGTH_SHORT).show();
            }
        }
        // done upload: server already received the last streamlet
        if (stremletNo == numberOfSegments) {
            RequestSender sender = new RequestSender(getString(R.string.client_id), serverURL);
            if (HttpUtils.isNetworkAvailable(getApplicationContext())) {
                sender.send_done_upload(serverID);
                barProgressDialog.dismiss();
            }else{
                Toast.makeText(getApplicationContext(), "no internet connection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Subscribe
    public void onEndUploadEvent(EndUploadEvent event) {
        isDoneUpload=true;
        Toast.makeText(getApplicationContext(), "Upload done! s_vid: "+serverID, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onStartLiveUploadEvent(StartLiveUploadEvent event){
        Log.i(TAG,"Received s_vid "+event.getS_vid());
        mLiveSvid = event.getS_vid();
        mUploadEngine = new UploadEngine(event.getS_vid(),mRequestSender);
        mUploadEngine.start();
    }

    private void liveVideoStart() {
        mLifeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HttpUtils.isNetworkAvailable(getApplicationContext())) {
                    FileUtils.cleanUpFolder(LIVE_VIDEO_FOLDER);
                    mIsLive = true;
                    mRequestSender.send_new_upload("live", -1);
                    mediaRecorder.initRecorder(mVideoView,true, 720, 480, String.format("life_segment_%s.mp4", index), LIVE_VIDEO_FOLDER);
                    mediaRecorder.getmRecorder().setOnInfoListener(new MediaRecorder.OnInfoListener() {
                        @Override
                        public void onInfo(MediaRecorder mr, int what, int extra) {
                            Log.v(TAG, "OnInfoListener() called");
                            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                                restartRecorder();
                            }
                        }
                    });
                    mediaRecorder.prepare();
                    mediaRecorder.getmRecorder().start();
                } else {
                    Toast.makeText(getApplicationContext(), "no internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showProgressDialog(int maxValue) {
        Log.v(TAG,"maxValue is "+maxValue);
        barProgressDialog = new ProgressDialog(MainActivity.this);
        barProgressDialog.setTitle("Upload");
        barProgressDialog.setMessage("Uploading Video");
        barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setProgress(0);
        barProgressDialog.setMax(maxValue);
        barProgressDialog.setProgressPercentFormat(null);
        barProgressDialog.show();
    }

    private void restartRecorder(){
        if(mediaRecorder!=null) {
            if(mediaRecorder.getmRecorder()!=null) {
                mediaRecorder.getmRecorder().reset();
                if (HttpUtils.isNetworkAvailable(getApplicationContext())) {
                    String old_path = STORAGE_PATH_LIVE + String.format("/life_segment_%s.mp4", index);
                    UploadStreamlet uploadStreamlet = new UploadStreamlet(mLiveSvid, index, old_path);
                    log_to_file(String.valueOf(System.nanoTime()));
                    SendingQueue.add(uploadStreamlet);
                }
                else {//lost internet. check if queue is not empty. Flush it in that case
                    if(!SendingQueue.isEmpty()){
                        SendingQueue.flush();
                    }
                }
                index++;
                mediaRecorder.initRecorder(mVideoView,true, 720, 480, String.format("life_segment_%s.mp4", index), LIVE_VIDEO_FOLDER);
                mediaRecorder.getmRecorder().setOnInfoListener(new MediaRecorder.OnInfoListener() {
                    @Override
                    public void onInfo(MediaRecorder mr, int what, int extra) {
                        Log.v(TAG, "OnInfoListener() called");
                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                            restartRecorder();
                        }
                    }
                });
                mediaRecorder.prepare();
                mediaRecorder.getmRecorder().start();
            }
        }
    }

    public static void log_to_file(String str){
        String TmpDirPath = Environment.getExternalStorageDirectory().getPath();
        File logFile = new File(TmpDirPath+"/send_enqueue_log.txt");
        boolean success = true;
        if (!logFile.exists())
        {
            try
            {
                success = logFile.createNewFile();
                Log.v("log_to_file","new logfile created");
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(str);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}