/**
 * 
 */
package com.srp.trading.client.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class CustomActionListener implements ActionListener {
	protected Object[] data;
	
	public CustomActionListener(Object[] data) {
		this.data = data;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}

	public Object[] getData() {
		return data;
	}
}
