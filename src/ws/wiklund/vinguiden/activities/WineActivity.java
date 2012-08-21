package ws.wiklund.vinguiden.activities;

import java.util.Date;

import ws.wiklund.guides.activities.BaseActivity;
import ws.wiklund.guides.model.Beverage;
import ws.wiklund.guides.model.Country;
import ws.wiklund.guides.model.Producer;
import ws.wiklund.guides.model.Provider;
import ws.wiklund.guides.model.Rating;
import ws.wiklund.guides.util.FacebookPostMessageTask;
import ws.wiklund.guides.util.ViewHelper;
import ws.wiklund.guides.util.facebook.FacebookConnector;
import ws.wiklund.guides.util.facebook.SessionEvents;
import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.util.WineTypes;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class WineActivity extends BaseActivity {
	private FacebookConnector connector;
	private Beverage beverage;
	private boolean isNew;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wine);

		connector = new FacebookConnector("263921010324730", this, getApplicationContext(), new String[] {"publish_stream", "read_stream", "offline_access"});
		
        beverage = (Beverage) getIntent().getSerializableExtra("ws.wiklund.guides.model.Beverage");
		Log.d(WineActivity.class.getName(), "Beverage: " + beverage);

		populateUI();
        isNew = true;
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.beverage_menu, menu);

		return true;
	}

	@Override
	public void onResume() {
		if(isNew) {
			isNew = false;
		} else {
			//Update the beverage because the data might have changed
			Beverage b = new WineDatabaseHelper(this).getBeverage(beverage.getId());
			if(!b.equals(beverage)) {
				beverage = b;
				populateUI();
			}
		}

		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menuShareOnFacebook:
				final FacebookPostMessageTask task = new FacebookPostMessageTask(WineActivity.this, connector, beverage, getString(R.string.recommend_wine_header));
				
				if (connector.getFacebook().isSessionValid()) {
					task.execute();
				} else {
					SessionEvents.addAuthListener(new SessionEvents.AuthListener() {
						@Override
						public void onAuthSucceed() {
							task.execute();
						}
						
						@Override
						public void onAuthFail(String error) {
						}
					});

					connector.login();
				}

				return true;
			case R.id.menuRateBeverage:
				final Dialog viewDialog = new Dialog(this); 
				viewDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND); 
				viewDialog.setTitle(R.string.rate); 

				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
				View dialogView = li.inflate(R.layout.rating, null); 
				viewDialog.setContentView(dialogView); 
				viewDialog.show(); 
				
				final RatingBar rating = (RatingBar) dialogView.findViewById(R.id.dialogRatingBar);
				
				if(beverage != null && beverage.getRating() != -1) {
					rating.setRating(beverage.getRating());
				}
				
				final Button okBtn = (Button) dialogView.findViewById(R.id.ratingOk);
				final Button cancelBtn = (Button) dialogView.findViewById(R.id.ratingCancel);
				
				okBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Rating r = Rating.fromFloat(rating.getRating()); 
						beverage.setRating(r.getRating());
					   	WineDatabaseHelper helper = new WineDatabaseHelper(getApplicationContext());
				    	
				    	helper.addBeverage(beverage);

						RatingBar rating = (RatingBar) findViewById(R.id.ratingBar);
						rating.setRating(r.getRating());
				    	viewDialog.dismiss();
					}
				});
				
				cancelBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
				    	viewDialog.hide();
					}
				});
				
				return true;
			case R.id.menuUpdateBeverage:
				Intent intent = new Intent(getApplicationContext(), ModifyWineActivity.class);
				intent.putExtra("ws.wiklund.guides.model.Beverage", beverage);
				
		    	startActivityForResult(intent, 0);
				return true;
		}
		
		return false;
	}
		

	private void populateUI() {
		setTitle(beverage.getName());
		
		ViewHelper.setThumbFromUrl((ImageView) findViewById(R.id.Image_thumbUrl), beverage.getThumb());
		
		if (beverage.getNo() != -1) {
			TextView no = (TextView) findViewById(R.id.Text_no);
			no.setText(String.valueOf(beverage.getNo()));
		}
		
		TextView type = (TextView) findViewById(R.id.Text_type);
		
		ViewHelper.setText(type, new WineTypes().findTypeFromId(beverage.getBeverageTypeId()).toString());

		Country c = beverage.getCountry();
		if (c != null) {
			ViewHelper.setCountryThumbFromUrl((ImageView) findViewById(R.id.Image_country_thumbUrl), c);
			TextView country = (TextView) findViewById(R.id.Text_country);
			ViewHelper.setText(country, c.getName());
		}

		if (beverage.getYear() != -1) {
			TextView year = (TextView) findViewById(R.id.Text_year);
			ViewHelper.setText(year, String.valueOf(beverage.getYear()));
		}
		
		Producer p = beverage.getProducer();
		if (p != null) {
			TextView producer = (TextView) findViewById(R.id.Text_producer);
			ViewHelper.setText(producer, p.getName());
		}

		if (beverage.getStrength() != -1) {
			TextView strength = (TextView) findViewById(R.id.Text_strength);
			ViewHelper.setText(strength, String.valueOf(beverage.getStrength()) + " %");
		}
		
		if (beverage.hasPrice()) {
			TextView label = (TextView) findViewById(R.id.label_price);
			label.setVisibility(View.VISIBLE);

			TextView price = (TextView) findViewById(R.id.Text_price);
			ViewHelper.setText(price, ViewHelper.formatPrice(beverage.getPrice()));
		} else {
			TextView price = (TextView) findViewById(R.id.label_price);
			price.setVisibility(View.GONE);
		}
		
		if (beverage.hasBottlesInCellar()) {
			TextView cellar = (TextView) findViewById(R.id.Text_cellar);
			cellar.setText(String.format(getString(R.string.bottles_in_cellar), new Object[]{
						String.valueOf(beverage.getBottlesInCellar()),
						ViewHelper.formatPrice(beverage.getPrice() * beverage.getBottlesInCellar())}));
		}
		
		TextView usage = (TextView) findViewById(R.id.Text_usage);
		ViewHelper.setText(usage, beverage.getUsage());

		TextView taste = (TextView) findViewById(R.id.Text_taste);
		ViewHelper.setText(taste, beverage.getTaste());

		Provider p1 = beverage.getProvider();
		if (p1 != null) {
			TextView provider = (TextView) findViewById(R.id.Text_provider);
			ViewHelper.setText(provider, p1.getName());
		}

		TextView tv = (TextView) findViewById(R.id.Text_category);

		if(!ViewHelper.isLightVersion(Integer.valueOf(getString(R.string.version_type)))) {
			ViewHelper.setText(tv, beverage.getCategory().getName());
		} else {
			TextView lbl = (TextView) findViewById(R.id.Text_category_lbl);
			
			lbl.setVisibility(View.GONE); 
			tv.setVisibility(View.GONE); 
		}
		
		TextView comment = (TextView) findViewById(R.id.Text_comment);
		ViewHelper.setText(comment, beverage.getComment());
		
		RatingBar rating = (RatingBar) findViewById(R.id.ratingBar);
		rating.setRating(beverage.getRating());

		TextView added = (TextView) findViewById(R.id.Text_added);
		
		added.setText(ViewHelper.getDateAsString((beverage.getAdded() != null ? beverage.getAdded() : new Date())));
	}

}
