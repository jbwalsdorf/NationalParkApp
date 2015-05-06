package com.jeffwalsdorf.nationalparksapp;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeffwalsdorf.nationalparksapp.data.ParkDataContract;
import com.jeffwalsdorf.nationalparksapp.data.ParkDataContract.ParkInfoEntry;
import com.jeffwalsdorf.nationalparksapp.data.ParkDataContract.ParkNamesEntry;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment implements
        OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    //private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    static final String DETAIL_URI = "URI";

    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            ParkInfoEntry.TABLE_NAME + "." + ParkInfoEntry._ID,
            ParkInfoEntry.TABLE_NAME + "." + ParkInfoEntry.COLUMN_AREA_ID,
            ParkNamesEntry.COLUMN_PARK_NAME,
            ParkInfoEntry.COLUMN_AREA_ADDR_1,
            ParkInfoEntry.COLUMN_AREA_ADDR_2,
            ParkInfoEntry.COLUMN_AREA_CITY,
            ParkInfoEntry.COLUMN_AREA_STATE,
            ParkInfoEntry.COLUMN_AREA_ZIP,
            ParkInfoEntry.COLUMN_AREA_PHONE,
            ParkInfoEntry.COLUMN_AREA_DESC,
            ParkInfoEntry.COLUMN_AREA_ACTIVITY_ARRAY,
            ParkNamesEntry.COLUMN_PARK_URL,
            ParkNamesEntry.COLUMN_PARK_LAT,
            ParkNamesEntry.COLUMN_PARK_LONG,
            ParkInfoEntry.COLUMN_AREA_URL
    };

    public static final int COL_PARK_ID = 0;
    public static final int COL_PARK_ID_NUM = 1;
    public static final int COL_PARK_NAME = 2;
    public static final int COL_PARK_ADDR1 = 3;
    public static final int COL_PARK_ADDR2 = 4;
    public static final int COL_PARK_CITY = 5;
    public static final int COL_PARK_STATE = 6;
    public static final int COL_PARK_ZIP = 7;
    public static final int COL_PARK_PHONE = 8;
    public static final int COL_PARK_DESC = 9;
    public static final int COL_PARK_ACTIVITY = 10;
    public static final int COL_PARK_URL = 11;
    public static final int COL_PARK_LAT = 12;
    public static final int COL_PARK_LONG = 13;
    public static final int COL_AREA_URL = 14;

    private ImageView mParkPicture;
    private TextView mParkName;
    private TextView mParkAddr1;
    private TextView mParkAddr2;
    private TextView mParkCityStateZip;
    private TextView mParkPhone;
    private TextView mParkDesc;
    private TextView mParkURL;

    private TextView mActHeader;
    private TextView mParkActivityDesc;

    private MapView mMapView;
    GoogleMap mParkMap;
    CameraPosition cameraPosition;

    private TextView mDataSource;

    LatLng latLng;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = getArguments();

        FetchParkDataTask fetchParkDataTask = new FetchParkDataTask(getActivity());

        if (bundle != null) {
            mUri = bundle.getParcelable(DETAIL_URI);
            String parkId = ParkDataContract.ParkInfoEntry.getParkIdFromUri(mUri);
            fetchParkDataTask.execute(parkId);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mParkPicture = (ImageView) rootView.findViewById(R.id.park_detail_image);
        mParkName = (TextView) rootView.findViewById(R.id.park_detail_name);
        mParkDesc = (TextView) rootView.findViewById(R.id.park_detail_desc);
        mParkAddr1 = (TextView) rootView.findViewById(R.id.park_detail_addr_1);
        mParkAddr2 = (TextView) rootView.findViewById(R.id.park_detail_addr_2);
        mParkCityStateZip = (TextView) rootView.findViewById(R.id.park_detail_C_S_Z);
        mParkPhone = (TextView) rootView.findViewById(R.id.park_detail_phone);
        mParkURL = (TextView) rootView.findViewById(R.id.park_detail_url);

        mActHeader = (TextView) rootView.findViewById(R.id.activities_header);
        mParkActivityDesc = (TextView) rootView.findViewById(R.id.park_detail_activities);

        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.setVisibility(View.GONE);

        mDataSource = (TextView) rootView.findViewById(R.id.data_source);

        return rootView;
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d("LOADER:", "Loader created");

        if (mUri != null) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {

            Log.d("LOADER:", "Loader finished");

            Uri uri = Uri.parse(data.getString(COL_PARK_URL));
            Picasso.with(getActivity()).load(uri).placeholder(R.drawable.usfs).into(mParkPicture);

            getActivity().setTitle(data.getString(COL_PARK_NAME));

            mParkName.setText(data.getString(COL_PARK_NAME));

            if (data.getString(COL_PARK_ADDR1) != null && !data.getString(COL_PARK_ADDR1).isEmpty()) {
                mParkAddr1.setText(data.getString(COL_PARK_ADDR1));
            } else {
                mParkAddr1.setVisibility(View.GONE);
            }

            if (data.getString(COL_PARK_ADDR2) != null && !data.getString(COL_PARK_ADDR2).isEmpty()) {
                mParkAddr2.setText(data.getString(COL_PARK_ADDR2));
            } else {
                mParkAddr2.setVisibility(View.GONE);
            }

            if (data.getString(COL_PARK_CITY) != null && !data.getString(COL_PARK_CITY).isEmpty()) {
                String cityStZip = data.getString(COL_PARK_CITY) + ", " +
                        data.getString(COL_PARK_STATE) + " " +
                        data.getString(COL_PARK_ZIP);
                mParkCityStateZip.setText(cityStZip);
            } else {
                mParkCityStateZip.setVisibility(View.GONE);
            }

            if (data.getString(COL_PARK_PHONE) != null && !data.getString(COL_PARK_PHONE).isEmpty()) {
                mParkPhone.setText(data.getString(COL_PARK_PHONE));
            } else {
                mParkPhone.setVisibility(View.GONE);
            }

            if (data.getString(COL_AREA_URL) != null && !data.getString(COL_AREA_URL).isEmpty()) {
                String officialUrl = "<a href=\"" + data.getString(COL_AREA_URL) + "\">Official Website</a>";
                mParkURL.setText(Html.fromHtml(officialUrl));
                mParkURL.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                mParkURL.setVisibility(View.GONE);
            }

            if (data.getString(COL_PARK_DESC) != null && !data.getString(COL_PARK_DESC).isEmpty()) {
                mParkDesc.setText(Html.fromHtml(data.getString(COL_PARK_DESC)));
                mParkDesc.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                mParkDesc.setVisibility(View.GONE);
            }

            if (data.getString(COL_PARK_ACTIVITY) != null && !data.getString(COL_PARK_ACTIVITY).isEmpty()) {
                mActHeader.setText("Activities:");
                mParkActivityDesc.setText(data.getString(COL_PARK_ACTIVITY));
            } else {
                mActHeader.setVisibility(View.GONE);
                mParkActivityDesc.setVisibility(View.GONE);
            }

            latLng = new LatLng(data.getDouble(COL_PARK_LAT), data.getDouble(COL_PARK_LONG));

            cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(13)
                    .build();

            if (mMapView != null) {
                mMapView.getMapAsync(this);
            }

            mDataSource.setText("Data Source: Recreation.gov");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mParkMap = googleMap;

        if (latLng.longitude != 0) {
            mParkMap.addMarker(new MarkerOptions().position(latLng));
            mParkMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mMapView.setVisibility(View.VISIBLE);
        } else {
            mMapView.setVisibility(View.GONE);
        }
    }
}
