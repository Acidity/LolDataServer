package com.TylerOMeara.LolDataServer.Server;

/**
 * Represents an API client that has authenticated itself successfully with the server.
 * @author Tyler O'Meara
 *
 */

public class AuthenticatedUser extends User 
{
	private String group;
	private String key;
	
	public AuthenticatedUser(String name) 
	{
		super();
		setName(name);
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
