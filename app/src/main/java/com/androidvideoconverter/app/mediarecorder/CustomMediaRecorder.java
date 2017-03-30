package com.androidvideoconverter.app.mediarecorder;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

/**
 * CustomMediaRecorder  is a class that can display the
 * live image data coming from a camera, so users can capture a video.
 * This class implements SurfaceHolder.Callback in order to capture the
 * callback events for creating and destroying the view, which are needed
 * for assigning the camera preview input.
 * The output is a video-only MP4 file
 *
 */
public class CustomMediaRecorder implements SurfaceHolder.Callback{

    private static final String TAG = CustomMediaRecorder.class.getName();
    private MediaRecorder mRecorder;
    private SurfaceHolder mHolder;
    private Camera mCamera = null;
    public static int orientation;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG, "in surfaceCreated");
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            }
        } catch (IOException e) {
            Log.v(TAG, "Could not start the preview");
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(TAG, "surfaceChanged: Width x Height = " + width + "x" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(TAG, "in surfaceDestroyed");
    }

    @SuppressWarnings("deprecation")
    public boolean initCamera(VideoView mVideoView) {
        try {
            mCamera = Camera.open();
            mCamera.lock();
            mHolder = mVideoView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        } catch (RuntimeException re) {
            Log.v(TAG, "Could not initialize the Camera");
            re.printStackTrace();
            return false;
        }
        return true;
    }

    public void releaseRecorder() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.reconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void initRecorder(VideoView mVideoView,boolean isLife, Integer resolution1,Integer resolution2, String fileName, String mediaStorageDir) {
        try {
            initCamera(mVideoView);
            mCamera.unlock();
            mRecorder = new MediaRecorder();
            mRecorder.setCamera(mCamera);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mRecorder.setAudioChannels(2);
            mRecorder.setAudioEncodingBitRate(128000);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setVideoSize(resolution1, resolution2);
            mRecorder.setVideoFrameRate(30);
            mRecorder.setVideoEncodingBitRate(3000000);
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setPreviewDisplay(mHolder.getSurface());
            if(isLife) {
                mRecorder.setMaxDuration(3000);
            }
            // save file
            File mediaDir = new File(Environment.getExternalStorageDirectory()+mediaStorageDir);
            if ( !mediaDir.exists() ) {
                if ( !mediaDir.mkdirs() ){
                    Log.v(TAG, "failed to create directory");
                }
            }
            String filePath = Environment.getExternalStorageDirectory()+mediaStorageDir+fileName;
            mRecorder.setOutputFile(filePath);
            Log.v(TAG, "MediaRecorder initialized");

        } catch (Exception e) {
        Log.v(TAG, "MediaRecorder failed to initialize");
        e.printStackTrace();
    }
}
    public void prepare(){
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaRecorder getmRecorder() {
        return mRecorder;
    }
}
