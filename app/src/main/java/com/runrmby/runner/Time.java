package com.runrmby.runner;

/**
 * Created by Mark on 2/9/2017.
 */

public class Time {

    private long time;
    private int minutes;
    private int seconds;
    private int milliseconds;
    private String timeForDisplay;

    public Time(){ //(int minutes, int seconds, int milliseconds, CharSequence timeForDisplay){
//        this.minutes = minutes;
//        this.seconds = seconds;
//        this.milliseconds = milliseconds;
//        this.timeForDisplay = timeForDisplay;
        this.minutes = 0;
        this.seconds = 0;
        this.milliseconds = 0;
        this.timeForDisplay = "0:00:00";
    }

    public long getTime(){
        return this.time;
    }

    public int getMinutes(){
        return this.minutes;
    }

    public int getSeconds(){
        return this.seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public String getTimeForDisplay(){
        return timeForDisplay;
    }

    public void changeTime(final long time) {
        this.time = time;
        updateTime(this.time);
    }

    private void updateTime(final long time){
        this.minutes = (int) time / 60000;
        int remainder = (int) time - minutes * 60000;
        this.seconds = (int) remainder / 1000;
        String secondsString;
        if(seconds > 10) {
            secondsString = String.valueOf(seconds);
        }else {
            secondsString = "0" + seconds;
        }
        remainder = remainder - seconds * 1000;
        this.milliseconds = remainder;
        this.timeForDisplay = minutes + ":" + secondsString + "." + milliseconds;
    }
}