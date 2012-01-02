package ws.wiklund.vinguiden.model;

import java.util.Date;

import ws.wiklund.vinguiden.bolaget.WineType;

@TableName(name = "wine")
public class Wine extends BaseModel {
	private static final long serialVersionUID = 8130320547884807505L;
	
	private int no = -1;
	private WineType type;
	private String thumb;
	private Country country;
	private int year = -1;
	private Producer producer;
	private double strength = -1;
	private String usage;
	private String taste;
	private Provider provider;
	private float rating = -1;
	private Date added;

	private String comment;
	
	public Wine() {
		this(null);
	}
	
	public Wine(String name) {
		this(-1, name);
	}

	public Wine(int id, String name) {
		super(id, name);
	}

	public Wine(int id, String name, int no, WineType type, String thumb,
			Country country, int year, Producer producer, double strength,
			String usage, String taste, Provider provider, float rating, String comment, Date added) {
		this(id, name);
		
		this.no = no;
		this.type = type;
		this.thumb = thumb;
		this.country = country;
		this.year = year;
		this.producer = producer;
		this.strength = strength;
		this.usage = usage;
		this.taste = taste;
		this.provider = provider;
		this.rating = rating;
		this.comment = comment;
		this.added = added;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}
	
	public WineType getType() {
		return type;
	}

	public void setType(WineType type) {
		this.type = type;
	}

	public String getThumb() {
		return thumb;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public Producer getProducer() {
		return producer;
	}

	public void setProducer(Producer producer) {
		this.producer = producer;
	}

	public double getStrength() {
		return strength;
	}

	public void setStrength(double strength) {
		this.strength = strength;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getTaste() {
		return taste;
	}

	public void setTaste(String taste) {
		this.taste = taste;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public float getRating() {
		return rating;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getAdded() {
		return added;
	}

	public void setAdded(Date added) {
		this.added = added;
	}

	@Override
	public String toString() {
		return "Wine [no=" + no + ", type=" + type + ", thumb=" + thumb
				+ ", country=" + country + ", year=" + year + ", producer="
				+ producer + ", strength=" + strength + ", usage=" + usage
				+ ", taste=" + taste + ", provider=" + provider + ", rating="
				+ rating + ", added=" + added + ", comment=" + comment
				+ ", getId()=" + getId() + ", getName()=" + getName()
				+ ", isNew()=" + isNew();
	}
	
	

}
