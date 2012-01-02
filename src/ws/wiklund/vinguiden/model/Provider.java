package ws.wiklund.vinguiden.model;

@TableName(name = "provider")
public class Provider extends BaseModel {
	private static final long serialVersionUID = 3934095154684537963L;

	public Provider(String name) {
		super(name);
	}

	public Provider(int id, String name) {
		super(id, name);
	}
	
}
