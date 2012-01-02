package ws.wiklund.vinguiden.activities;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.bolaget.WineType;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Country;
import ws.wiklund.vinguiden.model.Producer;
import ws.wiklund.vinguiden.model.Provider;
import ws.wiklund.vinguiden.model.Wine;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modifywine);

        if(Integer.valueOf(getString(R.string.version)) != BaseActivity.lightVersion) {
    		findViewById(R.id.adView).setVisibility(View.GONE);
        	findViewById(R.id.adView1).setVisibility(View.GONE);        	
        }

        wine = (Wine) getIntent().getSerializableExtra("ws.wiklund.vinguiden.activities.Wine");

		Log.d(ModifyWineActivity.class.getName(), "Wine: " + (wine != null ? wine.toString() : null));

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
    	setThumbFromUrl(wine.getThumb());
		TextView no = (TextView) findViewById(R.id.Text_no);
		no.setText(String.valueOf(wine.getNo()));
		
		EditText name = (EditText) findViewById(R.id.Edit_name);
		setText(name, wine.getName());
		
		Spinner type = (Spinner) findViewById(R.id.Spinner_type);
		populateAndSetTypeSpinner(type, wine.getType());
		
		Country c = wine.getCountry();
		if (c != null) {
			setCountryThumbFromUrl(c);
			EditText country = (EditText) findViewById(R.id.Edit_country);
			setText(country, c.getName());
		}
		
		Spinner year = (Spinner) findViewById(R.id.Spinner_year);
		populateAndSetYearSpinner(year, wine.getYear());
		
		Producer p = wine.getProducer();
		if (p != null) {
			EditText producer = (EditText) findViewById(R.id.Edit_producer);
			setText(producer, p.getName());
		}
		
		Spinner strength = (Spinner) findViewById(R.id.Spinner_strength);
		populateAndSetStrengthSpinner(strength, wine.getStrength());
		
		EditText usage = (EditText) findViewById(R.id.Edit_usage);
		setText(usage, wine.getUsage());
		
		EditText taste = (EditText) findViewById(R.id.Edit_taste);
		setText(taste, wine.getTaste());
		
		Provider p1 = wine.getProvider();
		if (p1 != null) {
			EditText provider = (EditText) findViewById(R.id.Edit_provider);
			setText(provider, p1.getName());
		}
		
		TextView added = (TextView) findViewById(R.id.Text_added);
		added.setText(DATE_FORMAT.format((wine.getAdded() != null ? wine.getAdded() : new Date())));
	}

	private void populateAndSetStrengthSpinner(Spinner strengthSpinner, double strength) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strengths);
		strengthSpinner.setAdapter(adapter);
		
		strengthSpinner.setSelection(adapter.getPosition(DECIMAL_FORMAT.format(strength) + " %"));
	}

	private void populateAndSetTypeSpinner(Spinner typeSpinner, WineType type) {
		ArrayAdapter<WineType> adapter = new ArrayAdapter<WineType>(this, android.R.layout.simple_spinner_dropdown_item, types);
		typeSpinner.setAdapter(adapter);

		typeSpinner.setSelection(adapter.getPosition(type));
	}

	private void populateAndSetYearSpinner(Spinner yearSpinner, int year) {
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, years);
		yearSpinner.setAdapter(adapter);
		
		if(year >= 1900 && year <= currentYear) {
			yearSpinner.setSelection(adapter.getPosition(year));
		}
	}

	private void showWineList() {
		Intent intent;
        if(Integer.valueOf(getString(R.string.version)) != BaseActivity.lightVersion) {
    		intent = new Intent(getApplicationContext(), WineListActivity.class);
        } else {
        	intent = new Intent(getApplicationContext(), FullAdActivity.class);
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
		
		try {
			BigDecimal bd = (BigDecimal) DECIMAL_FORMAT.parse(strength);
			wine.setStrength(bd.doubleValue());
		} catch (ParseException e) {
			Log.d(ModifyWineActivity.class.getName(), "Failed to parse strength(" + strength + ")", e);
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

