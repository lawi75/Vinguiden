package ws.wiklund.vinguiden.model;

@TableName(name = "producer")
public class Producer extends BaseModel {
	private static final long serialVersionUID = 9093370062883369060L;

	public Producer(String name) {
		super(name);
	}

	public Producer(int id, String name) {
		super(id, name);
	}
	
}
