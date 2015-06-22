package com.jeffwalsdorf.nationalparksapp;


import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;


public class MainActivity extends AppCompatActivity
        implements ParkNameFragment.Callback {

    private boolean mTwoPane;

    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
        }

        if (findViewById(R.id.detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new DetailFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    public void onItemSelected(Uri parkUri) {

        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, parkUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.detail_container, fragment, DETAIL_FRAGMENT_TAG);
            fragmentTransaction.commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(parkUri);
            startActivity(intent);
        }
    }
}
