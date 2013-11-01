package com.TylerOMeara.LolDataServer.Server.Enums;

public enum Season {
	ONE(1),
	TWO(2),
	THREE(3),
	CURRENT(3);
	
	private int seasonInt;
	
	Season(int seasonInt)
	{
		this.seasonInt = seasonInt;
	}
	
	public int getSeasonInt()
	{
		return seasonInt;
	}
	
	public static String convertToInt(String s)
	{
		for(Season se : Season.values())
		{
			if(se.toString().equals(s))
			{
				return String.valueOf(se.getSeasonInt());
			}
		}
		//Checks if it is an, if so returns the string
		try{
			return String.valueOf(Integer.valueOf(s));
		}
		catch(NumberFormatException e)
		{
			return null;
		}
	}
}
