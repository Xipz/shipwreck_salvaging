package com.shipwreck_salvaging;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ShipwreckSalvaging
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(com.shipwrecksalvaging.ShipwreckSalvagingPlugin.class);
		RuneLite.main(args);
	}
}