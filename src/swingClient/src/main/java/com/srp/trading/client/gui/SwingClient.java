package com.srp.trading.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.srp.trading.client.gui.usercontrols.AvailablePluginWidget;
import com.srp.trading.client.gui.usercontrols.CommandWidget;
import com.srp.trading.client.gui.usercontrols.LoadedPluginWidget;
import com.srp.trading.client.gui.util.CustomActionListener;
import com.srp.trading.core.BasicExecutionResult;
import com.srp.trading.core.Command;
import com.srp.trading.core.CommandDefinition;
import com.srp.trading.core.IServerAPI;
import com.srp.trading.core.PluginDescription;
import com.srp.trading.core.PluginExecInfo;

public class SwingClient {
	private static SwingClient mainWindow;
	private JFrame frmBitcoinTraderClient;
	private IServerAPI server;
	private Registry registry;
	private JLabel lblStatus;
	private JLabel lblMessage;
	JButton btnConnect;
	JButton btnDisconnect;
	private JLabel lblAvailablePlugins;
	private JButton btnRefresh;
	private JPanel availablePluginsPanel;
	private JLabel lblLoadedPlugins;
	private JSplitPane mainSplitPane;
	private JPanel left;
	private JSplitPane splitPaneCommandResults;
	private JPanel commandsPane;
	private Integer currentPluginId = null;
	private JPanel panel;
	private JLabel lblCommands;
	private JScrollPane scrollPaneLoadedPlugins;
	private JPanel loadedPluginsPanel;
	private JScrollPane scrollPaneAvailablePlugins;
	private JScrollPane scrollCommandsPane;
	private JPanel outputWrapper;
	private JLabel lblOutput;
	private JTextPane outputPane;
	private JPanel messageWrapper;
	private JPanel statusWrapper;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainWindow = new SwingClient();
					mainWindow.frmBitcoinTraderClient.setVisible(true);
					mainWindow.createConnection(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SwingClient() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBitcoinTraderClient = new JFrame();
		frmBitcoinTraderClient.setIconImage(Toolkit.getDefaultToolkit().getImage(SwingClient.class.getResource("/img48/bitcoin.png")));
		frmBitcoinTraderClient.setTitle("Bitcoin trader client");
		frmBitcoinTraderClient.setBounds(100, 100, 900, 600);
		frmBitcoinTraderClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmBitcoinTraderClient.getContentPane().setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		frmBitcoinTraderClient.getContentPane().add(toolBar, BorderLayout.NORTH);

		btnConnect = new JButton("");
		btnConnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				createConnection(false);
			}
		});
		btnConnect.setToolTipText("Connect to a server");
		btnConnect.setIcon(new ImageIcon(SwingClient.class.getResource("/img16/network-idle.png")));
		toolBar.add(btnConnect);

		btnDisconnect = new JButton("");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				disconnect();
			}
		});
		btnDisconnect.setEnabled(false);
		btnDisconnect.setIcon(new ImageIcon(SwingClient.class.getResource("/img16/network-offline.png")));
		btnDisconnect.setToolTipText("Disconnect from server");
		toolBar.add(btnDisconnect);

		btnRefresh = new JButton("");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reloadAvailablePluginsList();
				refreshPanels();
			}
		});
		btnRefresh.setEnabled(false);
		btnRefresh.setToolTipText("Refresh list of available plugins");
		toolBar.add(btnRefresh);
		btnRefresh.setIcon(new ImageIcon(SwingClient.class.getResource("/img16/refresh.png")));

		JPanel statusPanel = new JPanel();
		statusPanel.setPreferredSize(new Dimension(10, 24));
		statusPanel.setMinimumSize(new Dimension(10, 24));
		statusPanel.setMaximumSize(new Dimension(327672, 24));
		frmBitcoinTraderClient.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));

		statusWrapper = new JPanel();
		statusWrapper.setBorder(new EmptyBorder(2, 2, 2, 2));
		statusPanel.add(statusWrapper);
		statusWrapper.setLayout(new BoxLayout(statusWrapper, BoxLayout.X_AXIS));

		lblStatus = new JLabel("Disconnected");
		statusWrapper.add(lblStatus);
		lblStatus.setBorder(new LineBorder(Color.LIGHT_GRAY));
		lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblStatus.setMinimumSize(new Dimension(248, 24));
		lblStatus.setMaximumSize(new Dimension(248, 24));
		lblStatus.setPreferredSize(new Dimension(248, 24));
		lblStatus.setFont(new Font("Dialog", Font.PLAIN, 11));
		lblStatus.setForeground(Color.RED);
		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);

		messageWrapper = new JPanel();
		messageWrapper.setBorder(new EmptyBorder(2, 2, 2, 2));
		statusPanel.add(messageWrapper);
		messageWrapper.setLayout(new BoxLayout(messageWrapper, BoxLayout.X_AXIS));

		lblMessage = new JLabel("");
		lblMessage.setMinimumSize(new Dimension(95, 24));
		messageWrapper.add(lblMessage);
		lblMessage.setBorder(new LineBorder(Color.LIGHT_GRAY));
		lblMessage.setPreferredSize(new Dimension(0, 24));
		lblMessage.setMaximumSize(new Dimension(32767, 24));
		lblMessage.setFont(new Font("Dialog", Font.PLAIN, 11));
		lblMessage.setForeground(Color.DARK_GRAY);

		mainSplitPane = new JSplitPane();
		frmBitcoinTraderClient.getContentPane().add(mainSplitPane, BorderLayout.CENTER);

		left = new JPanel();
		mainSplitPane.setLeftComponent(left);
		left.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setMinimumSize(new Dimension(500, 25));
		splitPane.setBorder(null);
		splitPane.setPreferredSize(new Dimension(500, 27));
		left.add(splitPane);

		JPanel explorerPanel = new JPanel();
		explorerPanel.setMinimumSize(new Dimension(250, 10));
		explorerPanel.setBackground(new Color(102, 153, 204));
		explorerPanel.setPreferredSize(new Dimension(250, 10));
		splitPane.setLeftComponent(explorerPanel);
		explorerPanel.setLayout(new BorderLayout(0, 0));

		lblAvailablePlugins = new JLabel("Available plugins");
		lblAvailablePlugins.setForeground(Color.WHITE);
		lblAvailablePlugins.setBorder(new EmptyBorder(0, 5, 0, 5));
		lblAvailablePlugins.setPreferredSize(new Dimension(120, 24));
		explorerPanel.add(lblAvailablePlugins, BorderLayout.NORTH);
		lblAvailablePlugins.setFont(new Font("Dialog", Font.PLAIN, 10));

		availablePluginsPanel = new JPanel();
		availablePluginsPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		availablePluginsPanel.setBackground(Color.WHITE);
		availablePluginsPanel.setLayout(new BoxLayout(availablePluginsPanel, BoxLayout.Y_AXIS));

		scrollPaneAvailablePlugins = new JScrollPane(availablePluginsPanel);
		scrollPaneAvailablePlugins.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		explorerPanel.add(scrollPaneAvailablePlugins, BorderLayout.CENTER);

		JPanel explorerLoadedPanel = new JPanel();
		explorerLoadedPanel.setPreferredSize(new Dimension(250, 10));
		explorerLoadedPanel.setMinimumSize(new Dimension(250, 10));
		explorerLoadedPanel.setBackground(new Color(102, 153, 204));
		splitPane.setRightComponent(explorerLoadedPanel);
		explorerLoadedPanel.setLayout(new BorderLayout(0, 0));

		lblLoadedPlugins = new JLabel("Loaded plugins");
		lblLoadedPlugins.setForeground(Color.WHITE);
		lblLoadedPlugins.setBorder(new EmptyBorder(0, 5, 0, 5));
		lblLoadedPlugins.setPreferredSize(new Dimension(109, 24));
		explorerLoadedPanel.add(lblLoadedPlugins, BorderLayout.NORTH);
		lblLoadedPlugins.setFont(new Font("Dialog", Font.PLAIN, 10));
		lblLoadedPlugins.setToolTipText("List of loaded plugins");

		loadedPluginsPanel = new JPanel();
		loadedPluginsPanel.setBackground(Color.WHITE);
		loadedPluginsPanel.setLayout(new BoxLayout(loadedPluginsPanel, BoxLayout.Y_AXIS));

		scrollPaneLoadedPlugins = new JScrollPane(loadedPluginsPanel);
		scrollPaneLoadedPlugins.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		explorerLoadedPanel.add(scrollPaneLoadedPlugins, BorderLayout.CENTER);

		panel = new JPanel();
		panel.setBackground(new Color(102, 153, 204));
		mainSplitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		splitPaneCommandResults = new JSplitPane();
		splitPaneCommandResults.setBorder(null);
		panel.add(splitPaneCommandResults, BorderLayout.CENTER);
		splitPaneCommandResults.setOrientation(JSplitPane.VERTICAL_SPLIT);

		commandsPane = new JPanel();
		commandsPane.setBorder(new EmptyBorder(2, 2, 2, 2));
		commandsPane.setBackground(Color.WHITE);
		commandsPane.setLayout(new BoxLayout(commandsPane, BoxLayout.Y_AXIS));

		scrollCommandsPane = new JScrollPane(commandsPane);
		scrollCommandsPane.setPreferredSize(new Dimension(7, 400));
		scrollCommandsPane.setDoubleBuffered(true);
		scrollCommandsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		splitPaneCommandResults.setLeftComponent(scrollCommandsPane);

		outputWrapper = new JPanel();
		outputWrapper.setBackground(new Color(102, 153, 204));
		outputWrapper.setBorder(null);
		splitPaneCommandResults.setRightComponent(outputWrapper);
		outputWrapper.setLayout(new BorderLayout(0, 0));

		lblOutput = new JLabel("Output");
		lblOutput.setBorder(new EmptyBorder(0, 5, 0, 5));
		lblOutput.setMaximumSize(new Dimension(34, 24));
		lblOutput.setMinimumSize(new Dimension(34, 24));
		lblOutput.setPreferredSize(new Dimension(34, 24));
		lblOutput.setForeground(Color.WHITE);
		lblOutput.setFont(new Font("Dialog", Font.PLAIN, 10));
		outputWrapper.add(lblOutput, BorderLayout.NORTH);

		outputPane = new JTextPane();
		outputPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
		outputWrapper.add(outputPane, BorderLayout.CENTER);

		lblCommands = new JLabel("Commands");
		lblCommands.setForeground(Color.WHITE);
		lblCommands.setBorder(new EmptyBorder(0, 5, 0, 5));
		lblCommands.setMaximumSize(new Dimension(70, 24));
		lblCommands.setPreferredSize(new Dimension(70, 24));
		lblCommands.setFont(new Font("Dialog", Font.PLAIN, 10));
		panel.add(lblCommands, BorderLayout.NORTH);

		// Speed up scroolbars
		int scrollUnitInc = 20;
		scrollPaneAvailablePlugins.getVerticalScrollBar().setUnitIncrement(scrollUnitInc);
		scrollPaneLoadedPlugins.getVerticalScrollBar().setUnitIncrement(scrollUnitInc);
		scrollCommandsPane.getVerticalScrollBar().setUnitIncrement(scrollUnitInc);
	}

	/**
	 * Connect to the server
	 */
	private void createConnection(boolean autoConnect) {
		if (autoConnect) {
			try {
				// Connect automatically to localhost:1099
				// If it fails, show the dialog
				String host = "localhost";
				Integer port = 1099;

				registry = LocateRegistry.getRegistry(host, port);
				server = (IServerAPI) registry.lookup("btcTradingServer");
				setStatusLabel(host + ":" + port, false);
				btnConnect.setEnabled(false);
				btnDisconnect.setEnabled(true);
				btnRefresh.setEnabled(true);
				refreshPanels();
				outputPane.setText("");
			} catch (Exception e) {
				autoConnect = false;
			}
		}
		
		if (!autoConnect) {
			ConnectionDialog connDlg = new ConnectionDialog();
			connDlg.getTextHost().setText("localhost");
			connDlg.getTextPort().setText("1099");

			connDlg.setLocationRelativeTo(mainWindow.frmBitcoinTraderClient);
			connDlg.setVisible(true);

			connDlg.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentHidden(ComponentEvent e) {
					ConnectionDialog connDlg = (ConnectionDialog) e.getComponent();
					try {
						if (!connDlg.isSuccess())
							return;

						String host = connDlg.getHost();
						Integer port = connDlg.getPort();
						if (host == null || host.isEmpty()) {
							host = "localhost";
						}
						if (port == null) {
							port = 1099;
						}
						registry = LocateRegistry.getRegistry(host, port);
						server = (IServerAPI) registry.lookup("btcTradingServer");
						setStatusLabel(host + ":" + port, false);					
						btnConnect.setEnabled(false);
						btnDisconnect.setEnabled(true);
						btnRefresh.setEnabled(true);
					} catch (Exception ex) {
						setLabelMessage("Could not establish connection with the server.", true);
						setStatusLabel("Disconnected", true);
						return;
					}
					refreshPanels();
					outputPane.setText("");
				}
			});
		}
	}

	/**
	 * Refresh available and loaded plugins
	 */
	private void refreshPanels() {
		refreshAvailablePlugins();
		refreshLoadedPlugins();
	}

	/**
	 * Disconnect from server
	 */
	private void disconnect() {
		registry = null;
		server = null;
		currentPluginId = null;
		cleanUp();
	}

	/**
	 * Clean up containers
	 */
	private void cleanUp() {
		btnConnect.setEnabled(true);
		btnDisconnect.setEnabled(false);
		btnRefresh.setEnabled(false);
		lblMessage.setText("");
		setStatusLabel("Disconnected", true);
		availablePluginsPanel.removeAll();
		availablePluginsPanel.updateUI();
		loadedPluginsPanel.removeAll();
		loadedPluginsPanel.updateUI();
		commandsPane.removeAll();
		commandsPane.updateUI();
		outputPane.setText("");
	}

	/**
	 * Print message in the message label
	 * 
	 * @param test
	 * @param error
	 */
	private void setLabelMessage(String text, boolean error) {
		if (error) {
			lblMessage.setForeground(Color.RED);
		} else {
			lblMessage.setForeground(Color.DARK_GRAY);
		}
		lblMessage.setText(text);
		lblMessage.setToolTipText(text);
	}

	/**
	 * Set the status label text
	 */
	private void setStatusLabel(String text, boolean error) {
		if (error) {
			lblStatus.setForeground(Color.RED);
		} else {
			Color darkGreen = new Color(0, 0x80, 0);
			lblStatus.setForeground(darkGreen);
		}
		lblStatus.setText(text);
		lblStatus.setToolTipText(text);
	}

	/**
	 * Try to reload all the available plugins from the server
	 */
	private void reloadAvailablePluginsList() {
		setLabelMessage("Status: OK", false);
		try {
			BasicExecutionResult result = server.refreshAvailablePlugins();
			if (!result.isSuccess()) {
				setLabelMessage(result.getMessage(), false);
			}
		} catch (RemoteException e) {
			setLabelMessage(e.getMessage(), true);
		}
	}

	/**
	 * Get all the available plugins
	 */
	private void refreshAvailablePlugins() {
		setLabelMessage("Status: OK", false);
		try {
			List<PluginDescription> pds = server.getAvailablePlugins();
			availablePluginsPanel.removeAll();
			for (PluginDescription pd : pds) {
				Object[] data = new Object[] { pd };
				ActionListener loadPluginAction = new CustomActionListener(data) {
					@Override
					public void actionPerformed(ActionEvent e) {
						PluginDescription pd = (PluginDescription) data[0];
						loadPlugin(pd);
					}
				};

				AvailablePluginWidget widget = new AvailablePluginWidget(pd, loadPluginAction);
				availablePluginsPanel.add(widget);
			}
			availablePluginsPanel.updateUI();
		} catch (RemoteException e) {
			setLabelMessage(e.getMessage(), true);
		}
		availablePluginsPanel.updateUI();
	}

	/**
	 * Load a plugin by specifying its plugin description
	 */
	private void loadPlugin(PluginDescription plugDesc) {
		setLabelMessage("Status: OK", false);
		try {
			outputPane.setText("");
			BasicExecutionResult result = server.loadPlugin(plugDesc.getCompoundName());
			outputPane.setText(result.getMessage());
			if (result.isSuccess()) {
				refreshLoadedPlugins();
			} else {
				setLabelMessage("The plugin could not be loaded:  " + result.getMessage(), true);
			}
		} catch (RemoteException e) {
			setLabelMessage(e.getMessage(), true);
		}
	}

	/**
	 * Comparator by id
	 * 
	 * @return
	 */
	public static Comparator<PluginExecInfo> getPluginExecInfoComparatorId() {
		return new Comparator<PluginExecInfo>() {
			@Override
			public int compare(PluginExecInfo o1, PluginExecInfo o2) {
				return o1.getId() - o2.getId();
			}
		};
	}

	/**
	 * Refresh the list of loaded plugins
	 * 
	 * @throws RemoteException
	 */
	private void refreshLoadedPlugins() {
		try {
			List<PluginExecInfo> plugins = server.getExecPluginsInfo();
			java.util.Collections.sort(plugins, getPluginExecInfoComparatorId());

			loadedPluginsPanel.removeAll();

			for (PluginExecInfo p : plugins) {
				Object[] data = new Object[] { p };
				ActionListener unloadPluginAction = new CustomActionListener(data) {
					@Override
					public void actionPerformed(ActionEvent e) {
						PluginExecInfo pei = (PluginExecInfo) data[0];
						unloadPlugin(pei);
					}
				};

				ActionListener operatePluginAction = new CustomActionListener(data) {
					@Override
					public void actionPerformed(ActionEvent e) {
						outputPane.setText("");
						PluginExecInfo pei = (PluginExecInfo) data[0];
						currentPluginId = pei.getId();
						refreshCommandsList(pei);
						highlightPlugin(pei);
					}
				};

				LoadedPluginWidget widget = new LoadedPluginWidget(p, unloadPluginAction, operatePluginAction);
				loadedPluginsPanel.add(widget);
				highlightPlugin(currentPluginId);
			}
		} catch (RemoteException e) {
			setLabelMessage(e.getMessage(), true);
		}
		loadedPluginsPanel.updateUI();
	}

	/**
	 * Highlight the currently selected plugin
	 * 
	 * @param pei
	 */
	private void highlightPlugin(PluginExecInfo pei) {
		highlightPlugin(pei.getId());
	}

	/**
	 * Highlight the specified plugin id
	 * 
	 * @param id
	 */
	private void highlightPlugin(Integer id) {
		for (Component c : loadedPluginsPanel.getComponents()) {
			if (c instanceof LoadedPluginWidget) {
				LoadedPluginWidget widget = (LoadedPluginWidget) c;
				if (id != null && id == widget.getExecInfo().getId()) {
					widget.highlight(true);
				} else {
					widget.highlight(false);
				}
			}
		}
	}

	/**
	 * Unload the specified plugin
	 * 
	 * @param pei
	 */
	private void unloadPlugin(PluginExecInfo pei) {
		setLabelMessage("Status: OK", false);
		try {
			outputPane.setText("");
			BasicExecutionResult result = server.unloadPlugin(pei.getId());
			outputPane.setText(result.getMessage());
			if (result.isSuccess()) {
				refreshLoadedPlugins();
				if (currentPluginId != null && currentPluginId == pei.getId()) {
					commandsPane.removeAll();
					commandsPane.updateUI();
				}
			} else {
				setLabelMessage("The plugin could not be unloaded: " + result.getMessage(), true);
			}
		} catch (RemoteException e) {
			setLabelMessage(e.getMessage(), true);
		}
	}

	/**
	 * Refresh the commands for the current selected plugin
	 */
	private void refreshCommandsList(PluginExecInfo pei) {
		commandsPane.removeAll();
		setLabelMessage("Status: OK", false);

		try {
			if (pei != null) {
				BasicExecutionResult result = server.getAvailableCommands(pei.getId());
				if (result.isSuccess()) {
					List<CommandDefinition> commands = (List<CommandDefinition>) result.getAdditionalData().get("commands");
					for (CommandDefinition cmd : commands) {
						HashMap<String, String> parameters = new HashMap<String, String>();
						Object[] data = new Object[] { cmd, pei, parameters };
						CustomActionListener executeCommandAction = new CustomActionListener(data) {
							@Override
							public void actionPerformed(ActionEvent e) {
								CommandDefinition cmd = (CommandDefinition) data[0];
								PluginExecInfo pei = (PluginExecInfo) data[1];
								HashMap<String, String> parameters = (HashMap<String, String>) data[2];
								executeCommand(pei, cmd, parameters);
							}
						};

						CommandWidget widget = new CommandWidget(cmd, executeCommandAction);
						commandsPane.add(widget);
					}
				} else {
					setLabelMessage("The available commands could not be retrieved", true);
				}
			}
		} catch (RemoteException e) {
			setLabelMessage(e.getMessage(), true);
		}

		commandsPane.updateUI();
	}

	/**
	 * Send command to the current plugin
	 */
	private void executeCommand(PluginExecInfo pei, CommandDefinition cmdDef, HashMap<String, String> parameters) {
		Command cmd = new Command();
		cmd.setName(cmdDef.getName());
		cmd.setParameters(parameters);
		try {
			BasicExecutionResult result = server.sendCommandToPlugin(pei.getId(), cmd);
			if (result.isSuccess()) {
				if (result.getAdditionalData().containsKey("output")) {
					String output = (String) result.getAdditionalData().get("output");
					outputPane.setText(output);
				} else {
					outputPane.setText(result.getMessage());
				}
				refreshLoadedPlugins();
				refreshCommandsList(pei);
			} else {
				setLabelMessage("There was an error executing the command [" + cmdDef.getName() + "]: " + result.getMessage(), true);
			}
		} catch (RemoteException e) {
			setLabelMessage(e.getMessage(), true);
		}
	}
}
