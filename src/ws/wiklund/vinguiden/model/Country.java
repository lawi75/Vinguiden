package ws.wiklund.vinguiden.model;

@TableName(name = "country")
public class Country extends BaseModel {
	private static final long serialVersionUID = -856164118773294055L;

	private String thumbUrl;
	
	public Country(String name, String thumbUrl) {
		super(name);
		
		this.thumbUrl = thumbUrl;
	}

	public Country(int id, String name, String thumbUrl) {
		super(id, name);
		
		this.thumbUrl = thumbUrl;
	}

	@Column(name = "thumb_url")
	public String getThumbUrl() {
		return thumbUrl;
	}
	
}
