package ws.wiklund.vinguiden.activities.fragment;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.CellarProvider;
import ws.wiklund.vinguiden.model.Wine;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class WineTabsFragment extends Fragment implements OnTabChangeListener {
	static final String TAB_WINE = "wine";
	static final String TAB_CELLAR = "cellar";
	private static final int WINE_TAB = 0;
	private static final int CELLAR_TAB = 1;

	private View root;
	private TabHost tabHost;
	private static int currentTab;
	private Wine wine;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.wine_tabs, null);
		tabHost = (TabHost) root.findViewById(android.R.id.tabhost);

		wine = (Wine) getActivity().getIntent().getSerializableExtra("ws.wiklund.vinguiden.activities.Wine");
		
		setupTabs();
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);

		tabHost.setOnTabChangedListener(this);
		tabHost.setCurrentTab(currentTab);

		// manually start loading stuff in the first tab
		updateTab(TAB_WINE, R.id.wine_tab);
	}

	private void setupTabs() {
		tabHost.setup(); // you must call this before adding your tabs!

		tabHost.addTab(newTab(TAB_WINE, R.id.wine_tab));
		//tabHost.addTab(newTab(TAB_CELLAR, R.id.cellar_tab));
	}

	private TabSpec newTab(String tag, int tabContentId) {
		Log.d(WineTabsFragment.class.getName(), "buildTab(): tag=" + tag);

		View indicator = LayoutInflater.from(getActivity()).inflate(
				R.layout.tab, (ViewGroup) root.findViewById(android.R.id.tabs),
				false);

		if (TAB_WINE.equals(tag)) {
			((TextView) indicator.findViewById(R.id.text)).setText(wine.getName());
		} else if (TAB_CELLAR.equals(tag)) {
			setCellarText(indicator);
		}
		
		TabSpec tabSpec = tabHost.newTabSpec(tag);
		tabSpec.setIndicator(indicator);
		tabSpec.setContent(tabContentId);
		return tabSpec;
	}

	private void setCellarText(View indicator) {
		Cursor c = getActivity().getContentResolver().query(CellarProvider.CONTENT_URI, null, "wine_id = ?", new String[]{String.valueOf(wine.getId())}, null);
		
		String noBottles = null;
		if(c.moveToFirst()) {
			noBottles = " (" + c.getInt(2) + ")";
		}

		((TextView) indicator.findViewById(R.id.text)).setText(getActivity().getString(R.string.cellar) + (noBottles != null ? noBottles : ""));
	}

	@Override
	public void onTabChanged(String tabId) {
		Log.d(WineTabsFragment.class.getName(), "onTabChanged(): tabId=" + tabId);
		if (TAB_WINE.equals(tabId)) {
			updateTab(tabId, R.id.wine_tab);
			currentTab = WINE_TAB;
			return;
		}

		if (TAB_CELLAR.equals(tabId)) {
			updateTab(tabId, R.id.cellar_tab);
			currentTab = CELLAR_TAB;
			return;
		}
	}

	private void updateTab(String tabId, int placeholder) {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(tabId) == null) {
			fm.beginTransaction().replace(placeholder, tabId.equals(TAB_WINE) ? new WineFragment(wine) : new CellarListFragment(wine), tabId).commit();
		}
	}

}
