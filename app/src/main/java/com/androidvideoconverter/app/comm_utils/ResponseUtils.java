package com.androidvideoconverter.app.comm_utils;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

/**
 * Server response utility to convert messages between client and server
 */
public class ResponseUtils {
    public static Boolean isValidJSONResponse(String jsonString){
        try {
            new JsonParser().parse(jsonString);
            return true;
        } catch (JsonSyntaxException jse) {
            return false;
        }
    }

    public static String ResponseToString(Response response){
        String s = new String(((TypedByteArray) response.getBody()).getBytes());
        return s;
    }

    public static ResponseMsg convertToResponseMsg(String response){

        if(response == null)
            return null;

        if(isValidJSONResponse(response)){
            ResponseMsg response_msg = new ResponseMsg(response);
            return response_msg;
        }
        else {
            return null;
        }
    }

    public static boolean isValidMsg(ResponseMsg msg){
        //TODO Write the basic as well as special validation code
        return true;
    }
}
