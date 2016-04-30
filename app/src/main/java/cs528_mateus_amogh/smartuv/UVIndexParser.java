package cs528_mateus_amogh.smartuv;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mateus on 4/15/2016.
 */
public class UVIndexParser extends AsyncTask<String, Void, String> {

    public static final String UV_UPDATE = "UV_UPDATE";
    private final String TAG = "UvIdxParser";

    Context mContext;

    private static UVDayUpdate sUvDayUpdate = null;

    public static UVDayUpdate getLastUvDayUpdate(){
        return sUvDayUpdate;
    }

    public static void requestUvUpdate(Context context, String zipcode){
        String url = UVIndexParser.getUrlString(zipcode);
        new UVIndexParser(context).execute(url);
    }

    public static String getUrlString(String zipcode){
        return String.format("https://iaspub.epa.gov/enviro/efservice/getEnvirofactsUVHOURLY/ZIP/%s/JSON", zipcode);
    }

    public UVIndexParser(Context context){
        mContext = context;
    }

    protected String doInBackground(String... strings){
        String stream = null;
        String urlString = strings[0];
        HTTPDataHandler hh = new HTTPDataHandler();
        stream = hh.GetHTTPData(urlString);
        return stream;
    }

    protected void onPostExecute(String stream){
        sUvDayUpdate = new UVDayUpdate(stream);
            /*
                Important in JSON DATA
                -------------------------
                * Square bracket ([) represents a JSON array
                * Curly bracket ({) represents a JSON object
                * JSON object contains key/value pairs
                * Each key is a String and value may be different data types
             */
        //..........Process JSON DATA................
        if(stream !=null){
            try{
                // Get the full HTTP Data as JSONObject
                JSONArray reader= new JSONArray(stream);
                Intent intent = new Intent(UV_UPDATE);
                intent.putExtra(UV_UPDATE, reader.toString());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                Log.v(TAG, "Broadcasted UVdUpdate");
                // loop and add it to array or arraylist

            }catch(JSONException e){
                e.printStackTrace();
            }
        } // if statement end
    } // onPostExecute() end
}
