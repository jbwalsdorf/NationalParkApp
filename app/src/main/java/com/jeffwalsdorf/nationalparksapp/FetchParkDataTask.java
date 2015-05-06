package com.jeffwalsdorf.nationalparksapp;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static com.jeffwalsdorf.nationalparksapp.data.ParkDataContract.ParkInfoEntry;

public class FetchParkDataTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchParkDataTask.class.getSimpleName();

    private final Context mContext;

    private String text;

    public FetchParkDataTask(Context context) {
        mContext = context;
    }

    private void getParkInfoFromXML(String parkInfoXMLStr)
            throws XmlPullParserException, IOException {

        final String REC_AREA_PHONE = "RecAreaPhone";
        final String REC_AREA_DESC = "RecAreaDescription";
        final String REC_AREA_ID = "RecAreaID";

        final String REC_ACTIVITY_NAME = "RecActivityName";

        final String REC_AREA_ZIP = "PostalCode";
        final String REC_AREA_ADDR1 = "StreetAddress1";
        final String REC_AREA_ADDR2 = "StreetAddress2";
        final String REC_AREA_STATE = "AddressStateCode";
        final String REC_AREA_CITY = "City";

        final String REC_AREA_URL = "URL";

        ContentValues parkInfoValues = new ContentValues();

        List<String> activityList = new ArrayList<>();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();

        parser.setInput(new StringReader(parkInfoXMLStr));

        int eventType = parser.getEventType();

        Boolean copyUrl = false;

        while (eventType != XmlPullParser.END_DOCUMENT) {

            String tagname = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    parser.next();
                    text = parser.getText();

                    if (text == null) {
                        text = "";
                    }
                    text = text.trim();

                    if (text.equalsIgnoreCase("Official Web Site")) {
                        copyUrl = true;
                    }

                    if (tagname.equalsIgnoreCase(REC_AREA_PHONE)) {
                        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_PHONE, text);
                    } else if (tagname.equalsIgnoreCase(REC_AREA_DESC)) {
                        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_DESC, text);
                    } else if (tagname.equalsIgnoreCase(REC_AREA_ID)) {
                        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_ID, text);
                    } else if (tagname.equalsIgnoreCase(REC_AREA_ZIP)) {
                        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_ZIP, text);
                    } else if (tagname.equalsIgnoreCase(REC_AREA_ADDR1)) {
                        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_ADDR_1, text);
                    } else if (tagname.equalsIgnoreCase(REC_AREA_ADDR2)) {
                        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_ADDR_2, text);
                    } else if (tagname.equalsIgnoreCase(REC_AREA_STATE)) {
                        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_STATE, text);
                    } else if (tagname.equalsIgnoreCase(REC_AREA_CITY)) {
                        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_CITY, text);
                    } else if (tagname.equalsIgnoreCase(REC_ACTIVITY_NAME)) {
                        activityList.add(WordUtils.capitalizeFully(text));
                    } else if (tagname.equalsIgnoreCase(REC_AREA_URL) && copyUrl) {
                        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_URL, text);
                        copyUrl = false;
                    }
                    break;
            }

            eventType = parser.next();
        }

        java.util.Collections.sort(activityList);

        String activityString = TextUtils.join(", ", activityList);

        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_ACTIVITY_ARRAY, activityString);

        Uri insertedUri = mContext.getContentResolver()
                .insert(ParkInfoEntry.CONTENT_URI, parkInfoValues);

        Log.d("XML Parser:", "Data from web saved");
    }

    private void addParkInfoFromJson(String parkInfoJsonStr)
            throws JSONException {

//        This is never used as the Recreation.gov JSON server was taken down
//        during the middle of this project (awesome...).  The project uses
//        Recreation.gov's XML server until they take that down.

        final String REC_AREA_INFO = "RecArea";
        final String REC_AREA_PHONE = "RecAreaPhone";
        final String REC_AREA_DESC = "RecAreaDescription";
        final String REC_AREA_ID = "RecAreaID";

        final String REC_ACTIVITY_HEAD = "RecAreaActivity";
        final String REC_ACTIVITY_NAME = "RecAreaActivityDescription"; //Might have to be an array

        final String REC_AREA_ADDRESS = "RecAreaAddress";
        final String REC_AREA_ZIP = "PostalCode";
        final String REC_AREA_ADDR1 = "StreetAddress1";
        //final String REC_AREA_ADDR2 = "StreetAddress2";
        final String REC_AREA_STATE = "AddressStateCode";
        final String REC_AREA_CITY = "City";

        JSONObject parkInfoJson = new JSONObject(parkInfoJsonStr);

        JSONArray activityArray = parkInfoJson.getJSONArray(REC_ACTIVITY_HEAD);

        List<String> activityList = new ArrayList<>();

        int len = activityArray.length();

        for (int i = 0; i < len; i++) {

            String activityDescription;
            JSONObject activityObject = activityArray.getJSONObject(i);
            activityDescription = activityObject.getString(REC_ACTIVITY_NAME);
            activityList.add(activityDescription);
        }

        String activityArrayInString = TextUtils.join(",", activityList);

        JSONObject recAreaInfoJson = parkInfoJson.getJSONObject(REC_AREA_INFO);
        String areaPhone = recAreaInfoJson.getString(REC_AREA_PHONE);
        String areaDesc = recAreaInfoJson.getString(REC_AREA_DESC);
        int areaId = recAreaInfoJson.getInt(REC_AREA_ID);

        JSONObject recAreaAddrJson = parkInfoJson.getJSONObject(REC_AREA_ADDRESS);
        String areaZip = recAreaAddrJson.getString(REC_AREA_ZIP);
        String areaAddr1 = recAreaAddrJson.getString(REC_AREA_ADDR1);
        String areaState = recAreaAddrJson.getString(REC_AREA_STATE);
        String areaCity = recAreaAddrJson.getString(REC_AREA_CITY);

        Log.d(LOG_TAG, "FetchParkData Complete.");

        ContentValues parkInfoValues = new ContentValues();

        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_ACTIVITY_ARRAY, activityArrayInString);
        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_PHONE, areaPhone);
        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_DESC, areaDesc);
        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_ID, areaId);
        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_ZIP, areaZip);
        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_ADDR_1, areaAddr1);
        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_STATE, areaState);
        parkInfoValues.put(ParkInfoEntry.COLUMN_AREA_CITY, areaCity);

        Uri insertedUri = mContext.getContentResolver().insert(ParkInfoEntry.CONTENT_URI, parkInfoValues);
    }


    @Override
    protected Void doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }

        Log.d("ASYNC:", "Connection to web");

        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;

        String parkInfoJsonStr;

        String method = "ExportbyEntityIDandEntityType";
        //String format = "JSON";
        String format = "XML";
        String entityType = "RecArea";

        try {

            final String PARK_INFO_BASE_URL =
                    "https://ridb.recreation.gov/webservices/RIDBServiceAdv.cfc?";
            final String METHOD_PARAM = "method";
            final String FORMAT_PARAM = "format";
            final String ID_PARAM = "EntityID";
            final String ENTITY_PARAM = "EntityType";

            Uri builtUri = Uri.parse(PARK_INFO_BASE_URL).buildUpon()
                    .appendQueryParameter(METHOD_PARAM, method)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(ID_PARAM, params[0])
                    .appendQueryParameter(ENTITY_PARAM, entityType)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            parkInfoJsonStr = buffer.toString();
            //addParkInfoFromJson(parkInfoJsonStr);
            getParkInfoFromXML(parkInfoJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//            e.printStackTrace();
        } catch (XmlPullParserException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }
}
