package ws.wiklund.vinguiden.activities;

import java.util.ArrayList;
import java.util.List;

import ws.wiklund.guides.util.Selectable;
import ws.wiklund.guides.util.SelectableAdapter;
import ws.wiklund.guides.util.Sortable;
import ws.wiklund.guides.util.SortableAdapter;
import ws.wiklund.guides.util.ViewHelper;
import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.util.SelectableImpl;
import ws.wiklund.vinguiden.util.WineTypes;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public abstract class CustomListActivity extends ListActivity {
	private List<Sortable> sortableItems;

	private SortableAdapter sortableAdapter;
	private SelectableAdapter selectableAdapter;

	protected WineTypes wineTypes;

	private int currentPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.winelist);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		
		wineTypes = new WineTypes();

		if (!ViewHelper.isLightVersion(Integer.valueOf(getString(R.string.version_type)))) {
			View ad = findViewById(R.id.adView);
			if(ad != null) {
				ad.setVisibility(View.GONE);
			}
			
			View ad1 = findViewById(R.id.adView1);
			if(ad1 != null) {
				ad1.setVisibility(View.GONE);
			}
		}

		sortableItems = new ArrayList<Sortable>();
        
        sortableItems.add(new Sortable(
        		getString(R.string.sortOnName), 
        		getString(R.string.sortOnNameSub), 
        		R.drawable.descending, 
        		"beverage.name asc"));

        sortableItems.add(new Sortable(
        		getString(R.string.sortOnRank), 
        		getString(R.string.sortOnRankSub), 
        		R.drawable.rating, 
        		"beverage.rating desc"));

        sortableItems.add(new Sortable(
        		getString(R.string.sortOnType), 
        		getString(R.string.sortOnTypeSub), 
        		R.drawable.icon, 
        		"beverage.type asc"));
        
        if(!ViewHelper.isLightVersion(Integer.valueOf(getString(R.string.version_type)))) {
            sortableItems.add(new Sortable(
            		getString(R.string.sortOnCategory), 
            		getString(R.string.sortOnCategorySub), 
            		R.drawable.category, 
            		"category.name asc"));
        }
        
		sortableAdapter = new SortableAdapter(this, R.layout.spinner_row, sortableItems, getLayoutInflater());
		
		selectableAdapter = new SelectableAdapter(this, R.layout.spinner_row, getLayoutInflater()){
			public boolean isAvailableInCellar() {
				Cursor cursor = (Cursor) getListView().getItemAtPosition(currentPosition);
				return ViewHelper.getBeverageFromCursor(cursor).getBottlesInCellar() > 0;
			}
		};
		
		selectableAdapter.add(new SelectableImpl(getString(R.string.addToCellar), R.drawable.icon, Selectable.ADD_ACTION));
		selectableAdapter.add(new SelectableImpl(getString(R.string.removeFromCellar), R.drawable.from_cellar, Selectable.REMOVE_ACTION));
		selectableAdapter.add(new SelectableImpl(getString(R.string.deleteTitle), R.drawable.trash, Selectable.DELETE_ACTION));
	}
	
	public void addWine(View view) {
    	Intent intent = new Intent(view.getContext(), AddWineActivity.class);
    	startActivityForResult(intent, 0);
    }

	public void sortList(View view) {
		AlertDialog.Builder sortingDialog = new AlertDialog.Builder(this); 
		sortingDialog.setTitle(R.string.sort_list);
		sortingDialog.setSingleChoiceItems( sortableAdapter, 0, new OnClickListener() { 
	        @Override 
	        public void onClick(DialogInterface dialog, int which) { 
                dialog.dismiss();
                sort(sortableItems.get(which));
            }
        }); 

		sortingDialog.show(); 
	}
	
	protected void handleLongClick(final int position) {
		currentPosition = position;
		
		Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(cursor.getString(1));
		
		alertDialog.setSingleChoiceItems( selectableAdapter, 0, new OnClickListener() { 
            @Override 
            public void onClick(DialogInterface dialog, int which) { 
                dialog.dismiss();
                select(selectableAdapter.getItem(which), position);
            }
		}); 

		alertDialog.show(); 				
	}	

	abstract void sort(Sortable sortable);
	abstract void select(Selectable selectable, int position);
	
}
