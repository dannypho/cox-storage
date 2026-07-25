package com.coxstorage;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "CoX Storage"
)
public class CoxStoragePlugin extends Plugin
{
	@Override
	protected void startUp()
	{
		log.debug("CoX Storage started!");
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
}