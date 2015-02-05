package com.bradon.justinprather.chattytimerapp;

import android.app.Application;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.splunk.mint.Mint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by justinprather on 2015-01-05.
 */
public class TalkingTimerApplication extends Application {

    public LinkedList<TimeObject> timeList;
    public TextToSpeech timerTTS;
    private TextSwitcher RunTimerHours = null;
    private TextSwitcher RunTimerMinutes = null;
    private TextSwitcher RunTimerSeconds = null;
    private TextView RunTimerComment = null;
    private LinkedList<TimeObject> runningTimeList;
    private CountDownTimer timer = null;
    private TimeObject interval;
    private boolean isPaused = false;
    private static final String PROPERTY_ID = "UA-58922860-2";

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    @Override
    public void onCreate() {
        super.onCreate();

        timeList = new LinkedList<TimeObject>();

        timerTTS = new TextToSpeech( getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if ( status != TextToSpeech.ERROR ){
                    timerTTS.setLanguage(Locale.CANADA);
                }
            }
        });

        Mint.initAndStartSession(getApplicationContext(), "6b2f0ef1");

    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    public void startTimer(){

        runningTimeList = deepCopy( timeList );

       // runningTimeList.addFirst( new TimeObject( "", 3000 ) );
        runningTimeList.addLast( new TimeObject( "All Timers Finished", 0 ) );
        startNext();
    }

    private LinkedList<TimeObject> deepCopy(LinkedList<TimeObject> timeList) {
        LinkedList<TimeObject> retList = new LinkedList<TimeObject>();

        for( int i = 0; i < timeList.size(); i++ ){
            retList.add( i, new TimeObject( timeList.get(i) ) );
        }

        return retList;
    }

    public void startNext() {
        interval = runningTimeList.getFirst();
        RunTimerComment.setText( interval.getTimeComment() );

        timerTTS.speak(interval.getTimeComment(), TextToSpeech.QUEUE_FLUSH, null);

        timer = new CountDownTimer( interval.getTimeMillis(), 100 ) {
            @Override
            public void onTick(long millisUntilFinished) {
                if( RunTimerHours != null && RunTimerMinutes != null && RunTimerSeconds != null ){
                    interval.setTimeMillis( (int) millisUntilFinished );
//                    Log.d("Timer Tag", "current millis: " + millisUntilFinished );
                    updateRunTimerActivity();
                }
            }

            @Override
            public void onFinish() {
                interval.setTimeMillis( 0 );
                if( RunTimerHours != null && RunTimerMinutes != null && RunTimerSeconds != null ){
                    updateRunTimerActivity();
                }

                runningTimeList.pollFirst();
                if( !runningTimeList.isEmpty() ){
                    startNext();
                }

                else{
                    runningTimeList = null;
                    timer = null;
                    isPaused = true;
                }
            }
        };

        timer.start();
    }

    private void updateRunTimerActivity() {
        long milliseconds = interval.getTimeMillis();
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

        RunTimerHours.setText( String.format("%02d", hours));
        RunTimerMinutes.setText( String.format("%02d", minutes));
        RunTimerSeconds.setText( String.format("%02d", seconds));
    }

    private void updateRunTimerActivity( long milliseconds ) {
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

        RunTimerHours.setText( String.format("%02d", hours));
        RunTimerMinutes.setText( String.format("%02d", minutes));
        RunTimerSeconds.setText( String.format("%02d", seconds));
    }

    public TextSwitcher getRunTimerHours() {
        return RunTimerHours;
    }

    public void setRunTimerHours(TextSwitcher runTimerHours) {
        RunTimerHours = runTimerHours;
    }

    public TextSwitcher getRunTimerMinutes() {
        return RunTimerMinutes;
    }

    public void setRunTimerMinutes(TextSwitcher runTimerMinutes) {
        RunTimerMinutes = runTimerMinutes;
    }

    public TextSwitcher getRunTimerSeconds() {
        return RunTimerSeconds;
    }

    public void setRunTimerSeconds(TextSwitcher runTimerSeconds) {
        RunTimerSeconds = runTimerSeconds;
    }

    public TextView getRunTimerComment() {
        return RunTimerComment;
    }

    public void setRunTimerComment(TextView runTimerComment) {
        RunTimerComment = runTimerComment;
    }

    public boolean isPaused(){ return this.isPaused; }

    public void setPause(){
        this.isPaused = true;
    }

    public void clearPause(){
        this.isPaused = false;
    }

    public void stopTimer(){
        if( timer != null ) timer.cancel();
        timer = null;
    }

    public boolean resetRunTimer(){
        if(!timeList.isEmpty()) {
            runningTimeList = deepCopy(timeList);
            updateRunTimerActivity(runningTimeList.getFirst().getTimeMillis());
            return true;
        }
        return false;
    }

    public boolean isRunning(){
        if (timer == null) return false;
        else return true;
    }

    public boolean hasCurrent(){
        if ( runningTimeList == null ) return false;

        return true;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}


