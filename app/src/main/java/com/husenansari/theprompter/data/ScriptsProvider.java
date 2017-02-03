package com.husenansari.theprompter.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.husenansari.theprompter.data.PrompterContract.ScriptEntry;


public class ScriptsProvider extends ContentProvider {

    private static final String AUTHORITY = PrompterContract.AUTHORITY;
    
    private DatabaseHelper databaseHelper;
    private static final String TABLE = ScriptEntry.TABLE;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int SCRIPTS = 100;
    public static final int SCRIPT = 101;

    public static final Uri SCRIPT_BASE_URI = Uri.parse("content://"+AUTHORITY+"/script");
    public static final Uri SCRIPTS_BASE_URI = Uri.parse("content://"+AUTHORITY+"/scripts");

    static {
        uriMatcher.addURI(AUTHORITY, "scripts", SCRIPTS);
        uriMatcher.addURI(AUTHORITY, "script/#", SCRIPT);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE);
        Cursor cursor = null;

        if (sort == null) sort = ScriptEntry.TIMESTAMP+" DESC";

        switch (uriMatcher.match(uri)) {
            case SCRIPTS:
                cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sort);
                break;
            case SCRIPT:
                queryBuilder.appendWhere(ScriptEntry._ID +" = "+ uri.getLastPathSegment());
                cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sort);
                break;
            default: throw new UnsupportedOperationException("This operation is not allowed on '"+uri+"'");
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SCRIPTS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE+"/prompter-scripts";
            case SCRIPT:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE+"/prompter-script";
            default: throw new UnsupportedOperationException("This URI is not supported");
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        Uri inserted = null;
        switch (uriMatcher.match(uri)) {
            case SCRIPTS:
                contentValues.put(ScriptEntry.TIMESTAMP, System.currentTimeMillis()/1000);
                long id = database.insertOrThrow(TABLE, null, contentValues);
                inserted = Uri.withAppendedPath(SCRIPT_BASE_URI, id+"");
                break;
            default: throw new UnsupportedOperationException("This operations is not allowed on '"+uri+"'");
        }
        return inserted;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int deletedRows = 0;
        switch (uriMatcher.match(uri)) {
            case SCRIPT:
                String id = uri.getLastPathSegment();
                deletedRows = database.delete(TABLE, "_id = ?", new String[]{id});
                break;
            case SCRIPTS:
                deletedRows = database.delete(TABLE, "1", new String[]{});
                break;
            default: throw new UnsupportedOperationException("This operations is not allowed on '"+uri+"'");
        }
        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int updatedRows = 0;
        switch (uriMatcher.match(uri)) {
            case SCRIPT:
                String id = uri.getLastPathSegment();

                if (selection == null)
                    selection = "";
                else
                    selection += " AND ";

                selection += ScriptEntry._ID+" = "+id;
                updatedRows = database.update(TABLE, contentValues, selection, selectionArgs);
                break;
            default: throw new UnsupportedOperationException("This operations is not allowed on '"+uri+"'");
        }
        return updatedRows;
    }
}
