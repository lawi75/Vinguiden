package ws.wiklund.vinguiden.activities;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.bolaget.WineType;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Category;
import ws.wiklund.vinguiden.model.Country;
import ws.wiklund.vinguiden.util.DownloadImageTask;
import ws.wiklund.vinguiden.util.PayPalFactory;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalPayment;
import com.quietlycoding.android.picker.NumberPickerDialog;
import com.quietlycoding.android.picker.NumberPickerDialog.OnNumberSetListener;

public class BaseActivity extends Activity {
	private final DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
	private final DecimalFormat decimalFormat = new DecimalFormat("#.#");
	
	private Calendar calendar = Calendar.getInstance();
	private List<Integer> years = new ArrayList<Integer>();
	private List<WineType> types = new ArrayList<WineType>();
	private List<String> strengths = new ArrayList<String>();
	
	private static Set<Category> categories = new HashSet<Category>();

	private WineDatabaseHelper helper;
	
	public static int lightVersion = 1; 

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		decimalFormat.setDecimalSeparatorAlwaysShown(true);
		decimalFormat.setParseBigDecimal(true);

		helper = new WineDatabaseHelper(this);		
    }
    
    public void skip(View view) {    	
    	Intent intent = new Intent(getApplicationContext(), WineListActivity.class);
    	startActivityForResult(intent, 0);
    	finish();
    }

    protected void setText(TextView view, String value) {
		if (value != null) {
			view.setText(value);
		}
	}
    
    protected void setThumbFromUrl(String thumb) {
		new DownloadImageTask((ImageView) findViewById(R.id.Image_thumbUrl), 50, 100).execute(thumb);
	}
	
    protected void setCountryThumbFromUrl(Country country) {
		new DownloadImageTask((ImageView) findViewById(R.id.Image_country_thumbUrl), 29, 17).execute(country != null ? country.getThumbUrl() : null);
	}

    protected boolean isLightVersion() {
    	return Integer.valueOf(getString(R.string.version)) == lightVersion;
    }
    
	protected synchronized Set<Category> getCategories() {
		if(categories.isEmpty()) {
			categories.add(new Category(""));
			categories.add(new Category(Category.NEW_ID, getString(R.string.newStr)));
		}
		
		List<Category> c = helper.getCategories();
		
		if (c != null && !c.isEmpty()) {
			categories.addAll(c);
		}
		
		//TODO possibility to remove categories
		//TODO dialog if new is selected
		
		return categories;
	}
	
	protected CheckoutButton getCheckoutButton() {
		return getCheckoutButton(PayPal.BUTTON_152x33);
	}
	
	protected CheckoutButton getCheckoutButton(int btnSize) {
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.span = 2;
		params.topMargin = 10;
		
		PayPal payPal = PayPalFactory.getPayPal();
		
		CheckoutButton btn = null;
		if (payPal != null) {
			btn = payPal.getCheckoutButton(this, btnSize,
					CheckoutButton.TEXT_DONATE);
			btn.setLayoutParams(params);
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					donate();
				}
			});
		}
		
		return btn;
	}
	
	public Double getDoubleFromDecimalString(String value) {
		try {
			BigDecimal bd = (BigDecimal) decimalFormat.parse(value);
			return bd.doubleValue();
		} catch (ParseException e) {
			Log.d(ModifyWineActivity.class.getName(), "Failed to parse strength(" + value + ")", e);
		}
		
		return -1d;
	}
    
	public String getDecimalStringFromNumber(Number value) {
		String s = decimalFormat.format(value);
				
		if(s.endsWith(String.valueOf(decimalFormat.getDecimalFormatSymbols().getDecimalSeparator()))) {
			return s.substring(0, s.length() - 1);
		}
		
		return s;
	}
	
	public int getCurrentYear() {
		return calendar.get(Calendar.YEAR);
	}
	
	public String getDataAsString(Date date) {
		return dateFormat.format(date);
	}

	public synchronized List<Integer> getYears() {
		if(years.isEmpty()) {
			for(int i = 1900; i<= calendar.get(Calendar.YEAR); i++) {
				years.add(i);
			}	
		}
		
		return years;
	}

	public synchronized List<WineType> getTypes() {
		if (types.isEmpty()) {
			for (WineType wineType : WineType.values()) {
				types.add(wineType);
			}
		}
		
		return types;
	}

	public synchronized List<String> getStrengths() {
		if (strengths.isEmpty()) {
			for (Double i = 10.0; i <= 25.0; i += 0.1) {
				strengths.add(decimalFormat.format(i) + " %");
			}
		}
		
		return strengths;
	}
	
	private void donate() {
		NumberPickerDialog pickerDialog = new NumberPickerDialog(this, -1, 20);
		pickerDialog.setTitle(getString(R.string.donateTitle));
		pickerDialog.setOnNumberSetListener(new OnNumberSetListener() {
			@Override
			public void onNumberSet(int selectedNumber) {
				PayPalPayment payment = new PayPalPayment();

				payment.setSubtotal(new BigDecimal(selectedNumber));

				payment.setCurrencyType("SEK");

				payment.setRecipient("vinguiden@wiklund.ws");

				payment.setPaymentType(PayPal.PAYMENT_TYPE_PERSONAL);

				Intent checkoutIntent = PayPal.getInstance().checkout(payment, BaseActivity.this);

				startActivityForResult(checkoutIntent, 1);					
			}
		});
		
		pickerDialog.show();			
    }
	
}
