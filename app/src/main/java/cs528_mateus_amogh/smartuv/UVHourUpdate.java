package cs528_mateus_amogh.smartuv;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mateus on 4/16/2016.
 */
public class UVHourUpdate {
    private final String TAG = "UVHour";

    private int uvIndex;
    private DateTime dateTime;

    private final SimpleDateFormat readableDateFormat = new SimpleDateFormat("MM dd");
    private final SimpleDateFormat updateDateFormat = new SimpleDateFormat("MMM/dd/yyyy hh aa");

    private int exposureTime = 0;//FIXME: Not good OO design. It shouldn't be here
    private String zipCode;

    public int getExposureTime(){return exposureTime;}
    void setExposureTime(int newExposureTime){exposureTime = newExposureTime;}

    UVHourUpdate(JSONObject obj){
        try {
            uvIndex = obj.getInt("UV_VALUE");
            zipCode = obj.getString("ZIP");
        } catch (JSONException e) {
            Log.v(TAG, "jsonException");
            e.printStackTrace();
        }
        dateTime = new DateTime(obj);
    }

    public UVHourUpdate(int uvIndex, DateTime dateTime){
        this.uvIndex = uvIndex;
        this.dateTime = dateTime;
    }

    public int getUvIndex(){
        return uvIndex;
    }

    public DateTime getDateTime(){
        return dateTime;
    }

    public Calendar getCalendar(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime.getDate());
        return calendar;
    }

    public void addExposureTime(int addTime) {
        exposureTime += addTime;
    }

    public String getZipCode() {
        return zipCode;
    }
}
