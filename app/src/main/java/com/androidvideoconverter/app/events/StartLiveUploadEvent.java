package com.androidvideoconverter.app.events;

/**
 *  Wrapper for start live upload event.
 */
public class StartLiveUploadEvent {
    String s_vid;
    int streamlet_no;

    public StartLiveUploadEvent(String s_vid, int streamlet_no){
        this.s_vid=s_vid;
        this.streamlet_no = streamlet_no;
    }

    public String getS_vid() {
        return s_vid;
    }

    public int getStreamlet_no() {
        return streamlet_no;
    }
}
