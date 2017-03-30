package com.androidvideoconverter.app.uploader;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Wrapper for uploading streamlets Queue
 */
public class SendingQueue{
    private static ConcurrentLinkedQueue<UploadStreamlet> mSendingQueue = new ConcurrentLinkedQueue<>();

    public static void add(UploadStreamlet s){
        mSendingQueue.add(s);
    }

    public static boolean isEmpty(){
        return mSendingQueue.isEmpty();
    }

    public static UploadStreamlet poll(){
        return mSendingQueue.poll();
    }

    public static void flush(){
        mSendingQueue.clear();
    }

    public static int curr_size(){
        return mSendingQueue.size();
    }
}
