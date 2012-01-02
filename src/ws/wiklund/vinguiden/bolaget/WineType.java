package ws.wiklund.vinguiden.bolaget;

import java.io.Serializable;

public enum WineType implements Serializable {
	RED(100, "R�tt vin"),
	WHITE(200, "Vitt vin"),
	ROSE(300, "Ros�vin"),
	SPARKLING(400, "Mousserande vin, Vitt torrt"),
	SPARKLING_HALF_DRY(500, "Mousserande vin, halvtorrt"),
	SPARKLING_SWEET(600, "Mousserande vin, Vitt s�tt"),
	SPARKLING_ROSE(700, "Mousserande vin, Ros�"),
	FRUIT(800, "Fruktvin"),
	FRUIT_SWEET(900, "Fruktvin, S�tt"),
	FRUIT_DRY(950, "Fruktvin, Torrt"),
	OTHER(999, "�vriga");
	
	private int id = -1;
	private String type = null;

	WineType(int id, String type) {
		this.id = id;		
		this.type = type;		
	}
	
	public int getId() {
		return id;
	}
	
	public static WineType fromId(int id) {
	    if (id != -1) {
	    	for (WineType wineType : WineType.values()) {
	    		if (id == wineType.id) {
	    			return wineType;
	    		}
	    	}   	
	    }
	   
	    return OTHER;
	}
	
	public static WineType fromString(String type) {
	    if (type != null) {
	    	for (WineType wineType : WineType.values()) {
	    		if (type.equals(wineType.type)) {
	    			return wineType;
	    		}
	    	}   	
	    }
	   
	    return OTHER;
	}
	
	@Override
	public String toString() {
		return type;
	}
	
}
