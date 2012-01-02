package ws.wiklund.vinguiden.bolaget;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ws.wiklund.vinguiden.model.Country;
import ws.wiklund.vinguiden.model.Producer;
import ws.wiklund.vinguiden.model.Provider;
import ws.wiklund.vinguiden.model.Wine;
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
	

	public static Wine parseResponse(Document doc, String no) {
	    /*
         * Capture groups:
         * GROUP                    EXAMPLE DATA
         * 1: Name			        181 Merlot
         * 2: Type                  Rött vin
         * 3: ThumbUrl              /img/flags/usa.gif
         * 4: Country               USA
         * 5: Beverage facts 
         * 		Year					2008
         * 		Producer				181 Wine Cellars
         * 		Supplier				Hermansson & Co AB
         * 		Strength				14,0 %
         * 		Sugar level			Mindre än 5,0 gram/liter
         * 		Usage				Serveras vid cirka 16 grader till kyckling...
         * 		Taste				Kryddig, bärig smak med insla...
         * 		...
         * 6: Wine thumb			/ImageVaultFiles/id_15115/cf_399/528115.jpg
         */   			    
		Element productName = doc.select("span.produktnamnfet").first();
		
		Element type = doc.select("span.character > strong").first();
		Element country = doc.select("div.country > img").first();
		Element thumb = doc.select("div.image > img").first();
		
		Wine wine = new Wine(productName.text());
		wine.setNo(Integer.valueOf(no));
		wine.setType(getTypeFromString(type.text()));
		wine.setCountry(new Country(country.attr("alt"),country.attr("src")));
		wine.setThumb(thumb.attr("src"));

		updateBeverageFacts(wine, doc.select("ul.beverageFacts"));
		
		return wine;
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

}
