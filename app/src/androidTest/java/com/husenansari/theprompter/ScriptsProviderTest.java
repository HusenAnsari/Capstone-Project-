package com.husenansari.theprompter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.husenansari.theprompter.data.PrompterContract.ScriptEntry;
import com.husenansari.theprompter.data.Script;
import com.husenansari.theprompter.data.ScriptsProvider;


public class ScriptsProviderTest extends AndroidTestCase {

    public ScriptsProviderTest() {
        super();
    }

    protected ContentResolver resolver()
    {
        return mContext.getContentResolver();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearDatabase();
    }

    protected void clearDatabase()
    {
        resolver().delete(ScriptsProvider.SCRIPTS_BASE_URI, null, null);
        Cursor cursor = resolver().query(ScriptsProvider.SCRIPTS_BASE_URI, null, null, null, null);

        assertEquals("Database not emptied properly in clearDatabase()", 0, cursor.getCount());
    }

    public void testCreate()
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ScriptEntry.TITLE, "Martin Luther Speech");
        contentValues.put(ScriptEntry.CONTENT, "Lorem ipsum");

        Script script = Script.populate(contentValues);

        Uri uri = resolver().insert(ScriptsProvider.SCRIPTS_BASE_URI, contentValues);

        Cursor cursor = resolver().query(uri, null, null, null, null);
        cursor.moveToFirst();

        assertEquals(cursor.getCount(), 1);

        ContentValues returnedValues = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, returnedValues);

        Script returnedScript = Script.populate(returnedValues);

        assertEquals(returnedScript.getTitle(), script.getTitle());
        assertEquals(returnedScript.getContent(), script.getContent());
        assertTrue(returnedScript.getTimestamp() != null);
        assertTrue(returnedScript.getId() != null);
    }

    public void testUpdate()
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ScriptEntry.TITLE, "Martin Luther Speech");
        contentValues.put(ScriptEntry.CONTENT, "Lorem ipsum");

        Uri uri = resolver().insert(ScriptsProvider.SCRIPTS_BASE_URI, contentValues);

        Cursor cursor = resolver().query(uri, null, null, null, null);
        cursor.moveToFirst();

        assertEquals(cursor.getCount(), 1);
        cursor.close();

        contentValues.put(ScriptEntry.TITLE, "Martin Luther Speech #2");

        Script script = Script.populate(contentValues);

        int updated = resolver().update(uri, contentValues, null, null);

        assertEquals(updated, 1);

        cursor = resolver().query(uri, null, null, null, null);
        cursor.moveToFirst();

        ContentValues returnedValues = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, returnedValues);
        cursor.close();

        Script returnedScript = Script.populate(returnedValues);

        assertEquals(returnedScript.getTitle(), script.getTitle());
        assertEquals(returnedScript.getContent(), script.getContent());
        assertTrue(returnedScript.getTimestamp()!= null);
        assertTrue(returnedScript.getId() != null);
    }

    public void testDelete()
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ScriptEntry.TITLE, "Martin Luther Speech");
        contentValues.put(ScriptEntry.CONTENT, "Lorem ipsum");

        Uri uri = resolver().insert(ScriptsProvider.SCRIPTS_BASE_URI, contentValues);

        Cursor cursor = resolver().query(uri, null, null, null, null);
        cursor.moveToFirst();

        assertEquals(cursor.getCount(), 1);
        cursor.close();

        int deleted = resolver().delete(uri, null, null);

        assertEquals(deleted, 1);

        cursor = resolver().query(uri, null, null, null, null);
        cursor.moveToFirst();

        assertEquals(cursor.getCount(), 0);
    }
}
