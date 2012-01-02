package ws.wiklund.vinguiden.model;

import java.io.Serializable;

public class BaseModel implements Serializable {
	private static final long serialVersionUID = 7147299101780100916L;

	private int id = -1;
	
	private String name;

	public BaseModel(String name) {
		this(-1, name);
	}

	public BaseModel(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public boolean isNew() {
		return id == -1 && name != null;
	}
	
}
