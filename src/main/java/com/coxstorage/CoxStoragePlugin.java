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

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
		name = "CoX Storage"
)
public class CoxStoragePlugin extends Plugin
{
	@Inject
	private CoxStorageConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	private CoxStoragePanel panel;
	private NavigationButton navigationButton;


	@Override
	protected void startUp()
	{
		panel = new CoxStoragePanel();

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
		if (event.getContainerId() != 583 && event.getContainerId() != 33350)
		{
			return;
		}

		log.info("Container ID: {}", event.getContainerId());

		Item[] items = event.getItemContainer().getItems();

		for (int slot = 0; slot < items.length; slot++)
		{
			Item item = items[slot];

			log.info(
					"Slot: {}, Item ID: {}, Quantity: {}",
					slot,
					item.getId(),
					item.getQuantity()
			);
		}
	}

	@Provides
	CoxStorageConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CoxStorageConfig.class);
	}

}