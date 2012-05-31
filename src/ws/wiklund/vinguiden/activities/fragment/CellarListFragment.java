package ws.wiklund.vinguiden.activities.fragment;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.CellarProvider;
import ws.wiklund.vinguiden.list.CellarListCursorAdapter;
import ws.wiklund.vinguiden.model.Wine;
import ws.wiklund.vinguiden.util.ViewHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

public class CellarListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private View view;
	private Wine wine;
	private CellarListCursorAdapter adapter;

	public CellarListFragment(Wine wine) {
		Log.d(CellarListFragment.class.getName(), "wine: " + wine);
		this.wine = wine;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);

    	ViewHelper viewHelper = new ViewHelper(getActivity());

        if(!viewHelper.isLightVersion()) {
        	view.findViewById(R.id.adView).setVisibility(View.GONE);
        	view.findViewById(R.id.adView1).setVisibility(View.GONE);        	
        }
        
        getLoaderManager().initLoader(0, null, this);
        
        // Now create a new list adapter bound to the cursor.
        adapter = new CellarListCursorAdapter(getActivity(), wine);

        // Bind to our new adapter.
        setListAdapter(adapter);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		view = inflater.inflate(R.layout.cellarlist, container, false);
		
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.cellar_menu, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		FragmentActivity a = getActivity();
		TabHost tabHost = (TabHost) a.findViewById(android.R.id.tabhost);
		if(WineTabsFragment.TAB_CELLAR.equals(tabHost.getCurrentTabTag())) {
			menu.clear();
		    MenuInflater inflater = a.getMenuInflater();
			inflater.inflate(R.menu.cellar_menu, menu);
		}

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menuAddToCellar:
			ContentValues values = new ContentValues();
			values.put("wine_id", wine.getId());
			values.put("no_bottles", 1);
			values.put("added_to_cellar", SystemClock.elapsedRealtime());
			getActivity().getContentResolver().insert(CellarProvider.CONTENT_URI, values);
			
			Log.d(CellarListFragment.class.getName(), "Added one bottle of " + wine.getName() + " to cellar");
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), CellarProvider.CONTENT_URI, null, "wine_id = ?", new String[]{String.valueOf(wine.getId())}, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.changeCursor(cursor);

        if(adapter.isEmpty()) {
        	//showAddToCellarDialog();
        }
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.changeCursor(null);		
	}

	@Override
	public void onStop() {
		adapter.persist();
		super.onStop();
	}

	public void advanced(View view) {    	
    }

}
