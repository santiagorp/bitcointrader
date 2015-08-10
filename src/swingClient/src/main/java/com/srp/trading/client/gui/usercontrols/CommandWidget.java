/**
 * 
 */
package com.srp.trading.client.gui.usercontrols;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.srp.trading.client.gui.util.CustomActionListener;
import com.srp.trading.core.CommandDefinition;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class CommandWidget extends JPanel {
	private JPanel parametersPanel;
	private CustomActionListener commandAction;

	/**
	 * Create the panel.
	 */
	public CommandWidget(CommandDefinition cmd, CustomActionListener executeCommand) {
		commandAction = executeCommand;
		setMaximumSize(new Dimension(327679, 92));
		setAlignmentY(Component.TOP_ALIGNMENT);
		setPreferredSize(new Dimension(200, 92));
		setBackground(Color.WHITE);
		setBorder(new EmptyBorder(2, 2, 2, 2));
		setMinimumSize(new Dimension(200, 92));
		setLayout(new BorderLayout(0, 0));
		
		JPanel containerPanel = new JPanel();
		containerPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		add(containerPanel, BorderLayout.NORTH);
		containerPanel.setLayout(new BorderLayout(5, 5));
		
		JPanel descriptionPanel = new JPanel();
		containerPanel.add(descriptionPanel, BorderLayout.NORTH);
		descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS));
		
		JLabel lblCommandName = new JLabel("Command");
		lblCommandName.setFont(new Font("Dialog", Font.BOLD, 13));
		lblCommandName.setBorder(new EmptyBorder(0, 5, 0, 5));
		lblCommandName.setMaximumSize(new Dimension(32767, 24));
		lblCommandName.setPreferredSize(new Dimension(69, 24));
		descriptionPanel.add(lblCommandName);
		lblCommandName.setText(cmd.getName());
		
		JLabel lblCommandDescription = new JLabel("Description");
		lblCommandDescription.setBorder(new EmptyBorder(0, 5, 0, 5));
		lblCommandDescription.setMaximumSize(new Dimension(32767, 24));
		lblCommandDescription.setPreferredSize(new Dimension(81, 24));
		lblCommandDescription.setFont(lblCommandDescription.getFont().deriveFont(lblCommandDescription.getFont().getStyle() & ~Font.BOLD));
		descriptionPanel.add(lblCommandDescription);
		lblCommandDescription.setText(cmd.getDescription());
		
		parametersPanel = new JPanel();
		containerPanel.add(parametersPanel, BorderLayout.CENTER);
		parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
		
		JPanel buttonWrapper = new JPanel();
		buttonWrapper.setBorder(new EmptyBorder(0, 0, 5, 5));
		buttonWrapper.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		buttonWrapper.setAlignmentX(Component.RIGHT_ALIGNMENT);
		containerPanel.add(buttonWrapper, BorderLayout.SOUTH);
		buttonWrapper.setLayout(new BorderLayout(0, 0));
		
		JButton btnExecuteCommand = new JButton("Execute");
		btnExecuteCommand.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		btnExecuteCommand.setHorizontalTextPosition(SwingConstants.RIGHT);
		btnExecuteCommand.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonWrapper.add(btnExecuteCommand, BorderLayout.EAST);
		btnExecuteCommand.setIcon(new ImageIcon(CommandWidget.class.getResource("/img16/execute.png")));
		btnExecuteCommand.setPreferredSize(new Dimension(120, 24));
		btnExecuteCommand.setToolTipText("Execute command");
		btnExecuteCommand.setFont(btnExecuteCommand.getFont().deriveFont(btnExecuteCommand.getFont().getStyle() & ~Font.BOLD));
		btnExecuteCommand.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				// Update parameters
				commandAction.getData()[2] = getParameters();
				commandAction.actionPerformed(e);
			}
		});
		
		int maxHeight = this.getMaximumSize().height;		 
		for (String key : cmd.getParameterDefinitions().keySet()) {			
			String name = key;
			String description = cmd.getParameterDefinitions().get(name);
			String defaultValue = defaultValue = cmd.getDefaultValues().get(name);
			
			ParameterWidget paramWidget = new ParameterWidget(name, description, defaultValue);
			parametersPanel.add(paramWidget);
			maxHeight += paramWidget.getMaximumSize().height;
		}		
		Dimension maxSize = getMaximumSize();
		Dimension minSize = getMinimumSize();
		Dimension reqSize = getPreferredSize();
		maxSize.height = maxHeight;
		minSize.height = maxHeight;
		reqSize.height = maxHeight;		
		setMaximumSize(maxSize);
		setMinimumSize(minSize);
		setPreferredSize(reqSize);
		
	}
	
	/**
	 * Get the list of parameters
	 * @return
	 */	
	public HashMap<String, String> getParameters() {
		HashMap<String, String> params = new HashMap<String, String>();
		
		for (Component c : parametersPanel.getComponents()) {
			if (c instanceof ParameterWidget) {
				ParameterWidget pw = (ParameterWidget) c;
				params.put(pw.getParameterName(), pw.getParameterValue());
			}
		}
		
		return params;
	}

}
