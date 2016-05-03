package cs528_mateus_amogh.smartuv;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper class to deal with date and time variables
 *
 * Provides DateTime string conversion in different formats as well interface
 * with JSONObjects and Calendar objects
 */
public class DateTime{

    private final String TAG = "DateTime";//For debug purposes

    //DateFormats
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMM dd");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM/dd/yyyy");

    //This format match the date contained in the JSONObject broadcast by UVIndexParser
    //https://www.epa.gov/enviro/web-services
    private final SimpleDateFormat updateDateFormat = new SimpleDateFormat("MMM/dd/yyyy hh aa");


    private final SimpleDateFormat hourFormat = new SimpleDateFormat("hh aa");

    //Store the date and time (TODO: convert to Calendar)
    private Date dateTime;

    //Construct a date time based on the JSONObject broadcasted by UVIndexParser, that get UV Index
    //updates online
    public DateTime(JSONObject updateObj){
        try {
            update(updateObj.getString("DATE_TIME"));//https://www.epa.gov/enviro/web-services
        } catch (JSONException e) {
            Log.v(TAG, "JSONObject exception");
            e.printStackTrace();
        }
    }

    //Construct DateTime with the specified calendar values
    public DateTime(Calendar calendar){
        dateTime  = calendar.getTime();
    }

    //Create DateTime based on string in updateDateFormat with date and time separated
    public DateTime(String date, String time){
        update(date+" "+time);
    }

    //Create DateTime based on string in updateDateFormat
    public DateTime(String updateString){
        update(updateString);
    }

    public Date getDate(){
        return dateTime;
    }

    //Update DateTime based on string in updateDateFormat
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

    //Default updateDateFormat
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

    //Convert seconds to a string in the format "[mm]min[ss]s" (e.g. "32min01s")
    public static String secondsToString(int seconds){
        int min = seconds/60;
        int sec = seconds%60;
        if(min>0)
            return String.format("%1$2dmin%2$2ds", min, sec);
        else
            return String.format("%2ds", sec);
    }

}
