package cs528_mateus_amogh.smartuv.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mateus on 4/20/2016.
 */
public class UVIndexDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "uvDbHelper";//for debug purposes
    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "uvIndex.db";


    public UVIndexDbHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    public UVIndexDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + UVIndexDbSchema.uvUpdateTable.NAME + "(" +
                        " _id integer primary key autoincrement, " +
                        UVIndexDbSchema.uvUpdateTable.date + ", " +
                        UVIndexDbSchema.uvUpdateTable.time + ", " +
                        //UVIndexDbSchema.uvUpdateTable.zipcode + ", " +
                        UVIndexDbSchema.uvUpdateTable.uvIndex + ", " +
                        UVIndexDbSchema.uvUpdateTable.sunExposureTime +
                        ")"
        );

        db.execSQL("create table " + UVIndexDbSchema.ioStateTable.NAME + "(" +
                        " _id integer primary key autoincrement, " +
                        UVIndexDbSchema.ioStateTable.timestamp + ", " +
                        UVIndexDbSchema.ioStateTable.ioState +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
