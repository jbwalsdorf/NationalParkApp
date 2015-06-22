package com.jeffwalsdorf.nationalparksapp;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.jeffwalsdorf.nationalparksapp.data.ParkDataContract;

import static com.jeffwalsdorf.nationalparksapp.data.ParkDataContract.ParkNamesEntry;

public class ParkNameFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = ParkNameFragment.class.getSimpleName();

    private static final String LIST_STATE = "list_state";

    private static final String SEARCH_TEXT = "search_text";

    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    private String mSetSearchText;

    private static final String SORT_ORDER_PREF_KEY = "sort_order";

    private ParkNameAdapter mParkNameAdapter;

    private ListView mListview;

    private SearchView mSearchView;

    private String mCurrSelection = null;

    protected GoogleApiClient googleApiClient;

    protected Location mLastLocation;

    String mSortOrder;

    Parcelable state;

    private static final int PARK_NAME_LOADER = 0;


//  Projection for content provider
    private static final String[] PARK_DATA_COLUMNS = {
            ParkNamesEntry.TABLE_NAME + "." +
                    ParkNamesEntry._ID,
            ParkNamesEntry.COLUMN_PARK_NAME,
            ParkNamesEntry.COLUMN_PARK_IDNUM,
            ParkNamesEntry.COLUMN_PARK_LAT,
            ParkNamesEntry.COLUMN_PARK_LONG,
            ParkNamesEntry.COLUMN_PARK_TYPE,
            ParkNamesEntry.COLUMN_PARK_URL
    };

    static final int COL_PARK_DATA_ID = 0;
    static final int COL_PARK_DATA_NAME = 1;
    static final int COL_PARK_DATA_IDNUM = 2;
    static final int COL_PARK_DATA_LAT = 3;
    static final int COL_PARK_DATA_LONG = 4;
    static final int COL_PARK_DATA_TYPE = 5;
    static final int COL_PARK_DATA_URL = 6;

//  Main activity callbacks.  These will properly handle the data transfers between fragments.d
    public interface Callback {
        boolean isTwoPane();
        void onItemSelected(Uri parkUri);
    }

    public ParkNameFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = null;

        switch (mSortOrder) {
//          This will sort the data list using the pythagorean theorem.  True, this does not
//          account for the curve of the earth, which would require more processing, but it's
//          close enough for our needs.
            case "Distance": {
                if (mLastLocation != null) {
                    sortOrder = "((" +
                            mLastLocation.getLatitude() + " - " + ParkNamesEntry.COLUMN_PARK_LAT + ") * (" +
                            mLastLocation.getLatitude() + " - " + ParkNamesEntry.COLUMN_PARK_LAT + ") + (" +
                            mLastLocation.getLongitude() + " - " + ParkNamesEntry.COLUMN_PARK_LONG + ") * (" +
                            mLastLocation.getLongitude() + " - " + ParkNamesEntry.COLUMN_PARK_LONG + "))";
                }
                break;
            }
            case "Alphabetical": {
                sortOrder = ParkNamesEntry.COLUMN_PARK_NAME + " ASC";
                break;
            }
            default: {
                sortOrder = ParkNamesEntry.COLUMN_PARK_NAME + " ASC";
                break;
            }
        }
        Uri parkNamesUri;

        if (mCurrSelection != null) {
            parkNamesUri = Uri.withAppendedPath(ParkNamesEntry.CONTENT_URI,
                    Uri.encode(mCurrSelection));
        } else {
            parkNamesUri = ParkNamesEntry.CONTENT_URI;
        }

//      Pulls the data from the content provider
        return new CursorLoader(getActivity(),
                parkNamesUri,
                PARK_DATA_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Settings", 0);

        mSortOrder = sharedPreferences.getString(SORT_ORDER_PREF_KEY, "alphabetical");

        mParkNameAdapter = new ParkNameAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListview = (ListView) rootView.findViewById(R.id.listview_results);

        mListview.setAdapter(mParkNameAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(LIST_STATE)) {
            state = savedInstanceState.getParcelable(LIST_STATE);
        }

//      Calls the method to
        buildGoogleApiClient();

        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                Uri uri = ParkDataContract.ParkInfoEntry.buildParkInfoUri(cursor.getInt(COL_PARK_DATA_IDNUM));
                ((Callback) getActivity()).onItemSelected(uri);

                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SEARCH_TEXT)) {
            mSetSearchText = savedInstanceState.getString(SEARCH_TEXT);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            mListview.setSelection(mPosition);
        }

        return rootView;
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        state = mListview.onSaveInstanceState();

        outState.putParcelable(LIST_STATE, state);
        outState.putString(SEARCH_TEXT, mSearchView.getQuery().toString());

        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SORT_ORDER_PREF_KEY, mSortOrder);
        editor.apply();

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mParkNameAdapter.swapCursor(data);
        if(mPosition!=ListView.INVALID_POSITION){
            mListview.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mParkNameAdapter.swapCursor(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.parkmainmenu, menu);

        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setQueryHint("Enter Park Name");

        if (((Callback) getActivity()).isTwoPane()) {
            mSearchView.setIconified(false);
            mSearchView.clearFocus();
        } else {
            mSearchView.setIconified(true);
        }

//      This automatically updates the list of parks as the user types into the searchview.
//      Each character entry causes a new URI to be processed by the loader, resulting in
//      a new list being populated.
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                this.onQueryTextChange(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mCurrSelection = !TextUtils.isEmpty(newText) ? newText : null;
                getLoaderManager().restartLoader(PARK_NAME_LOADER, null, ParkNameFragment.this);
                return true;
            }
        });

//      This fixes a bug that caused the searchview to forget it's state (opened vs. iconified)
//      when the device was rotated.
        if (!TextUtils.isEmpty(mSetSearchText)) {
            mSearchView.setIconified(false);
            mSearchView.setQuery(mSetSearchText, true);
            mSetSearchText = "";
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String newSortOrder = null;

        switch (item.getItemId()) {
            case R.id.sort_alphabetically:
                newSortOrder = "Alphabetical";
                break;
            case R.id.sort_by_distance:
                newSortOrder = "Distance";
                break;
        }

        if (newSortOrder != mSortOrder) {
            mSortOrder = newSortOrder;
            mListview.setSelection(0);
            mPosition = ListView.INVALID_POSITION;
            getLoaderManager().restartLoader(PARK_NAME_LOADER, null, this);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PARK_NAME_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

//  The next three methods are for Google API Client callbacks

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//        if (mLastLocation == null) {
//            Toast.makeText(getActivity(), "No location Detected", Toast.LENGTH_SHORT).show();
//        } else {
        getLoaderManager().restartLoader(PARK_NAME_LOADER, null, ParkNameFragment.this);
//        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "Connection Suspended");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = " +
                connectionResult.getErrorCode());
    }

}
