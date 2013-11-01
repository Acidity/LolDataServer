package com.TylerOMeara.LolDataServer.Server;

public class AnonymousUser extends User 
{
	public AnonymousUser() 
	{
		super();
		setRequestLimit(Main.defaultLimitAmount);
		setLimitTime(Main.defaultLimitTime);
	}
}
