package ws.wiklund.vinguiden.activities;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.bolaget.WineType;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Category;
import ws.wiklund.vinguiden.model.Country;
import ws.wiklund.vinguiden.util.DownloadImageTask;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class BaseActivity extends Activity {
	protected static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
	protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
	
	protected static int currentYear; 
	protected static List<Integer> years = new ArrayList<Integer>();
	protected static List<WineType> types = new ArrayList<WineType>();
	protected static List<String> strengths = new ArrayList<String>();
	
	protected static int lightVersion = 1; 

	private static Set<Category> categories = new HashSet<Category>();

	private WineDatabaseHelper helper;

	static {
		DECIMAL_FORMAT.setDecimalSeparatorAlwaysShown(true);
		DECIMAL_FORMAT.setParseBigDecimal(true);
		
		Calendar cal = Calendar.getInstance();
		currentYear = cal.get(Calendar.YEAR);
		
		for(int i = 1900; i<= currentYear; i++) {
			years.add(i);
		}	
		
		for(WineType wineType : WineType.values()) {
			types.add(wineType);
		}
			
		for(Double i = 10.0; i<= 25.0; i+=0.1) {
			strengths.add(DECIMAL_FORMAT.format(i) + " %");
		}
	}

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	helper = new WineDatabaseHelper(this);
    }
    
    protected void setText(TextView view, String value) {
		if (value != null) {
			view.setText(value);
		}
	}
    
    protected void setThumbFromUrl(String thumb) {
		new DownloadImageTask((ImageView) findViewById(R.id.Image_thumbUrl), 50, 100).execute(thumb);
	}
	
    protected void setCountryThumbFromUrl(Country country) {
		new DownloadImageTask((ImageView) findViewById(R.id.Image_country_thumbUrl), 29, 17).execute(country != null ? country.getThumbUrl() : null);
	}

    protected boolean isLightVersion() {
    	return Integer.valueOf(getString(R.string.version)) == lightVersion;
    }
    
	protected synchronized Set<Category> getCategories() {
		if(categories.isEmpty()) {
			categories.add(new Category(""));
			categories.add(new Category(Category.NEW_ID, getString(R.string.newStr)));
		}
		
		List<Category> c = helper.getCategories();
		
		if (c != null && !c.isEmpty()) {
			categories.addAll(c);
		}
		
		//TODO possibility to remove categories
		//TODO dialog if new is selected
		
		return categories;
	}
    
    
    
    

}
