package com.coxstorage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;

public class CoxStoragePanel extends PluginPanel
{
    private final ItemManager itemManager;
    private final JLabel[] privateSlots = new JLabel[120];

    public CoxStoragePanel(ItemManager itemManager)
    {
        this.itemManager = itemManager;

        JPanel privateGrid = new JPanel(
                new GridLayout(15, 8, 2, 2)
        );

        privateGrid.setBackground(new Color(62, 53, 41));
        privateGrid.setPreferredSize(
                new Dimension(PluginPanel.PANEL_WIDTH - 20, 538)
        );

        for (int i = 0; i < 120; i++)
        {
            JLabel slot = new JLabel();
            privateSlots[i] = slot;

            slot.setOpaque(true);
            slot.setBackground(new Color(80, 70, 55));

            privateGrid.add(slot);
        }

        add(privateGrid);
    }

    public void updatePrivateStorage(Item[] items)
    {
        for (int i = 0; i < privateSlots.length; i++)
        {
            JLabel slot = privateSlots[i];

            if (i < items.length && items[i] != null && items[i].getId() != -1)
            {
                Item item = items[i];
                int quantity = item.getQuantity();

                itemManager.getImage(
                        item.getId(),
                        quantity > 1 ? quantity : 1,
                        quantity > 1
                ).addTo(slot);
            }
            else
            {
                slot.setIcon(null);
            }
        }
    }
}