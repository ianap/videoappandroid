package com.androidvideoconverter.app.events;

/**
 * Wrapper for  End upload event.
 *
 */
public class EndUploadEvent {
    String s_vid;

    public EndUploadEvent(String s_vid){
        this.s_vid = s_vid;
    }

    public String get_svid() {
        return s_vid;
    }
}
