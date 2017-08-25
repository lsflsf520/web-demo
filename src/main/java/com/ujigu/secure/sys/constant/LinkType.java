package com.ujigu.secure.sys.constant;

public enum LinkType {

	LINK((byte)0, "功能"),
	MENU((byte)1, "菜单");
	
	private byte dbCode;
	private String desc;
	private LinkType(byte dbCode, String desc){
		this.dbCode = dbCode;
		this.desc = desc;
	}
	
	public byte getDbCode() {
		return dbCode;
	}
	public String getDesc() {
		return desc;
	}
	
	public static LinkType getByDbCode(byte dbCode){
		for(LinkType state : LinkType.values()){
			if(state.getDbCode() == dbCode){
				return state;
			}
		}
		
		throw new IllegalArgumentException("NOT SUPPORTED PARAM");
	}
	
}
