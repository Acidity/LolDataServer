package com.TylerOMeara.LolDataServer.Server;

import java.io.IOException;

/**
 * Initializes a single new PvP.net client
 * @author Tyler O'Meara
 *
 */

public class PvPNetClientInitializationThread extends Thread
{
	private String x;
	public Integer tries = 1;
	
	/**
	 * Initializes a new thread for starting a PvP.net client
	 * @param x String that contains the region::username::password in that format
	 */
	public PvPNetClientInitializationThread(String x)
	{
		this.x = x;
	}
	
	/**
	 * Initialize the client, have it connect to Riot, and add it to the loadbalancer.
	 */
	public void run()
	{
		String[] xsplit = x.split("::");
		
		//Verifies that the argument has a username and password.
		if(xsplit.length < 3)
		{
			System.err.println("Error with argument " + x);
			LolDataServer.log.severe("Error with argument " + x);
			System.err.println("Ignoring that argument...");
			LolDataServer.log.severe("Ignoring that argument...");
			return;
		}
		
		//Continue attempting to reconnect but increase the time between attempts each time it fails.
		while(!addClient(x))
		{
			try {
				Thread.sleep(5000 * tries);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized(tries)
			{
				if(tries < 30)
				{
					tries++;
				}
			}
		}
	}
	
	/**
	 * Adds the specified client to the loadbalancer.
	 * @param x String that contains the region::username::password in that format x
	 * @return true if it was successfully added, false otherwise.
	 */
	public boolean addClient(String x)
	{
		try {
			LoadBalancer.registerNewClient(x);
		} catch (IOException e) {
			System.err.println("Had an error connecting to " + x.split("::")[0] + ": " + e.getMessage());
			System.err.println("Will continue to try to connect...");
			LolDataServer.log.warning("Had an error connecting to " + x.split("::")[0] + ": " + e.getMessage());
			LolDataServer.log.warning("Will continue to try to connect...");
			return false;
		}
		return true;
	}
}