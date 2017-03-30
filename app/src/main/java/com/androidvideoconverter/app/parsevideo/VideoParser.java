package com.androidvideoconverter.app.parsevideo;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;

import com.androidvideoconverter.app.mediarecorder.CustomMediaRecorder;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * Splitting an MP4 file into 3 s streamlets.
 * The output is  MP4 files
 * Gets the duration of a video.
 */
public class VideoParser extends AsyncTask<String, Integer, String> {
    public static final String VIDEO_TEST_FOLDER = "/RecordedVideoTest/";
    private static final int SEGMENT_LENGTH = 3;
    private static final File STORAGE_PATH = new File(Environment.getExternalStorageDirectory(), VIDEO_TEST_FOLDER);

    private String workingPath;
    private String fileName;
    private ProgressDialog progressDialog;
    private File videoFile ;
    public VideoParser(String workingPath, String fileName){
        this.fileName = fileName;
        this.workingPath = workingPath;
        this.videoFile = new File(workingPath, fileName);
    }
    @Override
    protected String doInBackground(String... params) {
        try {
            segmentVideo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void segmentVideo() throws IOException {
        Movie inputMovie = new Movie();
        double movieDuration = getDuration();
        if(videoFile.exists()){
            inputMovie = MovieCreator.build(videoFile.getPath());
        }
        STORAGE_PATH.mkdirs();
        int numberOfSegments = (int) movieDuration/ SEGMENT_LENGTH+1;
        Track audio = inputMovie.getTracks().get(0);
        Track video = inputMovie.getTracks().get(1);
        double fairStart =0;
        double startPos = 0;
        double endPos = SEGMENT_LENGTH;
        for(int i=0; i<numberOfSegments;i++){
            // get correct start/end time from all the tracks
            long startSampleAudio = findNextSyncSample(audio, startPos);
            long endSampleAudio = findNextSyncSample(audio, endPos);

            long startSampleVideo = findNextSyncSample(video, startPos);
            long endSampleVideo = findNextSyncSample(video, endPos);

            // split the track with the start and end sample time
            CroppedTrack audioCroppedTrack = new CroppedTrack(audio, startSampleAudio, endSampleAudio);
            CroppedTrack videoCroppedTrack = new CroppedTrack(video, startSampleVideo, endSampleVideo);
            // add tracks to the new Movie
            Movie movie = new Movie();
            movie.addTrack(audioCroppedTrack);
            movie.addTrack(videoCroppedTrack);
            Container outSegment  = new DefaultMp4Builder().build(movie);
            String timestamp = String.valueOf(new Date().getTime());
            //write new streamlet to the output file
            File segmentFile = new File(STORAGE_PATH, String.format("segment_%s.mp4",i));
            segmentFile.createNewFile();
            FileOutputStream fos =new FileOutputStream(segmentFile);
            outSegment.writeContainer(fos.getChannel());
            fos.close();

            fairStart = endPos;
            startPos = endPos-0.2;
            endPos = fairStart + SEGMENT_LENGTH;
            if(endPos > movieDuration) {
                endPos = movieDuration;
            }
        }
    }
    //Gets the duration of a video.
    private double getDuration() throws IOException {

        IsoFile isoFile = new IsoFile(videoFile.getPath());
        double lengthInSeconds = (double)
                isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        return lengthInSeconds;

    }

    private static long findNextSyncSample(Track track, double cutHere) {
        long currentSample = 0;
        double currentTime = 0;
        long[] durations = track.getSampleDurations();
        long[] syncSamples = track.getSyncSamples();
        for (int i = 0; i < durations.length; i++) {
            long delta = durations[i];

            if ((syncSamples == null || syncSamples.length > 0 || Arrays.binarySearch(syncSamples, currentSample + 1) >= 0)
                    && currentTime > cutHere) {
                return i;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;
        }
        return currentSample;
    }
}
