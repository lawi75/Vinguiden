package ws.wiklund.vinguiden.db;

import ws.wiklund.guides.db.BeverageDatabaseHelper;
import ws.wiklund.guides.db.DatabaseUpgrader;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WineDatabaseUpgrader extends DatabaseUpgrader {
	public WineDatabaseUpgrader(SQLiteDatabase db) {
		super(db);
	}

	//Available DB versions
	static final int VERSION_1 = 1;
	static final int VERSION_2 = 2;
	static final int VERSION_3 = 3;
	static final int VERSION_4 = 4;
	static final int VERSION_5 = 5;

	public int upgrade(int oldVersion, int newVersion) {
		int version = -1;

		switch (oldVersion) {
			case VERSION_1:
				if(newVersion > VERSION_1) {
					version = moveToVersion2();
					Log.d(WineDatabaseUpgrader.class.getName(), "Upgraded DB from version [" + oldVersion + "] to version [" + version + "]");
					
					if(version < newVersion) {
						return upgrade(version, newVersion);
					} 
					
					return VERSION_2;
				}
				
				break;				
			case VERSION_2:
				if(newVersion > VERSION_2) {
					version = moveToVersion3();
					Log.d(WineDatabaseUpgrader.class.getName(), "Upgraded DB from version [" + oldVersion + "] to version [" + version + "]");

					if(version < newVersion) {
						return upgrade(version, newVersion);
					} 
					
					return VERSION_3;
				}

				break;
			case VERSION_3:
				if(newVersion > VERSION_3) {
					version = moveToVersion4();
					Log.d(WineDatabaseUpgrader.class.getName(), "Upgraded DB from version [" + oldVersion + "] to version [" + version + "]");

					if(version < newVersion) {
						return upgrade(version, newVersion);
					} 
					
					return VERSION_4;					
				}
				break;
			case VERSION_4:
				if(newVersion > VERSION_4) {
					version = moveToVersion5();
					Log.d(WineDatabaseUpgrader.class.getName(), "Upgraded DB from version [" + oldVersion + "] to version [" + version + "]");

					if(version < newVersion) {
						return upgrade(version, newVersion);
					} 
					
					return VERSION_5;					
				}
				break;
			default:
				break;
		}

		return version;
	}

	private int moveToVersion2() throws SQLException {
		//Create CATEGORY table
		db.execSQL(BeverageDatabaseHelper.DB_CREATE_CATEGORY);

		//Create category_id column in BEVERAGE table
		db.execSQL("ALTER TABLE " + WineDatabaseHelper.BEVERAGE_TABLE + " ADD COLUMN category_id integer");
		
		//Create foreign key in BEVERAGE table. SQLite doesn't support this out of the box therefore a more complex way of solving this
		//1. Create tmp back up of beverage table
		db.execSQL("DROP TABLE IF EXISTS " + WineDatabaseHelper.BEVERAGE_TABLE + "_TMP");
		db.execSQL("ALTER TABLE " + WineDatabaseHelper.BEVERAGE_TABLE + " RENAME TO " + WineDatabaseHelper.BEVERAGE_TABLE + "_TMP");
		
		//2. Create new empty beverage table with category support
		db.execSQL(WineDatabaseHelper.DB_CREATE_BEVERAGE);
		
		//3. Copy data from tmp table
		db.execSQL("INSERT INTO " + WineDatabaseHelper.BEVERAGE_TABLE + " ("
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
			+ " FROM " + WineDatabaseHelper.BEVERAGE_TABLE +"_TMP");
		
		//4. Drop back up of beverage table
		db.execSQL("DROP TABLE " + WineDatabaseHelper.BEVERAGE_TABLE + "_TMP");
		
		return VERSION_2;
	}
	
	private int moveToVersion3() throws SQLException {
		Log.d(WineDatabaseUpgrader.class.getName(), "Moving to version 3 of DB");

		//Add price column to wine
		db.execSQL("ALTER TABLE " + WineDatabaseHelper.BEVERAGE_TABLE + " ADD COLUMN price float");
		Log.d(WineDatabaseUpgrader.class.getName(), "Added price column");
		
		//Create CELLAR table
		Log.d(WineDatabaseUpgrader.class.getName(), "Creating cellar table");
		db.execSQL(WineDatabaseHelper.DB_CREATE_CELLAR);
		Log.d(WineDatabaseUpgrader.class.getName(), "Cellar table created");

		return VERSION_3;
	}
	
	private int moveToVersion4() throws SQLException {
		Log.d(WineDatabaseUpgrader.class.getName(), "Moving to version 4 of DB");

		//1. Create new empty beverage table
		db.execSQL(WineDatabaseHelper.DB_CREATE_BEVERAGE);
		
		//2. Copy data from wine table to beverage
		db.execSQL("INSERT INTO " + WineDatabaseHelper.BEVERAGE_TABLE + " ("
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
			+ " FROM wine");
		
		//Rename wine_id to beverage_id in CELLAR table. SQLite doesn't support this out of the box therefore a more complex way of solving this
		//3. Create tmp back up of beverage table
		db.execSQL("DROP TABLE IF EXISTS " + WineDatabaseHelper.CELLAR_TABLE + "_TMP");
		db.execSQL("ALTER TABLE " + WineDatabaseHelper.CELLAR_TABLE + " RENAME TO " + WineDatabaseHelper.CELLAR_TABLE + "_TMP");
		
		//4. Create new empty cellar table with beverage_id support
		db.execSQL(WineDatabaseHelper.DB_CREATE_CELLAR);
		
		//5. Copy data from wine table to beverage
		db.execSQL("INSERT INTO " + WineDatabaseHelper.CELLAR_TABLE + " ("
				+ "_id, "
				+ "beverage_id, "
				+ "no_bottles, "
				+ "storage_location, "
				+ "comment, "
				+ "added_to_cellar, "
				+ "consumption_date, "
				+ "notification_id"
				+") "
			+ "SELECT " 			
			+ "_id, "
			+ "wine_id, "
			+ "no_bottles, "
			+ "storage_location, "
			+ "comment, "
			+ "added_to_cellar, "
			+ "consumption_date, "
			+ "notification_id "
			+ " FROM cellar_tmp");		

		//6. Drop old wine table
		db.execSQL("DROP TABLE IF EXISTS wine");

		//7. Drop tmp cellar table
		db.execSQL("DROP TABLE IF EXISTS cellar_tmp");

		return VERSION_4;		
	}
	
	private int moveToVersion5() throws SQLException {
		//TODO DEBUG CODE
		db.execSQL("DROP TABLE IF EXISTS " + WineDatabaseHelper.BEVERAGE_TYPE_TABLE);

		//1. Create and populate beverage type table
		createAndPopulateBeverageTypeTable(db);
		
		//TODO other??
		/*
			types.add(BeverageType.OTHER);

		 */
		
		//2. Update beverage type ids in beverage table
		updateBeverageTypeIdInBeverageTable(100, 1);
		updateBeverageTypeIdInBeverageTable(200, 2);
		updateBeverageTypeIdInBeverageTable(300, 3);
		updateBeverageTypeIdInBeverageTable(400, 4);
		updateBeverageTypeIdInBeverageTable(500, 5);
		updateBeverageTypeIdInBeverageTable(600, 6);
		updateBeverageTypeIdInBeverageTable(700, 7);
		updateBeverageTypeIdInBeverageTable(800, 8);
		updateBeverageTypeIdInBeverageTable(900, 9);
		updateBeverageTypeIdInBeverageTable(950, 10);
		
		//3. back up beverage table
		db.execSQL("DROP TABLE IF EXISTS " + WineDatabaseHelper.BEVERAGE_TABLE + "_TMP");
		db.execSQL("ALTER TABLE " + WineDatabaseHelper.BEVERAGE_TABLE + " RENAME TO " + WineDatabaseHelper.BEVERAGE_TABLE + "_TMP");
		
		//4. Create new beverage table
		db.execSQL(WineDatabaseHelper.DB_CREATE_BEVERAGE);
		
		//5. Populate new beverage table
		db.execSQL("INSERT INTO " + WineDatabaseHelper.BEVERAGE_TABLE + " ("
				+ "_id, "
				+ "name, "
				+ "no, "
				+ "thumb, "
				+ "country_id, "
				+ "year, "
				+ "beverage_type_id, "
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
			+ " FROM " + WineDatabaseHelper.BEVERAGE_TABLE + "_TMP");
		
		//6. clean up
		db.execSQL("DROP TABLE IF EXISTS " + WineDatabaseHelper.BEVERAGE_TABLE + "_TMP");
		
		return VERSION_5;		
	}

	@Override
	public void createAndPopulateBeverageTypeTable(SQLiteDatabase db) {
		//1. create beverage type table
		db.execSQL(WineDatabaseHelper.DB_CREATE_BEVERAGE_TYPE);
		
		//2. populate beverage type table
		insertBeverageType(1, "Rött vin");
		insertBeverageType(2, "Vitt vin");
		insertBeverageType(3, "Rosévin");
		insertBeverageType(4, "Mousserande vin, Vitt torrt");
		insertBeverageType(5, "Mousserande vin, halvtorrt");
		insertBeverageType(6, "Mousserande vin, Vitt sött");
		insertBeverageType(7, "Mousserande vin, Rosé");
		insertBeverageType(8, "Fruktvin");
		insertBeverageType(9, "Fruktvin, Sött");
		insertBeverageType(10, "Fruktvin, Torrt");
	}

}
