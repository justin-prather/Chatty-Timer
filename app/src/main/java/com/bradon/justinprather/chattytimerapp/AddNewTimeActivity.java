package com.bradon.justinprather.chattytimerapp;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.concurrent.TimeUnit;


public class AddNewTimeActivity extends ActionBarActivity {

    private NumberPicker hoursPicker, minutesPicker, secondsPicker;
    private boolean isNewInterval;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_time);

        hoursPicker = (NumberPicker) findViewById( R.id.hourPicker );
        minutesPicker = (NumberPicker) findViewById( R.id.minutePicker );
        secondsPicker = (NumberPicker) findViewById( R.id.secondPicker );

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isNewInterval = false;
            long timeMillis = extras.getLong("Extra_Time");
            String comment = extras.getString("Extra_comment");
            position = extras.getInt("EXTRA_Position");

            EditText intervalComment = (EditText) findViewById( R.id.newTimeComment);
            intervalComment.setText(comment);

            int seconds = (int) (timeMillis / 1000) % 60 ;
            int minutes = (int) ((timeMillis / (1000*60)) % 60);
            int hours   = (int) ((timeMillis / (1000*60*60)) % 24);

            initNumberPicker( hoursPicker, hours, 0, 10 );
            initNumberPicker( minutesPicker, minutes, 0, 59 );
            initNumberPicker( secondsPicker, seconds, 0, 59 );
        }
        else {
            isNewInterval = true;
            initNumberPicker(hoursPicker, 0, 0, 10);
            initNumberPicker(minutesPicker, 0, 0, 59);
            initNumberPicker(secondsPicker, 0, 0, 59);
        }

        // Get tracker.
        Tracker t = ((TalkingTimerApplication) this.getApplication()).getTracker(
                TalkingTimerApplication.TrackerName.APP_TRACKER);

        // Enable Advertising Features.
        t.enableAdvertisingIdCollection(true);

        // Set screen name.
        t.setScreenName("AddInterval");

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new_time, menu);
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

    private void initNumberPicker( NumberPicker np, int Value, int min, int max ){

        np.setMaxValue(max);
        np.setMinValue(min);
        np.setValue(Value);

        np.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });

        return;
    }

    public void saveNewTime( View view ){

        TalkingTimerApplication app = (TalkingTimerApplication) getApplication();
        EditText commentView = (EditText) findViewById( R.id.newTimeComment );

        int hours = hoursPicker.getValue();
        int minutes = minutesPicker.getValue();
        int seconds = secondsPicker.getValue();

        long millis = TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds);

        if( millis == 0 ){
           Toast noValidTime = Toast.makeText( getApplicationContext(), getString( R.string.toast_no_valid_time), Toast.LENGTH_LONG);
            noValidTime.show();
            return;
        }

        String comment = commentView.getText().toString();

        if( comment.equals( "" ) )
            comment = getString( R.string.no_user_time_comment );
        if( isNewInterval ) {
            TimeObject newTime = new TimeObject(comment, (int) millis);

            app.timeList.add(newTime);
        }

        else{
            TimeObject oldTime = app.timeList.get(position);
            oldTime.setTimeMillis( millis );
            oldTime.setTimeComment(comment);
        }

        NavUtils.navigateUpFromSameTask( this );
    }

    public void cancelNewTime( View view ){
        NavUtils.navigateUpFromSameTask( this );
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
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

}
