package co.idealwebsolutions.omgremote.model;

public final class GenericItem {
	
	private String title, description;
	private int id;
	
	public GenericItem(int id, String title, String description) {
		this.id = id;
		this.title = title;
		this.description = description;
	}
	
	public int getCode() {
		return id;
	}
	
	public void setCode(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

}
