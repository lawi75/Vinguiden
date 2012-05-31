package ws.wiklund.vinguiden.activities;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Wine;
import ws.wiklund.vinguiden.model.WineType;
import ws.wiklund.vinguiden.util.DownloadImageTask;
import ws.wiklund.vinguiden.util.ViewHelper;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class StatsActivity extends BaseActivity {
	private final String urlGoogleChart = "http://chart.apis.google.com/chart";
	private final String urlp3Api = "?chf=bg,s,65432100&cht=p3&chs=400x150&chl=";
	
	private WineDatabaseHelper helper;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);
        
        helper = new WineDatabaseHelper(this);
        
        List<Wine> wines = helper.getAllWines();
        
        //TODO History of wines
        
		TextView total = (TextView) findViewById(R.id.total);
		total.setText(String.format(getString(R.string.statsNow), 
				new Object[]{wines.size(), ViewHelper.getDecimalStringFromNumber(helper.getAverageRating())}));
		
		createChart();		
		
		TextView totalCellar = (TextView) findViewById(R.id.totalCellar);
		
		totalCellar.setText(String.format(getString(R.string.cellarStatsNow), 
				new Object[]{helper.getNoBottlesInCellar(), ViewHelper.formatPrice(helper.getCellarValue())}));
		
		
		TableLayout table = (TableLayout)findViewById(R.id.TableLayoutStats);
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		for(Wine wine : wines) {
			if(wine.hasBottlesInCellar()) {
				TableRow row = new TableRow(this);
				row.setLayoutParams(params);
				
				TextView amountView = new TextView(this);
				amountView.setPadding(0, 0, 10, 0);
				amountView.setText(String.format(getString(R.string.amount), wine.getBottlesInCellar()));
				row.addView(amountView);

				TextView yearView = new TextView(this);
				yearView.setPadding(0, 0, 10, 0);
				yearView.setText(String.valueOf(wine.getYear()));
				row.addView(yearView);

				TextView nameView = new TextView(this);
				nameView.setPadding(0, 0, 10, 0);
				nameView.setText(wine.getName());
				row.addView(nameView);

				table.addView(row);
			}
		}

    }
    
    private void createChart() {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(urlp3Api);

		List<WineType> types = helper.getAllAvailableWineTypes();

		StringBuilder values = new StringBuilder();
		
		Iterator<WineType> i = types.iterator();
		while(i.hasNext()) {
			WineType type = i.next();
			
			int amount = helper.getAllWinesForType(type);
						
			urlBuilder.append(URLEncoder.encode(type.toString())).append("(").append(amount).append(")");
			values.append(amount);
			
			if(i.hasNext()) {
				urlBuilder.append("|");
				values.append(",");
			} else {
				urlBuilder.append("&chd=t:");
			}				
		}
		
		urlBuilder.append(values.toString());

		new DownloadImageTask((ImageView)findViewById(R.id.pie), urlGoogleChart, 400, 150).execute(urlBuilder.toString());
	}

}
