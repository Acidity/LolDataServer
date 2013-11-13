package com.TylerOMeara.LolDataServer.Server;

/**
 * Class for API Clients. Should not be instantiated, use one of it's children classes instead.
 * @author Tyler O'Meara
 *
 */
public class User 
{
	private String ipAddress;
	private String name;
	private int requestLimit;
	private long limitTime;
	private long limitCheckTime;
	private int currentRequests;
	
	public int getCurrentRequests() {
		return currentRequests;
	}
	public void setCurrentRequests(int currentRequests) {
		this.currentRequests = currentRequests;
	}
	public void iterateCurrentRequests()
	{
		currentRequests++;
	}
	public long getLimitCheckTime() {
		return limitCheckTime;
	}
	public void setLimitCheckTime(long limitCheckTime) {
		this.limitCheckTime = limitCheckTime;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRequestLimit() {
		return requestLimit;
	}
	public void setRequestLimit(int requestLimit) {
		this.requestLimit = requestLimit;
	}
	public long getLimitTime() {
		return limitTime;
	}
	public void setLimitTime(long limitTime) {
		this.limitTime = limitTime;
	}
	
	public User()
	{
		
	}
}
