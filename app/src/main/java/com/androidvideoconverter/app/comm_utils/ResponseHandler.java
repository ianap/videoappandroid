package com.androidvideoconverter.app.comm_utils;

import android.util.Log;

import com.androidvideoconverter.app.activity.MainActivity;
import com.androidvideoconverter.app.events.EndUploadEvent;
import com.androidvideoconverter.app.events.ErrorMsgEvent;
import com.androidvideoconverter.app.events.StartLiveUploadEvent;
import com.androidvideoconverter.app.events.StreamletAckEvent;
import retrofit.client.Response;

import static com.androidvideoconverter.app.comm_utils.ResponseUtils.ResponseToString;
import static com.androidvideoconverter.app.comm_utils.ResponseUtils.convertToResponseMsg;
import static com.androidvideoconverter.app.comm_utils.ResponseUtils.isValidMsg;
import static com.androidvideoconverter.app.events.EventBusProvider.getEventBus;

/**
 * Handles HTTP responses from the server.
 * Sends different type of events for processing server response.
 */
public class ResponseHandler {

    public static void handle_response(Response response){
        String string_response = ResponseToString(response);

        ResponseMsg response_msg = convertToResponseMsg(string_response);
        if(response_msg == null) {
            Log.v("ResponseHandlerVod", "INVALID JSON RESPONSE: " + string_response);
            return; //we do not have a valid JSON string
        }

        Log.v("ResponseHandler", "handle_response() called with " + string_response);

        if(!isValidMsg(response_msg)){
            Log.v("ResponseHandlerVod", "Msg Validation Failed");
        }

        switch(response_msg.get_type()){
            case start_upload:
                handle_start_upload(response_msg);
                break;
            case upload_streamlet_ack:
                handle_upload_streamlet_ack(response_msg);
                break;
            case end_upload:
                handle_end_upload(response_msg);
                break;
            case error_msg:
                handle_err_msg(response_msg);
                break;
            default:
                Log.v("ResponseHandler", "Undefined msg_type");
                break;
        }

    }

    public static void handle_err_msg(ResponseMsg response_msg){
        Log.v("ResponseHandler", "handle_err_msg() called");
        Log.v("ResponseHandler", "The error_str is \"" + response_msg.get_msg_contents().get("error_str") + "\"");
        getEventBus().post(new ErrorMsgEvent(response_msg.get_msg_contents().get("error_str")));
    }

    public static void handle_start_upload(ResponseMsg response_msg){
        Log.v("ResponseHandler", "handle_start_upload() called");
        String s_vid = response_msg.get_msg_contents().get("s_vid");
        int streamlet_no = Integer.parseInt(response_msg.get_msg_contents().get("streamlet_no"));
        if(MainActivity.mIsLive)
            getEventBus().post(new StartLiveUploadEvent(s_vid,streamlet_no));
        else
            getEventBus().post(new StreamletAckEvent(s_vid,streamlet_no));
    }

    public static void handle_upload_streamlet_ack(ResponseMsg response_msg){
        Log.v("ResponseHandler", "handle_upload_streamlet_ack() called");
        getEventBus().post(new StreamletAckEvent(response_msg.get_msg_contents().get("s_vid"),
                Integer.parseInt(response_msg.get_msg_contents().get("streamlet_no"))+1));
       // getEventBus().post(new StreamletAckEvent(response_msg.get_msg_contents().get("s_vid"),
      //          Integer.parseInt(response_msg.get_msg_contents().get("streamlet_no"))));
    }

    public static void handle_end_upload(ResponseMsg response_msg){
        Log.v("ResponseHandler", "handle_end_upload() called");
        getEventBus().post(new EndUploadEvent(response_msg.get_msg_contents().get("s_vid")));
    }
}
