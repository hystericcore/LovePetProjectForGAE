package com.lovepetproject;

public class PetVO {
	private String thumbnailSrc;
	private String linkSrc;
	private String boardID;
	private String date;
	private String type;
	private String sex;
	private String foundLocation;
	private String detail;
	private String state;
	public String getThumbnailSrc() {
		return thumbnailSrc;
	}
	public void setThumbnailSrc(String thumbnailSrc) {
		this.thumbnailSrc = thumbnailSrc;
	}
	public String getLinkSrc() {
		return linkSrc;
	}
	public void setLinkSrc(String linkSrc) {
		this.linkSrc = linkSrc;
	}
	public String getBoardID() {
		return boardID;
	}
	public void setBoardID(String boardID) {
		this.boardID = boardID;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getFoundLocation() {
		return foundLocation;
	}
	public void setFoundLocation(String foundLocation) {
		this.foundLocation = foundLocation;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
}
