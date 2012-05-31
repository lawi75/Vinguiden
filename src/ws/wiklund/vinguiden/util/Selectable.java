package ws.wiklund.vinguiden.util;

public class Selectable {
	public static final int DELETE_ACTION = 3234;
	public static final int ADD_ACTION = 5435;
	public static final int REMOVE_ACTION = 1554;
	
	private String header;
	private int drawable;
	private int action;

	public Selectable(String header, int drawable, int action) {
		this.header = header;
		this.drawable = drawable;
		this.action = action;
	}

	public String getHeader() {
		return header;
	}

	public int getDrawable() {
		return drawable;
	}
	
	public int getAction() {
		return action;
	}

	@Override
	public String toString() {
		return "Selectable [header=" + header + ", drawable=" + drawable
				+ ", action=" + action + "]";
	}

}
