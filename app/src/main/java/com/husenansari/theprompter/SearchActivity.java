package com.husenansari.theprompter;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.husenansari.theprompter.data.PrompterContract;
import com.husenansari.theprompter.data.ScriptsProvider;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ProgressBar loading;
    TextView status;
    RecyclerView list;
    SearchResultAdapter adapter;

    String query;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.search));

        loading = (ProgressBar) findViewById(R.id.loading);
        status = (TextView) findViewById(R.id.status);
        list = (RecyclerView) findViewById(R.id.list);

        adapter = new SearchResultAdapter(this);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_item));

        searchView.setOnQueryTextListener(this);

        searchView.setIconified(false);
        searchView.setQuery(getString(R.string.default_search_value), true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        this.query = query;
        (new SearchTask()).execute();
        return true;
    }

    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);
        status.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
    }

    public void fetchScript(String title)
    {
        (new AddScriptTask()).execute(title);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public class SearchTask extends AsyncTask<Void, Void, JSONArray> {

        // PetScan is a service which allows searching for titles on MediaWiki based sites.
        // We use it to search on WikiSource's Speeches Category
        String url = "https://petscan.wmflabs.org/?psid=606068&format=json&output_compatability=quick-intersection";

        private String appendSearchParameter(String url)
        {
            return url.concat("&regexp_filter=^(.*"+query+".*)$");
        }

        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected JSONArray doInBackground(Void... strings) {
            try {
                Log.d("Search", appendSearchParameter(url));
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(appendSearchParameter(url))
                        .get()
                        .build();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    JSONObject data = new JSONObject(response.body().string());
                    return data.getJSONArray("pages");
                } else {
                    return null;
                }

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            loading.setVisibility(View.GONE);

            if (jsonArray == null) {
                status.setText(getString(R.string.search_error));
                status.setVisibility(View.VISIBLE);
            } else {
                adapter.setArray(jsonArray);
                list.setVisibility(View.VISIBLE);
                status.setVisibility(View.GONE);
            }
        }
    }

    public class AddScriptTask extends AsyncTask<String, Void, String> {

        String url = "https://en.wikisource.org/api/rest_v1/page/html/";

        String title;

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            title = strings[0];
            String text = null;
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url.concat(title)).get().build();
                Response response = client.newCall(request).execute();
                text = Jsoup.parse(response.body().string()).body().text();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return text;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null) {
                progressDialog.hide();
                Toast.makeText(getApplicationContext(), R.string.search_error, Toast.LENGTH_SHORT).show();
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(PrompterContract.ScriptEntry.TITLE, title);
                contentValues.put(PrompterContract.ScriptEntry.CONTENT, s);
                getContentResolver().insert(ScriptsProvider.SCRIPTS_BASE_URI, contentValues);

                finish();
            }
        }
    }
}
