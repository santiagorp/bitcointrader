package com.srp.trading.client.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Font;

public class ConnectionDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	private JTextField textHost;
	private JTextField textPort;
	SwingClient mainWindow;
	boolean success = false;

	/**
	 * Create the dialog.
	 */
	public ConnectionDialog() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		ActionListener escListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};

		this.getRootPane().registerKeyboardAction(escListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		setModal(true);
		setAlwaysOnTop(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ConnectionDialog.class.getResource("/img16/network-idle.png")));
		setTitle("Connect to server");
		setBounds(100, 100, 308, 160);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
		{
			JLabel lblNewLabel = new JLabel("Server details");
			contentPanel.add(lblNewLabel, "2, 2");
		}
		{
			JLabel lblHost = new JLabel("Host");
			lblHost.setFont(lblHost.getFont().deriveFont(lblHost.getFont().getStyle() & ~Font.BOLD));
			contentPanel.add(lblHost, "2, 4, left, default");
		}
		{
			textHost = new JTextField();
			textHost.setBounds(new Rectangle(0, 0, 240, 0));
			contentPanel.add(textHost, "4, 4, fill, default");
			textHost.setColumns(10);
		}
		{
			JLabel lblPort = new JLabel("Port");
			lblPort.setFont(lblPort.getFont().deriveFont(lblPort.getFont().getStyle() & ~Font.BOLD));
			contentPanel.add(lblPort, "2, 6, left, default");
		}
		{
			textPort = new JTextField();
			textPort.setBounds(new Rectangle(0, 0, 240, 0));
			contentPanel.add(textPort, "4, 6, fill, default");
			textPort.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setFont(okButton.getFont().deriveFont(okButton.getFont().getStyle() & ~Font.BOLD));
				okButton.setPreferredSize(new Dimension(80, 24));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						success = true;
						setVisible(false);
					}
				});
				{
					JButton cancelButton = new JButton("Cancel");
					cancelButton.setFont(cancelButton.getFont().deriveFont(cancelButton.getFont().getStyle() & ~Font.BOLD & ~Font.ITALIC));
					cancelButton.setPreferredSize(new Dimension(80, 24));
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							success = false;
							setVisible(false);
						}
					});
					cancelButton.setActionCommand("");
					buttonPane.add(cancelButton);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	/**
	 * Get the host or null
	 * 
	 * @return
	 */
	public String getHost() {
		String host = textHost.getText().isEmpty() ? null : textHost.getText().trim();
		return host;
	}

	/**
	 * Get the port number
	 * 
	 * @return
	 */
	public Integer getPort() {
		Integer value;
		try {
			value = Integer.parseInt(textPort.getText().trim());
		} catch (NumberFormatException e) {
			value = null;
		}
		return value;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean value) {
		success = value;
	}

	public JTextField getTextHost() {
		return textHost;
	}

	public JTextField getTextPort() {
		return textPort;
	}
}
