package ws.wiklund.vinguiden.activities;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.bolaget.SystembolagetParser;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Wine;
import ws.wiklund.vinguiden.model.WineType;
import ws.wiklund.vinguiden.util.BitmapManager;
import ws.wiklund.vinguiden.util.CoverFlow;
import ws.wiklund.vinguiden.util.GetWineFromCursorTask;
import ws.wiklund.vinguiden.util.SelectableAdapter;
import ws.wiklund.vinguiden.util.ViewHelper;
import ws.wiklund.vinguiden.util.WineViewHolder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class WineFlowActivity extends Activity {
	private CoverFlowAdapter adapter;
	private SelectableAdapter selectableAdapter;
	private WineDatabaseHelper helper;
	private int currentPosition;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			startActivityForResult(new Intent(getApplicationContext(), WineListActivity.class), 0);
		}
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.wineflow);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);

		ViewHelper viewHelper = new ViewHelper(this);

		if (!viewHelper.isLightVersion()) {
			findViewById(R.id.adView).setVisibility(View.GONE);
			findViewById(R.id.adView1).setVisibility(View.GONE);
		}

		helper = new WineDatabaseHelper(this);			
		
		final CoverFlow flow = (CoverFlow) findViewById(R.id.coverFlow);
		adapter = new CoverFlowAdapter(this);

		flow.setAdapter(adapter);

		flow.setSpacing(-25);
		flow.setSelection(adapter.getOptimalSelection(), true);
		flow.setAnimationDuration(1000);
		
		flow.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				handleLongClick(position);
				return true;
			}
		});
		
		flow.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				new GetWineFromCursorTask(WineFlowActivity.this).execute(adapter.getItem(position));
			}
			
		});
		
		selectableAdapter = new SelectableAdapter(this, R.layout.spinner_row, getLayoutInflater()){
			public boolean isAvailableInCellar() {
				final Wine w = helper.getWineFromCursor(adapter.getItem(currentPosition));
				return w.hasBottlesInCellar();
			}
		};
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.wine_list_menu, menu);

		return true;
	}
		
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menuStats).setEnabled(hasSomeStats());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuStats:
			startActivityForResult(
					new Intent(WineFlowActivity.this.getApplicationContext(), StatsActivity.class), 0);
			break;
		case R.id.menuAbout:
			startActivityForResult(
					new Intent(WineFlowActivity.this.getApplicationContext(), AboutActivity.class), 0);

			break;
		}

		return true;
	}
	
	public void addWine(View view) {
    	Intent intent = new Intent(view.getContext(), AddWineActivity.class);
    	startActivityForResult(intent, 0);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		notifyDataSetChanged();
	}

	private boolean hasSomeStats() {
		return adapter.getCount() > 0;
	}
	
	private void handleLongClick(final int position) {
		currentPosition = position;
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		
		final Wine w = helper.getWineFromCursor(adapter.getItem(position));
		alertDialog.setTitle(w != null ? w.getName() : "");
		
		alertDialog.setSingleChoiceItems( selectableAdapter, 0, new OnClickListener() { 
            @Override 
            public void onClick(DialogInterface dialog, int which) { 
                dialog.dismiss();
                selectableAdapter.getItem(which).select(WineFlowActivity.this, helper, w.getId(), w.getName());
                notifyDataSetChanged();
            }
		}); 

		alertDialog.show(); 				
	}	

	private void notifyDataSetChanged() {
		int bottles = helper.getNoBottlesInCellar();
		// Update title with no wines in cellar
		if (bottles > 0) {
			TextView view = (TextView) WineFlowActivity.this.findViewById(R.id.title);

			String text = view.getText().toString();
			if (text.contains("(")) {
				text = text.substring(0, text.indexOf("(") - 1);
			}

			view.setText(text + " (" + bottles + ")");
		}
		
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onDestroy() {
		adapter.destroy();
		super.onDestroy();
	}
	

	private class CoverFlowAdapter extends BaseAdapter {
		private LayoutInflater inflator;
		private SQLiteDatabase db;
		private Cursor cursor;

		public CoverFlowAdapter(Context c) {
			inflator = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			BitmapManager.INSTANCE.setPlaceholder(BitmapFactory.decodeResource(c.getResources(), R.drawable.icon));
		}

		public int getOptimalSelection() {
			int c = getCount();

			if(c > 4) {
				return 4;
			} else if(c > 0) {
				return c - 1;
			}

			return 0;
		}

		public void destroy() {
			if (cursor != null) {
				cursor.close();
			}

			if (db != null) {
				db.close();
			}
		}

		public int getCount() {
			return getNewOrReuseCursor().getCount();
		}

		public Cursor getItem(int position) {
			Cursor c = getNewOrReuseCursor();
			
			if (c.moveToPosition(position)) {
				return c;
			}

			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			WineViewHolder holder;
			
			if (convertView == null) {  
				convertView = inflator.inflate(R.layout.coveritem, null);
				
				TextView titleView = (TextView) convertView.findViewById(R.id.itemTitle);  
		        TextView textView = (TextView) convertView.findViewById(R.id.itemText);  
		        TextView typeView = (TextView) convertView.findViewById(R.id.itemType);  
		        ImageView imageView = (ImageView) convertView.findViewById(R.id.itemImage);
		        RatingBar rating = (RatingBar) convertView.findViewById(R.id.itemRatingBar);

		         
		        holder = new WineViewHolder();  
		        holder.titleView = titleView;  
		        holder.textView = textView;  
		        holder.imageView = imageView;
		        holder.rating = rating;
		        holder.typeView = typeView;
		         
		        convertView.setTag(holder);
			} else {
				holder = (WineViewHolder) convertView.getTag(); 
			}
			
			Cursor c = getItem(position);

			if (c != null) {
				int noBottles = c.getInt(22); 
				StringBuilder name = new StringBuilder(c.getString(1));
				
				if(noBottles > 0) {
					name.append("(").append(c.getInt(22)).append(")");
				}
						
				holder.titleView.setText(name.toString());
				holder.typeView.setText(WineType.fromId(c.getInt(3)).toString());
				
				int year = c.getInt(8); 
				holder.textView.setText(c.getString(6) + " " + (year != -1 ? year : ""));
				String url = SystembolagetParser.BASE_URL + c.getString(4);
				holder.rating.setRating(c.getFloat(16));
				holder.imageView.setTag(url);
				BitmapManager.INSTANCE.loadBitmap(url, holder.imageView, 50, 100);
			}
			
			return convertView;
		}

		private Cursor getNewOrReuseCursor() {
			if (db == null || !db.isOpen()) {
				db = helper.getReadableDatabase();
				cursor = db.rawQuery(WineDatabaseHelper.SQL_SELECT_ALL_WINES_INCLUDING_NO_IN_CELLAR, null);
			}
			
			return cursor;
		}

	}

}



