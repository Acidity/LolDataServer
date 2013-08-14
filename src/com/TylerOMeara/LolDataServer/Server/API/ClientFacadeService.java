package com.TylerOMeara.LolDataServer.Server.API;

public class ClientFacadeService 
{
	public static String getLoginDataPacketForUser(String region)
	{
		return BaseMethods.genericAPICall(region, "clientFacadeService", "getLoginDataPacketForUser", new Object[0]);
	}
}
