package com.androidvideoconverter.app.uploader;

import android.util.Log;

import com.androidvideoconverter.app.comm_utils.RequestSender;

/**
 * Sends streamlet to the server from the sending queue
 */
public class UploadEngine {
    private static final String TAG = "UploadEngine";
    String mS_vid;
    RequestSender mRequestSender;
    int mCurrStreamlet;
    Thread mUploadEngineThread;
    boolean mShouldStop;
    boolean mLiveEnded;

    public UploadEngine(String mS_vid, RequestSender requestSender){
        mRequestSender = requestSender;
        this.mS_vid = mS_vid;
        this.mCurrStreamlet = 0;
        this.mShouldStop = false;
        this.mLiveEnded = false;
    }

    public void start(){
        mShouldStop = false;
        mCurrStreamlet = 0;
        mUploadEngineThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runUploadEngine();
            }
        });

        mUploadEngineThread.start();
        Log.v(TAG,"UploadEngine started on thread "+Long.toString(mUploadEngineThread.getId()));
    }

    public void runUploadEngine(){
        while(!mShouldStop){
            if(!SendingQueue.isEmpty()){
                UploadStreamlet streamlet = SendingQueue.poll();
                Log.v(TAG,"Current mCurrStreamlet for upload is "+mCurrStreamlet+" and file is "+streamlet.getFilePath()+
                " 'index' is "+streamlet.getStreamlet_no());
                mRequestSender.send_upload_streamlet_live(mS_vid,mCurrStreamlet,streamlet.getFilePath());
                mCurrStreamlet++;
            }
            else if(SendingQueue.isEmpty() && mLiveEnded) {
                mRequestSender.send_done_upload(mS_vid);
                break;
            }

            try {
                Thread.sleep(1);                 //1000 milliseconds is one second.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }
    }

    public void stop() throws Exception{
        mShouldStop=true;
        if(mUploadEngineThread != null)
            mUploadEngineThread.join();
    }

    public boolean isRunning() {
        return mUploadEngineThread!=null && mUploadEngineThread.isAlive();
    }

    public void setLiveEnded(boolean state){
        mLiveEnded = state;
    }

}
