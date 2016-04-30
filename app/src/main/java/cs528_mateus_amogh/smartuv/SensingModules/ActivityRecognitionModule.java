package cs528_mateus_amogh.smartuv.SensingModules;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;


public class ActivityRecognitionModule implements IModule, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public final static String NAME = "ActivityRecognitionModule";
    private final String TAG = "ACT_MOD";

    private long mRequestInterval = 10000;
    private GoogleApiClient mApiClient = null;//To connect to Google Services
    private Context mContext;
    private PendingIntent mPendingIntent;
    private Class<?> mServiceClass;

    boolean isTracking = false;

    public ActivityRecognitionModule(Context context, Class<?> serviceClass) {
        mContext = context;
        mServiceClass = serviceClass;
        buildApiClient();
        Log.v(TAG, "Constructor");
    }

    private void buildApiClient() {
        mApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public boolean isTracking(){
        return isTracking;
    }

    @Override
    public void setRequestInterval(long interval){
        mRequestInterval = interval;
    }

    @Override
    public void requestUpdates(){
        mApiClient.connect();
    }

    @Override
    public void removeUpdates(){
        this.onlyRemoveUpdates();
        isTracking = false;
        mApiClient.disconnect();
    }

    public void onlyRemoveUpdates() {
        try {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mApiClient, mPendingIntent);
        } catch (IllegalStateException e) {
            //Ignore
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "onConnected");
        Intent intent = new Intent(mContext, mServiceClass);
        mPendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient, mRequestInterval, mPendingIntent);
        isTracking = true;
        Log.v(TAG, "onConnected end");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}