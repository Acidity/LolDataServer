package com.TylerOMeara.LolDataServer.Server;

import java.util.HashMap;

import com.gvaneyck.rtmp.LoLRTMPSClient;

public class LoadBalancer 
{
	public static HashMap<String, LoLRTMPSClient> clients = new HashMap<String, LoLRTMPSClient>();
	
	/**
	 * Returns a client from the specified region.
	 * @param region
	 * @return
	 */
	public static LoLRTMPSClient returnClient(String region)
	{
		return clients.get(region);
		//TODO: Actually loadbalance
	}
}
