package com.spauldhaliwal.homestead;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.spauldhaliwal.homestead.AppProvider.CONTENT_AUTHORITY;
import static com.spauldhaliwal.homestead.AppProvider.CONTENT_AUTHORITY_URI;


/**
 * Created by pauldhaliwal on 2018-03-07.
 */

public class JobsContract {

    static final String ROOT_NODE = "Jobs";
    static final String TYPE = "job";
    public static final String _ID = BaseColumns._ID;
    public static final String NAME = "name";
    public static final String OWNER = "owner";
    public static final String CREATOR = "creator";
    public static final String STATUS = "status";
    public static final String DESCRIPTION = "description";

    public static final String PRIVATE = "Private";
    public static final String PUBLIC = "Public";


    public static final int STATUS_OPEN = 0;
    public static final int STATUS_CLAIMED = 1;
    public static final int STATUS_CLOSED = 2;

    public static final String SCOPE = "scope";
    public static final String SORT_ORDER = "sortOrder";




    /**
     * The uri's to access the tasks table
     */

    public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, ROOT_NODE);

    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "." + ROOT_NODE;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "." + ROOT_NODE;


    static Uri buildJobUri(long jobId) {
        return ContentUris.withAppendedId(CONTENT_URI, jobId);
    }

    static long getJobId(Uri uri) {
        return ContentUris.parseId(uri);
    }


}
