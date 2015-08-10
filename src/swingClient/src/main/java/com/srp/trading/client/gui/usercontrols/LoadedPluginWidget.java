/**
 * 
 */
package com.srp.trading.client.gui.usercontrols;

import javax.swing.JPanel;

import com.srp.trading.client.gui.util.CustomActionListener;
import com.srp.trading.core.PluginDescription;
import com.srp.trading.core.PluginExecInfo;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.BoxLayout;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JButton;

import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.ImageIcon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.UIManager;

import java.awt.event.ActionEvent;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class LoadedPluginWidget extends JPanel {
	private PluginExecInfo execInfo;	
	private JPanel mainContainer;
	private ActionListener operateAction = null;

	/**
	 * Create the panel.
	 */
	public LoadedPluginWidget(PluginExecInfo plugExecInfo, ActionListener unloadPlugin, ActionListener operatePluginAction) {
		operateAction = operatePluginAction;
		setMaximumSize(new Dimension(32767, 50));
		setMinimumSize(new Dimension(214, 50));
		setBackground(Color.WHITE);
				
		setBounds(new Rectangle(5, 5, 0, 0));
		setPreferredSize(new Dimension(214, 50));
		setAlignmentY(Component.TOP_ALIGNMENT);
		setBorder(new LineBorder(Color.WHITE, 2));
		execInfo = plugExecInfo;
		this.setToolTipText(execInfo.getDescription().getDescription());
		setLayout(new BorderLayout(0, 0));
		
		mainContainer = new JPanel();
		mainContainer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ActionEvent ae = new ActionEvent(e.getSource(), e.getID(), "operate"); 
				operateAction.actionPerformed(ae);
			}						
		});
		mainContainer.setCursor(new Cursor(Cursor.HAND_CURSOR));
		mainContainer.setBorder(new LineBorder(new Color(0, 0, 0)));
		add(mainContainer);
		mainContainer.setLayout(new BorderLayout(0, 0));		
		
		JPanel panel_1 = new JPanel();
		mainContainer.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
		
		JLabel lblName = new JLabel("Plugin Name");
		lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_1.add(lblName);
		lblName.setFont(new Font("Dialog", Font.BOLD, 12));
		lblName.setText(execInfo.getDescription().getName());
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_1.add(lblVersion);
		lblVersion.setFont(new Font("Dialog", Font.ITALIC, 10));
		lblVersion.setText("v" + execInfo.getDescription().getVersion().toString());
		
		JLabel lblStatus = new JLabel("status");
		lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_1.add(lblStatus);
		lblStatus.setFont(new Font("Dialog", Font.PLAIN, 10));
		lblStatus.setText(execInfo.getStatusMessage());
		lblStatus.setToolTipText(execInfo.getStatusMessage());
		
		JPanel unloadWrapperPane = new JPanel();
		unloadWrapperPane.setBorder(new EmptyBorder(0, 0, 0, 3));
		mainContainer.add(unloadWrapperPane, BorderLayout.EAST);
		unloadWrapperPane.setLayout(new BoxLayout(unloadWrapperPane, BoxLayout.X_AXIS));
		
		JButton btnUnload = new JButton("");
		btnUnload.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		btnUnload.setMinimumSize(new Dimension(2, 20));
		btnUnload.setMaximumSize(new Dimension(20, 20));
		unloadWrapperPane.add(btnUnload);
		btnUnload.setBorderPainted(false);
		btnUnload.setToolTipText("Unload plugin");
		btnUnload.addActionListener(unloadPlugin);
		btnUnload.setPreferredSize(new Dimension(20, 20));
		btnUnload.setIcon(new ImageIcon(LoadedPluginWidget.class.getResource("/img16/deleteCross.png")));
	}
	
	/**
	 * Higlight the control
	 * @param value
	 */
	public void highlight(boolean value) {
		Border border;
		if (value) {			
			border = BorderFactory.createLineBorder(Color.green, 2);
		} else {
			border = BorderFactory.createLineBorder(Color.white, 2);
		}
		this.setBorder(border);
	}
	
	public PluginExecInfo getPluginExecInfo() {
		return execInfo;
	}

	public PluginExecInfo getExecInfo() {
		return execInfo;
	}
}
