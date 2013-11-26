/*
 *  LolDataServer
 *  Copyright (C) 2013 Tyler O'Meara
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.TylerOMeara.LolDataServer.Server;

/**
 * Class for API Clients. Should not be instantiated, use one of it's children classes instead.
 * @author Tyler O'Meara
 *
 */
//TODO Comment code
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
