package com.TylerOMeara.LolDataServer.Server.API;

public class ClientFacadeService 
{
	/**
	 * Not available in API
	 * @param region
	 * @return
	 */
	public static String getLoginDataPacketForUser(String region, boolean async)
	{
		return BaseMethods.genericAPICall(region, "clientFacadeService", "getLoginDataPacketForUser", async, new Object[0]);
	}
}
