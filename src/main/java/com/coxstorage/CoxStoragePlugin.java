package com.coxstorage;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import javax.inject.Inject;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.game.ItemManager;
import net.runelite.api.Client;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetDrag;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.VarClientStrChanged;
import net.runelite.api.gameval.VarClientID;
import java.util.HashMap;
import java.util.Map;

import java.util.Objects;

@Slf4j
@PluginDescriptor(
		name = "CoX Storage"
)
public class CoxStoragePlugin extends Plugin
{
	private boolean wasDragging = false;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Client client;

	@Inject
	private CoxStorageConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	private CoxStoragePanel panel;
	private NavigationButton navigationButton;
	private Map<Integer, Integer> previousInventory = new HashMap<>();
	private String lastInput = "";
	private int lastDraggedOnIndex = -1;
	private boolean storageInitialized = false;

	private final Item[] simulatedStorage = new Item[120];
	private Map<Integer, Integer> getItemCounts(Item[] items)
	{
		Map<Integer, Integer> counts = new HashMap<>();

		for (Item item : items)
		{
			if (item.getId() == -1)
			{
				continue;
			}

			counts.merge(
					item.getId(),
					item.getQuantity(),
					Integer::sum
			);
		}

		return counts;
	}

	@Subscribe
	public void onVarClientStrChanged(VarClientStrChanged event)
	{
		if (event.getIndex() != VarClientID.MESLAYERINPUT)
		{
			return;
		}

		String input = client.getVarcStrValue(VarClientID.MESLAYERINPUT);

		if (!input.isEmpty())
		{
			lastInput = input;
		}
		else if (!lastInput.isEmpty())
		{
			log.info("{}", lastInput);
			lastInput = "";
		}
	}

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
		if (event.getContainerId() == 93)
		{
			log.info("--- INVENTORY UPDATE ---");

			Map<Integer, Integer> currentInventory =
					getItemCounts(event.getItemContainer().getItems());

			for (Integer itemId : currentInventory.keySet())
			{
				int oldQuantity = previousInventory.getOrDefault(itemId, 0);
				int newQuantity = currentInventory.get(itemId);

				if (oldQuantity != newQuantity)
				{
					log.info(
							"Inventory item {} changed: {} -> {}",
							itemId,
							oldQuantity,
							newQuantity
					);
				}
			}

			previousInventory = currentInventory;
			return;
		}

		if (event.getContainerId() == 583 && !storageInitialized)
		{
			Item[] items = event.getItemContainer().getItems();

			for (int i = 0; i < simulatedStorage.length; i++)
			{
				if (i < items.length)
				{
					simulatedStorage[i] = items[i];
				}
				else
				{
					simulatedStorage[i] = null;
				}
			}

			storageInitialized = true;

			panel.updatePrivateStorage(simulatedStorage);
		}
	}

	@Subscribe
	public void onWidgetDrag(WidgetDrag event)
	{
		Widget dragged = client.getDraggedWidget();
		Widget draggedOn = client.getDraggedOnWidget();

		if (dragged == null)
		{
			return;
		}
		int initialIndex = dragged.getIndex();
		int currentIndex;

		if (draggedOn != null)
		{
			currentIndex = draggedOn.getIndex();
		}
		else
		{
			currentIndex = dragged.getIndex();
		}

		if (currentIndex != lastDraggedOnIndex)
		{
			lastDraggedOnIndex = currentIndex;
			log.info("initial index: {} dragged onto index={}", initialIndex, currentIndex);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (Objects.equals(event.getMenuOption(), "Deposit all") ||
				Objects.equals(event.getMenuOption(), "Store") ||
				Objects.equals(event.getMenuOption(), "Store-5") ||
				Objects.equals(event.getMenuOption(), "Store-10") ||
				Objects.equals(event.getMenuOption(), "Store-X") ||
				Objects.equals(event.getMenuOption(), "Store-All") ||
				Objects.equals(event.getMenuOption(), "Withdraw all") ||
				Objects.equals(event.getMenuOption(), "Withdraw") ||
				Objects.equals(event.getMenuOption(), "Withdraw-5") ||
				Objects.equals(event.getMenuOption(), "Withdraw-10") ||
				Objects.equals(event.getMenuOption(), "Withdraw-X") ||
				Objects.equals(event.getMenuOption(), "Withdraw-All"))
		{
			log.info(
					"option={}, itemId={}, param0={}, param1={}",
					event.getMenuOption(),
					event.getItemId(),
					event.getParam0(),
					event.getParam1()
			);
        }
    }

	@Provides
	CoxStorageConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CoxStorageConfig.class);
	}

}