package com.babamodz.darktricks;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

/**
 * Modern cross-process Preferences Provider.
 * Allows system_server and other hooked processes to safely read preferences
 * without relying on deprecated XSharedPreferences or world-readable files.
 */
public class PreferencesProvider extends ContentProvider {
    public static final String AUTHORITY = "com.babamodz.darktricks.provider.prefs";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String METHOD_GET_ALL = "getAll";
    public static final String METHOD_GET = "get";

    private SharedPreferences mPrefs;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            mPrefs = context.getSharedPreferences(SettingsActivity.PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
        return true;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (mPrefs == null && getContext() != null) {
            mPrefs = getContext().getSharedPreferences(SettingsActivity.PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
        if (mPrefs == null) return null;

        Bundle result = new Bundle();
        if (METHOD_GET_ALL.equals(method)) {
            Map<String, ?> all = mPrefs.getAll();
            for (Map.Entry<String, ?> entry : all.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                if (val instanceof Boolean) {
                    result.putBoolean(key, (Boolean) val);
                } else if (val instanceof Integer) {
                    result.putInt(key, (Integer) val);
                } else if (val instanceof Long) {
                    result.putLong(key, (Long) val);
                } else if (val instanceof String) {
                    result.putString(key, (String) val);
                }
            }
            return result;
        } else if (METHOD_GET.equals(method) && arg != null) {
            Object val = mPrefs.getAll().get(arg);
            if (val instanceof Boolean) {
                result.putBoolean(arg, (Boolean) val);
            } else if (val instanceof String) {
                result.putString(arg, (String) val);
            }
            return result;
        }
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
