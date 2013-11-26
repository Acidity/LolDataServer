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

package com.TylerOMeara.LolDataServer.Server.API;

public class SummonerService 
{
	public static String getSummonerByName(String region, String name, boolean async)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getSummonerByName", async, new Object[] {name});
	}
	
	public static String getSummonersByIDs(String region, Object[] summonerIDs, boolean async)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getSummonerNames", async, new Object[]{summonerIDs});
	}
	
	public static String getAllPublicSummonerDataByAccount(String region, int accountID, boolean async)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getAllPublicSummonerDataByAccount", async, new Object[]{accountID});
	}
	
	public static String getAllSummonerDataByAccount(String region, int accountID, boolean async)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getAllSummonerDataByAccount", async, new Object[]{accountID});
	}
}
