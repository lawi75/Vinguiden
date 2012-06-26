package ws.wiklund.vinguiden.util;

import ws.wiklund.vinguiden.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class SelectableAdapter extends ArrayAdapter<Selectable>{
	private LayoutInflater layoutInflater;
	
	public SelectableAdapter(Context context, int textViewResourceId, LayoutInflater layoutInflater) {
		super(context, textViewResourceId);
		
		this.layoutInflater = layoutInflater;
		
		add(new Selectable(
				context.getString(R.string.addToCellar), 
	    		R.drawable.icon, Selectable.ADD_ACTION));

		add(new Selectable(
				context.getString(R.string.removeFromCellar), 
	    		R.drawable.from_cellar, Selectable.REMOVE_ACTION));

		add(new Selectable(
				context.getString(R.string.deleteTitle), 
	    		R.drawable.trash, Selectable.DELETE_ACTION));
	
	}
	          
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public boolean areAllItemsEnabled() {
        return false;
    }

	@Override
    public boolean isEnabled(int position) {
		Selectable item = getItem(position);
    	if(item.getAction() == Selectable.REMOVE_ACTION) {
    		return isAvailableInCellar();
    	}
    	
		return true;
    }
    
	public abstract boolean isAvailableInCellar();

	public View getCustomView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		
		if(row == null) {
			row=layoutInflater.inflate(R.layout.spinner_row, parent, false);
		}
		
		Selectable s = getItem(position);
		
		TextView label=(TextView)row.findViewById(R.id.spinner_header);
		label.setText(s.getHeader());
		ImageView icon=(ImageView)row.findViewById(R.id.spinner_image);
		icon.setImageResource(s.getDrawable());
	    
		if (s.getAction() == Selectable.REMOVE_ACTION && !isEnabled(position)) {
			label.setTextColor(Color.GRAY);
			row.setBackgroundColor(Color.LTGRAY);
		} else {
			label.setTextColor(Color.BLACK);
			row.setBackgroundColor(Color.WHITE);
		}
		
		return row;		    
	}

}
