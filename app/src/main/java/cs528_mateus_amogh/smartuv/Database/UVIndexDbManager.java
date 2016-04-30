package cs528_mateus_amogh.smartuv.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.util.LinkedList;
import java.util.List;

import cs528_mateus_amogh.smartuv.DateTime;
import cs528_mateus_amogh.smartuv.UVDayUpdate;
import cs528_mateus_amogh.smartuv.UVHourUpdate;

/**
 * Created by mateus on 4/25/2016.
 */
public class UVIndexDbManager {

    private Context mContext;
    private SQLiteDatabase mDatabase = null;
    private static UVIndexDbManager sInstance = null;


    public static UVIndexDbManager getInstance(Context context){
        if(sInstance==null)
            sInstance = new UVIndexDbManager(context);
        return sInstance;
    }

    public UVIndexDbManager(Context context){
        mContext = context;
        mDatabase = new UVIndexDbHelper(mContext).getWritableDatabase();
    }

    //Update UVIndex values of future hours. Create a new instance if it does not exist yet
    public boolean saveUVDayUpdate(UVDayUpdate dayUpdate){
        if(getCursorByDate(dayUpdate.getDateTime().getDateString()) == null) {
            for(UVHourUpdate hourUpdate : dayUpdate.getUVHourUpdateList()) {
                ContentValues values = new ContentValues();
                values.put(UVIndexDbSchema.uvUpdateTable.date,hourUpdate.getDateTime().getDateString());
                values.put(UVIndexDbSchema.uvUpdateTable.time,hourUpdate.getDateTime().getHourString());
                //TODO: add locality
                values.put(UVIndexDbSchema.uvUpdateTable.uvIndex,hourUpdate.getUvIndex());
                values.put(UVIndexDbSchema.uvUpdateTable.sunExposureTime,hourUpdate.getExposureTime());

                mDatabase.insert(UVIndexDbSchema.uvUpdateTable.NAME, null, values);
            }
            return true;
        }
        return false;
    }

    //Return if it was successful or not (not successful if the specified date and time don't exist in the database)
    public boolean updateSunExposureTime(String date, String time, int uvExposure){
        ContentValues value = new ContentValues();
        value.put(UVIndexDbSchema.uvUpdateTable.sunExposureTime, uvExposure);
        int nRows = mDatabase.update(UVIndexDbSchema.uvUpdateTable.NAME, value,
                UVIndexDbSchema.uvUpdateTable.date + " = ? and " + UVIndexDbSchema.uvUpdateTable.time + " = ?",
                new String[]{date,time});
        Log.v("DbManager", "nRows = " + nRows);
        if(nRows > 0)
            return true;
        return false;
    }

    private Cursor getCursor(String query){
        Log.v("Manager", "before");
        Cursor cursor = mDatabase.rawQuery(query, null);
        Log.v("Manager", "passed");
        if(cursor.getCount() <= 0){
            cursor.close();
            return null;
        }
        return cursor;
    }

    private Cursor getCursorByDateTime(String date, String time){
        String query = "Select * from " + UVIndexDbSchema.uvUpdateTable.NAME + " where " +
                UVIndexDbSchema.uvUpdateTable.date + " = '" + date + "' and " +
                UVIndexDbSchema.uvUpdateTable.time + " = '" + time + "'";
        return getCursor(query);
    }

    private Cursor getCursorByDate(String date){
        String query = "Select * from " + UVIndexDbSchema.uvUpdateTable.NAME + " where " +
                UVIndexDbSchema.uvUpdateTable.date + " = '" + date + "'";
        return getCursor(query);
    }

    //get the UVDayUpdate of a particular date. Return null if it does not exist
    public UVDayUpdate getUVDayUpdate(String date){
        Cursor cursor = getCursorByDate(date);

        if(cursor == null)
            return null;

        cursor.moveToFirst();
        List<UVHourUpdate> list = new LinkedList<UVHourUpdate>();

        do{
            String date_out = cursor.getString(cursor.getColumnIndex(UVIndexDbSchema.uvUpdateTable.date));
            String time = cursor.getString(cursor.getColumnIndex(UVIndexDbSchema.uvUpdateTable.time));
            int uvIndex = cursor.getInt(cursor.getColumnIndex(UVIndexDbSchema.uvUpdateTable.uvIndex));
            int sunExposure = cursor.getInt(cursor.getColumnIndex(UVIndexDbSchema.uvUpdateTable.sunExposureTime));
            UVHourUpdate update = new UVHourUpdate(uvIndex,new DateTime(date_out,time));
            update.addExposureTime(sunExposure);
            list.add(update);
        }while(cursor.moveToNext());

        cursor.close();

        return new UVDayUpdate(list);
    }

    public void close(){
        mDatabase.close();
    }

    public void addIoState(int state, long timestamp){
        ContentValues values = new ContentValues();

        values.put(UVIndexDbSchema.ioStateTable.timestamp,timestamp);
        values.put(UVIndexDbSchema.ioStateTable.ioState, state);

        mDatabase.insert(UVIndexDbSchema.ioStateTable.NAME, null, values);
    }

}
