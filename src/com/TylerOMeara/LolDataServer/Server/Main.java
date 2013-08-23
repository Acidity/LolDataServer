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
	public static String PvPNetVersion = "3.10.13_08_21_11_50";
	
	public static LoadBalancer loadBalancer = new LoadBalancer();
	
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
			//TODO: DEBUG CODE
			args = new String[2];

			//return;
		}
		int y = 1;
		//Loops through the arguments and creates a new client for each username password pair.
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
			
			LoadBalancer.registerNewClient(x);
			y++;
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
