package ws.wiklund.vinguiden.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseUpgrader {
	//Available DB versions
	private static final int VERSION_1 = 1;
	private static final int VERSION_2 = 2;

	private WineDatabaseHelper helper;

	public void doUpdate(WineDatabaseHelper helper, int oldVersion, int newVersion) {
		Log.d(DatabaseUpgrader.class.getName(), "Will upgrade from DB version [" + oldVersion + "] to DB version [" + newVersion + "]");

		this.helper = helper;
		
		if(oldVersion != newVersion) {
			int version = upgrade(oldVersion, newVersion);
			Log.d(DatabaseUpgrader.class.getName(), "Upgrade DB from version [" + oldVersion + "] to version [" + version + "]");
		}
	}

	private int upgrade(int oldVersion, int newVersion) {
		int version = -1;
		
		switch (oldVersion) {
			case VERSION_1:
				if(newVersion > VERSION_1) {
					version = moveToVersion2();
					
					if(version < newVersion) {
						return upgrade(version, newVersion);
					} 
					
					return VERSION_2;
				}
				
				break;				
			case VERSION_2:
				//TODO when needed
				break;
	
			default:
				break;
		}

		return version;
	}

	private int moveToVersion2() {
		SQLiteDatabase db = helper.getWritableDatabase();
		
		try {
			//Create CATEGORY table
			db.execSQL(WineDatabaseHelper.DB_CREATE_CATEGORY);

			// TODO			
			//Create category_id column in WINE table
			//Create foreign key in WINE table
		} finally {
			db.close();
		}
		
		return VERSION_2;
	}

}
