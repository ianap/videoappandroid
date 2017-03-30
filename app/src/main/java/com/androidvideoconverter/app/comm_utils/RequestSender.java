package com.androidvideoconverter.app.comm_utils;

import android.util.Log;

import com.androidvideoconverter.app.events.ErrorMsgEvent;

import java.io.File;
import java.net.SocketException;

import retrofit.Callback;
import retrofit.Profiler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import static com.androidvideoconverter.app.comm_utils.ResponseUtils.ResponseToString;
import static com.androidvideoconverter.app.comm_utils.ResponseUtils.convertToResponseMsg;
import static com.androidvideoconverter.app.events.EventBusProvider.getEventBus;

/**
 * Sends HTTP request to start upload, to upload streamlet and to end upload. *
 */
public class RequestSender {
    private VideoStreamingService streaming_api;
    private static final String TAG = "RequestSender";
    private String client_id;

    public RequestSender(String clientID, String base_url){
        //Create a new profiler
        Profiler profiler = new Profiler() {
            @Override
            public Object beforeCall() {
                return null;
            }

            @Override
            public void afterCall(RequestInformation requestInfo, long elapsedTime, int statusCode, Object beforeCallData) {
                Log.v("PROFILER", "Status Code: " + statusCode);
                Log.v("PROFILER", "Time taken for the request " + elapsedTime + "ms");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(base_url)
                .setProfiler(profiler)
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build();

        streaming_api = restAdapter.create(VideoStreamingService.class);
        client_id = clientID;
    }

    public void send_new_upload(String media_type, int total_streamlets){

        //Prepare a Callback to be sent to the async request
        Callback<Response> cb = new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Log.v("send_new_upload", "Returned SUCCESS");
                ResponseHandler.handle_response(response);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.v("send_new_upload", "Returned FAILED");
                if(error.getResponse()==null){
                    getEventBus().post(new ErrorMsgEvent(null));
                    return;
                }
                ResponseHandler.handle_response(error.getResponse());
            }
        };

        if(media_type.equals("vod"))
            streaming_api.new_upload_vod(client_id, "new_upload", media_type, total_streamlets, cb);
        else if(media_type.equals("live"))
            streaming_api.new_upload_live(client_id, "new_upload", media_type,cb);
        else {
            Log.v("send_new_upload", "Invalid media_type");
            return;
        }
    }

    public void send_upload_streamlet(String s_vid,Integer streamlet_no, String streamlet_path){
        File file = new File(streamlet_path);
        if(file == null) {
            Log.v("NULL", "File streamlet is NULL");
            return;
        }

        TypedFile streamlet_file = new TypedFile("video/mp4",file);
        if(streamlet_file == null) {
            Log.v("NULL", "TypedFile streamlet_file is NULL");
            return;
        }

        //Prepare a Callback to be sent to the async request
        Callback<Response> cb = new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Log.v("send_upload_streamlet", "Returned SUCCESS");
                ResponseHandler.handle_response(response);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.v("send_upload_streamlet", "Returned FAILED");
                if(error.getResponse()==null){
                    getEventBus().post(new ErrorMsgEvent(null));
                    return;
                }
                ResponseHandler.handle_response(error.getResponse());
            }
        };

        streaming_api.upload_streamlet(client_id, "upload_streamlet", s_vid, streamlet_no, streamlet_file,cb);

    }

    /*
    For 'live' uploader
     */
    public void send_upload_streamlet_live(String s_vid,Integer streamlet_no, String streamlet_path){
        File file = new File(streamlet_path);
        Log.v("upload_streamlet_live",streamlet_path);
        if(file == null) {
            Log.v("NULL", "File streamlet is NULL");

        }

        TypedFile streamlet_file = new TypedFile("video/mp4",file);
        if(streamlet_file == null) {
            Log.v("NULL", "TypedFile streamlet_file is NULL");

        }

        try {
            Response response = streaming_api.upload_streamlet_live(client_id, "upload_streamlet", s_vid, streamlet_no, streamlet_file);
            if(response.getBody().toString()!=null)
                Log.v("upload_streamlet_live","RESPONSE: "+response.getBody().toString());
        }
        catch (Exception e)
        {
            Log.e(TAG,"upload_streamlet_live failed:"+e.getMessage());
        }


    }

    public void send_resume_upload(String s_vid){
        Callback<Response> cb = new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Log.v("send_resume_upload", "Returned SUCCESS");
                ResponseHandler.handle_response(response);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.v("send_resume_upload", "Returned FAILED");
                if(error.getResponse()==null){
                    getEventBus().post(new ErrorMsgEvent(null));
                    return;
                }
                ResponseHandler.handle_response(error.getResponse());
            }
        };

        streaming_api.resume_upload(client_id, "resume_upload", s_vid, cb);
    }

    public void send_done_upload(String s_vid){
        Callback<Response> cb = new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Log.v("send_done_upload", "Returned SUCCESS");
                ResponseHandler.handle_response(response);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.v("send_done_upload", "Returned FAILED");
                if(error.getResponse()==null){
                    getEventBus().post(new ErrorMsgEvent(null));
                    return;
                }
                ResponseHandler.handle_response(error.getResponse());
            }
        };

        streaming_api.done_upload(client_id,"done_upload",s_vid,cb);
    }

}
