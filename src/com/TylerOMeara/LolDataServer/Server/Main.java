package com.TylerOMeara.LolDataServer.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import com.gvaneyck.rtmp.LoLRTMPSClient;

public class Main 
{
	/**
	 * Port on which the server should listen for connections.
	 */
	//TODO:Allow admins to change
	public static int serverPort = 22222;
	
	/**
	 * Stores the PvP.Net version that the clients should use when connecting to League's servers.
	 */
	//TODO:Make Variable based on region
	public static String PvPNetVersion = "3.10.13_07_26_19_59";
	
	/**
	 * Stores all of the PvP.Net clients provided by the server admin.
	 */
	public static HashMap<String, HashMap<String,LoLRTMPSClient>> PvPNetClients = new HashMap<String, HashMap<String,LoLRTMPSClient>>();
	
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
			if(PvPNetClients.containsKey(xsplit[0]))
			{
				HashMap<String, LoLRTMPSClient> region = PvPNetClients.get(xsplit[0]);
				//Creates a new client object for this particular username/pass combo
				LoLRTMPSClient client = new LoLRTMPSClient(xsplit[0], PvPNetVersion, xsplit[1], xsplit[2]);
				//Adds client to region hashmap
				region.put(xsplit[1], client);
				//Adds region hashmap to global clients hashmap
				PvPNetClients.put(xsplit[0], region);
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
				PvPNetClients.put(xsplit[0], region);
			}
		}
		
		//Creates server socket and server loop
		try 
		{
			ServerSocket ssocket = new ServerSocket(serverPort);
			Socket listen;
			//Main Server Loop, should never exit except upon program destruction
			while((listen = ssocket.accept()) != null)
			{
				NetworkingThread thread = new NetworkingThread(listen);
				thread.start();
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
