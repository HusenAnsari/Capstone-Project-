package com.husenansari.theprompter;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.husenansari.theprompter.data.Script;
import com.husenansari.theprompter.data.ScriptsProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class RecentScriptWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int n = appWidgetIds.length;

        for (int i = 0; i < n; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, RecentScriptWidgetProvider.WidgetUpdater.class);
            intent.putExtra("widget_id", appWidgetId);
            context.startService(intent);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(context.getString(R.string.recent_script_updated))) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, RecentScriptWidgetProvider.class);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName));
        }
        super.onReceive(context, intent);
    }

    static public class WidgetUpdater extends IntentService {

        private SimpleDateFormat format = new SimpleDateFormat("d MMM yy");
        private Calendar calendar = new GregorianCalendar();

        public WidgetUpdater() {
            super("WidgetUpdater");
        }
        public WidgetUpdater(String name) {
            super(name);
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            int widgetId = intent.getExtras().getInt("widget_id");

            Context context = getApplicationContext();

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String scriptId = preferences.getString("recent_script_id", "no_recent");

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recent_script_widget);

            Intent openActivityIntent = new Intent(context, MainActivity.class);

            if (scriptId.equals("no_recent")) {
                showNoRecentScripts(widgetId, openActivityIntent, views);
            } else {

                Cursor values = getContentResolver().query(
                        ScriptsProvider.SCRIPT_BASE_URI.buildUpon().appendPath(scriptId).build(),
                        null,
                        null,
                        null,
                        null
                );
                values.moveToFirst();

                if (values.getCount() == 0) {
                    showNoRecentScripts(widgetId, openActivityIntent, views);
                } else {

                    Script script = Script.populate(values);
                    values.close();

                    openActivityIntent.putExtra("script_id", scriptId);

                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            context,
                            widgetId,
                            openActivityIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT);

                    views.setOnClickPendingIntent(R.id.content_container, pendingIntent);
                    views.setTextViewText(R.id.script_title, script.getTitle());
                    views.setTextViewText(R.id.script_content_excerpt, script.getContent());
                    calendar.setTimeInMillis(script.getTimestamp() * 1000);
                    views.setTextViewText(R.id.script_date, format.format(calendar.getTime()));
                }
            }
            appWidgetManager.updateAppWidget(widgetId, views);
        }


        private void showNoRecentScripts(int widgetId, Intent openActivityIntent, RemoteViews views)
        {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    widgetId,
                    openActivityIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            views.setOnClickPendingIntent(R.id.content_container, pendingIntent);
            views.setTextViewText(R.id.script_title, getString(R.string.no_recent_scripts));
        }
    }
}
