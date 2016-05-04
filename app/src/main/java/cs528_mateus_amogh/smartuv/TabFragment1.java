package cs528_mateus_amogh.smartuv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;

import cs528_mateus_amogh.smartuv.SensingModules.AdminAreaParser;
import cs528_mateus_amogh.smartuv.SensingModules.CityLocationModule;


/**This fragment show current information about UVIndex and basic advices
 */
public class TabFragment1 extends Fragment{

    //Constants
    public static final String TAG = "TabFragment1";//Used in MainActivity to create the TabHost

    private static final String UV_INDEX = "UV_INDEX";
    private static final String DATE_TIME = "DATE_TIME";
    private static final String LOCALITY_NAME = "LOCALITY_NAME";

    //Map the UVIndex number with the corresponding resourceID
    private final int mUVIndexImageMap[] = {R.drawable.index0, R.drawable.index1, R.drawable.index2, R.drawable.index3, R.drawable.index4, R.drawable.index5,
            R.drawable.index6, R.drawable.index7, R.drawable.index8, R.drawable.index9,R.drawable.index10, R.drawable.index11,
            R.drawable.index11,R.drawable.index11,R.drawable.index11,R.drawable.index11};//Assume maximum UV Index 15

    //Views
    private ImageView mUvIndexImageView;

    private TextView mCityTextView;
    private TextView mDateTextView;
    private TextView mRiskTextView;
    private TextView mMsgTextView;

    //City-State (e.g. Worcester-MA)
    private String mLocalityName = null;

    //Store the last UvHourUpdate (FIXME: I think we can keep the UVDayUpdate)
    UVHourUpdate mUvHourUpdate=null;

    //Receive the UVIndex Update from UVIndexParser
    //It assumes only the MainActivity and MainService will call for updates
    private BroadcastReceiver mUVIndexReceiver= new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(UVIndexParser.UV_UPDATE)){
                UVDayUpdate uvDayUpdate = new UVDayUpdate(intent.getStringExtra(UVIndexParser.UV_UPDATE));
                updateUvUpdate(uvDayUpdate);//Update screen and internal variables
                updateAddress();//Assume the MainActivity has already gotten the address
            }
            //Log.v(TAG, "received message");
        }
    };

    public TabFragment1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
    }

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_tab_fragment1, container, false);

        //The the view instances
        mUvIndexImageView = (ImageView) mView.findViewById(R.id.uvIndexImageView);

        mCityTextView = (TextView) mView.findViewById(R.id.cityTextView);
        mRiskTextView = (TextView) mView.findViewById(R.id.riskTextView);
        mDateTextView = (TextView) mView.findViewById(R.id.dateTextView);
        mMsgTextView = (TextView) mView.findViewById(R.id.msgTextView);

        updateAddress();//update the view with city and state names


        if(UVIndexParser.getLastUvDayUpdate()!=null) {
            updateUvUpdate(UVIndexParser.getLastUvDayUpdate());//Get an already requested update
        }else{//If there is no already available UVDayUpdate, request a new one
            if(CityLocationModule.getLastAddress()!=null)//check for the address
                UVIndexParser.requestUvUpdate(getActivity(),CityLocationModule.getLastAddress().getPostalCode());
        }

        //register the BroadcastReceiver to receive UVDayUpdates
        LocalBroadcastManager.getInstance(getActivity()).
                registerReceiver(mUVIndexReceiver, new IntentFilter(UVIndexParser.UV_UPDATE));
        return mView;
    }

    //Update internal variables and screen based on a new UVDayUpdate
    private void updateUvUpdate(UVDayUpdate dayUpdate){
        mUvHourUpdate = dayUpdate.getUVHourUpdate(Calendar.getInstance());//Update current HourUpdate

        mUvIndexImageView.setImageResource(mUVIndexImageMap[mUvHourUpdate.getUvIndex()]);//Update UVIndex Image

        //Get the risk number
        int risk = HealthInfo.getRisk(mUvHourUpdate.getUvIndex());

        //Set risk textView
        mRiskTextView.setText(getString(HealthInfo.getRiskResource(risk).nameId));
        mRiskTextView.setTextColor(getResources().getColor(HealthInfo.getRiskResource(risk).colorId));
        mDateTextView.setText(new DateTime(Calendar.getInstance()).toShortString());
        String[] msgs = getResources().getStringArray(HealthInfo.getRiskResource(risk).msgId);
        String finalMsg = new String();
        for(String msg : msgs){
            finalMsg += ">"+msg+"\n";
        }
        mMsgTextView.setText(finalMsg);

        int[] advices = HealthInfo.getRiskResource(risk).msgImgViewId;
        int[] allAdvices = HealthInfo.getRiskResource(HealthInfo.Risk.EXTREME).msgImgViewId;

        ImageView view;

        for(int i = 0; i < allAdvices.length; i++){
            view = (ImageView) mView.findViewById(allAdvices[i]);
            if(i>=advices.length)
                view.setVisibility(View.INVISIBLE);
            else
                view.setVisibility(View.VISIBLE);
        }
    }

    //Update the localityName based on the address gotten by MainActivity
    //Assume MainActivity already has the address information
    private void updateAddress(){
        if(CityLocationModule.getLastAddress()!=null) {
            //City Name - State Abbreviation (e.g. Worcester-MA)
            mLocalityName = CityLocationModule.getLastAddress().getLocality()+"-"+
                    AdminAreaParser.map.get(CityLocationModule.getLastAddress().getAdminArea());
            mCityTextView.setText(mLocalityName);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(TAG, "onDestroyView");
        LocalBroadcastManager.getInstance(getActivity()).
                unregisterReceiver(mUVIndexReceiver);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }
}
