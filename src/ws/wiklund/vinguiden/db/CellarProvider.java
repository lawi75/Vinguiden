package ws.wiklund.vinguiden.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class CellarProvider extends ContentProvider {
    private static final String AUTHORITY = "ws.wiklund.vinguiden.db.CellarProvider";
    private static final String CELLAR_BASE_PATH = "cellar";

	public static final int CELLAR = 100;
    public static final int CELLAR_ID = 105;
    public static final int WINE_ID = 110;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CELLAR_BASE_PATH);

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/mt-cellar";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/mt-cellar";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
    	uriMatcher.addURI(AUTHORITY, CELLAR_BASE_PATH, CELLAR);
    	uriMatcher.addURI(AUTHORITY, CELLAR_BASE_PATH + "/#", WINE_ID);
    }

	private WineDatabaseHelper db;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = db.getWritableDatabase();
        int rowsAffected = 0;
        switch (uriType) {
        	case CELLAR:
        		rowsAffected = sqlDB.delete(WineDatabaseHelper.CELLAR_TABLE, selection, selectionArgs);
        		break;
        	case CELLAR_ID:
        		String id = uri.getLastPathSegment();
        		if (TextUtils.isEmpty(selection)) {
        			rowsAffected = sqlDB.delete(WineDatabaseHelper.CELLAR_TABLE, "_id =" + id, null);
        		} else {
        			rowsAffected = sqlDB.delete(WineDatabaseHelper.CELLAR_TABLE, selection + " and _id = " + id, selectionArgs);
        		}
        		break;
        	default:
        		throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
	}

	@Override
	public String getType(Uri uri) {
	    int uriType = uriMatcher.match(uri);
        switch (uriType) {
	        case CELLAR:
	            return CONTENT_TYPE;
	        case CELLAR_ID:
	            return CONTENT_ITEM_TYPE;
	        case WINE_ID:
	            return CONTENT_ITEM_TYPE;
	        default:
	            return null;
        }
    }

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = uriMatcher.match(uri);
		if (uriType != CELLAR) {
			throw new IllegalArgumentException("Invalid URI for insert");
		}
		
		SQLiteDatabase sqlDB = db.getWritableDatabase();
		long newID = sqlDB.insert(WineDatabaseHelper.CELLAR_TABLE, null, values);
		if (newID > 0) {
			Uri newUri = ContentUris.withAppendedId(uri, newID);
			getContext().getContentResolver().notifyChange(uri, null);
			return newUri;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		db = new WineDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	queryBuilder.setTables(WineDatabaseHelper.CELLAR_TABLE);

    	int uriType = uriMatcher.match(uri);
	    switch (uriType) {
	    	case WINE_ID:
	    		queryBuilder.appendWhere("wine_id =" + uri.getLastPathSegment());
	        	break;
	    	case CELLAR:
	    		// no filter
	    		break;
	    	default:
	    		throw new IllegalArgumentException("Unknown URI");
	    }

    	Cursor cursor = queryBuilder.query(db.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = db.getWritableDatabase();

        int rowsAffected;

        switch (uriType) {
        	case CELLAR_ID:
        		String id = uri.getLastPathSegment();
        		StringBuilder modSelection = new StringBuilder("_id = " + id);

        		if (!TextUtils.isEmpty(selection)) {
        			modSelection.append(" AND " + selection);
        		}

        		rowsAffected = sqlDB.update(WineDatabaseHelper.CELLAR_TABLE, values, modSelection.toString(), null);
        		break;
        	case CELLAR:
        		rowsAffected = sqlDB.update(WineDatabaseHelper.CELLAR_TABLE, values, selection, selectionArgs);
        		break;
        	default:
        		throw new IllegalArgumentException("Unknown URI");
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

}
