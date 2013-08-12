package com.TylerOMeara.LolDataServer.Server;

import com.gvaneyck.rtmp.LoLRTMPSClient;

public class LoadBalancer 
{
	/**
	 * Returns a client from the specified region.
	 * @param region
	 * @return
	 */
	public LoLRTMPSClient returnClient(String region)
	{
		return Main.PvPNetClients.get(region).values().iterator().next();
		//TODO: Actually loadbalance
	}
}
