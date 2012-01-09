package ws.wiklund.vinguiden.activities;

import java.util.Date;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.bolaget.SystembolagetParser;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Country;
import ws.wiklund.vinguiden.model.Producer;
import ws.wiklund.vinguiden.model.Provider;
import ws.wiklund.vinguiden.model.Rating;
import ws.wiklund.vinguiden.model.Wine;
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
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class WineActivity extends BaseActivity implements DialogListener {	
	private Wine wine;
	private Facebook facebook;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wine);

        if(Integer.valueOf(getString(R.string.version)) != BaseActivity.lightVersion) {
    		findViewById(R.id.adView).setVisibility(View.GONE);
        }

		wine = (Wine) getIntent().getSerializableExtra("ws.wiklund.vinguiden.activities.Wine");
		setTitle(wine.getName());

		Log.d(WineActivity.class.getName(), "Wine: " + wine);
		
		populateUI();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.wine_menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menuShareOnFacebook:
		        facebook = new Facebook("263921010324730");
		        facebook.authorize(this, new String[] {"publish_stream", "read_stream", "offline_access"}, this);		
				return true;
			case R.id.menuRateWine:
				final Dialog viewDialog = new Dialog(this); 
				viewDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND); 
				viewDialog.setTitle(R.string.rate); 

				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
				View dialogView = li.inflate(R.layout.rating, null); 
				viewDialog.setContentView(dialogView); 
				viewDialog.show(); 
				
				final RatingBar rating = (RatingBar) dialogView.findViewById(R.id.dialogRatingBar);
				
				if(wine != null && wine.getRating() != -1) {
					rating.setRating(wine.getRating());
				}
				
				final Button okBtn = (Button) dialogView.findViewById(R.id.ratingOk);
				final Button cancelBtn = (Button) dialogView.findViewById(R.id.ratingCancel);
				
				okBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Rating r = Rating.fromFloat(rating.getRating()); 
						wine.setRating(r.getRating());
					   	WineDatabaseHelper helper = new WineDatabaseHelper(WineActivity.this.getApplicationContext());
				    	
				    	helper.addWine(wine);

						RatingBar rating = (RatingBar) WineActivity.this.findViewById(R.id.ratingBar);
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
			case R.id.menuUpdateWine:
				Intent intent = new Intent(getApplicationContext(), ModifyWineActivity.class);
				intent.putExtra("ws.wiklund.vinguiden.activities.Wine", wine);
				
		    	startActivityForResult(intent, 0);
				return true;
		}
		
		return false;
	}
	
	@Override
	public void onComplete(Bundle values) {
		if (values.isEmpty()) {
			// "skip" clicked ?
			return;
		}

		// if facebookClient.authorize(...) was successful, this runs
		// this also runs after successful post
		// after posting, "post_id" is added to the values bundle
		// I use that to differentiate between a call from
		// faceBook.authorize(...) and a call from a successful post
		// is there a better way of doing this?
		if (!values.containsKey("post_id")) {
			try {
				Bundle parameters = new Bundle();
				parameters.putString("picture", SystembolagetParser.BASE_URL + wine.getThumb());
				parameters.putString("name", getString(R.string.app_name));
				parameters.putString("link", SystembolagetParser.BASE_URL + "/" + wine.getNo());
				
				StringBuilder builder = new StringBuilder(getString(R.string.recommend_wine));
				builder.append(" ").append(wine.getName());
				
				if(wine.getNo() != -1) {
					builder.append(" (" + wine.getNo() + ")"); 
				}
				
				if(wine.getRating() != -1) {
					builder.append(" ").append(getString(R.string.recommend_wine1)).append(" ");
					String rating = DECIMAL_FORMAT.format(wine.getRating());

					if(rating.endsWith(String.valueOf(DECIMAL_FORMAT.getDecimalFormatSymbols().getDecimalSeparator()))) {
						rating = rating.substring(0, rating.length() - 1);
					}
					
					builder.append(rating).append(" ").append(getString(R.string.recommend_wine2));
				}
				
				
				parameters.putString("description", builder.toString());

				facebook.dialog(this, "stream.publish", parameters, this); // "stream.publish" is an API call
			} catch (Exception e) {
				Log.w(WineActivity.class.getName(), "Failed to post to Facebook wall", e);
			}
		}
	}

	@Override
	public void onFacebookError(FacebookError e) {
		Log.w(WineActivity.class.getName(), "Got FacebookError", e);
	}

	@Override
	public void onError(DialogError e) {
		Log.w(WineActivity.class.getName(), "Got error", e);
	}

	@Override
	public void onCancel() {
	}

	private void populateUI() {
		setThumbFromUrl(wine.getThumb());
		
		TextView no = (TextView) findViewById(R.id.Text_no);
		no.setText(String.valueOf(wine.getNo()));

		TextView type = (TextView) findViewById(R.id.Text_type);
		setText(type, wine.getType().toString());

		Country c = wine.getCountry();
		if (c != null) {
			setCountryThumbFromUrl(c);
			TextView country = (TextView) findViewById(R.id.Text_country);
			setText(country, c.getName());
		}

		if (wine.getYear() != -1) {
			TextView year = (TextView) findViewById(R.id.Text_year);
			setText(year, String.valueOf(wine.getYear()));
		}
		
		Producer p = wine.getProducer();
		if (p != null) {
			TextView producer = (TextView) findViewById(R.id.Text_producer);
			setText(producer, p.getName());
		}

		if (wine.getStrength() != -1) {
			TextView strength = (TextView) findViewById(R.id.Text_strength);
			setText(strength, String.valueOf(wine.getStrength()) + " %");
		}
		
		TextView usage = (TextView) findViewById(R.id.Text_usage);
		setText(usage, wine.getUsage());

		TextView taste = (TextView) findViewById(R.id.Text_taste);
		setText(taste, wine.getTaste());

		Provider p1 = wine.getProvider();
		if (p1 != null) {
			TextView provider = (TextView) findViewById(R.id.Text_provider);
			setText(provider, p1.getName());
		}

		RatingBar rating = (RatingBar) findViewById(R.id.ratingBar);
		rating.setRating(wine.getRating());

		TextView added = (TextView) findViewById(R.id.Text_added);
		
		added.setText(DATE_FORMAT.format((wine.getAdded() != null ? wine.getAdded() : new Date())));
	}

}
