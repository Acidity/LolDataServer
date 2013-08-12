package com.TylerOMeara.LolDataServer.Server;

import java.util.HashMap;

import com.gvaneyck.rtmp.LoLRTMPSClient;

public class Main 
{
	public static String PvPNetVersion = "3.10.13_07_26_19_59";
	
	/**
	 * Stores all of the clients provided by the server admin.
	 */
	public static HashMap<String, HashMap<String,LoLRTMPSClient>> clients = new HashMap<String, HashMap<String,LoLRTMPSClient>>();
	
	/**
	 * Main server method.
	 * @param args region::username::password
	 */
	
	public static void main(String[] args)
	{
		//TODO: Pull Version number from args
		//Check that the program has args provided.
		if(args.length == 0)
		{
			System.out.println("You must provide at least 1 username and password.");
			return;
		}
		
		//Loops through the arguments and creates a new client for each username password pair.
		int y = 1;
		for(String x : args)
		{
			String[] xsplit = x.split("::");
			
			//Verifies that the argument has a username and password.
			if(xsplit.length < 3)
			{
				System.out.println("Error with arg #" + y);
				System.out.println("Exiting...");
				return;
			}
			
			//Handles if other clients from same region exist.
			if(clients.containsKey(xsplit[0]))
			{
				HashMap<String, LoLRTMPSClient> region = clients.get(xsplit[0]);
				//Creates a new client object for this particular username/pass combo
				LoLRTMPSClient client = new LoLRTMPSClient(xsplit[0], PvPNetVersion, xsplit[1], xsplit[2]);
				//Adds client to region hashmap
				region.put(xsplit[1], client);
			}
			else //Handles first client for region.
			{
				//Creates a new hashmap to hold all clients for the region
				HashMap<String, LoLRTMPSClient> region = new HashMap<String, LoLRTMPSClient>();
				//Creates a new client object for this particular username/pass combo
				LoLRTMPSClient client = new LoLRTMPSClient(xsplit[0], PvPNetVersion, xsplit[1], xsplit[2]);
				//Adds client to region hashmap
				region.put(xsplit[1], client);
				//Adds region hashmap to global clients hashmap
				clients.put(xsplit[0], region);
			}
		}
	}
}
