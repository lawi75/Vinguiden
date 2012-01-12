package ws.wiklund.vinguiden.db;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseUpgrader {
	//Available DB versions
	private static final int VERSION_1 = 1;
	private static final int VERSION_2 = 2;

	private SQLiteDatabase db;

	public void doUpdate(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(DatabaseUpgrader.class.getName(), "Will upgrade from DB version [" + oldVersion + "] to DB version [" + newVersion + "]");

		this.db = db;
		
		if(oldVersion < newVersion) {
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

	private int moveToVersion2() throws SQLException {
		//Create CATEGORY table
		db.execSQL(WineDatabaseHelper.DB_CREATE_CATEGORY);

		//Create category_id column in WINE table
		db.execSQL("ALTER TABLE " + WineDatabaseHelper.WINE_TABLE + " ADD COLUMN category_id integer");
		
		//Create foreign key in WINE table. SQLite doesn't support this out of the box therefore a more complex way of solving this
		//1. Create tmp back up of wine table
		db.execSQL("DROP TABLE IF EXISTS " + WineDatabaseHelper.WINE_TABLE + "_TMP");
		db.execSQL("ALTER TABLE " + WineDatabaseHelper.WINE_TABLE + " RENAME TO " + WineDatabaseHelper.WINE_TABLE + "_TMP");
		
		//2. Create new empty wine table with category support
		db.execSQL(WineDatabaseHelper.DB_CREATE_WINE);
		
		//3. Copy data from tmp table
		db.execSQL("INSERT INTO " + WineDatabaseHelper.WINE_TABLE + " ("
				+ "_id, "
				+ "name, "
				+ "no, "
				+ "thumb, "
				+ "country_id, "
				+ "year, "
				+ "type, "
				+ "producer_id, "
				+ "strength, "
				+ "usage, "
				+ "taste, "
				+ "provider_id, "
				+ "rating, "
				+ "comment, "			
				+ "category_id, "
				+ "added"
				+") "
			+ "SELECT " 			
				+ "_id, "
				+ "name, "
				+ "no, "
				+ "thumb, "
				+ "country_id, "
				+ "year, "
				+ "type, "
				+ "producer_id, "
				+ "strength, "
				+ "usage, "
				+ "taste, "
				+ "provider_id, "
				+ "rating, "
				+ "comment, "			
				+ "category_id, "
				+ "added "
			+ " FROM " + WineDatabaseHelper.WINE_TABLE +"_TMP");
		
		//4. Drop back up of wine table
		db.execSQL("DROP TABLE " + WineDatabaseHelper.WINE_TABLE + "_TMP");
		
		return VERSION_2;
	}

}
