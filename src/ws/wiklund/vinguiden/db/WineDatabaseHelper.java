package ws.wiklund.vinguiden.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ws.wiklund.vinguiden.model.BaseModel;
import ws.wiklund.vinguiden.model.Category;
import ws.wiklund.vinguiden.model.Column;
import ws.wiklund.vinguiden.model.Country;
import ws.wiklund.vinguiden.model.Producer;
import ws.wiklund.vinguiden.model.Provider;
import ws.wiklund.vinguiden.model.TableName;
import ws.wiklund.vinguiden.model.Wine;
import ws.wiklund.vinguiden.model.WineType;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WineDatabaseHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "wineguide.db";
	private static final int DATABASE_VERSION = 3;

	// Database tables
	private static final String COUNTRY_TABLE = "country";
	private static final String PRODUCER_TABLE = "producer"; 
	private static final String PROVIDER_TABLE = "provider";
	private static final String CATEGORY_TABLE = "category";
	
	public static final String WINE_TABLE = "wine";
	static final String CELLAR_TABLE = "cellar";	

	// Database creation sql statements
	static final String DB_CREATE_WINE = "create table " + WINE_TABLE + " (_id integer primary key autoincrement, "
			+ "name text not null, "
			+ "no integer, "
			+ "thumb text, "
			+ "country_id integer, "
			+ "year integer, "
			+ "type integer, "
			+ "producer_id integer, "
			+ "strength float, "
			+ "price float, "
			+ "usage text, "
			+ "taste text, "
			+ "provider_id integer, "
			+ "rating float, "
			+ "comment text, "
			+ "category_id integer, "
			+ "added timestamp not null default current_timestamp, "
			+ "foreign key (country_id) references country (_id), "
			+ "foreign key (producer_id) references producer (_id), "
			+ "foreign key (producer_id) references producer (_id), "
			+ "foreign key (category_id) references category (_id));";

	private static final String DB_CREATE_COUNTRY = "create table " + COUNTRY_TABLE + " (_id integer primary key autoincrement, "
			+ "name text not null, " 
			+ "thumb_url text);";

	private static final String DB_CREATE_PRODUCER = "create table " + PRODUCER_TABLE + " (_id integer primary key autoincrement, "
			+ "name text not null);";

	private static final String DB_CREATE_PROVIDER = "create table " + PROVIDER_TABLE + " (_id integer primary key autoincrement, "
			+ "name text not null);";

	static final String DB_CREATE_CATEGORY = "create table if not exists " + CATEGORY_TABLE + " (_id integer primary key autoincrement, "
			+ "name text not null);";

	static final String DB_CREATE_CELLAR = "create table if not exists " + CELLAR_TABLE + " (_id integer primary key autoincrement, "
			+ "wine_id integer, "
			+ "no_bottles integer, "
			+ "storage_location text, "
			+ "comment text, "
			+ "added_to_cellar integer, "
			+ "consumption_date integer, "
			+ "notification_id integer, "
			+ "foreign key (wine_id) references wine (_id));";

	private static final String WINE_COLUMNS = 
			"wine._id, "
			+ "wine.name, "
			+ "wine.no, "
			+ "wine.type, "
			+ "wine.thumb, "
			+ "wine.country_id, "
			+ "country.name, "
			+ "country.thumb_url, "
			+ "wine.year, "
			+ "wine.producer_id, "
			+ "producer.name, "
			+ "wine.strength, "
			+ "wine.price, "
			+ "wine.usage, "
			+ "wine.taste, "
			+ "wine.provider_id, "
			+ "provider.name, "
			+ "wine.rating, "
			+ "wine.comment, "			
			+ "wine.category_id, "
			+ "category.name, "
			+ "(strftime('%s', added) * 1000) AS added ";

	private static final String WINE_JOIN_COLUMNS = "LEFT JOIN country ON "
			+ "wine.country_id = country._id "
			+ "LEFT JOIN producer ON "
			+ "wine.producer_id = producer._id "
			+ "LEFT JOIN category ON "
			+ "wine.category_id = category._id "
			+ "LEFT JOIN provider ON "
			+ "wine.provider_id = provider._id ";

	public static final String SQL_SELECT_ALL_WINES = "SELECT "
			+ WINE_COLUMNS
			+ "FROM "
			+ WINE_TABLE + " "
			+ WINE_JOIN_COLUMNS;
	
	private static final String SQL_SELECT_WINE = 
			SQL_SELECT_ALL_WINES
			+ "WHERE "
			+ "wine._id = ?";
	
	/*
	private static final String CELLAR_COLUMNS = 
			"cellar._id, "
			+ "cellar.wine_id, "
			+ "cellar.no_bottles, "
			+ "cellar.storage_location, "
			+ "cellar.comment, "
			+ "cellar.added_to_cellar, "
			+ "cellar.consumption_date, "
			+ "cellar.notification_id "
		+ "FROM "
			+ CELLAR_TABLE + " ";
	*/
	
	public static final String SQL_SELECT_ALL_WINES_INCLUDING_NO_IN_CELLAR = "SELECT "
			+ WINE_COLUMNS + ", "
			+ "ifnull(("
				+ "SELECT "
					+ "sum(no_bottles) AS tot "
				+ "FROM "
					+ CELLAR_TABLE + " "
				+ "WHERE "
					+ "wine._id = cellar.wine_id"
				+ "), 0) AS total " 
			+ "FROM "
			+ WINE_TABLE + " "
			+ WINE_JOIN_COLUMNS;
	
	public WineDatabaseHelper(Context context) {
		this(context, DATABASE_NAME);
	}

	//Used for testing so that db can be created and dropped with out destroying dev data
	public WineDatabaseHelper(Context context, String dbName) {
		super(context, dbName, null, DATABASE_VERSION);
		
		
		/*if((context.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
			debugSetDBVersion(2);
		}*/
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE_PRODUCER);
		db.execSQL(DB_CREATE_PROVIDER);
		db.execSQL(DB_CREATE_COUNTRY);
		db.execSQL(DB_CREATE_CATEGORY);
		db.execSQL(DB_CREATE_WINE);
		db.execSQL(DB_CREATE_CELLAR);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion) {
			Log.d(WineDatabaseHelper.class.getName(),
					"Upgrading database from version " + oldVersion + " to "
							+ newVersion);
			new DatabaseUpgrader().doUpdate(db, oldVersion, newVersion);
		}
	}

	public Wine getWine(int id) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = null;
		
		try {
			c = db.rawQuery(SQL_SELECT_WINE, new String[] {String.valueOf(id)});

			if (c.moveToFirst()) {
				return getWineFromCursor(c);
			}
		} finally {
			c.close();
			db.close();
			close();
		}
		
		return null;
	}
	
	public Country getCountry(int id) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = null;
		
		try {
			c = db.query("country", null, "_id=", new String[]{String.valueOf(id)}, null, null, null);

			if (c.moveToFirst()) {
				return getCountryFromCursor(c);
			}
		} finally {
			c.close();
			db.close();
			close();
		}
		
		return null;
	}

	public List<Wine> getAllWines() {
		List<Wine> l = new ArrayList<Wine>();
		SQLiteDatabase db = getReadableDatabase();
		
		try {
			Cursor c = db.rawQuery(SQL_SELECT_ALL_WINES_INCLUDING_NO_IN_CELLAR, null);
			
			for (boolean b = c.moveToFirst(); b; b = c.moveToNext()) {
				l.add(getWineFromCursor(c));
			}

			return l;
		} finally {
			db.close();
		}
	}

	public List<Category> getCategories() {
		// TODO Auto-generated method stub
		return null;
	}
	

	public boolean deleteWine(int id) {
		SQLiteDatabase db = getWritableDatabase();

		try {
			db.beginTransaction();
			
			boolean b = true;
			if(inCellar(id)) {
				b = db.delete(CELLAR_TABLE, "wine_id=?", new String[]{String.valueOf(id)}) > 0;
			}		
			
			if (b) {
				b = db.delete(WINE_TABLE, "_id=?", new String[] { String.valueOf(id) }) == 1;
				if (b) {
					db.setTransactionSuccessful();
				}
			}
			
			return b; 
		} finally {
			db.endTransaction();
			db.close();
			close();
		}
	}

	public boolean updateWine(Wine wine) {
		if (exists(wine)) {			
			return update(wine);
		}
		
		Log.d(WineDatabaseHelper.class.getName(),"Trying to update wine that doesn't exist in db" + (wine != null ? wine.toString() : null));
		return false;
	}

	public Wine addWine(Wine wine) {
		if (!exists(wine)) {
			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();

			try {
				ContentValues values = convertWineToValues(wine);

				//Add country if country doesn't exists in DB
				Integer countryId = values.getAsInteger("country_id");
				if (countryId == null) {
					values.put("country_id", addCountry(db, wine.getCountry()));
				}
				
				//Add producer if producer doesn't exists in DB
				Integer producerId = values.getAsInteger("producer_id");
				if (producerId == null) {
					values.put("producer_id", addProducer(db, wine.getProducer()));
				}
				
				//Add provider if provider doesn't exists in DB
				Integer providerId = values.getAsInteger("provider_id");
				if (providerId == null) {
					values.put("provider_id", addProvider(db, wine.getProvider()));
				}
				
				//Add category if category doesn't exists in DB
				Integer categoryId = values.getAsInteger("category_id");
				if (categoryId == null) {
					values.put("category_id", addCategory(db, wine.getCategory()));
				}

				long id = db.insert(WINE_TABLE, null, values);
				
				//Update wine with the newly created id
				wine.setId((int) id);
				
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				db.close();
				close();
			}
		} else {
			update(wine);
		}

		return wine;
	}
	
	public Wine getWineFromCursor(Cursor c) {
		int i = 0;
		return new Wine(c.getInt(i++),
				c.getString(i++),
				c.getInt(i++),
				WineType.fromId(c.getInt(i++)),
				c.getString(i++),
				new Country(c.getInt(i++), c.getString(i++), c.getString(i++)),
				c.getInt(i++),
				new Producer(c.getInt(i++), c.getString(i++)),
				c.getFloat(i++),
				c.getFloat(i++),
				c.getString(i++),
				c.getString(i++),
				new Provider(c.getInt(i++), c.getString(i++)),
				c.getFloat(i++),
				c.getString(i++),
				new Category(c.getInt(i++), c.getString(i++)),
				new Date(c.getLong(i++)),
				c.getInt(i++));
	}
	
	public int getWineIdFromNo(int no) {
		SQLiteDatabase db = getReadableDatabase();			
		Cursor c = db.query(WINE_TABLE, new String[]{"_id"}, "no = ?", new String[] {String.valueOf(no)}, null, null, null);

		try {					
			if (c.moveToFirst()) {
				return c.getInt(0);
			}
		} finally {
			c.close();
			db.close();
			close();
		}
		
		return -1;
	}

	public int getVersion() {
		SQLiteDatabase db = getReadableDatabase();
		try {
			return db.getVersion();
		} finally {
			db.close();
			close();
		}
	}

	public int getNoBottlesInCellar() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = null;
		
		try {
			c = db.rawQuery("SELECT sum(no_bottles) as sum FROM cellar", null);

			if (c.moveToFirst()) {
				return c.getInt(0);
			}

			return -1;
		} finally {
			c.close();
			db.close();
			close();
		}
	}
	
	public int getNoBottlesForWine(int id) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = null;
		
		try {
			c = db.rawQuery("SELECT sum(no_bottles) as sum FROM cellar WHERE wine_id = ?", new String[]{String.valueOf(id)});

			if (c.moveToFirst()) {
				return c.getInt(0);
			}

			return -1;
		} finally {
			c.close();
			db.close();
			close();
		}
	}

	/**
	 * Internal method with out check for duplicates
	 * @param wine
	 * @return
	 */
	private boolean update(Wine wine) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();

		try {
			Country country = wine.getCountry();
			if (country != null && country.isNew()) {
				int id = addCountry(db, country);
				wine.setCountry(new Country(id, country.getName(), country.getThumbUrl()));
			}
			
			Producer producer = wine.getProducer();
			if (producer != null && producer.isNew()) {
				int id = addProducer(db, producer);
				wine.setProducer(new Producer(id, producer.getName()));
			}
			
			Provider provider = wine.getProvider();
			if (provider != null && provider.isNew()) {
				int id = addProvider(db, provider);
				wine.setProvider(new Provider(id, provider.getName()));
			}
			
			ContentValues values = convertWineToValues(wine);
						
			db.update(WINE_TABLE, values, "_id=?", new String[]{String.valueOf(wine.getId())});

			db.setTransactionSuccessful();
			return true;		
		} finally {
			db.endTransaction();
			db.close();
			close();
		}
	}

	private Country getCountryFromCursor(Cursor c) {
		return new Country(c.getInt(0), c.getString(1), c.getString(2));
	}

	private int addCountry(SQLiteDatabase db, Country country) {
		return addModel(db, country);
	}

	private int addProducer(SQLiteDatabase db, Producer producer) {
		return addModel(db, producer);
	}

	private int addProvider(SQLiteDatabase db, Provider provider) {
		return addModel(db, provider);
	}

	private int addCategory(SQLiteDatabase db, Category category) {
		return addModel(db, category);
	}
	
	private int addModel(SQLiteDatabase db, BaseModel model) {
		int id = -1;
		if(model != null) {
			//Check if exists
			TableName tableName = model.getClass().getAnnotation(TableName.class);			
			id = getIdFromName(db, tableName.name(), model.getName());
			
			if(id == -1) {				
				try {
					Method[] methods = model.getClass().getMethods();
					ContentValues values = new ContentValues(methods.length);
				
					for(Method method : methods) {
						Annotation[] annotations = method.getDeclaredAnnotations();
					
						for(Annotation annotation : annotations){
							if(annotation instanceof Column){
								Column column = (Column) annotation;
							
								//Assuming that all fields are Strings
								values.put(column.name(), (String)method.invoke(model, new Object[0]));
							}
						}
					}
			
					return (int) db.insert(tableName.name(), null, values);				
				} catch (Exception e) {
					Log.e(WineDatabaseHelper.class.getName(), "Failed to add model to database", e);
				}
			}
		}
		
		return id;
	}

	private int getIdFromName(SQLiteDatabase db, String table, String name) {
		if(name != null) {
			Cursor c = db.query(table, new String[] {"_id"}, "name=?", new String[] {name}, null, null, null);
			
			try {
				if(c.moveToFirst()) {
					return c.getInt(0);
				}
			} finally {			
				c.close();
			}
		}
		
		return -1;
	}

	private boolean exists(Wine wine) {
		Log.d(WineDatabaseHelper.class.getName(),"Found wine: " + (wine != null ? wine.toString() : null));
		
		if (wine.isNew()) {
			if (wine.getNo() > 0) {
				// Check if wine is already added in earlier search
				int id = getWineIdFromNo(wine.getNo());
				
				Log.d(WineDatabaseHelper.class.getName(),"Found wine with id[" + id + "]");
				
				wine.setId(id);
				return id != -1;
			}
			
			return false;
		}

		return true;
	}

	private ContentValues convertWineToValues(Wine wine) {
		ContentValues values = new ContentValues();

		Country country = wine.getCountry();
		if(country != null && !country.isNew()) {
			values.put("country_id", country.getId());  
		}
		
		Producer producer = wine.getProducer();
		if(producer != null && !producer.isNew()) {
			values.put("producer_id", producer.getId());  
		}

		Provider provider = wine.getProvider();
		if(provider != null && !provider.isNew()) {
			values.put("provider_id", provider.getId());  
		}
		
		Category category = wine.getCategory();
		if(category != null && !category.isNew()) {
			values.put("category_id", category.getId());  
		}

		values.put("name", wine.getName());  
		values.put("no", wine.getNo());  
		values.put("thumb", wine.getThumb());
		values.put("year", wine.getYear());  
		values.put("type", wine.getType().getId());  
		values.put("strength", wine.getStrength());  
		values.put("price", wine.getPrice());  
		values.put("usage", wine.getUsage());  
		values.put("taste", wine.getTaste());  
		values.put("rating", wine.getRating()); 
		values.put("comment", wine.getComment()); 
		
		return values;
	}

	private boolean inCellar(int id) {
		SQLiteDatabase db = getReadableDatabase();			
		Cursor c = db.query(CELLAR_TABLE, new String[]{"wine_id"}, "wine_id = ?", new String[] {String.valueOf(id)}, null, null, null);

		return c.moveToFirst();		
	}
	
	@SuppressWarnings("unused")
	private void debugSetDBVersion(int version) {
		SQLiteDatabase db = getWritableDatabase();

		try {
			db.setVersion(version);
		} finally {
			db.close();
			close();
		}
	}

	public int getNextNotificationId() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = null;
		
		try {
			c = db.rawQuery("SELECT max(notification_id) as max FROM cellar", null);

			if (c.moveToFirst()) {
				return c.getInt(0)+1;
			}

			return -1;
		} finally {
			c.close();
			db.close();
			close();
		}
	}

	// Stats
	public double getCellarValue() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = null;
		
		try {
			c = db.rawQuery(WineDatabaseHelper.SQL_SELECT_ALL_WINES_INCLUDING_NO_IN_CELLAR, null);

			double value = 0;
			for (boolean b = c.moveToFirst(); b; b = c.moveToNext()) {
				value += c.getDouble(12) * c.getInt(22);
			}

			return value;
		} finally {
			c.close();
			db.close();
			close();
		}

	}

	public double getAverageRating() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = null;
		
		try {
			c = db.rawQuery("select avg(rating) from wine where rating != -1", null);

			if (c.moveToFirst()) {
				return c.getDouble(0);
			}
		} finally {
			c.close();
			db.close();
			close();
		}
		
		return 0;
	}

	public int getAllWinesForType(WineType type) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = null;
		
		try {
			c = db.rawQuery("select count(_id) from wine where type = ?", new String[]{String.valueOf(type.getId())});

			if (c.moveToFirst()) {
				return c.getInt(0);
			}
		} finally {
			c.close();
			db.close();
			close();
		}
		
		return 0;
	}

	public List<WineType> getAllAvailableWineTypes() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = null;
		List<WineType> types = new ArrayList<WineType>();
		try {
			c = db.rawQuery("select distinct type from wine", null);

			for (boolean b = c.moveToFirst(); b; b = c.moveToNext()) {
				types.add(WineType.fromId(c.getInt(0)));
			}
		} finally {
			c.close();
			db.close();
			close();
		}
		
		return types;
	}

	// End Stats
	
}
