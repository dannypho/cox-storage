package com.coxstorage;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
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
import net.runelite.api.events.WidgetDrag;
import net.runelite.api.widgets.Widget;
import net.runelite.api.events.VarClientStrChanged;
import net.runelite.api.gameval.VarClientID;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.Queue;

import java.util.Objects;

@Slf4j
@PluginDescriptor(
		name = "CoX Storage"
)



public class CoxStoragePlugin extends Plugin
{

	private static class PendingStore
	{
		private final int itemId;
		private final int inventorySlot;

		private PendingStore(int itemId, int inventorySlot)
		{
			this.itemId = itemId;
			this.inventorySlot = inventorySlot;
		}
	}

	private static class PendingWithdraw
	{
		private final int itemId;
		private final int storageSlot;

		private PendingWithdraw(int itemId, int storageSlot)
		{
			this.itemId = itemId;
			this.storageSlot = storageSlot;
		}
	}

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
	private boolean storageInitialized = false;
	private final Queue<PendingStore> pendingStores = new ArrayDeque<>();
	private final Queue<PendingWithdraw> pendingWithdraws = new ArrayDeque<>();
	private final Item[] simulatedStorage = new Item[120];

	private int findFirstEmptyStorageSlot()
	{
		for (int i = 0; i < simulatedStorage.length; i++)
		{
			Item item = simulatedStorage[i];

			if (item == null || item.getId() == -1)
			{
				return i;
			}
		}

		return -1;
	}

	private int findStorageItemSlot(int itemId)
	{
		for (int i = 0; i < simulatedStorage.length; i++)
		{
			Item item = simulatedStorage[i];

			if (item != null && item.getId() == itemId)
			{
				return i;
			}
		}

		return -1;
	}

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

	private void swapStorageItems(int fromIndex, int toIndex)
	{
		Item temp = simulatedStorage[fromIndex];

		simulatedStorage[fromIndex] = simulatedStorage[toIndex];
		simulatedStorage[toIndex] = temp;

		panel.updatePrivateStorage(simulatedStorage);
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
		clientToolbar.removeNavigation(navigationButton);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == 93)
		{
			log.info("--- INVENTORY UPDATE ---");

			Map<Integer, Integer> currentInventory =
					getItemCounts(event.getItemContainer().getItems());

			Map<Integer, Integer> confirmedStores = new HashMap<>();
			Map<Integer, Integer> confirmedWithdraws = new HashMap<>();

			for (Integer itemId : previousInventory.keySet())
			{
				int oldQuantity = previousInventory.getOrDefault(itemId, 0);
				int newQuantity = currentInventory.getOrDefault(itemId, 0);

				if (newQuantity < oldQuantity)
				{
					log.info(
							"Inventory item {} changed: {} -> {}",
							itemId,
							oldQuantity,
							newQuantity
					);

					confirmedStores.put(
							itemId,
							oldQuantity - newQuantity
					);
				}
			}

			for (Integer itemId : currentInventory.keySet())
			{
				int oldQuantity = previousInventory.getOrDefault(itemId, 0);
				int newQuantity = currentInventory.getOrDefault(itemId, 0);

				if (newQuantity > oldQuantity)
				{
					log.info(
							"Confirmed inventory increase: itemId={}, {} -> {}",
							itemId,
							oldQuantity,
							newQuantity
					);

					confirmedWithdraws.put(
							itemId,
							newQuantity - oldQuantity
					);
				}
			}

			Queue<PendingStore> remainingStores = new ArrayDeque<>();
			Queue<PendingWithdraw> remainingWithdraws = new ArrayDeque<>();

			while (!pendingStores.isEmpty())
			{
				PendingStore pendingStore = pendingStores.poll();

				int itemId = pendingStore.itemId;

				int confirmedAmount =
						confirmedStores.getOrDefault(itemId, 0);

				if (confirmedAmount > 0)
				{
					log.info(
							"Confirmed store: itemId={}, inventorySlot={}",
							pendingStore.itemId,
							pendingStore.inventorySlot
					);

					confirmedStores.put(
							itemId,
							confirmedAmount - 1
					);

					boolean stackable =
							itemManager.getItemComposition(itemId).isStackable();

					if (!stackable)
					{
						int emptySlot = findFirstEmptyStorageSlot();

						if (emptySlot != -1)
						{
							simulatedStorage[emptySlot] = new Item(itemId, 1);
							panel.updatePrivateStorage(simulatedStorage);
						}

						pendingStores.removeIf(store ->
								store.itemId == itemId &&
										store.inventorySlot == pendingStore.inventorySlot
						);
					}

					else
					{
						int existingSlot = findStorageItemSlot(itemId);

						if (existingSlot != -1)
						{
							Item existingItem = simulatedStorage[existingSlot];

							simulatedStorage[existingSlot] =
									new Item(itemId, existingItem.getQuantity() + 1);
						}
						else
						{
							int emptySlot = findFirstEmptyStorageSlot();

							if (emptySlot != -1)
							{
								simulatedStorage[emptySlot] = new Item(itemId, 1);
							}
						}

						panel.updatePrivateStorage(simulatedStorage);
					}

				}
				else
				{
					remainingStores.offer(pendingStore);
				}
			}

			while (!pendingWithdraws.isEmpty())
			{
				PendingWithdraw pendingWithdraw = pendingWithdraws.poll();

				int itemId = pendingWithdraw.itemId;

				int confirmedAmount =
						confirmedWithdraws.getOrDefault(itemId, 0);

				if (confirmedAmount > 0)
				{
					log.info(
							"Confirmed withdraw: itemId={}, storageSlot={}",
							pendingWithdraw.itemId,
							pendingWithdraw.storageSlot
					);

					confirmedWithdraws.put(
							itemId,
							confirmedAmount - 1
					);

					boolean stackable =
							itemManager.getItemComposition(itemId).isStackable();

					int storageSlot = pendingWithdraw.storageSlot;
					Item storedItem = simulatedStorage[storageSlot];

					if (!stackable)
					{
						simulatedStorage[storageSlot] = null;

						pendingWithdraws.removeIf(withdraw ->
								withdraw.itemId == itemId &&
										withdraw.storageSlot == storageSlot
						);
					}
					else if (storedItem != null && storedItem.getId() == itemId)
					{
						int newQuantity = storedItem.getQuantity() - 1;

						if (newQuantity > 0)
						{
							simulatedStorage[storageSlot] =
									new Item(itemId, newQuantity);
						}
						else
						{
							simulatedStorage[storageSlot] = null;

							pendingWithdraws.removeIf(withdraw ->
									withdraw.itemId == itemId &&
											withdraw.storageSlot == storageSlot
							);
						}
					}

					panel.updatePrivateStorage(simulatedStorage);
				}
				else
				{
					remainingWithdraws.offer(pendingWithdraw);
				}
			}


			pendingStores.addAll(remainingStores);
			pendingWithdraws.addAll(remainingWithdraws);
			log.info("Pending stores after update: {}", pendingStores);
			log.info("Pending withdraws after update: {}", pendingWithdraws);

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
		if (client.getMouseCurrentButton() != 0)
		{
			return; // wait until release
		}

		Widget source = client.getDraggedWidget();
		Widget destination = client.getDraggedOnWidget();

		if (source == null || destination == null
				|| source.getId() != InterfaceID.RaidsStoragePrivate.ITEMS
				|| destination.getId() != InterfaceID.RaidsStoragePrivate.ITEMS)
		{
			return;
		}

		int sourceSlot = source.getIndex();
		int destinationSlot = destination.getIndex();

		log.info("source index: {} destination index: {}", sourceSlot, destinationSlot);

		if (sourceSlot != destinationSlot)
		{
			swapStorageItems(sourceSlot, destinationSlot);
		}

		// Indices are 0–27
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

		if (event.getMenuOption().equals("Store"))
		{
			pendingStores.offer(
					new PendingStore(
							event.getItemId(),
							event.getParam0()
					)
			);

			log.info("Queued store: itemId={}", event.getItemId());
		}

		if (event.getMenuOption().equals("Withdraw"))
		{
			pendingWithdraws.offer(
					new PendingWithdraw(
							event.getItemId(),
							event.getParam0()
					)
			);

			log.info("Queued withdraw: itemId={}", event.getItemId());
		}
    }

	@Provides
	CoxStorageConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CoxStorageConfig.class);
	}

}