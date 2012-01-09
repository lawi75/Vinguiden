package ws.wiklund.vinguiden.model;

@TableName(name = "category")
public class Category extends BaseModel {
	private static final long serialVersionUID = 1163582987915361095L;

	public static final int NEW_ID = -453543;

	public Category(String name) {
		super(name);
	}

	public Category(int id, String name) {
		super(id, name);
	}

}
