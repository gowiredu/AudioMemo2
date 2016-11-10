package com.gowiredu.audiomemotest2;

public class Audio {
    private String title;
    //private String transcription_preview;

    Audio(String title){
        title = title;
        //transcription_preview = transcription_preview;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    /*
    public String getTranscriptionPreview() {
        return transcription_preview;
    }

    public void setTranscriptionPreview(String transcription_preview) {
        this.transcription_preview = transcription_preview;
    }
    */
}