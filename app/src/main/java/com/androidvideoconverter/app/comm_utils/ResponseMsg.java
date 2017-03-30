package com.androidvideoconverter.app.comm_utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

/*
* Response message handler. Converts string server message to enumeration.
 */
public class ResponseMsg {

    private HashMap<String,String> msg_contents;

    public ResponseMsg(String json_string){
        msg_contents = new Gson().fromJson(json_string, new TypeToken<HashMap<String, String>>(){}.getType());
    }

    public HashMap<String,String> get_msg_contents(){return msg_contents;}

    public MsgType get_type(){
        MsgType msg_type = MsgType.undefined;
        switch (msg_contents.get("msg_type")){
            case "start_upload":
                msg_type = MsgType.start_upload;
                break;
            case "upload_streamlet_ack":
                msg_type = MsgType.upload_streamlet_ack;
                break;
            case "end_upload":
                msg_type = MsgType.end_upload;
                break;
            case "error_msg":
                msg_type = MsgType.error_msg;
                break;
            default:
                msg_type = MsgType.undefined;
                break;
        }
        return msg_type;
    }

}
