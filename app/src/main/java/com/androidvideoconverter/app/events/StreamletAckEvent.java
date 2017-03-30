package com.androidvideoconverter.app.events;

/**
 *  Wrapper for  streamlet ack event.
 */
public class StreamletAckEvent {
    String s_vid;
    Integer streamlet_no;

    public StreamletAckEvent(String s_vid,Integer streamlet_no){
        this.s_vid = s_vid;
        this.streamlet_no = streamlet_no;
    }

    public String get_svid(){return s_vid;}

    public Integer getStreamlet_no() {
        return streamlet_no;
    }
}
