package cs528_mateus_amogh.smartuv;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Calendar;

import cs528_mateus_amogh.smartuv.Database.UVIndexDbManager;


/**
 */
public class TabFragment2 extends Fragment {

    public static final String TAG = "TabFragment2";

    private ToggleButton mTrackingToggle;
    private TextView mIoTextView=null;

    private Intent mServiceIntent;

    private int mIOState  = MainService.UNKNOWN_STATUS;

    private UVIndexDbManager mDatabase = null;


    private BroadcastReceiver mIoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.v(TAG, "Received IOState");
            //float state = (float)intent.getDoubleExtra(MainService.IO_STATUS,-1);
            //mIoTextView.setText(String.valueOf(state));
            if (mIoTextView != null) {
                mIOState  = intent.getIntExtra(MainService.IO_STATUS, -1);
                updateIOView();
                updateSunExposureTime();
            }
        }
    };


    private void updateIOView(){
        if (mIOState == MainService.INDOOR_STATUS)
            mIoTextView.setText("INDOOR");
        else if (mIOState  == MainService.OUTDOOR_STATUS)
            mIoTextView.setText("OUTDOOR");
        else
            mIoTextView.setText("UNKNOWN");
    }


    private ToggleButton.OnCheckedChangeListener mTrackingToggleListener = new ToggleButton.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked)
                getActivity().startService(mServiceIntent);
            else {
                getActivity().stopService(mServiceIntent);
                Intent intent = new Intent(getActivity(), MainService.class);
                intent.putExtra(MainService.SCHEDULE_EXTRA, true);
                PendingIntent pIntent = PendingIntent.getService(getActivity(),0,intent,0);
                AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pIntent);
            }
        }
    };

    public TabFragment2() {
        // Required empty public constructor
    }

    UVDayUpdate mUvDayUpdate = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServiceIntent = new Intent(getActivity(),MainService.class);
        mServiceIntent.putExtra(MainService.START_EXTRA, true);
    }

    private BroadcastReceiver mSunExposureUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSunExposureTime();
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "OnStart");
        LocalBroadcastManager.getInstance(getActivity()).
                registerReceiver(mIoReceiver, new IntentFilter(MainService.IO_STATUS_INTENT));
        LocalBroadcastManager.getInstance(getActivity()).
                registerReceiver(mSunExposureUpdateReceiver, new IntentFilter(MainService.NOTIFY_SUN_EXPOSURE_UPDATE));
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(TAG, "OnStop");
        LocalBroadcastManager.getInstance(getActivity()).
                unregisterReceiver(mIoReceiver);
        LocalBroadcastManager.getInstance(getActivity()).
                unregisterReceiver(mSunExposureUpdateReceiver);
    }

    View mView = null;
    TableLayout mTableView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_tab_fragment2, container, false);

        mTrackingToggle = (ToggleButton) mView.findViewById(R.id.trackingToggleButton);
        mTrackingToggle.setChecked(MainService.isTracking());
        mTrackingToggle.setOnCheckedChangeListener(mTrackingToggleListener);

        createTableView();

        mIoTextView = (TextView) mView.findViewById(R.id.ioTextView);

        updateIOView();

        updateSunExposureTime();

        return mView;
    }

    private void createTableView(){
        if(mView!=null) {
            mTableView = (TableLayout) mView.findViewById(R.id.tableLayout);
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for(int i = 0; i < 24; i++){
                mTableView.addView(inflater.inflate(R.layout.table_row, null));
            }
        }
    }


    private void updateExposureTimeView(UVDayUpdate dayUpdate){
        if(mTableView!=null && dayUpdate != null) {
            for(int i = 0; i < dayUpdate.getUVHourUpdateList().size(); i++){
                TableRow row = (TableRow)mTableView.getChildAt(i+1);
                TextView timeView = (TextView) row.getChildAt(0);
                TextView uvView = (TextView) row.getChildAt(1);
                TextView exposureView = (TextView) row.getChildAt(2);

                UVHourUpdate update = dayUpdate.getUVHourUpdateList().get(i);

                timeView.setText(update.getDateTime().getHourString());
                uvView.setText(String.valueOf(update.getUvIndex()));
                int risk = HealthInfo.getRisk(dayUpdate.getUVHourUpdateList().get(i).getUvIndex());
                uvView.setTextColor(getResources().getColor(HealthInfo.getRiskResource(risk).colorId));

                exposureView.setText(DateTime.secondsToString(update.getExposureTime()));

                //Log.v(TAG, update.getDateTime().toString() + " " +
                        //update.getUvIndex() + " " +
                        //update.getExposureTime());
            }
        }
    }

    private boolean mOpenDatabase = false;

    private void updateSunExposureTime(){
        UVDayUpdate dayUpdate = MainService.getUvDayUpdate();
        if(MainService.isTracking() && dayUpdate!=null) {
            dayUpdate = dayUpdate.getDayUpdateOfInterest();//FIXME: uncomment
            updateExposureTimeView(dayUpdate);
        }else{
            mDatabase = UVIndexDbManager.getInstance(getActivity());
            mUvDayUpdate = mDatabase.getUVDayUpdate(new DateTime(Calendar.getInstance()).getDateString());
            if(mUvDayUpdate!=null)
                updateExposureTimeView(mUvDayUpdate.getDayUpdateOfInterest());
            //mDatabase.close();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
