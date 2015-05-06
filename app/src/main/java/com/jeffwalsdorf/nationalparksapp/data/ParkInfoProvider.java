package com.jeffwalsdorf.nationalparksapp.data;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import static com.jeffwalsdorf.nationalparksapp.data.ParkDataContract.ParkInfoEntry;
import static com.jeffwalsdorf.nationalparksapp.data.ParkDataContract.ParkNamesEntry;


public class ParkInfoProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private ParkDataDbHelper mOpenHelper;

    static final int ALL_PARKS = 100;
    static final int PARK_NAME = 101;
    static final int PARK_INFO = 200;

    private static final SQLiteQueryBuilder sParkInfoQueryBuilder;

    static {
        sParkInfoQueryBuilder = new SQLiteQueryBuilder();

        sParkInfoQueryBuilder.setTables(
                ParkInfoEntry.TABLE_NAME + " INNER JOIN " +
                        ParkNamesEntry.TABLE_NAME +
                        " ON (" + ParkInfoEntry.TABLE_NAME +
                        "." + ParkInfoEntry.COLUMN_AREA_ID +
                        "=" + ParkNamesEntry.TABLE_NAME +
                        "." + ParkNamesEntry.COLUMN_PARK_IDNUM + ")"
        );
    }

    private static final String sParkNameSelection =
            ParkNamesEntry.TABLE_NAME + "." + ParkNamesEntry.COLUMN_PARK_NAME + " LIKE ? ";

    private static final String sParkInfoSelection =
            ParkInfoEntry.TABLE_NAME + "." + ParkInfoEntry.COLUMN_AREA_ID + " = ? ";


    private Cursor getParkInfo(Uri uri, String[] projection, String sortOrder) {
        String parkId = ParkInfoEntry.getParkIdFromUri(uri);

        String[] selectionArgs = new String[]{parkId};
        String selection = sParkInfoSelection;

        Cursor debug = sParkInfoQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        return debug;
    }

    private Cursor getParkName(Uri uri, String[] projection, String sortOrder) {
        String parkName = ParkNamesEntry.getParkNameFromUri(uri);

        parkName = "%"+parkName+"%";

        String[] selectionArgs = new String[]{parkName};
        String selection = sParkNameSelection;

        Cursor cursor = mOpenHelper.getReadableDatabase().query(
                ParkNamesEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        return cursor;


    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ParkDataContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ParkDataContract.PATH_PARK_NAMES, ALL_PARKS);
        matcher.addURI(authority, ParkDataContract.PATH_PARK_NAMES + "/*", PARK_NAME);
        matcher.addURI(authority, ParkDataContract.PATH_PARK_INFO, PARK_INFO);
        matcher.addURI(authority, ParkDataContract.PATH_PARK_INFO + "/*", PARK_INFO);

        return matcher;
    }


    public boolean onCreate() {
        mOpenHelper = new ParkDataDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case PARK_INFO: {
                retCursor = getParkInfo(uri, projection, sortOrder);
                break;
            }
            case PARK_NAME:{
                retCursor = getParkName(uri,projection,sortOrder);
                break;
            }
            case ALL_PARKS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ParkNamesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PARK_INFO:
                return ParkInfoEntry.CONTENT_TYPE;
            case PARK_NAME:
                return ParkNamesEntry.CONTENT_TYPE;
            case ALL_PARKS:
                return ParkNamesEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ALL_PARKS: {
                long _id = db.insert(ParkNamesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ParkNamesEntry.buildParkNameUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PARK_INFO: {
                long _id = db.insert(ParkInfoEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ParkInfoEntry.buildParkInfoUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_PARKS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues cv : values) {
                        long _id = db.insert(ParkNamesEntry.TABLE_NAME, null, cv);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (match) {
            case ALL_PARKS:
                rowsDeleted = db.delete(ParkNamesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ALL_PARKS:
                rowsUpdated = db.update(ParkNamesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
