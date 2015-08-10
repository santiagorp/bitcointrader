/**
 * 
 */
package com.srp.trading.client.gui.usercontrols;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.border.EmptyBorder;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class ParameterWidget extends JPanel {
	private String parameterName;
	private String parameterDescription;
	private JTextField textValue;
	
	/**
	 * Create the panel.
	 */
	public ParameterWidget(String name, String description, String defaultValue) {
		setPreferredSize(new Dimension(300, 30));
		setMinimumSize(new Dimension(10, 30));
		setMaximumSize(new Dimension(327672, 30));
		setLayout(new BorderLayout(0, 0));
		
		JPanel panelWrapper = new JPanel();
		panelWrapper.setMinimumSize(new Dimension(10, 24));
		panelWrapper.setMaximumSize(new Dimension(32767, 24));
		add(panelWrapper);
		panelWrapper.setLayout(new BorderLayout(0, 0));
		
		JPanel panelLabels = new JPanel();
		panelLabels.setBorder(new EmptyBorder(3, 0, 0, 0));
		panelLabels.setMaximumSize(new Dimension(3276730, 24));
		panelWrapper.add(panelLabels, BorderLayout.WEST);
		panelLabels.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblName = new JLabel("Name");
		lblName.setPreferredSize(new Dimension(160, 15));
		lblName.setMaximumSize(new Dimension(160, 15));
		lblName.setMinimumSize(new Dimension(160, 15));
		panelLabels.add(lblName);
		
		parameterName = name;
		parameterDescription = description;
		
		lblName.setText(parameterName);
		
		JLabel lblInfo = new JLabel("");
		panelLabels.add(lblInfo);
		lblInfo.setToolTipText("Description");
		lblInfo.setIcon(new ImageIcon(ParameterWidget.class.getResource("/img16/info.png")));
		lblInfo.setToolTipText(parameterDescription);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(0, 0, 0, 5));
		panelWrapper.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		textValue = new JTextField();
		textValue.setMaximumSize(new Dimension(2147483647, 24));
		textValue.setMinimumSize(new Dimension(40, 24));
		panel.add(textValue);
		textValue.setPreferredSize(new Dimension(40, 24));
		textValue.setText("defaultValue");
		textValue.setColumns(10);
		textValue.setText(defaultValue);
		
		if (description.equals(name)) {
			lblInfo.setVisible(false);
		}
	}

	public String getParameterName() {
		return parameterName;
	}

	public String getParameterValue() {
		return textValue.getText();
	}

}
