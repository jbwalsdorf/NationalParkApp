package com.jeffwalsdorf.nationalparksapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class DetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState==null){
            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailFragment.DETAIL_URI,getIntent().getData());

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(bundle);

            getFragmentManager().beginTransaction()
                    .add(R.id.detail_container,fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
