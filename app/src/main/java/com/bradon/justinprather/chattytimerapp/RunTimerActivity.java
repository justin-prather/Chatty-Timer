package com.bradon.justinprather.chattytimerapp;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class RunTimerActivity extends ActionBarActivity {

    private TalkingTimerApplication mApp;
    private Button pauseButton;
    private TextSwitcher hours;
    private TextSwitcher minutes;
    private TextSwitcher seconds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_timer);

        mApp = (TalkingTimerApplication) getApplication();

        pauseButton = (Button) findViewById(R.id.button_pause);

        hours = (TextSwitcher) findViewById(R.id.run_timer_hours);
        minutes = (TextSwitcher) findViewById(R.id.run_timer_minutes);
        seconds = (TextSwitcher) findViewById(R.id.run_timer_seconds);

        hours.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(RunTimerActivity.this);
                myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(50);
                myText.setTextColor(Color.BLACK);
                return myText;
            }
        });

        minutes.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(RunTimerActivity.this);
                myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(50);
                myText.setTextColor(Color.BLACK);
                return myText;
            }
        });

        seconds.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(RunTimerActivity.this);
                myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(50);
                myText.setTextColor(Color.BLACK);
                return myText;
            }
        });

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(this,android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        // set the animation type of textSwitcher
        hours.setInAnimation(in);
        hours.setOutAnimation(out);

        minutes.setInAnimation(in);
        minutes.setOutAnimation(out);

        seconds.setInAnimation(in);
        seconds.setOutAnimation(out);

        mApp.setRunTimerHours( hours );
        mApp.setRunTimerMinutes( minutes );
        mApp.setRunTimerSeconds( seconds );
        mApp.setRunTimerComment( (TextView) findViewById(R.id.runTimer_time_note));

        if( !mApp.isRunning() ) {
            mApp.startTimer();
            mApp.clearPause();
        }

        // Get tracker.
        Tracker t = ((TalkingTimerApplication) this.getApplication()).getTracker(
                TalkingTimerApplication.TrackerName.APP_TRACKER);

        // Enable Advertising Features.
        t.enableAdvertisingIdCollection(true);

        // Set screen name.
        t.setScreenName("RunTimer");

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

    }

    @Override
    protected void onResume(){
        super.onResume();
        mApp.setRunTimerHours( hours );
        mApp.setRunTimerMinutes( minutes );
        mApp.setRunTimerSeconds( seconds );
        mApp.setRunTimerComment( (TextView) findViewById(R.id.runTimer_time_note));
    }

    @Override
    protected void onPause(){
        super.onPause();
        mApp.setRunTimerHours( null );
        mApp.setRunTimerMinutes( null );
        mApp.setRunTimerSeconds( null );
        mApp.setRunTimerComment( null );
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_run_timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_rate) {
            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onPauseClick( View view ){

        if( mApp.isPaused() ){
            pauseButton.setText(R.string.button_pause_text);
            mApp.clearPause();
            if( mApp.hasCurrent() ) {
                mApp.startNext();
            }
            else{
                mApp.startTimer();
            }
        }

        else{
            pauseButton.setText(R.string.button_resume_text);
            mApp.setPause();
            mApp.stopTimer();
        }

    }

    public void onResetClick( View view ){
        mApp.stopTimer();
        if( mApp.resetRunTimer() ) {
            mApp.setPause();
            pauseButton.setText(R.string.button_resume_text);
        }

        else{
            Toast noIntervals = Toast.makeText( getApplicationContext(), getString( R.string.toast_no_intervals), Toast.LENGTH_LONG);
            noIntervals.show();
        }
    }

    public static class AdFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ad, container, false);
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);
            AdView mAdView = (AdView) getView().findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("2A03F21CFB927C7611295A40C8666F5F").build();
            mAdView.loadAd(adRequest);
        }
    }
}
