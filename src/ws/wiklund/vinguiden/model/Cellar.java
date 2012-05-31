package ws.wiklund.vinguiden.model;

import java.util.Date;

@TableName(name = "cellar")
public class Cellar extends BaseModel {
	private static final long serialVersionUID = 2332215573690089857L;

	private int id;
	private Wine wine;
	private int noBottles;
	private String storageLocation;
	private String comment;
	private Date addedDate;
	private Date consumptionDate;
	private int notificationId;

	public Cellar(int id, Wine wine, int noBottles, String storageLocation,
			String comment, long addedDate, long consumptionDate, int notificationId) {
		super(null);
		this.wine = wine;
		this.noBottles = noBottles;
		this.storageLocation = storageLocation;
		this.comment = comment;
		this.addedDate = new Date(addedDate);
		this.consumptionDate = new Date(consumptionDate);
		this.notificationId = notificationId;
	}

	public void setNoBottles(int noBottles) {
		this.noBottles = noBottles;
	}

	public int getNoBottles() {
		return noBottles;
	}

	public String getStorageLocation() {
		return storageLocation;
	}

	public Date getAddedDate() {
		return addedDate;
	}

	public boolean hasReminder() {
		return notificationId > 0;
	}

	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	public int getNotificationId() {
		return notificationId;
	}
	
}
