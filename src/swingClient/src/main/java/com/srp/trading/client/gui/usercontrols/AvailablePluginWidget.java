/**
 * 
 */
package com.srp.trading.client.gui.usercontrols;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.srp.trading.core.PluginDescription;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class AvailablePluginWidget extends JPanel {
	PluginDescription pluginDescription;	

	/**
	 * Create the panel.
	 */
	public AvailablePluginWidget(PluginDescription plugDesc, ActionListener onLoad) {
		setMaximumSize(new Dimension(32767, 50));
		setMinimumSize(new Dimension(215, 50));
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(215, 50));
		setBorder(new EmptyBorder(2, 2, 2, 2));
		pluginDescription = plugDesc;
		this.setToolTipText(pluginDescription.getDescription());
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
		
		JLabel lblName = new JLabel("Plugin Name");
		lblName.setBorder(new EmptyBorder(3, 0, 0, 0));
		panel_1.add(lblName);
		lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblName.setFont(new Font("Dialog", Font.BOLD, 12));
		
		
		lblName.setText(plugDesc.getName());
		
		JLabel lblVersion = new JLabel("Version");
		panel_1.add(lblVersion);
		lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblVersion.setFont(new Font("Dialog", Font.ITALIC, 10));
		lblVersion.setText("v" + plugDesc.getVersion().toString());
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(0, 5, 0, 5));
		panel.add(panel_2, BorderLayout.EAST);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
		
		JButton btnLoad = new JButton("");
		btnLoad.setFocusPainted(false);
		panel_2.add(btnLoad);
		btnLoad.setMinimumSize(new Dimension(20, 20));
		btnLoad.setMaximumSize(new Dimension(20, 20));
		btnLoad.setBorderPainted(false);
		btnLoad.addActionListener(onLoad);
		btnLoad.setToolTipText("Load");
		btnLoad.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnLoad.setPreferredSize(new Dimension(20, 20));
		btnLoad.setIcon(new ImageIcon(AvailablePluginWidget.class.getResource("/img16/add.png")));
		btnLoad.setBackground(new Color(238, 238, 238));
		btnLoad.setSelectedIcon(new ImageIcon(AvailablePluginWidget.class.getResource("/img16/add.png")));
	}
	
	public PluginDescription getPluginDescription() {
		return pluginDescription;
	}
}
