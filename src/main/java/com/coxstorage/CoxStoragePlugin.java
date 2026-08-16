package com.coxstorage;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import javax.inject.Inject;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
		name = "CoX Storage"
)
public class CoxStoragePlugin extends Plugin
{
	@Inject
	private ItemManager itemManager;

	@Inject
	private CoxStorageConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	private CoxStoragePanel panel;
	private NavigationButton navigationButton;


	@Override
	protected void startUp()
	{
		panel = new CoxStoragePanel(itemManager);

		navigationButton = NavigationButton.builder()
				.tooltip("CoX Storage")
				.icon(ImageUtil.loadImageResource(
						CoxStoragePlugin.class,
						"/cox_storage_icon.png"
				))
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown()
	{
		log.debug("CoX Storage stopped!");
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != 583)
		{
			return;
		}

		panel.updatePrivateStorage(
				event.getItemContainer().getItems()
		);
	}

	@Provides
	CoxStorageConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CoxStorageConfig.class);
	}

}