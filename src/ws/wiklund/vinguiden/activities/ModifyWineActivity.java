package ws.wiklund.vinguiden.activities;

import java.util.ArrayList;
import java.util.Date;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Category;
import ws.wiklund.vinguiden.model.Country;
import ws.wiklund.vinguiden.model.Producer;
import ws.wiklund.vinguiden.model.Provider;
import ws.wiklund.vinguiden.model.Wine;
import ws.wiklund.vinguiden.model.WineType;
import ws.wiklund.vinguiden.util.ViewHelper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ModifyWineActivity extends BaseActivity {
	private Wine wine;
	private ViewHelper viewHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modifywine);

        if(!isLightVersion()) {
    		findViewById(R.id.adView).setVisibility(View.GONE);
        	findViewById(R.id.adView1).setVisibility(View.GONE);        	
        }

        wine = (Wine) getIntent().getSerializableExtra("ws.wiklund.vinguiden.activities.Wine");

		Log.d(ModifyWineActivity.class.getName(), "Wine: " + (wine != null ? wine.toString() : null));

		viewHelper = new ViewHelper(this);

		if (wine != null) {
			setTitle(wine.getName());
			populateUI();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.modify_wine_menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menuSaveWine:
		    	Wine w = getWineFromUI();
			   	WineDatabaseHelper helper = new WineDatabaseHelper(ModifyWineActivity.this.getApplicationContext());
		    	helper.addWine(w);
		    	showWineList();					
				return true;
			case R.id.menuCancel:
		    	showWineList();
				return true;
		}
		
		return false;
	}	
	
    private void populateUI() {
    	viewHelper.setThumbFromUrl(findViewById(android.R.id.content), wine.getThumb());
		TextView no = (TextView) findViewById(R.id.Text_no);
		no.setText(String.valueOf(wine.getNo()));
		
		EditText name = (EditText) findViewById(R.id.Edit_name);
		ViewHelper.setText(name, wine.getName());
		
		Spinner type = (Spinner) findViewById(R.id.Spinner_type);
		populateAndSetTypeSpinner(type, wine.getType());
		
		Country c = wine.getCountry();
		if (c != null) {
			viewHelper.setCountryThumbFromUrl(findViewById(android.R.id.content), c);
			EditText country = (EditText) findViewById(R.id.Edit_country);
			ViewHelper.setText(country, c.getName());
		}
		
		Spinner year = (Spinner) findViewById(R.id.Spinner_year);
		populateAndSetYearSpinner(year, wine.getYear());
		
		Producer p = wine.getProducer();
		if (p != null) {
			EditText producer = (EditText) findViewById(R.id.Edit_producer);
			ViewHelper.setText(producer, p.getName());
		}
		
		Spinner strength = (Spinner) findViewById(R.id.Spinner_strength);
		populateAndSetStrengthSpinner(strength, wine.getStrength());
		
		EditText price = (EditText) findViewById(R.id.Edit_price);
		ViewHelper.setText(price, String.valueOf(wine.getPrice()));

		EditText usage = (EditText) findViewById(R.id.Edit_usage);
		ViewHelper.setText(usage, wine.getUsage());
		
		EditText taste = (EditText) findViewById(R.id.Edit_taste);
		ViewHelper.setText(taste, wine.getTaste());
		
		Spinner category = (Spinner) findViewById(R.id.Spinner_category);
		if(!isLightVersion()) {
			populateAndSetCategorySpinner(category, wine.getCategory());
		} else {
			TextView tv = (TextView) findViewById(R.id.Text_category);
			
			tv.setVisibility(View.GONE); 
			category.setVisibility(View.GONE); 
		}

		Provider p1 = wine.getProvider();
		if (p1 != null) {
			EditText provider = (EditText) findViewById(R.id.Edit_provider);
			ViewHelper.setText(provider, p1.getName());
		}
		
		TextView added = (TextView) findViewById(R.id.Text_added);
		added.setText(ViewHelper.getDateAsString((wine.getAdded() != null ? wine.getAdded() : new Date())));
	}

	private void populateAndSetCategorySpinner(Spinner categorySpinner, Category category) {
		ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<Category>(getCategories()));
		categorySpinner.setAdapter(adapter);
		
		categorySpinner.setSelection(adapter.getPosition(category));
	}

	private void populateAndSetStrengthSpinner(Spinner strengthSpinner, double strength) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ViewHelper.getStrengths());
		strengthSpinner.setAdapter(adapter);
		
		strengthSpinner.setSelection(adapter.getPosition(ViewHelper.getDecimalStringFromNumber(strength) + " %"));
	}

	private void populateAndSetTypeSpinner(Spinner typeSpinner, WineType type) {
		ArrayAdapter<WineType> adapter = new ArrayAdapter<WineType>(this, android.R.layout.simple_spinner_dropdown_item, getTypes());
		typeSpinner.setAdapter(adapter);

		typeSpinner.setSelection(adapter.getPosition(type));
	}

	private void populateAndSetYearSpinner(Spinner yearSpinner, int year) {
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, getYears());
		yearSpinner.setAdapter(adapter);
		
		if(year >= 1900 && year <= getCurrentYear()) {
			yearSpinner.setSelection(adapter.getPosition(year));
		}
	}

	private void showWineList() {
		Intent intent;
        if(!isLightVersion()) {
    		intent = new Intent(getApplicationContext(), WineListActivity.class);
        } else {
        	intent = new Intent(getApplicationContext(), FullAdActivity.class);

        	/* Removed PayPal for now
        	if (rand.nextInt(10) == 0) {
            	intent = new Intent(getApplicationContext(), DonateActivity.class);
            } else {
            	intent = new Intent(getApplicationContext(), FullAdActivity.class);
            }
            */
        }
        
    	startActivityForResult(intent, 0);
    	finish();
	}

    private Wine getWineFromUI() {
    	String name = ((EditText) findViewById(R.id.Edit_name)).getText().toString();		
		WineType type = (WineType) ((Spinner) findViewById(R.id.Spinner_type)).getSelectedItem();
		String country = ((EditText) findViewById(R.id.Edit_country)).getText().toString();
		
		int	year = (Integer) ((Spinner) findViewById(R.id.Spinner_year)).getSelectedItem();

		String producer = ((EditText) findViewById(R.id.Edit_producer)).getText().toString();
		String strength = getStrenghtFromSpinner();
		String price = ((EditText) findViewById(R.id.Edit_price)).getText().toString();
		String usage = ((EditText) findViewById(R.id.Edit_usage)).getText().toString();
		String taste = ((EditText) findViewById(R.id.Edit_taste)).getText().toString();
		String provider = ((EditText) findViewById(R.id.Edit_provider)).getText().toString();

		if(wine == null) {
			wine = new Wine();

			String noStr = ((TextView) findViewById(R.id.Text_no)).getText().toString();
			if (noStr.length() > 0) {
				wine.setNo(Integer.valueOf(noStr));
			}
    	}
		
		wine.setName(name);
		wine.setType(type);		
		wine.setYear(year);
		wine.setStrength(ViewHelper.getDoubleFromDecimalString(strength));
		
		if (price.length() > 0) {
			wine.setPrice(ViewHelper.getDoubleFromDecimalString(price));
		}
		
		wine.setUsage(usage);
		wine.setTaste(taste);
		
		updateCountry(country);
		updateProducer(producer);
		updateProvider(provider);

		return wine;
	}

	private void updateCountry(String country) {
		if(wine != null && country.length() > 0) {
			Country c = wine.getCountry();
			
			if(c == null || (!c.getName().equals(country))) {
				wine.setCountry(new Country(country, null));
			}
		}
	}

	private void updateProducer(String producer) {
		if(wine != null && producer.length() > 0) {
			Producer p = wine.getProducer();
			
			if(p == null || (!p.getName().equals(producer))) {
				wine.setProducer(new Producer(producer));
			}
		}
	}

	private void updateProvider(String provider) {
		if(wine != null && provider.length() > 0) {
			Producer p = wine.getProducer();
			
			if(p == null || (!p.getName().equals(provider))) {
				wine.setProvider(new Provider(provider));
			}
		}
	}

	private String getStrenghtFromSpinner() {
		String s = (String) ((Spinner) findViewById(R.id.Spinner_strength)).getSelectedItem();
		return s.substring(0, s.indexOf(" "));
	}
        
}

