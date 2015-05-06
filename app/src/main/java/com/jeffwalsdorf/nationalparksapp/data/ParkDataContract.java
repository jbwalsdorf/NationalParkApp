package com.jeffwalsdorf.nationalparksapp.data;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class ParkDataContract {

    public static final String CONTENT_AUTHORITY = "com.jeffwalsdorf.nationalparksapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PARK_NAMES = "parkNames";

    public static final String PATH_PARK_INFO = "parkInfo";

    public static final class ParkNamesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PARK_NAMES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +
                        "/" + CONTENT_AUTHORITY + "/" + PATH_PARK_NAMES;

        public static final String TABLE_NAME = PATH_PARK_NAMES;

        public static final String COLUMN_PARK_NAME = "RECAREANAME";

        public static final String COLUMN_PARK_IDNUM = "RECAREAID";

        public static final String COLUMN_PARK_LAT = "RECAREALATITUDE";

        public static final String COLUMN_PARK_LONG = "RECAREALONGITUDE";

        public static final String COLUMN_PARK_TYPE = "ENTITYTYPE";

        public static final String COLUMN_PARK_URL = "PICTUREURL";

        public static Uri buildParkNameUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildParkNameFinderUri(String parkName) {
            return CONTENT_URI.buildUpon().appendPath(parkName).build();
        }

        public static String getParkNameFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class ParkInfoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PARK_INFO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" + PATH_PARK_INFO;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" + PATH_PARK_INFO;

        public static final String TABLE_NAME = PATH_PARK_INFO;

        public static final String COLUMN_AREA_ACTIVITY_ARRAY = "activityList";

        public static final String COLUMN_AREA_PHONE = "areaPhone";

        public static final String COLUMN_AREA_DESC = "areaDesc";

        public static final String COLUMN_AREA_ID = "areaId";

        public static final String COLUMN_AREA_ZIP = "areaZip";

        public static final String COLUMN_AREA_ADDR_1 = "areaAddr1";

        public static final String COLUMN_AREA_ADDR_2 = "areaAddr2";

        public static final String COLUMN_AREA_STATE = "areaState";

        public static final String COLUMN_AREA_CITY = "areaCity";

        public static final String COLUMN_AREA_URL = "URL";

        public static Uri buildParkInfoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildParkInfoId(int parkId) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(COLUMN_AREA_ID, Integer.toString(parkId))
                    .build();
        }

        public static String getParkIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }


    }

}
