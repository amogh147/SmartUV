package cs528_mateus_amogh.smartuv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.LocationListener;

import cs528_mateus_amogh.smartuv.SensingModules.CityLocationModule;

/*
    Contain the main interface fragments. Perform some general task: request UV Index and City Name
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MAIN_ACT";
    private FragmentTabHost mTabHost;

    //Get the city location zipcode and name
    private CityLocationModule mCityLocationModule;
    private LocationListener mCityLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Extract the address
            Address address = CityLocationModule.locationToAddress(getApplicationContext(),location);
            //UV Index update based on the extracted zipcode
            UVIndexParser.requestUvUpdate(getApplicationContext(),address.getPostalCode());
            Log.v(TAG, "Received City Update");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Just to show the icon in the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.icon);
        actionBar.setTitle(R.string.app_name);
        actionBar.show();

        //Initialize CityLocationModule
        mCityLocationModule = new CityLocationModule(this,mCityLocationListener);

        //Initialize UI components
        setContentView(R.layout.activity_main);
        checkPermissions();//for now it just request for location permissions
        initTabHost();//Initialize tabs
    }


    void initTabHost(){
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);

        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec(TabFragment1.TAG)
                        .setIndicator(getString(R.string.tab1_title), null),
                TabFragment1.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(TabFragment2.TAG)
                        .setIndicator(getString(R.string.tab2_title), null),
                TabFragment2.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(TabFragment3.TAG)
                        .setIndicator(getString(R.string.tab3_title), null),
                TabFragment3.class, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
        mCityLocationModule.requestUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCityLocationModule.removeUpdates();
    }


    //Only requests location updates for now
    private void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //TODO: Handle situation where the user does not grant the permissions
        //for(int i = 0; i < grantResults.length; i++){
        //    if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
        //        return;
        //}
        recreate();//restart the app in case the user grant the permissions
    }

}
