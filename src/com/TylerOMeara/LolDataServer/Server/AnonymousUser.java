package com.TylerOMeara.LolDataServer.Server;

/**
 * Class that represents an API client that is not authenticated.
 * @author Tyler O'Meara
 *
 */

public class AnonymousUser extends User 
{
	public AnonymousUser() 
	{
		super();
		setRequestLimit(LolDataServer.defaultLimitAmount);
		setLimitTime(LolDataServer.defaultLimitTime);
	}
}
