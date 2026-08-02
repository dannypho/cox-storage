package com.coxstorage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.*;
import javax.swing.JScrollPane;
import net.runelite.client.ui.PluginPanel;

public class CoxStoragePanel extends PluginPanel
{
    public CoxStoragePanel()
    {
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
}