package com.androidvideoconverter.app.comm_utils;


import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;


/**
 * Interface that defines server contract.
 */
public interface VideoStreamingService {
    @Multipart
    @POST("/~team04/streaming_server/msg_handler.php")
    void new_upload_vod(@Part("client_id") String client_id,
                        @Part("msg_type") String msg_type,
                        @Part("media_type") String media_type,
                        @Part("total_streamlets") Integer total_streamlets,
                        Callback<Response> cb);

    @Multipart
    @POST("/~team04/streaming_server/msg_handler.php")
    void new_upload_live(@Part("client_id") String client_id,
                         @Part("msg_type") String msg_type,
                         @Part("media_type") String media_type,
                         Callback<Response> cb);

    @Multipart
    @POST("/~team04/streaming_server/msg_handler.php")
    void upload_streamlet(@Part("client_id") String client_id,
                          @Part("msg_type") String msg_type,
                          @Part("s_vid") String s_vid,
                          @Part("streamlet_no") Integer streamlet_no,
                          @Part("fileUpload") TypedFile streamlet_file,
                          Callback<Response> cb);

    @Multipart
    @POST("/~team04/streaming_server/msg_handler.php")
    Response upload_streamlet_live(@Part("client_id") String client_id,
                          @Part("msg_type") String msg_type,
                          @Part("s_vid") String s_vid,
                          @Part("streamlet_no") Integer streamlet_no,
                          @Part("fileUpload") TypedFile streamlet_file);

    @Multipart
    @POST("/~team04/streaming_server/msg_handler.php")
    void resume_upload(@Part("client_id") String client_id,
                       @Part("msg_type") String msg_type,
                       @Part("s_vid") String s_vid,
                       Callback<Response> cb);

    @Multipart
    @POST("/~team04/streaming_server/msg_handler.php")
    void done_upload(@Part("client_id") String client_id,
                     @Part("msg_type") String msg_type,
                     @Part("s_vid") String s_vid,
                     Callback<Response> cb);

}
