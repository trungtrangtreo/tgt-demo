package ca.TransCanadaTrail.TheGreatTrail.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Houari on 10/11/2016.
 */


public class OfflineTrailDBHelper extends SQLiteOpenHelper {

    private static OfflineTrailDBHelper sInstance;

    public static String DB_PATH = "/data/data/ca.TransCanadaTrail.TheGreatTrail/databases/";

    public static String DB_NAME = "OfflineDB.sqlite";
    public static final int DB_VERSION = 1;

    public static final String TB_USER = "db_version";

    private SQLiteDatabase myDB;
    private Context context;

    public static synchronized OfflineTrailDBHelper getInstance(Context context) {

        Log.e("LocationService", "Appel de getInstance   ------------------------------------------------------------------------------   getInstance ");

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new OfflineTrailDBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */

    public OfflineTrailDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        Log.e("LocationService", "Creation de ActivityDBHelperTrail   ------------------------------------------------------------------------------   ActivityDBHelperTrail ");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void close(){
        if(myDB!=null){
            myDB.close();
        }
        super.close();
    }


    /***
     * Copy database from source code assets to device
     * @throws IOException
     */

    public void copyDataBase() throws IOException {
        try {
            InputStream myInput = context.getAssets().open(DB_NAME);
            String outputFileName = DB_PATH + DB_NAME;
            OutputStream myOutput = new FileOutputStream(outputFileName);

            byte[] buffer = new byte[1024];
            int length;

            while((length = myInput.read(buffer))>0){
                myOutput.write(buffer, 0, length);
            }

            myOutput.flush();
            myOutput.close();
            myInput.close();

            Log.e("LocationService", "Copie avec succes ------------------------------------------------------------------------------ succes");

        } catch (Exception e) {
            Log.e("LocationService", e.getMessage()+"------------------------------------------------------------------------------ error");
        }

    }


    /***
     * Open database
     * @throws SQLException
     */
    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }


    /***
     * Check if the database doesn't exist on device, create new one
     * @throws IOException
     */
    public void createDataBase() throws IOException {
        Log.i("LocationService","entree      ----Reussssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssie ------------------------");


        boolean dbExist = checkDataBase();

        if (dbExist) {

        } else {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                Log.e("LocationService", e.getMessage());
            }
        }
    }


    /***
     * Check if the database is exist on device or not
     * @return
     */
    private boolean checkDataBase() {
        SQLiteDatabase tempDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            tempDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            Log.e("LocationService", e.getMessage());
        }
        if (tempDB != null)
            tempDB.close();
        Log.i("LocationService","REponse Trouve est ------------------------"+(tempDB != null ? true : false));

        return tempDB != null ? true : false;
    }



    public List<String> getAllUsers(){
        List<String> listUsers = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;

        try {
            c = db.rawQuery("SELECT * FROM " + TB_USER , null);
            if(c == null) return null;

            String name;
            c.moveToFirst();
            do {
                name = c.getString(1);
                listUsers.add(name);
            } while (c.moveToNext());
            c.close();
        } catch (Exception e) {
            Log.e("LocationService", e.getMessage());
        }

        db.close();

        return listUsers;
    }


    public String  getVersion(){
        String version= "0.0";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelperTrail : Request = "+"Select version  from db_version limit 1");

        Cursor cursor = db.rawQuery("Select version  from db_version limit 1", null);


        if (cursor != null) {
            cursor.moveToFirst();
            version = cursor.getString(cursor.getColumnIndex("version"));
            Log.e("LocationService", "ActivityDBHelper ----------------------------------------------  version" + version);
        }
        return version;
    }


    public Cursor getAllSegments(){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelperTrail : Request = "+"Select * from  trail_data ORDER BY segmentid");

        Cursor cursor = db.rawQuery("Select * from  trail_data ORDER BY segmentid", null);


        if (cursor != null) {
            cursor.moveToFirst();
            Log.e("LocationService", "ActivityDBHelper : getAllActivities  Cursor not null");
        }
        return cursor;
    }


    public Cursor getAllSegmentsLight(){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelperTrail : Request = "+"Select objectid, geometry from  trail_data ORDER BY trailid");

        Cursor cursor = null;

        try{
            cursor = db.rawQuery("Select objectid, geometry, statuscode, categorycode from  trail_data   WHERE provinceid='05' AND sectionid = '01'   ORDER BY trailid", null);  // WHERE provinceid='05' AND sectionid = '01'
        }
        catch(Exception e) {
            Log.e("LocationService", "pb in datbase ......................................................."+e.toString());
        }



        if (cursor != null) {
            cursor.moveToFirst();
            Log.e("LocationService", "ActivityDBHelper : getAllActivities  Cursor not null");
        }
        return cursor;
    }

    public Cursor getAllCoordinates(){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelperTrail : Request = "+"Select objectid, geometry from  trail_data ORDER BY trailid");

        Cursor cursor = db.rawQuery("Select geometry from  trail_data  ORDER BY trailid", null);  //  WHERE provinceid='05'


        if (cursor != null) {
            cursor.moveToFirst();
            Log.e("LocationService", "ActivityDBHelper : getAllActivities  Cursor not null");
        }
        return cursor;
    }


    public Cursor getSpecificSegments(int objectId){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from  trail_data  where objectid = "+objectId, null);  // WHERE provinceid='05' AND sectionid = '01'

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }


    public Cursor getTrailsBySearch(String searchTxt){

        //SELECT * FROM trail_data WHERE trailname LIKE '%a%'
        SQLiteDatabase db = this.getReadableDatabase();
        String query="SELECT * FROM trail_data WHERE trailname LIKE '%"+searchTxt+"%' GROUP BY trailid ORDER BY trailname ASC";
        Cursor cursor = db.rawQuery(query, null);  // WHERE provinceid='05' AND sectionid = '01'

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public Cursor getTrailByID(String trailID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query="SELECT * FROM trail_data WHERE trailid = '"+trailID+"' ORDER BY segmentid";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

}


 /*   // Table Names
    private static final String TRAIL_TABLE = "trail_data";
    private static final String VERSION_TABLE = "db_version";


    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TCTrailData.sqlite";



    private static final String SQL_DELETE_ACTIVITY = "DROP TABLE IF EXISTS " + "activity";
    private static final String SQL_DELETE_LOCATION = "DROP TABLE IF EXISTS " + "location";
    private static final String SQL_DELETE_ACTIVITY_REGION = "DROP TABLE IF EXISTS " + "activity_region";

    public ActivityDBHelperTrail(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i("LocationService","JE suis entree dans la base de donnees .............................................................................. TCTrailDb.sqlite");
    }

    public void onCreate(SQLiteDatabase db) {

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over

    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    //  Activity Table CRUD






    public Cursor getAllSegments(){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelperTrail : Request = "+"Select * from "+TRAIL_TABLE+" ORDER BY segmentid");

        Cursor cursor = db.rawQuery("Select * from "+TRAIL_TABLE+" ORDER BY segmentid", null);


        if (cursor != null) {
            cursor.moveToFirst();
            Log.e("LocationService", "ActivityDBHelper : getAllActivities  Cursor not null");
        }
        return cursor;
    }


    private void addTrailToMap() {

    }


    public String  getVersion(){
        String version= "0.0";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelperTrail : Request = "+"Select version  from db_version limit 1");

        Cursor cursor = db.rawQuery("Select version  from db_version limit 1", null);


        if (cursor != null) {
            cursor.moveToFirst();
            version = cursor.getString(cursor.getColumnIndex("version"));
            Log.e("LocationService", "ActivityDBHelper ----------------------------------------------  version" + version);
        }
        return version;
    }

*/


