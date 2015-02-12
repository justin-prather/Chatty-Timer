package com.bradon.justinprather.chattytimerapp;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

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
    private LinkedList<TimeObject> runningTimeList;
    private CountDownTimer timer = null;
    private TimeObject interval;
    private boolean isPaused = false;
    private static final String PROPERTY_ID = "UA-58922860-2";
    private ObservableTextObject mObservable;
    private int lastHour = 0;
    private int lastMinute = 0;
    private int lastSecond = 0;
    private NotificationManager mNotificationManager;
    private int notificationID = 74;
    private NotificationCompat.Builder  mBuilder;
    private AudioManager mAudioManager;
    private HashMap<String, String> mAudioParams;

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

                timerTTS.setOnUtteranceCompletedListener( new TextToSpeech.OnUtteranceCompletedListener() {
                    @Override
                    public void onUtteranceCompleted(String utteranceId) {
                        Log.d("AUDIO",mAudioManager.abandonAudioFocus(null) + "");
                    }
                });
            }
        });

        Mint.initAndStartSession(getApplicationContext(), "6b2f0ef1");
        mObservable = new ObservableTextObject();
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

        runningTimeList.addLast( new TimeObject( getString(R.string.last_timer_note), 0 ) );

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_stat_chattytimernotificationicon);
        mBuilder.setAutoCancel(true);
        long[] vibePattern = {0, 750};
        mBuilder.setVibrate( vibePattern );

         /* Creates an explicit intent for an Activity in your app */
        Intent resultIntent = new Intent(this, RunTimerActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(RunTimerActivity.class);

      /* Adds the Intent that starts the Activity to the top of the stack */
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        mAudioParams = new HashMap<String, String>();
        mAudioParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");

        lastHour = 0;
        lastMinute = 0;
        lastSecond = 0;

        mObservable.setDone(false);

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
        mObservable.setComment(interval.getTimeComment());

        lastHour = (int) (interval.getTimeMillis() / (1000*60*60)) % 24;
        lastMinute = (int) ((interval.getTimeMillis() / (1000*60)) % 60);
        lastSecond = (int) (interval.getTimeMillis() / 1000) % 60;
        mObservable.setHours( lastHour );
        mObservable.setMinutes( lastMinute );
        mObservable.setSeconds( lastSecond );

        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

        timerTTS.speak(interval.getTimeComment(), TextToSpeech.QUEUE_FLUSH, mAudioParams);

        timer = new CountDownTimer( interval.getTimeMillis(), 100 ) {
            @Override
            public void onTick(long millisUntilFinished) {
                if( mObservable.countObservers() > 0 ){
                    interval.setTimeMillis( (int) millisUntilFinished );
//                    Log.d("Timer Tag", "current millis: " + millisUntilFinished );
                    updateRunTimerActivity();
                }
            }

            @Override
            public void onFinish() {
                interval.setTimeMillis( 0 );
                if( mObservable.countObservers() > 0 ){
                    updateRunTimerActivity();
                }

                runningTimeList.pollFirst();

                if( runningTimeList.size() > 1 ){
                    mBuilder.setContentTitle( mObservable.getComment()
                            + getString(R.string.notification_just_finished));
                    mBuilder.setWhen( System.currentTimeMillis() );
                    mBuilder.setContentText( runningTimeList.getFirst().getTimeComment() +
                            getString(R.string.notification_is_starting) );
                    mBuilder.setTicker( mObservable.getComment() +  getString(R.string.notification_just_finished));
                }
                else{
                    mBuilder.setContentTitle( timeList.getLast().getTimeComment()
                            + getString(R.string.notification_just_finished));
                    mBuilder.setWhen( System.currentTimeMillis() );
                    mBuilder.setContentText(getString(R.string.notification_all_finished));
                    mBuilder.setTicker(getString(R.string.notification_all_finished));
                }

                mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                /* notificationID allows you to update the notification later on. */
                mNotificationManager.notify(notificationID, mBuilder.build());

                if( !runningTimeList.isEmpty() ){
                    startNext();
                }

                else{
                    runningTimeList = null;
                    timer = null;
                    isPaused = true;
                    mObservable.setDone(true);
                }
            }
        };

        timer.start();
    }

    private void updateRunTimerActivity() {
        long milliseconds = interval.getTimeMillis();
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

        if( lastSecond != seconds ) {
            mObservable.setSeconds(seconds);
            lastSecond = seconds;

            if( lastMinute != minutes ){
                mObservable.setMinutes(minutes);
                lastMinute = minutes;

                if( lastHour != hours ){
                    mObservable.setHours(hours);
                    lastHour = hours;
                }
            }
        }
    }

    private void updateRunTimerActivity( long milliseconds ) {
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

        if( lastSecond != seconds ) {
            mObservable.setSeconds(seconds);
            lastSecond = seconds;

            if( lastMinute != minutes ){
                mObservable.setMinutes(minutes);
                lastMinute = minutes;

                if( lastHour != hours ){
                    mObservable.setHours(hours);
                    lastHour = hours;
                }
            }
        }
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
        updateRunTimerActivity(0);
        runningTimeList = null;
        mObservable.setComment(getString(R.string.last_timer_note));
        return false;
    }

    public boolean isRunning(){
        return timer != null;
    }

    public boolean hasCurrent(){
        return runningTimeList != null;

    }

    public ObservableTextObject getObservable() {
        return mObservable;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}


