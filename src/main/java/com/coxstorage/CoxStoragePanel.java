package com.coxstorage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import net.runelite.client.ui.PluginPanel;

public class CoxStoragePanel extends PluginPanel
{
    public CoxStoragePanel()
    {
        JTabbedPane tabs = new JTabbedPane();

        JPanel publicPanel = new JPanel();
        publicPanel.add(new JLabel("Public storage"));

        JPanel privatePanel = new JPanel();
        privatePanel.add(new JLabel("Private storage"));

        tabs.addTab("Public", publicPanel);
        tabs.addTab("Private", privatePanel);

        add(tabs);
    }
}