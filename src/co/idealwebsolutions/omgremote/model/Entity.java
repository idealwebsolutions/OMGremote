package co.idealwebsolutions.omgremote.model;

/**
 * Defines a talking entity
 */

public final class Entity {
	
	private String name = "";
	private boolean typing = false;
	
	public Entity(String name, boolean typing) {
		this.name = name;
		this.typing = typing;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isTyping() {
		return typing;
	}
	
	public void setTyping(boolean typing) {
		this.typing = typing;
	}

}
