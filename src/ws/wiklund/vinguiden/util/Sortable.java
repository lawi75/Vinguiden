package ws.wiklund.vinguiden.util;

public class Sortable {
	private String header;
	private String sub;
	private int drawable;

	private String sortColumn;

	public Sortable(String header, String sub, int drawable, String sortColumn) {
		this.header = header;
		this.sub = sub;
		this.drawable = drawable;
		this.sortColumn = sortColumn;
	}

	public String getHeader() {
		return header;
	}

	public String getSub() {
		return sub;
	}

	public int getDrawable() {
		return drawable;
	}

	public String getSortColumn() {
		return sortColumn;
	}

	@Override
	public String toString() {
		return "Sortable [header=" + header + ", sub=" + sub + ", drawable="
				+ drawable + ", sortColumn=" + sortColumn + "]";
	}

}
