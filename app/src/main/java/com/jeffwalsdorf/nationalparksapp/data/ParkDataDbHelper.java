package com.jeffwalsdorf.nationalparksapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.jeffwalsdorf.nationalparksapp.data.ParkDataContract.ParkInfoEntry;

public class ParkDataDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "ParkDataDbHelper";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "park_data.s3db";
    private static String DATABASE_PATH = null;

    private final Context mContext;

    public ParkDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;

        if (Build.VERSION.SDK_INT >= 17) {
            DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DATABASE_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }

        if (!this.checkDataBase()) {
            try {
                this.createDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_PARK_INFO_TABLE = "CREATE TABLE " +
                ParkInfoEntry.TABLE_NAME + " (" +
                ParkInfoEntry._ID + " INTEGER PRIMARY KEY," +
                ParkInfoEntry.COLUMN_AREA_ACTIVITY_ARRAY + " TEXT NULL," +
                ParkInfoEntry.COLUMN_AREA_PHONE + " TEXT NULL," +
                ParkInfoEntry.COLUMN_AREA_DESC + " TEXT NULL," +
                ParkInfoEntry.COLUMN_AREA_ID + " TEXT UNIQUE NOT NULL," +
                ParkInfoEntry.COLUMN_AREA_ZIP + " TEXT NULL," +
                ParkInfoEntry.COLUMN_AREA_ADDR_1 + " TEXT NULL," +
                ParkInfoEntry.COLUMN_AREA_ADDR_2 + " TEXT NULL," +
                ParkInfoEntry.COLUMN_AREA_STATE + " TEXT NULL," +
                ParkInfoEntry.COLUMN_AREA_CITY + " TEXT NULL," +
                ParkInfoEntry.COLUMN_AREA_URL + " TEXT NULL," +

//                " FOREIGN KEY (" + ParkInfoEntry.COLUMN_AREA_ID + ") REFERENCES " +
//                ParkNamesEntry.TABLE_NAME + " (" + ParkNamesEntry.COLUMN_PARK_IDNUM + "), " +

                " UNIQUE (" + ParkInfoEntry.COLUMN_AREA_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_PARK_INFO_TABLE);
    }

    public void createDatabase() throws IOException {

        boolean mDataBaseExist = checkDataBase();

        if (!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDataBase();
                Log.d(TAG, "Database created");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(DATABASE_NAME);
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ParkInfoEntry.TABLE_NAME);
        onCreate(db);
    }

//    public boolean openDataBase() throws SQLException {
//        String mPath = DATABASE_PATH + DATABASE_NAME;
//        mDataBase = SQLiteDatabase.openDatabase(mPath, null,
//                SQLiteDatabase.CREATE_IF_NECESSARY);
//        return mDataBase != null;
//    }

//    public synchronized void close() {
//        if (mDataBase != null) mDataBase.close();
//        super.close();
//    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //super.onDowngrade(db, oldVersion, newVersion);

    }
}