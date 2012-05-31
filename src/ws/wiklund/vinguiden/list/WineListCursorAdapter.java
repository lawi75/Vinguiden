package ws.wiklund.vinguiden.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.bolaget.SystembolagetParser;
import ws.wiklund.vinguiden.model.WineType;
import ws.wiklund.vinguiden.util.BitmapManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class WineListCursorAdapter extends SimpleCursorAdapter implements SectionIndexer {
	private LayoutInflater inflator;
	private Map<String, Integer> alphaIndexer;
	private String[] sections;

	public WineListCursorAdapter(Context context, Cursor c) {
		super(context, R.layout.item, c, new String[] {"thumb", "name", "country_id", "year", "rating"}, new int[] {android.R.id.icon, android.R.id.text1});

		alphaIndexer = new HashMap<String, Integer>();
		
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		BitmapManager.INSTANCE.setPlaceholder(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		ViewHolder holder;
		
		if (convertView == null) {  
			convertView = inflator.inflate(R.layout.item, null);
			
			 TextView titleView = (TextView) convertView.findViewById(R.id.itemTitle);  
	         TextView textView = (TextView) convertView.findViewById(R.id.itemText);  
	         TextView typeView = (TextView) convertView.findViewById(R.id.itemType);  
	         ImageView imageView = (ImageView) convertView.findViewById(R.id.itemImage);
	         RatingBar rating = (RatingBar) convertView.findViewById(R.id.itemRatingBar);

	         
	         holder = new ViewHolder();  
	         holder.titleView = titleView;  
	         holder.textView = textView;  
	         holder.imageView = imageView;
	         holder.rating = rating;
	         holder.typeView = typeView;
	         
	         convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag(); 
		}
		
		Cursor c = getCursor();
		
		if (c.moveToPosition(position)) {
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
			BitmapManager.INSTANCE.loadBitmap(url, holder.imageView, 25, 50);

			
			alphaIndexer.put(name.substring(0,1).toUpperCase(), position);
			
			Set<String> sectionLetters = alphaIndexer.keySet();
			 
		    // create a list from the set to sort
            List<String> sectionList = new ArrayList<String>(sectionLetters); 
            Collections.sort(sectionList);
 
            sections = new String[sectionList.size()];
 
            sectionList.toArray(sections);
		}
		
		return convertView;
	}
	
    private static class ViewHolder {  
        public RatingBar rating;
		public ImageView imageView;  
        public TextView titleView;  
        public TextView textView; 
        public TextView typeView;
    }

	@Override
	public int getPositionForSection(int section) {
		return alphaIndexer.get(sections[section]);
	}

	@Override
	public int getSectionForPosition(int position) {
		return 1;
	}

	@Override
	public Object[] getSections() {
		return sections;
	}
	
}
