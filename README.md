LolDataServer
=============

A server that communicates with Riot's data servers to obtain data relating to League of Legends and formats the data into JSON.
This is *not* designed to be a desktop application, and should be used as the backend for your own desktop application.

The server requires that you enter the username and password for 1 (or more) League accounts for 1 or more Server regions in order to function. You must therefore have at least 1 account for each region you wish to be able to retrieve information from.

API Documentation
=================

This API is subject to change.

The generic format for API requests is: "Region~Request~Arg1&Arg2&Arg3..."

The region is the Riot server platform from which you wish to retrieve the data from. A list of regions is provided below.
The request is the API call that you wish the server to perform. A list of requests is provided below.
The arguments are the necessary information required to specify what exactly you want. These differ for each API call and are listed with their specific call.

Regions
-------

**NA**    - North America  
**EUW**  - Europe West  
**EUNE**  - Europe Nordic & East  
**BR**    - Brazil  
**TR**    - Turkey  
**RU**    - Russia  
**LAN**   - Latin America North  
**LAS**  - Latin America South  
**OCE**   - Oceania  
**KR**    - Korea  
**PBE**   - Public Beta Environment  


*note:* Garena platforms are available, but untested and unsupported.

Requests
--------

This is a list of all available requests. The operator of the server to which you are connecting may have disabled 1 or more of these. Please note: SummonerID DOES NOT EQUAL AccountID. They are different numbers. The requests are **case-sensitive**, so make sure you capitalize them properly. A list of valid queues, seasons and gamemodes is provided below.

**getInGameProgressInfo**: String SummonerName - Retrieves various info for the game which the specified summoner is in.  
**getLeagueForPlayer**: int SummonerID, String queue - Retrieves the entire league for the specified player and queue.  
**getAllLeaguesForPlayer**: int SummonerID - Retrieves every league that the specified player is in.  
**getRecentGames**: int AccountID - Retrieves the last 10 games played by the specified summoner.  
**retrievePlayerStatsByAccountId**:  int AccountID - Returns various stats about the player.  
**getRankedStats**: int AccountID, String GameMode, String Season - Retrieves the ranked stats of the player for the specified game mode and season.  
**getSummonerByName**: String SummonerName - Retrieves information about the summoner such as level, xp, SummonerID, and Account ID (and more).  
**getSummonerNamesByIDs**: int SummonerID1, int SummonerID2... - Retrieves the corresponding summoner names for the specified IDs.  
**getAllPublicSummonerDataByAccount**: int AccountID - Retrieves "All public summoner data" for the account.  
**getAllSummonerDataByAccount**: int AccountID - Retrieves "All summoner data" for the account.  
**getPlayerRankedTeams**: int SummonerID - Gets all of the ranked teams for the player.  
**getAvailableQueues**: null - Gets all of the available queues for the region. It doesn't matter what you put for the argument as long as you put something.

Queues
------

RANKED_SOLO_5x5  
RANKED_TEAM_5x5  
RANKED_TEAM_3x3  

GameModes
---------

CLASSIC  
ODIN

Seasons
-------

You can also use the integer value for the season you want.

ONE  
TWO  
THREE  
CURRENT  
