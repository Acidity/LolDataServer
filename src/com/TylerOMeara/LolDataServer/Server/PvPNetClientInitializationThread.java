/*
 *  LolDataServer
 *  Copyright (C) 2013 Tyler O'Meara
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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