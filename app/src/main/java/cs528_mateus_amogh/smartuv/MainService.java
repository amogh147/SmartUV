package cs528_mateus_amogh.smartuv;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Calendar;

import cs528_mateus_amogh.smartuv.Database.UVIndexDbManager;
import cs528_mateus_amogh.smartuv.SensingModules.ActivityRecognitionModule;
import cs528_mateus_amogh.smartuv.SensingModules.CityLocationModule;
import cs528_mateus_amogh.smartuv.SensingModules.GpsModule;
import cs528_mateus_amogh.smartuv.SensingModules.GpsStatusHandler;
import cs528_mateus_amogh.smartuv.SensingModules.SensorModule;

/**
 * This Service perform all Sun-Exposure estimation tasks
 */
public class MainService extends Service{

    public static final String NAME = "MainService";
    private static final String TAG = "MAIN_SVC";//for log purposes

    //Sensor Request Intervals
    private static final long CITY_REQUEST_INTERVAL = 3600*1000;//ms
    private static final long ACTIVITY_REQUEST_INTERVAL = 15*1000;//ms
    private static final long GPS_REQUEST_INTERVAL = 1000;//ms
//    private static final long GPS_REQUEST_INTERVAL = 60*1000;//ms

    private static final long LIGHT_REQUEST_INTERVAL = 10*1000000;//us
    private static final long PROXIMITY_REQUEST_INTERVAL = 5*1000000;//us

    //Strings used to send Intents to this service
    public static final String SERVICE_COMMAND = "SERVICE_COMMAND";//Action name
    public static final String SCHEDULE_EXTRA = "SCHEDULE_EXTRA";//To identify Schedule Intents
    public static final String START_EXTRA = "START_EXTRA";//To identify StartService Intents

    public static final String IO_STATUS_INTENT = "IOStatusIntent";
    public static final String IO_STATUS = "IOStatus";

    public static final int INDOOR_STATUS = 0;
    public static final int OUTDOOR_STATUS = 1;
    public static final int UNKNOWN_STATUS = 2;

    private int mIOStatus = UNKNOWN_STATUS;

    //Indoor-Outdoor Estimation Parameters
    private static final int LIGHT_SENSOR_THRESHOLD = 1500;


    //Sensor Managers and Listeners
    private ActivityRecognitionModule mActivityRecognitionModule;
    private GpsModule mGpsModule;
    private SensorModule mLightSensorModule;
    private SensorModule mProximitySensorModule;
    private CityLocationModule mCityLocationModule;

    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private Sensor mProximitySensor;

    private SensorEventListener mLightSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float value = event.values[0];
            //Log.v(TAG, "LightSensor = " + String.valueOf(value));

            //FIXME: uncomment this part
            if(value>LIGHT_SENSOR_THRESHOLD)
                updateIOStatus(OUTDOOR_STATUS);
            else
                updateIOStatus(INDOOR_STATUS);

            //mSensorDataDb.addSensorEvent(event,System.currentTimeMillis());
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private SensorEventListener mProximitySensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.v(TAG, "ProximitySensor = " + String.valueOf(event.values[0]));
            //Log.v(TAG, "MaximumRange = " + String.valueOf(mProximitySensor.getMaximumRange()));
            if(event.values[0] == mProximitySensor.getMaximumRange()) {//If it's far
                //FIXME: uncomment the mode switch
                turnLightModeOn();
                turnGpsModeOff();
            }else{
                turnGpsModeOn();
                turnLightModeOff();
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private boolean isGpsModeOn = false;
    private boolean isLightModeOn = false;

    private void turnGpsModeOn(){
        Log.v(TAG, "turn Gps Mode on");
        if(isGpsModeOn==false) {
            mGpsModule.requestUpdates();
            mActivityRecognitionModule.requestUpdates();
            isGpsModeOn = true;
        }
    }

    private void turnGpsModeOff(){
        Log.v(TAG, "turn Gps Mode off");
        if(isGpsModeOn==true) {
            mGpsModule.removeUpdates();
            mActivityRecognitionModule.removeUpdates();
            isGpsModeOn = false;
        }
    }

    private void turnLightModeOn(){
        Log.v(TAG, "turn Light Mode on");
        if(isLightModeOn==false) {
            mLightSensorModule.requestUpdates();
            isLightModeOn = true;
        }
    }

    private void turnLightModeOff(){
        Log.v(TAG, "turn Light Mode off");
        if(isLightModeOn==true) {
            mLightSensorModule.removeUpdates();
            isLightModeOn = false;
        }
    }

    //Auxiliary Variables
    private int mGpsStatusCount = 0;//Count gps reads in window with requestLocation on
    private int mLastActivity = DetectedActivity.UNKNOWN;//store the last registered activity
    private boolean alarmStop = false;//Indicates the service was stopped by an alarm call
    private Location mLastLocation = null;
    private long mLastIOUpdateTime = SystemClock.elapsedRealtime();
    private Calendar mLastCalendar = Calendar.getInstance();

    private UVIndexDbManager mDatabase;

    //Tell if the service is running or not. Can be used by other activities
    private static boolean sTracking = false;

    public static boolean isTracking() {
        return sTracking;
    }

    //Last read UVDayUpdate
    private static UVDayUpdate mUvDayUpdate = null;

    public static UVDayUpdate getUvDayUpdate(){
        return mUvDayUpdate;
    }

    //Receive updates from UVIndexParser
    private BroadcastReceiver mUVDayUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UVIndexParser.UV_UPDATE)) {
                Log.v(TAG, "Received UvDayUpdate");
                UVDayUpdate newUvDayUpdate = new UVDayUpdate(intent.getStringExtra(UVIndexParser.UV_UPDATE));

                if(mUvDayUpdate==null) {
                    Log.v(TAG, "New Day");
                    mUvDayUpdate = newUvDayUpdate;
                    mDatabase.saveUVDayUpdate(mUvDayUpdate);
                }//else
                    //mUvDayUpdate.update(newUvDayUpdate.getUVHourUpdateList());

                notifySunExposureUpdate();

                setNextAlarm(mUvDayUpdate);
                if (mUvDayUpdate.getUVHourUpdate(Calendar.getInstance()).getUvIndex() == 0) {
                    Log.v(TAG, "current UVIndex is zero, lets stop");
                    //FIXME: Uncomment these 2 lines for the final version
                    alarmStop = true;
                    stopSelf();
                }

            }
            //Log.v(TAG, "received message");
        }
    };

    //Set the next alarm based on the passed UVDayUpdate
    private void setNextAlarm(UVDayUpdate uvDayUpdate) {
        long nextTime = uvDayUpdate.getNextAlarmTime(Calendar.getInstance());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nextTime);
        Log.v(TAG, "Next Alarm = " + calendar.toString());
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, mAlarmIntent);
    }

    private final int NOTIFICATION_ID = 1;
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private PendingIntent mNotificationIntent;

    //Start tracking the IOState
    public void startTracking() {
        //mGpsModule.requestUpdates();//For GPS Status
        //mActivityRecognitionModule.requestUpdates();//For activity Recognition
        //mLightSensorModule.requestUpdates();
        mDatabase = UVIndexDbManager.getInstance(this);
        mUvDayUpdate = mDatabase.getUVDayUpdate(new DateTime(Calendar.getInstance()).getDateString());

        mProximitySensorModule.requestUpdates();
        mSensorManager.registerListener(mLightSensorListener, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mProximitySensorListener, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(mUVDayUpdateReceiver, new IntentFilter(UVIndexParser.UV_UPDATE));
        mCityLocationModule.requestUpdates();

        //turnGpsModeOn();//FIXME: comment this line

        startForeground(NOTIFICATION_ID, mNotification);

        Log.v(TAG, "Start Tracking");
        sTracking = true;
    }

    public void stopTracking() {
        //mGpsModule.removeUpdates();
        //mActivityRecognitionModule.removeUpdates();
        //mLightSensorModule.removeUpdates();
        turnGpsModeOff();
        turnLightModeOff();

        mProximitySensorModule.removeUpdates();
        LocalBroadcastManager.getInstance(this).
                unregisterReceiver(mUVDayUpdateReceiver);

        mCityLocationModule.removeUpdates();

        Log.v(TAG, "Stop Tracking");
        sTracking = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_STICKY;
    }

    protected void handleIntent(Intent intent) {
        Log.v(TAG, "handleIntent");
        if (intent == null) {
            Log.v(TAG, "null Intent: the process was killed");
            startTracking();
        } else if (intent.getBooleanExtra(START_EXTRA, false)) {
            Log.v(TAG, "StartIntent: from the App");
            startTracking();
        } else if (intent.hasExtra(Intent.EXTRA_ALARM_COUNT)) {
            if (isTracking()) {
                Log.v(TAG, "Schedule Intent. isTracking -> turn service off");
                alarmStop = true;
                stopSelf();
            } else {
                Log.v(TAG, "Schedule Intent. isNotTracking -> turn service on");
                startTracking();
            }
        } else if (ActivityRecognitionResult.hasResult(intent)) {
            Log.v(TAG, "Activity Intent");
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result);
        }
    }

    private PendingIntent mAlarmIntent;
    private AlarmManager mAlarmManager;

    LocationListener mGpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            //mGpsModule.removeUpdates();
            Log.v(TAG, "Remove GPS Updates");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };



    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");

        //Create Sensing Modules
        mGpsModule = new GpsModule(this, mGpsStatusListener,mGpsLocationListener);
        mActivityRecognitionModule = new ActivityRecognitionModule(this, MainService.class);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mLightSensorModule = new SensorModule(this,mSensorManager,mLightSensor,mLightSensorListener);
        mProximitySensorModule = new SensorModule(this,mSensorManager,mProximitySensor,mProximitySensorListener);

        mCityLocationModule = new CityLocationModule(this,mCityLocationLister);

        //Set Request Intervals
        mGpsModule.setRequestInterval(GPS_REQUEST_INTERVAL);
        mActivityRecognitionModule.setRequestInterval(ACTIVITY_REQUEST_INTERVAL);
        mLightSensorModule.setRequestInterval(LIGHT_REQUEST_INTERVAL);
        mProximitySensorModule.setRequestInterval(PROXIMITY_REQUEST_INTERVAL);
        mCityLocationModule.setRequestInterval(CITY_REQUEST_INTERVAL);

        mNotificationIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        mNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getString(R.string.notificationTitle))
                .setContentText(getString(R.string.notificationSubtitle))
                .setContentIntent(mNotificationIntent).build();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        setupAlarm();
    }

    private void setupAlarm(){
        Intent intent = new Intent(getApplicationContext(), MainService.class);
        intent.putExtra(SCHEDULE_EXTRA, true);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();
        if (!alarmStop) {//commanded by the activity{
            mAlarmManager.cancel(mAlarmIntent);
            Log.v(TAG, "Cancel Alarms");
        }
    }


    private Intent ioIntent = new Intent(IO_STATUS_INTENT);


    private void updateIOStatus(int newStatus){
        if(mIOStatus!=newStatus){

            if(mIOStatus!=UNKNOWN_STATUS)
                mDatabase.addIoState(newStatus, System.currentTimeMillis());

            long newTime = SystemClock.elapsedRealtime();
            if(newStatus != OUTDOOR_STATUS && mUvDayUpdate!=null && mIOStatus==OUTDOOR_STATUS){//changed from outdoor to indoor
                int period = (int)(newTime-mLastIOUpdateTime)/1000;//in seconds
                mUvDayUpdate.addExposureTime(Calendar.getInstance(),period);
                UVHourUpdate hourUpdate =  mUvDayUpdate.getUVHourUpdate(Calendar.getInstance());
                Log.v(TAG, "update sun exposure");
                mDatabase.updateSunExposureTime(hourUpdate.getDateTime().getDateString(),
                        hourUpdate.getDateTime().getHourString(), hourUpdate.getExposureTime());
                notifySunExposureUpdate();
            }
            mLastIOUpdateTime = newTime;
            mIOStatus = newStatus;

            //TODO: Notify current UV Index
            mNotification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(getString(R.string.notificationTitle))
                    .setContentText(getString((mIOStatus == INDOOR_STATUS)?R.string.indoor:R.string.outdoor))
                    .setContentIntent(mNotificationIntent).build();

            mNotificationManager.notify(NOTIFICATION_ID,mNotification);
        }
        ioIntent.putExtra(IO_STATUS, newStatus);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(ioIntent);
    }

    public final static String NOTIFY_SUN_EXPOSURE_UPDATE = "NOTIFY_SUN_EXPOSURE";

    private void notifySunExposureUpdate(){
        Log.v(TAG, "Notify Sun Exposure");
        Intent intent = new Intent(NOTIFY_SUN_EXPOSURE_UPDATE);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int mGpsOutdoorCount = 0;
    private int mGpsIndoorCount = 0;

    private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener(){

        @Override
        public void onGpsStatusChanged(int event) {
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {

                GpsStatus status = mGpsModule.getGpsStatus();
                GpsStatusHandler handler = new GpsStatusHandler(status);
                Log.v(TAG, handler.toString(0));

                if (handler.isOutdoor()){
                    if (++mGpsOutdoorCount >= 3) {
                        mGpsModule.removeUpdates();
                        mGpsIndoorCount = 0;
                        mGpsOutdoorCount = 0;
                        updateIOStatus(OUTDOOR_STATUS);
                    }
                }else {
                    if(++mGpsIndoorCount >= 3){
                        mGpsModule.removeUpdates();
                        mGpsIndoorCount =0;
                        mGpsOutdoorCount =0;
                        updateIOStatus(INDOOR_STATUS);
                    }
                }
            }
        }
    };

    private final Integer targetActivities[] = {DetectedActivity.IN_VEHICLE, DetectedActivity.ON_BICYCLE,
            DetectedActivity.ON_FOOT, DetectedActivity.STILL};

    private final int CONFIDENCE_THRESHOLD = 0;

    private void handleDetectedActivities(ActivityRecognitionResult result) {
        DetectedActivity activity = result.getMostProbableActivity();
        Log.v(TAG, "Activity " + String.valueOf(activity.getType()));

        //FIXME: uncomment these lines
        if (activity.getType() == DetectedActivity.STILL && mLastActivity == activity.getType())
            return;

        Log.v(TAG, "Request Single Update");
        mGpsModule.requestUpdates();
        mLastActivity = activity.getType();
    }

    com.google.android.gms.location.LocationListener mCityLocationLister = new com.google.android.gms.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            Log.v(TAG, "CityLocation received");
            UVIndexParser.requestUvUpdate(getApplicationContext(),
                    mCityLocationModule.locationToAddress(getApplicationContext(), mLastLocation).getPostalCode());
        }
    };
}
