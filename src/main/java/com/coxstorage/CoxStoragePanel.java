package com.coxstorage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JScrollPane;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.game.ItemManager;
import net.runelite.api.Item;

public class CoxStoragePanel extends PluginPanel
{
    private final ItemManager itemManager;
    private final JLabel[] privateSlots = new JLabel[120];

    public CoxStoragePanel(ItemManager itemManager)
    {
        this.itemManager = itemManager;
        JTabbedPane tabs = new JTabbedPane();

        JPanel publicPanel = new JPanel();
        publicPanel.add(new JLabel("Public storage"));

        JPanel privateGrid = new JPanel(
                new GridLayout(15, 8, 2, 2)
        );

        privateGrid.setBackground(new Color(62, 53, 41));
        privateGrid.setPreferredSize(
                new Dimension(PluginPanel.PANEL_WIDTH - 20, 15 * 34)
        );


        for (int i = 0; i < 120; i++)
        {
            JLabel slot = new JLabel();
            privateSlots[i] = slot;
            slot.setOpaque(true);
            slot.setBackground(new Color(80, 70, 55));
            privateGrid.add(slot);
        }

        JScrollPane privateScrollPane = new JScrollPane(privateGrid);

        privateScrollPane.setPreferredSize(
                new Dimension(PluginPanel.PANEL_WIDTH, 180)
        );

        privateScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        tabs.addTab("Public", publicPanel);
        tabs.addTab("Private", privateScrollPane);

        add(tabs);
    }

    public void updatePrivateStorage(Item[] items)
    {
        for (int i = 0; i < privateSlots.length; i++)
        {
            JLabel slot = privateSlots[i];

            if (i < items.length && items[i].getId() != -1)
            {
                Item item = items[i];

                itemManager
                        .getImage(item.getId(), item.getQuantity(), true)
                        .addTo(slot);
            }
            else
            {
                slot.setIcon(null);
            }
        }
    }
}