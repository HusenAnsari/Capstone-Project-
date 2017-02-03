package com.husenansari.theprompter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.husenansari.theprompter.data.Script;
import com.husenansari.theprompter.data.ScriptsProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ScriptDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    String scriptId;
    Script script;

    MainActivity activity;
    ActionBar actionBar;

    TextView timestamp, contents;
    FloatingActionButton playButton;

    DateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy  hh:mm a");
    Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.script_details, container, false);

        timestamp = (TextView) view.findViewById(R.id.timestamp);
        contents = (TextView) view.findViewById(R.id.script_content);

        playButton = (FloatingActionButton) view.findViewById(R.id.play_fab);


        AdView adView = (AdView) view.findViewById(R.id.script_details_banner_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        scriptId = getArguments().getString("id");
        activity = ((MainActivity) getActivity());
        actionBar = activity.getSupportActionBar();
        if (!activity.isDualScreen()) {
            actionBar.setTitle(getString(R.string.script_details));
            actionBar.setDisplayHomeAsUpEnabled(true);
            playButton.setVisibility(View.VISIBLE);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), TeleprompterActivity.class);
                    intent.putExtra("id", scriptId);
                    startActivity(intent);
                }
            });
        }

        setHasOptionsMenu(true);

        getActivity().getSupportLoaderManager().initLoader((int) Long.parseLong(scriptId), null, this);
        ((Application) getActivity().getApplication()).startTracking();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.script_detail_menu, menu);
        if (! activity.isDualScreen()) {
            menu.setGroupVisible(R.id.add_dummy_content_group, false);
            menu.setGroupVisible(R.id.play_item_menu, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((MainActivity) getActivity()).showMainScreen();
                break;
            case R.id.edit:
                Intent intent = new Intent(getContext(), EditScriptActivity.class);
                intent.putExtra("id", scriptId);
                startActivity(intent);
                break;
            case R.id.play:
                Intent intent2 = new Intent(getContext(), TeleprompterActivity.class);
                intent2.putExtra("id", scriptId);
                startActivity(intent2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), ScriptsProvider.SCRIPT_BASE_URI.buildUpon().appendPath(scriptId).build(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        if (data.getCount() != 0) {
            script = Script.populate(data);
            if (! activity.isDualScreen()) {
                actionBar.setTitle(script.getTitle());
            }

            calendar.setTimeInMillis(script.getTimestamp()*1000);
            timestamp.setText(dateFormat.format(calendar.getTime()));
            contents.setText(script.getContent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
