package ws.wiklund.vinguiden.util;

import java.util.ArrayList;
import java.util.List;

import ws.wiklund.guides.model.BeverageType;
import ws.wiklund.guides.model.BeverageTypes;

public class WineTypes implements BeverageTypes {
	private static final long serialVersionUID = 3885537634006722049L;
	private static final List<BeverageType> types = new ArrayList<BeverageType>();

	static {
		if(types.isEmpty()) {
			types.add(new BeverageType(100, "R�tt vin"));
			types.add(new BeverageType(200, "Vitt vin"));
			types.add(new BeverageType(300, "Ros�vin"));
			types.add(new BeverageType(400, "Mousserande vin, Vitt torrt"));
			types.add(new BeverageType(500, "Mousserande vin, halvtorrt"));
			types.add(new BeverageType(600, "Mousserande vin, Vitt s�tt"));
			types.add(new BeverageType(700, "Mousserande vin, Ros�"));
			types.add(new BeverageType(800, "Fruktvin"));
			types.add(new BeverageType(900, "Fruktvin, S�tt"));
			types.add(new BeverageType(950, "Fruktvin, Torrt"));
			types.add(new BeverageType(950, "Fruktvin, Torrt"));
			types.add(BeverageType.OTHER);
		}
	}
	
	@Override
	public List<BeverageType> getAllBeverageTypes() {
		return types;
	}
	
	@Override
	public BeverageType findTypeFromId(int id){
		for(BeverageType type : types) {
			if(type.getId() == id) {
				return type;
			}
		}
		
		return null;
	}

	@Override
	public BeverageType findTypeFromString(String text) {
		for(BeverageType type : types) {
			if(type.getName().equals(text)) {
				return type;
			}
		}
		
		return BeverageType.OTHER;
	}

	@Override
	public boolean useSubTypes() {
		return false;
	}
	
}
