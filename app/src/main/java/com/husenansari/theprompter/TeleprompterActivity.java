package com.husenansari.theprompter;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.husenansari.theprompter.data.Script;
import com.husenansari.theprompter.data.ScriptsProvider;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class TeleprompterActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    Drawable background;

    public static final String BLACK = "black";
    public static final String CYAN = "cyan";
    public static final String RED = "red";
    public static final String TEAL = "teal";
    public static final String INDIGO = "indigo";
    public static final String PURPLE = "purple";

    public static final String COLOR_SCHEME = "color_scheme";
    public static final String PLAY_SPEED = "play_speed";
    public static final String TEXT_SIZE = "text_size";

    private SharedPreferences sharedPreferences;

    private int backgroundColor;
    private int textSize, playSpeed;
    private boolean isPlaying = false;

    private BackgroundChooserDialog dialog;

    private String scriptId;

    private TextView content, speedDisplay, sizeDisplay;
    private View play, pause;

    private TimerTask timerTask;
    private Timer timer = new Timer();

    private Tracker tracker;

    private DecimalFormat speedFormat = new DecimalFormat("#.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleprompter);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        backgroundColor = getSchemeColor(sharedPreferences.getString(COLOR_SCHEME, BLACK));
        textSize = sharedPreferences.getInt(TEXT_SIZE, 46);
        playSpeed = sharedPreferences.getInt(PLAY_SPEED, 1);

        scriptId = getIntent().getExtras().getString("id");

        content = (TextView) findViewById(R.id.content);
        content.setMovementMethod((new ScrollingMovementMethod()));
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        speedDisplay = (TextView) findViewById(R.id.speed_display);
        sizeDisplay = (TextView) findViewById(R.id.size_display);

        setupDisplay();

        getSupportLoaderManager().initLoader(0, null, this);

        sharedPreferences.edit().putString("recent_script_id", scriptId).apply();
        Intent intent = new Intent(this, RecentScriptWidgetProvider.class);
        intent.setAction(getString(R.string.recent_script_updated));
        sendBroadcast(intent);

        tracker = ((Application) getApplication()).startTracking();
        sendSpeedEvent(playSpeed, "used");
        sendSizeEvent(textSize, "used");
        sendColorSchemeEvent(sharedPreferences.getString(COLOR_SCHEME, BLACK), "used");
    }

    @Override
    protected void onDestroy() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(0);
        super.onDestroy();
    }

    public void showColorSchemeChooser(View v)
    {
        dialog = new BackgroundChooserDialog();
        dialog.show(getSupportFragmentManager(), "color_scheme_chooser");
    }

    public void scrollText()
    {
        content.scrollBy(0, (2 * playSpeed));
    }

    public void setupDisplay()
    {
        background = new ColorDrawable(getResources().getColor(backgroundColor));
        getWindow().setBackgroundDrawable(background);

        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        setupSpeedAndSizeDisplays();
    }

    protected void setupSpeedAndSizeDisplays()
    {
        sizeDisplay.setText(String.format("%dsp", textSize));
        speedDisplay.setText(speedFormat.format(playSpeed) +"x");
    }

    public int getSchemeColor(String schemeName)
    {
        switch (schemeName) {
            case BLACK:
                return R.color.schemeBlackBg;
            case RED:
                return R.color.schemeRedBg;
            case INDIGO:
                return R.color.schemeIndigoBg;
            case PURPLE:
                return R.color.schemePurpleBg;
            case TEAL:
                return R.color.schemeTealBg;
            case CYAN:
                return R.color.schemeCyanBg;
            default: throw new IllegalArgumentException("Invalid scheme name");
        }
    }

    public void propertyClicked(View v)
    {
        String property = v.getTag().toString();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        switch (property) {
            case "text_size_increase":
                textSize += 4;
                editor.putInt(TEXT_SIZE, textSize);
                sendSizeEvent(textSize, "changed");
                break;
            case "text_size_decrease":
                textSize -= 4;
                editor.putInt(TEXT_SIZE, textSize);
                sendSizeEvent(textSize, "changed");
                break;
            case "speed_increase":
                playSpeed += 1;
                editor.putInt(PLAY_SPEED, playSpeed);
                sendSpeedEvent(playSpeed, "changed");
                break;
            case "speed_decrease":
                playSpeed -= 1;
                editor.putInt(PLAY_SPEED, playSpeed);
                sendSpeedEvent(playSpeed, "changed");
                break;
            case "play":
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                isPlaying = true;
                play();
                break;
            case "pause":
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                isPlaying = false;
                pause();
                break;
        }
        editor.commit();
        setupDisplay();
    }

    public void sendSpeedEvent(int speed, String action)
    {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("playSpeed")
                .setAction(action)
                .setLabel(String.valueOf(speed))
                .build());
    }

    public void sendSizeEvent(int size, String action)
    {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("textSize")
                .setAction(action)
                .setLabel(String.valueOf(size))
                .build());
    }

    public void sendColorSchemeEvent(String scheme, String action)
    {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("colorScheme")
                .setAction(action)
                .setLabel(scheme)
                .build());
    }

    public void play()
    {
        Log.i("PROMPTER", "played");
        timerTask = new TimerTask() {
            @Override
            public void run() {
                scrollText();
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 24);
    }

    public void pause()
    {
        Log.i("PROMPTER", "paused");
        timerTask.cancel();
    }

    public void setScheme(View v)
    {
        String scheme = v.getTag().toString();
        backgroundColor = getSchemeColor(scheme);

        PreferenceManager.getDefaultSharedPreferences(this).edit()
        .putString("color_scheme", scheme)
        .apply();

        sendColorSchemeEvent(scheme, "changed");
        setupDisplay();
        dialog.dismiss();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ScriptsProvider.SCRIPT_BASE_URI.buildUpon().appendPath(scriptId).build(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        Script script = Script.populate(data);
        content.setText(script.getContent());

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, getIntent(), PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.play_circle_outline)
                .setContentTitle(script.getTitle())
                .setContentText(getString(R.string.return_to_prompter))
                .setContentIntent(pendingIntent)
                .build();

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public static class BackgroundChooserDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            View view = inflater.inflate(R.layout.color_scheme_chooser_dialog, null);

            builder.setTitle(getString(R.string.choose_color));
            builder.setView(view);

            return builder.create();
        }
    }
}
