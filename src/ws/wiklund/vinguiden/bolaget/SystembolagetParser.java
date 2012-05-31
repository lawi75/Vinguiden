package ws.wiklund.vinguiden.bolaget;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ws.wiklund.vinguiden.model.Country;
import ws.wiklund.vinguiden.model.Producer;
import ws.wiklund.vinguiden.model.Provider;
import ws.wiklund.vinguiden.model.Wine;
import ws.wiklund.vinguiden.model.WineType;
import ws.wiklund.vinguiden.util.ViewHelper;
import android.util.Log;

public class SystembolagetParser {
	public static final String BASE_URL = "http://www.systembolaget.se";

	private static final String YEAR = "Årgång";
	private static final String STRENGTH = "Alkoholhalt";
	private static final String USAGE = "Användning";
	private static final String TASTE = "Smak";
	private static final String PRODUCER = "Producent";
	private static final String PROVIDER = "Leverantör";
	private static final String SWEETNESS = "Sockerhalt";
	

	public static Wine parseResponse(String no) throws IOException {
		Document doc = Jsoup.connect(BASE_URL + "/" + no).get();
		
		if(isValidResponse(doc)) {
			Element productName = doc.select("span.produktnamnfet").first();
			
			Element type = doc.select("span.character > strong").first();
			Element country = doc.select("div.country > img").first();
			Element thumb = doc.select("div.image > img").first();
			
			Elements e = doc.select("td:contains(Pris)");
			Element price = null;
			if(e != null && !e.isEmpty()) {
				price = e.first().nextElementSibling();
			}
			
			Wine wine = new Wine(productName.text());
			wine.setNo(Integer.valueOf(no));
			wine.setType(getTypeFromString(type.text()));
			wine.setCountry(new Country(country.attr("alt"),country.attr("src")));
			wine.setThumb(thumb.attr("src"));
			
			if (price != null) {
				updatePrice(wine, price);
			}
			updateBeverageFacts(wine, doc.select("ul.beverageFacts"));
			
			return wine;
		}
		
		return null;
	}

	private static void updatePrice(Wine wine, Element price) {
		String p = null;

		try {
			if (price != null) {
				p = price.text();
				int idx = p.indexOf(" ");
				if (idx != -1) {
					p = p.substring(0, idx);					
					wine.setPrice(ViewHelper.getDoubleFromDecimalString(p));
				} else {
					Log.d(SystembolagetParser.class.getName(), "Invalid price tag[" + price.text() + "]");			
				}
			}
		} catch (NumberFormatException e) {
			Log.d(SystembolagetParser.class.getName(), "Failed to parse price data [" + p + "]");			
		}
	}

	private static void updateBeverageFacts(Wine wine, Elements beverageFacts) {
		for(Element e : beverageFacts) {
			Elements facts = e.select("li");
			for(Element fact : facts) {
				String key = fact.select("span").text();
				String data = fact.select("strong").text().replaceAll("[\\s\\u00A0]+$", "");
				
				updateBeverageFact(wine, key, data);
			}
			
		}
	}


	private static void updateBeverageFact(Wine wine, String key, String data) {
		if(key.equals(YEAR)) {
			wine.setYear(Integer.valueOf(data));
		} else if(key.equals(STRENGTH)) {
			wine.setStrength(getStrengthFromString(data));
		} else if(key.equals(USAGE)) {				
			wine.setUsage(data);
		} else if(key.equals(TASTE)) {				
			wine.setTaste(data);
		} else if(key.equals(PRODUCER)) {				
			wine.setProducer(new Producer(data));
		} else if(key.equals(PROVIDER)) {				
			wine.setProvider(new Provider(data));			
		} else if(key.equals(SWEETNESS)) {				
			//Not used
		} else {
			Log.d(SystembolagetParser.class.getName(), "Unused Beverage Fact [" +  key + ":" + data + "]");
		}
	}

	private static double getStrengthFromString(String data) {
		String percentData = data.substring(0, data.lastIndexOf("%") - 1).replace(',', '.');
		
		return Double.valueOf(percentData);
	}


	private static WineType getTypeFromString(String data) {
		
		WineType type = WineType.fromString(data);
		
		if(type.equals(WineType.OTHER)) {
			Log.d(SystembolagetParser.class.getName(), "Unknown wine type [" + data + "]");			
		}
		
		return type;
	}
	
	private static boolean isValidResponse(Document doc) {
		return doc.select("div.top_exception_message").first() == null;
	}
	

}
