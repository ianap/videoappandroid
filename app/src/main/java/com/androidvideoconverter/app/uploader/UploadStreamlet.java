package com.androidvideoconverter.app.uploader;

/**
 * Creates upload streamlet objects for sending queue.
 */
public class UploadStreamlet {
    String s_vid;
    int streamlet_no;
    String filePath;

    public UploadStreamlet(String s_vid, int streamlet_no, String filePath){
        this.s_vid = s_vid;
        this.streamlet_no = streamlet_no;
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getStreamlet_no() {
        return streamlet_no;
    }

}
