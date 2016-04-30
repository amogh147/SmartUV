package cs528_mateus_amogh.smartuv.SensingModules;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

/**
 * Created by mateus on 4/23/2016.
 */
public class CityLocationModule implements IModule, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private boolean mIsTracking = false;
    private Context mContext;

    GoogleApiClient mGoogleApiClient;
    LocationListener mLocationListener;
    LocationRequest mLocationRequest;

    private static Address sAddress = null;
    public static Address getLastAddress(){
        return sAddress;
    }

    public static Address locationToAddress(Context context, Location location) {
        List<Address> addresses = null;
        try {
            addresses = new Geocoder(context).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if(addresses!=null){
            if(addresses.size()>0)
                sAddress = addresses.get(0);
        }
        return sAddress;
    }

    public boolean isTracking() {
        return mIsTracking;
    }

    public CityLocationModule(Context context, LocationListener locationListener) {
        mContext = context;
        mLocationListener = locationListener;
        buildApiClient();
        mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(3600 * 1000);
        //mLocationRequest.setFastestInterval(3600 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }


    public void requestUpdates() {
        mGoogleApiClient.connect();
    }

    public void removeUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void setRequestInterval(long time) {
        mLocationRequest.setInterval(time);
        mLocationRequest.setFastestInterval(time);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void buildApiClient(){
        //just build the GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }
}
