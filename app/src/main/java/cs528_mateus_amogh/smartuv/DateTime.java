package cs528_mateus_amogh.smartuv;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mateus on 4/20/2016.
 */
public class DateTime{

    private final String TAG = "DateTime";

    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMM dd");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM/dd/yyyy");
    private final SimpleDateFormat updateDateFormat = new SimpleDateFormat("MMM/dd/yyyy hh aa");
    private final SimpleDateFormat hourFormat = new SimpleDateFormat("hh aa");
    private Date dateTime;

    public DateTime(JSONObject updateObj){
        try {
            update(updateObj.getString("DATE_TIME"));
        } catch (JSONException e) {
            Log.v(TAG, "JSONObject exception");
            e.printStackTrace();
        }
    }

    public DateTime(Calendar calendar){
        dateTime  = calendar.getTime();
    }

    public DateTime(String date, String time){
        update(date+" "+time);
    }

    public DateTime(String updateString){
        update(updateString);
    }

    public Date getDate(){
        return dateTime;
    }

    private void update(String updateString){
        try {
            dateTime = updateDateFormat.parse(updateString);
            dateTime.setMinutes(0);
            dateTime.setSeconds(0);
        } catch (ParseException e) {
            Log.v(TAG, "parse exception");
            e.printStackTrace();
        }
    }

    public String toString(){
        return updateDateFormat.format(dateTime);
    }

    public String toShortString(){
        return shortDateFormat.format(dateTime);
    }

    public String getDateString(){
        return dateFormat.format(dateTime);
    }

    public String getHourString() {
        return hourFormat.format(dateTime);
    }

    public static String secondsToString(int seconds){
        int min = seconds/60;
        int sec = seconds%60;

        if(min>0)
            return String.format("%1$2dmin%2$2ds", min, sec);
        else
            return String.format("%2ds", sec);
    }

}
