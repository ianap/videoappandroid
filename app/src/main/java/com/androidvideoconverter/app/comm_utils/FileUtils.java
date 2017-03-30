package com.androidvideoconverter.app.comm_utils;

import android.os.Environment;

import java.io.File;

/**
 * Removes all files from a folder.
 */
public class FileUtils {
    static  public void cleanUpFolder(String folderPath) {
        File mediaDir = new File(Environment.getExternalStorageDirectory()+folderPath);
        if (mediaDir.exists()) {
            String[] myFiles;
            if (mediaDir.isDirectory()) {
                myFiles = mediaDir.list();
                for (int i = 0; i < myFiles.length; i++) {
                    File myFile = new File(mediaDir, myFiles[i]);
                    myFile.delete();
                }
            }
        }
    }
}
