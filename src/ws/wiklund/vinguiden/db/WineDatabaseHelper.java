package ws.wiklund.vinguiden.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import ws.wiklund.vinguiden.bolaget.WineType;
import ws.wiklund.vinguiden.model.BaseModel;
import ws.wiklund.vinguiden.model.Category;
import ws.wiklund.vinguiden.model.Column;
import ws.wiklund.vinguiden.model.Country;
import ws.wiklund.vinguiden.model.Producer;
import ws.wiklund.vinguiden.model.Provider;
import ws.wiklund.vinguiden.model.TableName;
import ws.wiklund.vinguiden.model.Wine;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WineDatabaseHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "wineguide.db";
	private static final int DATABASE_VERSION = 5;

	// Database tables
	private static final String COUNTRY_TABLE = "country";
	private static final String PRODUCER_TABLE = "producer"; 
	private static final String PROVIDER_TABLE = "provider";
	private static final String CATEGORY_TABLE = "category";
	
	static final String WINE_TABLE = "wine";

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

	static final String DB_CREATE_CATEGORY = "create table " + CATEGORY_TABLE + " (_id integer primary key autoincrement, "
			+ "name text not null);";

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
			+ "wine.usage, "
			+ "wine.taste, "
			+ "wine.provider_id, "
			+ "provider.name, "
			+ "wine.rating, "
			+ "wine.comment, "			
			+ "wine.category_id, "
			+ "category.name, "
			+ "(strftime('%s', added) * 1000) AS added "
		+ "FROM "
			+ WINE_TABLE + " ";

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
			+ WINE_JOIN_COLUMNS;
	
	private static final String SQL_SELECT_WINE = 
			SQL_SELECT_ALL_WINES
			+ "WHERE "
			+ "wine._id = ?";
	
	public WineDatabaseHelper(Context context) {
		this(context, DATABASE_NAME);
	}

	//Used for testing so that db can be created and dropped with out destroying dev data
	public WineDatabaseHelper(Context context, String dbName) {
		super(context, dbName, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE_PRODUCER);
		db.execSQL(DB_CREATE_PROVIDER);
		db.execSQL(DB_CREATE_COUNTRY);
		db.execSQL(DB_CREATE_CATEGORY);
		db.execSQL(DB_CREATE_WINE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		
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
			c = db.query("country", null, "_id = " + id, null, null, null, null);

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

	public Cursor getAllWines() {
		SQLiteDatabase db = getReadableDatabase();
		
		try {
			return db.rawQuery(SQL_SELECT_ALL_WINES, null);
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
			 return db.delete(WINE_TABLE, "_id=?", new String[]{String.valueOf(id)}) == 1;
		} finally {
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

	/**
	 * Internal method with out check for duplicates
	 * @param wine
	 * @return
	 */
	private boolean update(Wine wine) {
		SQLiteDatabase db = getWritableDatabase();

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

			return true;		
		} finally {
			db.close();
			close();
		}
	}

	public Wine addWine(Wine wine) {
		if (!exists(wine)) {
			SQLiteDatabase db = getWritableDatabase();

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
			} finally {
				db.close();
				close();
			}
		} else {
			update(wine);
		}

		return wine;
	}
	
	public Wine getWineFromCursor(Cursor c) {
		return new Wine(c.getInt(0),
				c.getString(1),
				c.getInt(2),
				WineType.fromId(c.getInt(3)),
				c.getString(4),
				new Country(c.getInt(5), c.getString(6), c.getString(7)),
				c.getInt(8),
				new Producer(c.getInt(9), c.getString(10)),
				c.getFloat(11),
				c.getString(12),
				c.getString(13),
				new Provider(c.getInt(14), c.getString(15)),
				c.getFloat(16),
				c.getString(17),
				new Category(c.getInt(18), c.getString(19)),
				new Date(c.getLong(20)));
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
		values.put("usage", wine.getUsage());  
		values.put("taste", wine.getTaste());  
		values.put("rating", wine.getRating()); 
		values.put("comment", wine.getComment()); 
		
		return values;
	}
	
}
