package com.TylerOMeara.LolDataServer.Server;

import java.io.IOException;
import java.util.HashMap;

import com.gvaneyck.rtmp.LoLRTMPSClient;

public class LoadBalancer 
{
	/**
	 * Stores all of the PvP.Net clients provided by the server admin.
	 */
	public static HashMap<String, HashMap<String,LoLRTMPSClient>> PvPNetClients = new HashMap<String, HashMap<String,LoLRTMPSClient>>();
	
	/**
	 * Returns a client from the specified region.
	 * @param region
	 * @return
	 */
	public static LoLRTMPSClient returnClient(String region)
	{
		int numClients = PvPNetClients.get(region).values().size();
		LoLRTMPSClient client = (LoLRTMPSClient) PvPNetClients.get(region).values().toArray()[(int)(Math.random()*numClients)];
		System.out.println("Loadbalancer returned " + region + "::" + client.getUserName());
		return client;
	}
	
	public static void registerNewClient(String x)
	{
		String[] xsplit = x.split("::");
		//Creates a new client object for this particular username/pass combo
		LoLRTMPSClient client = new LoLRTMPSClient(xsplit[0], Main.PvPNetVersion, xsplit[1], xsplit[2]);
		
		HashMap<String, LoLRTMPSClient> region;
		//Handles if other clients from same region exist.
		if(PvPNetClients.containsKey(xsplit[0]))
		{
			region = PvPNetClients.get(xsplit[0]);
		}
		else //No other clients for this region
		{
			//Creates a new hashmap to hold all clients for the region
			region = new HashMap<String, LoLRTMPSClient>();
		}
		try 
		{
			client.connectAndLogin();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO DEBUG CODE
		System.out.println("Connected to " + xsplit[0] + " with username: " + xsplit[1]);
		
		//Adds client to region hashmap
		region.put(xsplit[1], client);
		//Adds region hashmap to global clients hashmap
		PvPNetClients.put(xsplit[0], region);
	}
}
