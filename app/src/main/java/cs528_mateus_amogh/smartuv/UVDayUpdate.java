package cs528_mateus_amogh.smartuv;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mateus on 4/16/2016.
 */
public class UVDayUpdate {

    private final String TAG = "UVDayUpdate";

    public static final String ZIP_CODE_INTENT  = "ZIP_CODE_INTENT";
    public static final String CITY_NAME_INTENT = "CITY_NAME_INTENT";

    private List<UVHourUpdate> uvHourUpdates = new LinkedList<UVHourUpdate>();
    private String zipcode = new String();
    private DateTime dateTime = null;
    private int first = -1;
    private int last = -1;

    public UVDayUpdate(){
        uvHourUpdates = null;
        zipcode = null;
        dateTime = null;
    }

    public UVDayUpdate(JSONArray array){
        fromJSONArray(array);
    }

    public List<UVHourUpdate> getUVHourUpdateList(){
        return uvHourUpdates;
    }

    public int getExposureTime(Calendar calendar){
        return getUVHourUpdate(calendar).getExposureTime();
    }

    public void setExposureTime(Calendar calendar, int newTime){
        getUVHourUpdate(calendar).setExposureTime(newTime);
    }

    public void addExposureTime(Calendar calendar, int addTime){
        getUVHourUpdate(calendar).addExposureTime(addTime);
    }


    public UVHourUpdate getUVHourUpdate(Calendar calendar){
        Date date = calendar.getTime();
        //Log.v(TAG, "Current" + date.toString());
        for(int i = 1; i<uvHourUpdates.size(); i++){
            //Log.v(TAG, uvHourUpdates.get(i).getDateTime().toString());
            if(date.compareTo(uvHourUpdates.get(i).getDateTime().getDate())<0)
                return uvHourUpdates.get(i-1);
        }
        return uvHourUpdates.get(0);
    }

    public long getNextAlarmTime(Calendar now){
        //Before first time with positive UV Index
        if(now.before(getFirstPositiveUpdate().getCalendar()))
            return getFirstPositiveUpdate().getCalendar().getTimeInMillis();
        //After the last time with positive UV Index (actually, the first zero after this point)
        else if(now.after(getFirstAfternoon0UV().getCalendar()))
            return getFirstPositiveUpdate().getCalendar().getTimeInMillis()+24*3600*1000;
        //Return the last time
        return getFirstAfternoon0UV().getCalendar().getTimeInMillis();
    }

    public UVHourUpdate getFirstPositiveUpdate(){
        return uvHourUpdates.get(first);
    }

    public UVHourUpdate getFirstAfternoon0UV(){
        return uvHourUpdates.get(last);
    }

    public UVDayUpdate(String str){
        try {
            fromJSONArray(new JSONArray(str));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public UVDayUpdate(List<UVHourUpdate> list){
        update(list);
    }

    public void update(List<UVHourUpdate> list){
        uvHourUpdates = list;
        for(int i = 0; i < list.size(); i++){
            if(uvHourUpdates.get(i).getUvIndex()>0 && first < 0)
                first = i;
            else if(uvHourUpdates.get(i).getUvIndex()==0 && first>=0 && last < 0)
                last = i;
            if(i==0) {
                zipcode = uvHourUpdates.get(0).getZipCode();
                dateTime = uvHourUpdates.get(0).getDateTime();
            }
        }
    }


    private void fromJSONArray(JSONArray array){
        uvHourUpdates = new LinkedList<UVHourUpdate>();
        for(int i = 0; i < array.length(); i++){
            try {
                uvHourUpdates.add(new UVHourUpdate(array.getJSONObject(i)));
                if(uvHourUpdates.get(i).getUvIndex()>0 && first < 0)
                    first = i;
                else if(uvHourUpdates.get(i).getUvIndex()==0 && first>=0 && last < 0)
                    last = i;

                if(i==0) {
                    zipcode = array.getJSONObject(0).getString("ZIP");
                    dateTime = uvHourUpdates.get(0).getDateTime();
                }
            } catch (JSONException e) {
                Log.v(TAG, "JSONObject exception");
                e.printStackTrace();
            }
        }
        updateLast();
    }

    private void updateLast(){
        last = 1;
        for (int i = uvHourUpdates.size()-2;i>first;i--){
            if(uvHourUpdates.get(i).getUvIndex()>0) {
                last = i+1;
                return;
            }
        }
    }

    public DateTime getDateTime(){
        return dateTime;
    }

    public UVDayUpdate getDayUpdateOfInterest(){
        UVDayUpdate newDayUpdate = new UVDayUpdate();
        newDayUpdate.uvHourUpdates = uvHourUpdates.subList(first,last);
        newDayUpdate.zipcode = zipcode;
        newDayUpdate.first = 0;
        newDayUpdate.last = last-first;
        newDayUpdate.dateTime = dateTime;
        return newDayUpdate;
    }


}
