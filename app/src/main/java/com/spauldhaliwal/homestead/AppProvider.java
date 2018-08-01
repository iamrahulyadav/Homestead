package com.spauldhaliwal.homestead;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by pauldhaliwal on 2018-03-07.
 */

public class AppProvider extends ContentProvider {

    private static final String TAG = "AppProvider";

//    private AppDatabase mOpenHelper;

    static final String CONTENT_SCHEME = "content://";
    static final String CONTENT_AUTHORITY = "com.spauldhaliwal.homestead.provider";

    static final Uri CONTENT_AUTHORITY_URI = Uri.parse(CONTENT_SCHEME + CONTENT_AUTHORITY);

    private static final int JOBS = 100;
    private static final int JOBS_ID = 101;

    @Override
    public boolean onCreate() {
        return false;
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

    //    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//
//    static {
//
//        // e.g. content://com.spauldhaliwal.homestead.provider/Jobs
//        sUriMatcher.addURI(CONTENT_AUTHORITY, JobsContract.ROOT_NODE, JOBS);
//
//        // e.g. content://com.spauldhaliwal.homestead.provider/Jobs/4
//        sUriMatcher.addURI(CONTENT_AUTHORITY, JobsContract.ROOT_NODE + "/#", JOBS_ID);
//
//    }
//
//    @Override
//    public boolean onCreate() {
//        mOpenHelper = AppDatabase.getInstance(getContext());
//        return false;
//    }
//
//    @Nullable
//    @Override
//    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
//
//        Log.d(TAG, "query: called with uri: " + uri);
//        final int match = sUriMatcher.match(uri);
//        Log.d(TAG, "query: match is " + match);
//
//        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
//
//        switch (match) {
//            case JOBS:
//                queryBuilder.setTables(JobsContract.ROOT_NODE);
//                break;
//            case JOBS_ID:
//                long jobId = JobsContract.getJobId(uri);
//                queryBuilder.setTables(JobsContract.ROOT_NODE);
//                queryBuilder.appendWhere(JobsContract.Columns._ID + " = " + jobId);
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown uri: " + uri);
//        }
//
//        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
//
//        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
//        cursor.setNotificationUri(getContext().getContentResolver(), uri);
//        return cursor;
//    }
//
//    @Nullable
//    @Override
//    public Uri insertJob(@NonNull Uri uri, @Nullable ContentValues values) {
//        Log.d(TAG, "Entering insertJob method with uri: " + uri);
//        final int match = sUriMatcher.match(uri);
//        Log.d(TAG, "insertJob: match is: " + match);
//
//        final SQLiteDatabase db;
//
//        long recordId;
//        Uri returnUri;
//
//        switch (match) {
//            case JOBS:
//                db = mOpenHelper.getWritableDatabase();
//                recordId = db.insertJob(JobsContract.ROOT_NODE, null, values);
//                if (recordId >=0 ) {
//
//                    returnUri = JobsContract.buildJobUri(recordId);
//                } else {
//                    throw new android.database.SQLException("Failed to insertJob into " + uri.toString());
//                }
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown uri: " + uri);
//        }
//
//        if (recordId >= 0) {
//            // Notify observers that uri was changed
//            Log.d(TAG, "insertJob: Calling notifyChange with " + uri);
//            getContext().getContentResolver().notifyChange(uri, null);
//        } else {
//            Log.d(TAG, "insertJob: nothing was inserted");
//        }
//
//        Log.d(TAG, "exiting insertJob: returning uri " + returnUri);
//        return null;
//    }
//
//    @Override
//    public int updateJob(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
//        Log.d(TAG, "updateJob: entering updateJob method with uri " + uri);
//        final int match = sUriMatcher.match(uri);
//        Log.d(TAG, "updateJob: match is " + match);
//
//        final SQLiteDatabase db;
//
//        long recordId;
//        int rowsUpdated;
//
//        switch (match) {
//            case JOBS_ID:
//                db = mOpenHelper.getWritableDatabase();
//                recordId = JobsContract.getJobId(uri);
//                rowsUpdated = db.updateJob(JobsContract.ROOT_NODE,values, JobsContract.Columns._ID + " = " + recordId, null);
//                break;
//            default:
//                throw new IllegalArgumentException("Incompatible uri " + uri);
//
//        }
//
//        if (rowsUpdated >=0) {
//            //something was updated
//            Log.d(TAG, "updateJob: notifying contentResolver of change with notifychange");
//            getContext().getContentResolver().notifyChange(uri, null);
//        }
//
//        Log.d(TAG, "updateJob: " + rowsUpdated + " rows updated.");
//
//        return rowsUpdated;
//    }
//
//    @Override
//    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
//        Log.d(TAG, "Entering delete method with uri " + uri);
//
//        final int match = sUriMatcher.match(uri);
//        Log.d(TAG, "delete: match is " + match);
//        int rowsDeleted;
//
//        SQLiteDatabase db;
//
//        switch (match) {
//            case JOBS_ID:
//                db = mOpenHelper.getWritableDatabase();
//                long recordId = JobsContract.getJobId(uri);
//                rowsDeleted = db.delete(JobsContract.ROOT_NODE, JobsContract.Columns._ID + " = " + recordId, null);
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown uri or uri has no id: " + uri);
//        }
//
//        if (rowsDeleted >=0 ) {
//            // something was deleted
//            Log.d(TAG, "delete: notifying contentResolver of change with notifychange");
//            getContext().getContentResolver().notifyChange(uri, null);
//        }
//
//
//        if (rowsDeleted >=0) {
//            Log.d(TAG, "delete: " + rowsDeleted + " rows deleted");
//            //something was deleted
//            getContext().getContentResolver().notifyChange(uri, null);
//        }
//        return rowsDeleted;
//    }
//
//    @Nullable
//    @Override
//    public String getType(@NonNull Uri uri) {
//
//        Log.d(TAG, "getType: entering getType");
//        final int match = sUriMatcher.match(uri);
//        Log.d(TAG, "getType: match is " + match);
//
//        switch (match) {
//            case JOBS:
//                return JobsContract.CONTENT_TYPE;
//            case JOBS_ID:
//                return JobsContract.CONTENT_ITEM_TYPE;
//            default:
//                throw new IllegalArgumentException("Incompatible uri " + uri);
//
//        }
//
//    }
}
