package co.idealwebsolutions.omgremote.model;

public final class ChatObject {
	
	private String timestamp, chatLine;
	private boolean alert, questionMode;
	
	private Entity entity;
	
	public ChatObject(String timestamp, String chatLine, Entity entity, boolean alert, boolean questionMode) {
		this.timestamp = timestamp;
		this.chatLine = chatLine;
		this.entity = entity;
		this.alert = alert;
		this.questionMode = questionMode;
	}
	
	public ChatObject(int[] timestamp, String chatLine, Entity entity, boolean alert, boolean questionMode) {
		this.setTimestamp(timestamp[0], timestamp[1], timestamp[2]);
		this.chatLine = chatLine;
		this.entity = entity;
		this.alert = alert;
		this.questionMode = questionMode;
	}
	
	public void setTimestamp(int hour, int minute, int seconds) {
		if(hour >= 0 && hour < 10) {
			this.timestamp = "0" + hour + ":" + minute + ":" + seconds;
		}
		if(minute >= 0 && minute < 10) {
			this.timestamp = hour + ":0" + minute + ":" + seconds;
		}
		if(seconds >= 0 && seconds < 10) {
			this.timestamp = hour + ":" + minute + ":0" + seconds;
		} else {
			this.timestamp = hour + ":" + minute + ":" + seconds;
		}
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setChatline(String chatLine) {
		this.chatLine = chatLine;
	}
	
	public String getChatline() {
		if(entity != null)
			return entity.getName() + chatLine;
		return chatLine;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	
	public void setAlert(boolean alert) {
		this.alert = alert;
	}
	
	public boolean isAlert() {
		return alert;
	}
	
	public void setInQuestionMode(boolean questionMode) {
		this.questionMode = questionMode;
	}
	
	public boolean isInQuestionMode() {
		return questionMode;
	}
	
	@Override
	public String toString() {
		return getTimestamp() + " " + getChatline();
	}

}
