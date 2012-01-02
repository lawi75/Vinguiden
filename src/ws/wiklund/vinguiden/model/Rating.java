package ws.wiklund.vinguiden.model;

public enum Rating {
	UNRATED(-1),
	HALF(0.5f),
	ONE(1),
	ONE_HALF(1.5f),
	TWO(2),
	TWO_HALF(2.5f),
	THREE(3),
	THREE_HALF(3.5f),
	FOUR(4),
	FOUR_HALF(4.5f),
	FIVE(5);
	
	float rating;
	
	private Rating(float rating) {
		this.rating = rating;
	}
	
	public float getRating() {
		return rating;
	}
	
	public static Rating fromFloat(float i) {
		for(Rating r : values()) {
			if(r.rating == i) {
				return r;
			}
		}
		
		return UNRATED;
	}
	
}
