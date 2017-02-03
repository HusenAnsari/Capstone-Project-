package com.husenansari.theprompter;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.husenansari.theprompter.data.PrompterContract;
import com.husenansari.theprompter.data.Script;
import com.husenansari.theprompter.data.ScriptsProvider;

public class EditScriptActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    String scriptId;

    private TextView scriptTitle, scriptContent;
    private TextInputLayout scriptTitleLayout, scriptContentLayout;

    private Script script;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_script);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        scriptId = getIntent().getExtras().getString("id");
        getSupportActionBar().setTitle(getString(R.string.edit_script));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scriptTitle = (TextView) findViewById(R.id.script_title);
        scriptContent = (TextView) findViewById(R.id.script_content);

        scriptTitleLayout = (TextInputLayout) findViewById(R.id.script_title_container);
        scriptContentLayout = (TextInputLayout) findViewById(R.id.script_content_container);

        getSupportLoaderManager().initLoader(0, null, this);

        ((Application) getApplication()).startTracking();
    }

    public void save()
    {
        if (scriptTitle.getText().length() <= 5) {
            scriptTitleLayout.setError(getString(R.string.error_title_length));
            scriptTitleLayout.setErrorEnabled(true);
            return;
        }
        if (scriptContent.getText().length() <= 10) {
            scriptContentLayout.setError(getString(R.string.error_content_length));
            scriptContentLayout.setErrorEnabled(true);
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(PrompterContract.ScriptEntry.TITLE, scriptTitle.getText().toString());
        contentValues.put(PrompterContract.ScriptEntry.CONTENT, scriptContent.getText().toString());
        getContentResolver().update(ScriptsProvider.SCRIPT_BASE_URI.buildUpon().appendPath(scriptId).build(), contentValues, null, null);

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_script_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                save();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getBaseContext(), ScriptsProvider.SCRIPT_BASE_URI.buildUpon().appendPath(scriptId).build(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        script = Script.populate(data);

        scriptTitle.setText(script.getTitle());
        scriptContent.setText(script.getContent());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
