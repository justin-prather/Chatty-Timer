package com.bradon.justinprather.chattytimerapp;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import android.widget.Button;

import java.util.Observable;
import java.util.Observer;


public class NewTimer extends ActionBarActivity implements Observer {

    private TalkingTimerApplication mApp;
    private TimeObectAdapter mTimeAdapter;
    private android.support.v7.view.ActionMode mActionMode;
    private boolean isRunningState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_timer);
        ListView TimeListView;

        // init list view
        mApp = (TalkingTimerApplication)getApplicationContext();
        mTimeAdapter = new TimeObectAdapter(this, mApp.timeList );
        TimeListView = (ListView) findViewById(R.id.timeList);
        View emptyView = getLayoutInflater().inflate(R.layout.empty_time_element_layout, null);
        ((ViewGroup)TimeListView.getParent()).addView(emptyView);
        TimeListView.setEmptyView(emptyView);
        TimeListView.setAdapter(mTimeAdapter);
        TimeListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode == null) {
                    // edit selected interval
                    Intent intent = new Intent( getApplicationContext(), AddNewTimeActivity.class );
                    TimeObject interval = (TimeObject) mTimeAdapter.getItem(position);
                    intent.putExtra("Extra_Time", interval.getTimeMillis() );
                    intent.putExtra("Extra_comment", interval.getTimeComment());
                    intent.putExtra("EXTRA_Position", position);
                    startActivity(intent);
                } else
                    // add or remove selection for current list item
                    onListItemSelect(position);
            }
        });
        TimeListView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemSelect(position);
                return true;
            }
        });

        // Get tracker.
        Tracker t = ((TalkingTimerApplication) this.getApplication()).getTracker(
                TalkingTimerApplication.TrackerName.APP_TRACKER);

        // Enable Advertising Features.
        t.enableAdvertisingIdCollection(true);

        // Set screen name.
        t.setScreenName("NewTimer");

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

    }

    private void onListItemSelect(int position) {
        mTimeAdapter.toggleSelection(position);
        boolean hasCheckedItems = mTimeAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = startSupportActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(mTimeAdapter
                    .getSelectedCount()) + " selected");
    }

    @Override
    protected void onPause(){
        super.onPause();
        mApp.getObservable().deleteObserver(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mTimeAdapter.notifyDataSetChanged();
        mApp.getObservable().addObserver(this);

        isRunningState = mApp.isRunning();

        initButtons();
    }

    private void initButtons() {
        Button left = (Button) findViewById(R.id.clearTimeListButton);
        Button right = (Button) findViewById(R.id.startTimerButton);

        if( isRunningState ){
            left.setText(getString(R.string.button_restart));
            right.setText(getString(R.string.button_resume));

            left.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mApp.stopTimer();
                    launch_run_timer_activity(v);
                }
            });

            right.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launch_run_timer_activity(v);
                }
            });
        }

        else{
            left.setText(getString(R.string.clearTimeListButtonText));
            right.setText(getString(R.string.startTimerButton));

            left.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clear_time_list(v);
                }
            });

            right.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launch_run_timer_activity(v);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_timer, menu);
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

    public void launch_run_timer_activity( View view ){
        if( !mTimeAdapter.isEmpty() ) {
            Intent intent = new Intent(this, RunTimerActivity.class);
            startActivity(intent);
        }

        else{
            Toast noIntervals = Toast.makeText( getApplicationContext(), getString( R.string.toast_no_intervals), Toast.LENGTH_LONG);
            noIntervals.show();
        }
    }

    public void launch_add_new_time_activity( View view ){
        Intent intent = new Intent( this, AddNewTimeActivity.class );
        startActivity(intent);
    }

    public void clear_time_list( View view ){
        mApp.timeList.clear();
        mTimeAdapter.notifyDataSetChanged();
    }

    @Override
    public void update(Observable observable, Object data) {
        isRunningState = !mApp.getObservable().isDone();
        initButtons();
    }

    private class ActionModeCallback implements android.support.v7.view.ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_new_timer, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_delete:
                    // retrieve selected items and delete them out
                    SparseBooleanArray selected = mTimeAdapter
                            .getSelectedIds();
                    for (int i = (selected.size() - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) {
                            TimeObject selectedItem = (TimeObject) mTimeAdapter
                                    .getItem(selected.keyAt(i));
                            mTimeAdapter.remove(selectedItem);
                        }
                    }
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_duplicate:
                    // retrieve selected items and delete them out
                    selected = mTimeAdapter
                            .getSelectedIds();
                    for (int i = 0; i <= (selected.size() - 1); i++) {
                        if (selected.valueAt(i)) {
                            TimeObject selectedItem = (TimeObject) mTimeAdapter
                                    .getItem(selected.keyAt(i));
                            mTimeAdapter.add(new TimeObject(selectedItem));
                        }
                    }
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
            // remove selection
            mTimeAdapter.removeSelection();
            mActionMode = null;
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
