package com.androidvideoconverter.app.comm_utils;

/**
 * Enumeration of message types which can be sent to the server.
 */
public enum MsgType{
    error_msg,
    start_upload,
    upload_streamlet_ack,
    end_upload,
    undefined
}
