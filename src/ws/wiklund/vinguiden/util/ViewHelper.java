package ws.wiklund.vinguiden.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.bolaget.SystembolagetParser;
import ws.wiklund.vinguiden.model.Country;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHelper {
	private final static DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
	private final static DecimalFormat decimalFormat = new DecimalFormat("#.#");
	private final static DecimalFormat currencyFormat = new java.text.DecimalFormat("SEK 0.00");
	
	private Context context;

	private static List<String> strengths = new ArrayList<String>();
	
	private static int lightVersion = 1;
	
	static {
		decimalFormat.setDecimalSeparatorAlwaysShown(true);
		decimalFormat.setParseBigDecimal(true);
	}
	
    public ViewHelper(Context context) {
    	this.context = context;
	}

    public boolean isLightVersion() {
    	return Integer.valueOf(context.getString(R.string.version_type)) == lightVersion;
    }
    
    public static void setText(TextView view, String value) {
		if (value != null) {
			view.setText(value);
		}
	}
    
    public void setThumbFromUrl(View view, String thumb) {
		new DownloadImageTask((ImageView) view.findViewById(R.id.Image_thumbUrl), SystembolagetParser.BASE_URL, 50, 100).execute(thumb);
	}
	
    public void setCountryThumbFromUrl(View view, Country country) {
		new DownloadImageTask((ImageView) view.findViewById(R.id.Image_country_thumbUrl), SystembolagetParser.BASE_URL, 29, 17).execute(country != null ? country.getThumbUrl() : null);
	}
    
	public static String getDateAsString(Date date) {
		return dateFormat.format(date);
	}

	public static String getDecimalStringFromNumber(Number value) {
		String s = decimalFormat.format(value);
				
		if(s.endsWith(String.valueOf(decimalFormat.getDecimalFormatSymbols().getDecimalSeparator()))) {
			return s.substring(0, s.length() - 1);
		}
		
		return s;
	}
	
	public static Double getDoubleFromDecimalString(String value) {
		try {
			BigDecimal bd = (BigDecimal) decimalFormat.parse(value);
			return bd.doubleValue();
		} catch (ParseException e) {
			Log.d(ViewHelper.class.getName(), "Failed to parse string (" + value + ")", e);
		}
		
		return -1d;
	}

	public static synchronized List<String> getStrengths() {
		if (strengths.isEmpty()) {
			for (Double i = 10.0; i <= 25.0; i += 0.1) {
				strengths.add(decimalFormat.format(i) + " %");
			}
		}
		
		return strengths;
	}

	public static String formatPrice(double price) {
		return currencyFormat.format(price);
	}

}
