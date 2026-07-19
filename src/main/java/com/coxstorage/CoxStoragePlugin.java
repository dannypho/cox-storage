package com.coxstorage;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "CoX Storage"
)
public class CoxStoragePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private CoxStorageConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("CoX Storage started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("CoX Storage stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		// Nothing for now
	}

	@Provides
	CoxStorageConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CoxStorageConfig.class);
	}
}
