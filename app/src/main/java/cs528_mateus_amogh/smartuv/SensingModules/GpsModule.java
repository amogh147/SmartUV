package cs528_mateus_amogh.smartuv.SensingModules;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by mateus on 4/8/2016.
 */
public class GpsModule implements IModule{

    private LocationManager mLocationManager;
    private LocationListener mGpsLocationListener;
    private GpsStatus.Listener mGpsStatusListener;
    public static final int REQUEST_GPS = 0;
    private long GPS_REQUEST_INTERVAL = 5000;

    private Context mContext;

    private boolean isTracking = false;

    @Override
    public boolean isTracking(){
        return isTracking;
    }

    public GpsModule(Context context, GpsStatus.Listener gpsStatusListener, LocationListener gpsLocationListener) {
        mGpsStatusListener = gpsStatusListener;
        mContext = context;
        mGpsLocationListener = gpsLocationListener;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void setRequestInterval(long time) {
        GPS_REQUEST_INTERVAL = time;
    }


    public GpsStatus getGpsStatus() {
        return mLocationManager.getGpsStatus(null);
    }

    //TODO: NOT WORKING. NOT SUPPOSED TO BE HERE
    public static void checkLocationPermission(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_GPS);
        }
    }

    public void requestSingleLocationUpdate(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
        }else{
            //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 100, mNetworkLocationListener);
            mLocationManager.addGpsStatusListener(mGpsStatusListener);
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mGpsLocationListener, null);
        }
    }

    @Override
    public void requestUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
        }else{
            //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 100, mNetworkLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_REQUEST_INTERVAL, 0, mGpsLocationListener);
            mLocationManager.addGpsStatusListener(mGpsStatusListener);
            isTracking = true;
        }
    }

    @Override
    public void removeUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }else {
            //mLocationManager.removeUpdates(mNetworkLocationListener);
            mLocationManager.removeGpsStatusListener(mGpsStatusListener);
            mLocationManager.removeUpdates(mGpsLocationListener);
            isTracking = false;
        }
    }
}
