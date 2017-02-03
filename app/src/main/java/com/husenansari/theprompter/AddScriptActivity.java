package com.husenansari.theprompter;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.husenansari.theprompter.data.PrompterContract;
import com.husenansari.theprompter.data.ScriptsProvider;

public class AddScriptActivity extends AppCompatActivity {

    private TextView scriptTitle, scriptContent;
    private TextInputLayout scriptTitleLayout, scriptContentLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_script);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getString(R.string.add_script));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scriptTitle = (TextView) findViewById(R.id.script_title);
        scriptContent = (TextView) findViewById(R.id.script_content);

        scriptTitleLayout = (TextInputLayout) findViewById(R.id.script_title_container);
        scriptContentLayout = (TextInputLayout) findViewById(R.id.script_content_container);

        ((Application) getApplication()).startTracking();
    }

    private void save()
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
        getContentResolver().insert(ScriptsProvider.SCRIPTS_BASE_URI, contentValues);

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


    public void launchSearch(View view) {
        startActivity(new Intent(this, SearchActivity.class));
    }
}
