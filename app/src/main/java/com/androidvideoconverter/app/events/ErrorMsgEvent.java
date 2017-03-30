package com.androidvideoconverter.app.events;

/**
 * Wrapper for  Error event.
 */
public class ErrorMsgEvent {
    String err_string;

    public ErrorMsgEvent(String err_string){
        this.err_string = err_string;
    }

    public String get_err_string(){return err_string;}

}
