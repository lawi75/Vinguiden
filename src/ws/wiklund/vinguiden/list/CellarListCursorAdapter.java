package ws.wiklund.vinguiden.list;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Cellar;
import ws.wiklund.vinguiden.model.Wine;
import ws.wiklund.vinguiden.util.AlarmReceiver;
import ws.wiklund.vinguiden.util.ViewHelper;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateSlider.OnDateSetListener;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;

import de.viktorreiser.toolbox.widget.NumberPicker;
import de.viktorreiser.toolbox.widget.NumberPicker.OnChangedListener;

public class CellarListCursorAdapter extends SimpleCursorAdapter {
	private static int REMINDER_ID =1;

	private LayoutInflater inflator;
	private Wine wine;
	private String valueString;
	private Context applicationContext;
	
	private Map<Integer, ViewHolder> holders = new HashMap<Integer, ViewHolder>();

	public CellarListCursorAdapter(Context context, Wine wine) {
		super(context, R.layout.cellaritem, null, new String[] {"no_bottles", "storage_location", "consumption_date", "notification_id"}, new int[] {android.R.id.icon, android.R.id.text1});
		
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.wine = wine;
		
		valueString = context.getString(R.string.totalValue) + " ";
		
		applicationContext = context.getApplicationContext();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		
		final ViewHolder holder = (convertView == null)?new ViewHolder():(ViewHolder) convertView.getTag();
		
		if (convertView == null) {  
			convertView = inflator.inflate(R.layout.cellaritem, null);
			
			NumberPicker numberPicker = (NumberPicker) convertView.findViewById(R.id.numberPicker);
			numberPicker.setOnChangeListener(new OnChangedListener() {
				@Override
				public void onChanged(NumberPicker picker, int oldVal, int newVal) {
					Log.d(CellarListCursorAdapter.class.getName(), "New val: " + newVal);
					if(wine.hasPrice()) {
						holder.priceView.setText(valueString + String.valueOf(newVal * wine.getPrice()));
						holder.cellar.setNoBottles(newVal);
						holder.modified = true;
					}
				}
			});

			TextView priceView = (TextView) convertView.findViewById(R.id.itemTotPrice);  
			TextView locationView = (TextView) convertView.findViewById(R.id.itemLocation);  
			TextView consumptionView = (TextView) convertView.findViewById(R.id.itemConsumption);  
			ImageView reminderView = (ImageView) convertView.findViewById(R.id.itemReminder);
			
			holder.numberPicker = numberPicker;  
			holder.priceView = priceView;  
			holder.locationView = locationView;  
			holder.reminderView = reminderView;
			holder.consumptionView = consumptionView;
			
			holder.reminderView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					holder.modified = true;

					if (!holder.cellar.hasReminder()) {
						DateTimeSlider d = new DateTimeSlider(
								inflator.getContext(), new OnDateSetListener() {

									@Override
									public void onDateSet(DateSlider view, Calendar selectedDate) {
										setReminder(selectedDate, holder);
									}
								}, Calendar.getInstance());
						d.show();
					} else {
						NotificationManager manger = (NotificationManager)applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
						manger.cancel(holder.cellar.getNotificationId());
						holder.reminderView.setBackgroundResource(R.drawable.add_bell);
						holder.cellar.setNotificationId(-1);
					}
				}
			});
			 
			convertView.setTag(holder);
			
			holders.put(position, holder);
		}
		
		Cursor c = getCursor();
		
		if (c.moveToPosition(position)) {
			int noBottles = c.getInt(2);
			String location = c.getString(3);
			long addedDate = c.getInt(5);
			long consumptionDate = c.getInt(6);
			
			holder.cellar = new Cellar(
					c.getInt(0), 
					wine, 
					noBottles, 
					location, 
					c.getString(4), 
					addedDate, 
					consumptionDate, 
					c.getInt(7));

			holder.numberPicker.setCurrent(noBottles);
			if (location != null) {
				holder.locationView.setText(location);
			}
			
			if (consumptionDate > 0) {
				holder.consumptionView.setText(ViewHelper.getDateAsString(new Date(consumptionDate)));
			}
			
			if(holder.cellar.hasReminder()) {
				holder.reminderView.setBackgroundResource(R.drawable.bell);
			} 
		}
		
		if(wine.hasPrice()) {
			holder.priceView.setText(valueString + String.valueOf(holder.numberPicker.getCurrent() * wine.getPrice()));
		}

		return convertView;
	}
	
	public void persist() {
		for(ViewHolder holder : holders.values()) {
			if(holder.modified) {
				
			}
		}
	}

	private void setReminder(Calendar cal, ViewHolder holder) {
		holder.reminderView.setBackgroundResource(R.drawable.bell);
		 
		//TODO DEBUG CODE
		cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 3);

		Intent alarmintent = new Intent(applicationContext, AlarmReceiver.class);
		alarmintent.putExtra("cellar", holder.cellar);
		alarmintent.putExtra("nextNotificationId", getNextNotificationId());
		alarmintent.putExtra("title", String.format(applicationContext.getString(R.string.reminderTitle), new Object[]{holder.cellar.getNoBottles(), wine.getName()}));
		
		StringBuilder builder = new StringBuilder();
		builder.append(String.format(applicationContext.getString(R.string.reminderNote), ViewHelper.getDateAsString(holder.cellar.getAddedDate())));
		
		if(holder.cellar.getStorageLocation() != null) {
			builder.append("\n");
			builder.append(String.format(applicationContext.getString(R.string.reminderNote1), holder.cellar.getStorageLocation()));
		}
		
		alarmintent.putExtra("note", builder.toString());

		//VERY IMPORTANT TO SET FLAG_UPDATE_CURRENT... this will send correct extra's informations to 
		PendingIntent sender = PendingIntent.getBroadcast(applicationContext, REMINDER_ID, alarmintent,PendingIntent.FLAG_UPDATE_CURRENT|Intent.FILL_IN_DATA);

		AlarmManager am = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);							
	}

	private int getNextNotificationId() {
		WineDatabaseHelper helper = new WineDatabaseHelper(applicationContext);
		return helper.getNextNotificationId();
	}

	private static class ViewHolder {  
        public NumberPicker numberPicker;  
        public TextView priceView; 
        public TextView locationView; 
        public TextView consumptionView;
		public ImageView reminderView;
		
		public Cellar cellar;
		
		public boolean modified = false;

    }

}
