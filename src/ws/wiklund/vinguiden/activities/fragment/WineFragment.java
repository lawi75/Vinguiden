package ws.wiklund.vinguiden.activities.fragment;

import java.util.Date;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.activities.ModifyWineActivity;
import ws.wiklund.vinguiden.bolaget.SystembolagetParser;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Country;
import ws.wiklund.vinguiden.model.Producer;
import ws.wiklund.vinguiden.model.Provider;
import ws.wiklund.vinguiden.model.Rating;
import ws.wiklund.vinguiden.model.Wine;
import ws.wiklund.vinguiden.util.ViewHelper;
import ws.wiklund.vinguiden.util.facebook.FacebookConnector;
import ws.wiklund.vinguiden.util.facebook.SessionEvents;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class WineFragment extends Fragment {
	private final Handler facebookHandler = new Handler();

	private Wine wine;
	private View view;
	private ViewHelper viewHelper;
	private FacebookConnector connector;

	final Runnable updateFacebookNotification = new Runnable() {
        public void run() {
        	Toast.makeText(getActivity().getBaseContext(), getString(R.string.facebookPosted), Toast.LENGTH_LONG).show();
        }
    };

	public WineFragment(Wine wine) {
		Log.d(WineFragment.class.getName(), "Wine: " + wine);
		this.wine = wine;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.wine_menu, menu);
	}	

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		FragmentActivity a = getActivity();
		TabHost tabHost = (TabHost) a.findViewById(android.R.id.tabhost);
		if(WineTabsFragment.TAB_WINE.equals(tabHost.getCurrentTabTag())) {
			menu.clear();
		    MenuInflater inflater = a.getMenuInflater();
			inflater.inflate(R.menu.wine_menu, menu);
		}

		super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		view = inflater.inflate(R.layout.wine, container, false);
		
		return view;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);
        
		viewHelper = new ViewHelper(getActivity());
        if(!viewHelper.isLightVersion()) {
    		view.findViewById(R.id.adView).setVisibility(View.GONE);
        }

		connector = new FacebookConnector("263921010324730", getActivity(), getActivity().getApplicationContext(), new String[] {"publish_stream", "read_stream", "offline_access"});
		
        populateUI();
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menuShareOnFacebook:
				if (connector.getFacebook().isSessionValid()) {
					new FacebookPostMessageTask().execute();
				} else {
					SessionEvents.addAuthListener(new SessionEvents.AuthListener() {
						@Override
						public void onAuthSucceed() {
							new FacebookPostMessageTask().execute();
						}
						
						@Override
						public void onAuthFail(String error) {
						}
					});

					connector.login();
				}

				return true;
			case R.id.menuRateWine:
				final Dialog viewDialog = new Dialog(getActivity()); 
				viewDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND); 
				viewDialog.setTitle(R.string.rate); 

				LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
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
					   	WineDatabaseHelper helper = new WineDatabaseHelper(getActivity().getApplicationContext());
				    	
				    	helper.addWine(wine);

						RatingBar rating = (RatingBar) getActivity().findViewById(R.id.ratingBar);
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
				Intent intent = new Intent(getActivity().getApplicationContext(), ModifyWineActivity.class);
				intent.putExtra("ws.wiklund.vinguiden.activities.Wine", wine);
				
		    	startActivityForResult(intent, 0);
				return true;
		}
		
		return false;
	}
		

	private void populateUI() {
		viewHelper.setThumbFromUrl(view, wine.getThumb());
		
		TextView no = (TextView) view.findViewById(R.id.Text_no);
		no.setText(String.valueOf(wine.getNo()));

		TextView type = (TextView) view.findViewById(R.id.Text_type);
		ViewHelper.setText(type, wine.getType().toString());

		Country c = wine.getCountry();
		if (c != null) {
			viewHelper.setCountryThumbFromUrl(view, c);
			TextView country = (TextView) view.findViewById(R.id.Text_country);
			ViewHelper.setText(country, c.getName());
		}

		if (wine.getYear() != -1) {
			TextView year = (TextView) view.findViewById(R.id.Text_year);
			ViewHelper.setText(year, String.valueOf(wine.getYear()));
		}
		
		Producer p = wine.getProducer();
		if (p != null) {
			TextView producer = (TextView) view.findViewById(R.id.Text_producer);
			ViewHelper.setText(producer, p.getName());
		}

		if (wine.getStrength() != -1) {
			TextView strength = (TextView) view.findViewById(R.id.Text_strength);
			ViewHelper.setText(strength, String.valueOf(wine.getStrength()) + " %");
		}
		
		if (wine.hasPrice()) {
			TextView label = (TextView) view.findViewById(R.id.label_price);
			label.setVisibility(View.VISIBLE);

			TextView price = (TextView) view.findViewById(R.id.Text_price);
			ViewHelper.setText(price, ViewHelper.formatPrice(wine.getPrice()));
		} else {
			TextView price = (TextView) view.findViewById(R.id.label_price);
			price.setVisibility(View.GONE);
		}
		
		if (wine.hasBottlesInCellar()) {
			TextView cellar = (TextView) view.findViewById(R.id.Text_cellar);
			cellar.setText(
					String.format(getString(R.string.bottles_in_cellar), 
							new Object[]{
						String.valueOf(wine.getBottlesInCellar()),
						ViewHelper.formatPrice(wine.getPrice() * wine.getBottlesInCellar())}));
		}
		
		TextView usage = (TextView) view.findViewById(R.id.Text_usage);
		ViewHelper.setText(usage, wine.getUsage());

		TextView taste = (TextView) view.findViewById(R.id.Text_taste);
		ViewHelper.setText(taste, wine.getTaste());

		Provider p1 = wine.getProvider();
		if (p1 != null) {
			TextView provider = (TextView) view.findViewById(R.id.Text_provider);
			ViewHelper.setText(provider, p1.getName());
		}

		TextView tv = (TextView) view.findViewById(R.id.Text_category);

		if(!viewHelper.isLightVersion()) {
			ViewHelper.setText(tv, wine.getCategory().getName());
		} else {
			TextView lbl = (TextView) view.findViewById(R.id.Text_category_lbl);
			
			lbl.setVisibility(View.GONE); 
			tv.setVisibility(View.GONE); 
		}

		
		RatingBar rating = (RatingBar) view.findViewById(R.id.ratingBar);
		rating.setRating(wine.getRating());

		TextView added = (TextView) view.findViewById(R.id.Text_added);
		
		added.setText(ViewHelper.getDateAsString((wine.getAdded() != null ? wine.getAdded() : new Date())));
	}
	
	
	private class FacebookPostMessageTask extends AsyncTask<Void, Void, Void> {
	    
		@Override
		protected Void doInBackground(Void... params) {
			Bundle bundle = new Bundle();
			bundle.putString("picture", SystembolagetParser.BASE_URL + wine.getThumb());
			bundle.putString("name", getString(R.string.recommend_wine_header));
			bundle.putString("link", SystembolagetParser.BASE_URL + "/" + wine.getNo());
			
			StringBuilder builder = new StringBuilder(getString(R.string.recommend_wine));
			builder.append(" ").append(wine.getName());
			
			if(wine.getNo() != -1) {
				builder.append(" (" + wine.getNo() + ")"); 
			}
			
			if(wine.getRating() != -1) {
				builder.append(" ").append(getString(R.string.recommend_wine1)).append(" ");					
				builder.append(ViewHelper.getDecimalStringFromNumber(wine.getRating())).append(" ").append(getString(R.string.recommend_wine2));
			}
			
			bundle.putString("description", builder.toString());

			connector.postMessageOnWall(bundle);
			facebookHandler.post(updateFacebookNotification);
			return null;
		}
		
	}

}
