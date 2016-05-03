package cs528_mateus_amogh.smartuv.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.LinkedList;
import java.util.List;
//Custom packages
import cs528_mateus_amogh.smartuv.DateTime;
import cs528_mateus_amogh.smartuv.UVDayUpdate;
import cs528_mateus_amogh.smartuv.UVHourUpdate;

/**
 Handle the interaction with the database
 */
public class UVIndexDbManager {

    private static final String TAG = "DbManager";

    private Context mContext;
    private SQLiteDatabase mDatabase = null;
    private static UVIndexDbManager sInstance = null;//To avoid copies along the app

    //Instantiate an UVIndexDbManager with this static function
    public static UVIndexDbManager getInstance(Context context){
        if(sInstance==null)
            sInstance = new UVIndexDbManager(context);
        return sInstance;
    }

    //Do not use this constructor
    public UVIndexDbManager(Context context){
        mContext = context;
        mDatabase = new UVIndexDbHelper(mContext).getWritableDatabase();
    }

    /*Create a new instance if it does not exist yet
    * Returns false if the instance already exist
    */
    public boolean saveUVDayUpdate(UVDayUpdate dayUpdate){
        //If there is no record for that particular date
        if(getCursorByDate(dayUpdate.getDateTime().getDateString()) == null) {
            for(UVHourUpdate hourUpdate : dayUpdate.getUVHourUpdateList()) {//for each hourUpdate
                //Fill the ContentValues
                ContentValues values = new ContentValues();
                values.put(UVIndexDbSchema.uvUpdateTable.date,hourUpdate.getDateTime().getDateString());
                values.put(UVIndexDbSchema.uvUpdateTable.time,hourUpdate.getDateTime().getHourString());
                //TODO: add locality
                values.put(UVIndexDbSchema.uvUpdateTable.uvIndex,hourUpdate.getUvIndex());
                values.put(UVIndexDbSchema.uvUpdateTable.sunExposureTime,hourUpdate.getExposureTime());
                //Insert in the database
                mDatabase.insert(UVIndexDbSchema.uvUpdateTable.NAME, null, values);
            }
            return true;
        }
        return false;
    }

    /* Update only the exposure time for a particulare day and time
    * Return true if it was successful (not successful if the specified date and time don't exist in the database)
    */
    public boolean updateSunExposureTime(String date, String time, int uvExposure){
        //Fill the contentValues
        ContentValues value = new ContentValues();
        value.put(UVIndexDbSchema.uvUpdateTable.sunExposureTime, uvExposure);
        //Update the value
        int nRows = mDatabase.update(UVIndexDbSchema.uvUpdateTable.NAME, value,
                UVIndexDbSchema.uvUpdateTable.date + " = ? and " + UVIndexDbSchema.uvUpdateTable.time + " = ?",
                new String[]{date,time});
        Log.v(TAG, "nRows = " + nRows);
        if(nRows > 0)//supposed to be only 1 affected row
            return true;
        return false;
    }

    //Perform the specified raw query and return the resulting Cursor. Returns null if there is no result
    private Cursor getCursor(String query){
        Cursor cursor = mDatabase.rawQuery(query, null);
        if(cursor.getCount() <= 0){//no result
            cursor.close();
            return null;
        }
        return cursor;
    }

    //Query instances with the specified date and time. Return the resulting Cursor (null if there is no result)
    //Supposed to return only one instance or null
    private Cursor getCursorByDateTime(String date, String time){
        String query = "Select * from " + UVIndexDbSchema.uvUpdateTable.NAME + " where " +
                UVIndexDbSchema.uvUpdateTable.date + " = '" + date + "' and " +
                UVIndexDbSchema.uvUpdateTable.time + " = '" + time + "'";
        return getCursor(query);
    }

    //Query instances with the specified date. Return the resulting Cursor (null if there is no result)
    //Supposed to return one instance for each hour or null
    private Cursor getCursorByDate(String date){
        String query = "Select * from " + UVIndexDbSchema.uvUpdateTable.NAME + " where " +
                UVIndexDbSchema.uvUpdateTable.date + " = '" + date + "'";
        return getCursor(query);
    }

    //get the UVDayUpdate of a particular date. Return null if it does not exist
    public UVDayUpdate getUVDayUpdate(String date){
        Cursor cursor = getCursorByDate(date);

        if(cursor == null)//no instance found
            return null;

        cursor.moveToFirst();
        List<UVHourUpdate> list = new LinkedList<UVHourUpdate>();
        do{
            //extract database components
            String date_out = cursor.getString(cursor.getColumnIndex(UVIndexDbSchema.uvUpdateTable.date));
            String time = cursor.getString(cursor.getColumnIndex(UVIndexDbSchema.uvUpdateTable.time));
            int uvIndex = cursor.getInt(cursor.getColumnIndex(UVIndexDbSchema.uvUpdateTable.uvIndex));
            int sunExposure = cursor.getInt(cursor.getColumnIndex(UVIndexDbSchema.uvUpdateTable.sunExposureTime));

            //Construct the UvHourUpdate and add to the list
            UVHourUpdate update = new UVHourUpdate(uvIndex,new DateTime(date_out,time));
            update.addExposureTime(sunExposure);
            list.add(update);
        }while(cursor.moveToNext());

        cursor.close();

        return new UVDayUpdate(list);
    }

    //Close database
    public void close(){
        mDatabase.close();
    }

    //Add an Indoor/Outdoor transition event
    //state: MainService.INDOOR (0) or MainService.OUTDOOR (1)
    public void addIoState(int state, long timestamp){
        ContentValues values = new ContentValues();

        values.put(UVIndexDbSchema.ioStateTable.timestamp,timestamp);
        values.put(UVIndexDbSchema.ioStateTable.ioState, state);

        mDatabase.insert(UVIndexDbSchema.ioStateTable.NAME, null, values);
    }

}
