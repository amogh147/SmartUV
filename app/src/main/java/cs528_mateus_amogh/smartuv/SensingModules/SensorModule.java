package cs528_mateus_amogh.smartuv.SensingModules;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by mateus on 4/23/2016.
 */
public class SensorModule implements IModule{
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mListener;
    private int mRequestInterval = SensorManager.SENSOR_DELAY_NORMAL;
    private boolean mIsTracking = false;

    public SensorModule(Context context, SensorManager sensorManager, Sensor sensor, SensorEventListener listener){
        mSensorManager = sensorManager;
        mSensor = sensor;
        mListener = listener;
    }

    @Override
    public boolean isTracking(){
        return mIsTracking;
    }

    @Override
    public void setRequestInterval(long time){
        mRequestInterval = (int)time;
    }

    @Override
    public void requestUpdates(){
        mSensorManager.registerListener(mListener, mSensor, mRequestInterval);
    }

    @Override
    public void removeUpdates(){
        mSensorManager.unregisterListener(mListener);
    }

}
