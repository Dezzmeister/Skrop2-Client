package com.dezzy.skrop2_client.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.text.NumberFormatter;

import com.dezzy.skrop2_client.assets.Config;
import com.dezzy.skrop2_client.assets.Fonts;
import com.dezzy.skrop2_client.net.tcp.Client;

public class GUI extends JFrame implements ComponentListener, MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4348592485678370253L;
	
	private final Font harambe40;
	private final Font harambe32;
	private final Font harambe20;
	
	private final MainMenuButtons mainMenuButtons;
	private final MainMenuBackground mainMenuBackground;
	private World mainMenuBackgroundWorld;
	private int points = 0;
	
	private final JoinMenu joinMenu;
	private final HostMenu hostMenu;
	
	private final ClientController clientController;
	private final Thread clientControllerThread;
	private Client tcpClient;
	private Thread tcpThread;
	
	private Game game;
	
	private GameState gameState = GameState.MAIN_MENU;
	
	
	public GUI(int width, int height) {
		super("Skrop 2");
		
		harambe40 = Fonts.HARAMBE_8.deriveFont(Font.PLAIN, 40);
		harambe32 = Fonts.HARAMBE_8.deriveFont(Font.PLAIN, 32);
		harambe20 = Fonts.HARAMBE_8.deriveFont(Font.PLAIN, 20);
		
		setSize(width, height);
		setLayout(new BorderLayout());
		addComponentListener(this);
		addMouseListener(this);
		
		mainMenuBackground = new MainMenuBackground();
		setContentPane(mainMenuBackground);
		getContentPane().setLayout(new BorderLayout());
		
		mainMenuBackgroundWorld = new World(10);
		
		mainMenuButtons = new MainMenuButtons();
		add(mainMenuButtons, BorderLayout.PAGE_END);
		
		joinMenu = new JoinMenu();
		hostMenu = new HostMenu();
		
		setVisible(true);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ActionListener gameTickListener = e -> gameTick();
		new Timer(60, gameTickListener).start();
		
		clientController = new ClientController(this);
		clientControllerThread = new Thread(clientController, "Skrop 2 Client Controller Thread");
		clientControllerThread.start();
	}
	
	private String serverName = "";
	private int gamePort = -1;
	
	public void processServerEvent(final String event) {
		String header = event.contains(" ") ? event.substring(0, event.indexOf(" ")) : event;
		String body = event.substring(event.indexOf(" ") + 1);
		
		System.out.println(event);
		
		if (gameState == GameState.JOINING_GAME) {
			if (header.equals("server-info")) {
				String[] fields = body.split(" ");
				
				for (String field : fields) {
					String fieldHeader = field.substring(0, field.indexOf(":"));
					String fieldBody = field.substring(field.indexOf(":") + 1);
					
					if (fieldHeader.equals("name")) {
						serverName = fieldBody;
					} else if (fieldHeader.equals("game-running")) {
						if (fieldBody.equals("false")) {
							tcpClient.sendString("quit");
							gameState = GameState.JOIN_MENU;
							updateGameState();
							postStatus("No game running on this server!", Color.YELLOW, 0.5f, 0.4f);
						} else {
							tcpClient.sendString("game-info-request");
						}
					}
				}
			} else if (header.equals("game-info")) {
				game = new Game(body);
				tcpClient.sendString("join-game");
			} else if (header.equals("port")) {
				gamePort = Integer.parseInt(body);
				
				tcpClient.sendString("quit");
				tcpClient.stop();
				
				try {
					tcpThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				gameState = GameState.WAITING_FOR_GAME_START;
				updateGameState();
			} else if (header.equals("game-full")) {
				tcpClient.sendString("quit");
				tcpClient.stop();
				
				try {
					tcpThread.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				gameState = GameState.JOIN_MENU;
				updateGameState();
				postStatus("Game is full!", Color.YELLOW, 0.5f, 0.4f);
			}
		} else if (gameState == GameState.WAITING_FOR_GAME_START) {
			if (header.equals("player-list")) {
				List<Player> players = new ArrayList<Player>();
				String[] fields = body.split(" ");
				
				for (int i = 0; i < fields.length - 1; i += 2) {
					String name = fields[i].substring(fields[i].indexOf(":") + 1);
					Color color = new Color(Integer.parseInt(fields[i + 1].substring(fields[i + 1].indexOf(":") + 1)));
					
					players.add(new Player(name, color));
				}
				
				game.players = players.toArray(new Player[players.size()]);
			}
		} else if (gameState == GameState.HOSTING_GAME) {
			if (header.equals("server-info")) {
				String[] fields = body.split(" ");
				
				for (String field : fields) {
					String fieldHeader = field.substring(0, field.indexOf(":"));
					String fieldBody = field.substring(field.indexOf(":") + 1);
					
					if (fieldHeader.equals("name")) {
						serverName = fieldBody;
					} else if (fieldHeader.equals("game-running")) {
						if (fieldBody.equals("false")) {
							//Create a game
							String gameName = hostMenu.gameName();
							int maxPlayers = hostMenu.maxPlayers();
							SkropWinCondition winCondition = hostMenu.winCondition();
							int winConditionArg = hostMenu.winConditionArg();
							
							tcpClient.sendString("create-game name:" + gameName.replace(' ', '_') + " max-players:" + maxPlayers + " win-condition:" + winCondition.getName() + " win-condition-arg:" + winConditionArg);
						} else {
							tcpClient.sendString("quit");
							tcpClient.stop();
							gameState = GameState.HOST_MENU;
							updateGameState();
							postStatus("Game already running on this server!", Color.YELLOW, 0.5f, 0.4f);
						}
					}
				}
			} else if (header.equals("game-info")) {
				game = new Game(body);
				tcpClient.sendString("join-game");
			} else if (header.equals("port")) {
				gamePort = Integer.parseInt(body);
				
				tcpClient.sendString("quit");
				tcpClient.stop();
				
				try {
					tcpThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				gameState = GameState.WAITING_FOR_GAME_START;
				updateGameState();
			} else if (header.equals("game-full")) {
				tcpClient.sendString("quit");
				tcpClient.stop();
				
				try {
					tcpThread.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				gameState = GameState.HOST_MENU;
				updateGameState();
				postStatus("Game is full!", Color.YELLOW, 0.5f, 0.4f);
			}
		}
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void gameTick() {
		switch (gameState) {
		case MAIN_MENU:
		case JOIN_MENU:
		case HOST_MENU:
		case JOINING_GAME:
		case HOSTING_GAME:
		case WAITING_FOR_GAME_START:
			mainMenuBackgroundWorld.update();
			mainMenuBackground.repaint();
			break;
		default:
			break;
		}
	}
	
	private String status = "";
	private Color statusColor = Color.RED;
	private boolean showStatus = false;
	private float statusXPos = 0;
	private float statusYPos = 0;
	
	private void updateGameState() {
		showStatus = false;
		
		switch(gameState) {
		case MAIN_MENU:
			remove(joinMenu);
			remove(hostMenu);
			add(mainMenuButtons, BorderLayout.PAGE_END);
			break;
		case JOIN_MENU:
			remove(mainMenuButtons);
			add(joinMenu, BorderLayout.PAGE_END);
			revalidate();
			break;
		case HOST_MENU:
			remove(mainMenuButtons);
			add(hostMenu, BorderLayout.PAGE_END);
			revalidate();
			break;
		case JOINING_GAME:
			remove(joinMenu);
			postStatus("Connecting to infoserver...", Color.BLUE, 0.5f, 0.4f);
			revalidate();
			try {
				tcpClient = new Client(clientController, joinMenu.fields.ipEntry(), joinMenu.fields.portEntry()); //IP and port have already been validated
				tcpThread = new Thread(tcpClient, "Skrop 2 TCP Client Thread");
				tcpThread.start();
				tcpClient.sendString("server-info-request");
			} catch (Exception e) {				
				gameState = GameState.JOIN_MENU;
				updateGameState();
				postStatus("Connection failed!", Color.RED, 0.5f, 0.4f);
				
				e.printStackTrace();
			}
			break;
		case HOSTING_GAME:
			remove(hostMenu);
			postStatus("Connecting to infoserver...", Color.BLUE, 0.5f, 0.4f);
			revalidate();
			
			try {
				tcpClient = new Client(clientController, hostMenu.infoserverFields.ipEntry(), hostMenu.infoserverFields.portEntry());
				tcpThread = new Thread(tcpClient, "Skrop 2 TCP Client Thread");
				tcpThread.start();
				tcpClient.sendString("server-info-request");
			} catch (Exception e) {
				gameState = GameState.HOST_MENU;
				updateGameState();
				postStatus("Connection failed!", Color.RED, 0.5f, 0.4f);
				
				e.printStackTrace();
			}
			break;
		case WAITING_FOR_GAME_START:
			
			try {
				tcpClient = new Client(clientController, joinMenu.fields.ipEntry(), gamePort);
				tcpThread = new Thread(tcpClient, "Skrop 2 TCP Client Thread");
				tcpThread.start();
				tcpClient.sendString("init-player name:" + Config.name + " color:" + Config.color);
			} catch (Exception e) {
				gameState = GameState.JOIN_MENU;
				updateGameState();
				postStatus("Connection failed!", Color.RED, 0.5f, 0.4f);
				
				e.printStackTrace();
			}
			break;
		}
	}
	
	private void postStatus(final String _status, final Color _statusColor, float _statusXPos, float _statusYPos) {
		status = _status;
		statusColor = _statusColor;
		statusXPos = _statusXPos;
		statusYPos = _statusYPos;
		showStatus = true;
	}
	
	/**
	 * Returns true if the IP address is a valid IP address.
	 * 
	 * @param ip String to be validated
	 * @return true if <code>ip</code> is a valid IP address
	 */
	private boolean validateIP(final String ip) {
		String expression = "(\\d{1,3}[\\.]){3}\\d{1,3}";
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(ip);
		
		return (matcher.find() && ip.equals(matcher.group(0))) || ip.equals("localhost");
	}
	
	private class HostMenu extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -5567380506654423856L;

		private final InfoserverFields infoserverFields;
		
		private final JPanel gameInfoFields;
		
		private final JLabel gameNameLabel;
		private final JTextField gameName;
		
		private final JLabel maxPlayersLabel;
		private final JFormattedTextField maxPlayers;
		
		private final JPanel winConditionPanel;		
		private final JLabel winConditionLabel;
		private final JComboBox<String> winCondition;		
		private final JLabel winConditionArgLabel;
		private final JFormattedTextField winConditionArg;
		
		private final JButton host;
		private final JButton back;
		
		private HostMenu() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
			infoserverFields = new InfoserverFields(harambe32);
			infoserverFields.setAlignmentX(CENTER_ALIGNMENT);
			
			gameInfoFields = new JPanel();
			gameInfoFields.setLayout(new GridBagLayout());
			gameInfoFields.setOpaque(false);
			
			gameName = new JTextField(40);
			gameName.setFont(harambe32);
			
			gameNameLabel = new JLabel("Game Name: ", JLabel.TRAILING);
			gameNameLabel.setFont(harambe32);
			gameNameLabel.setForeground(Color.BLUE);
			gameNameLabel.setLabelFor(gameName);
			
			NumberFormatter maxPlayersFormatter = new CorrectNumberFormatter(NumberFormat.getIntegerInstance());
			maxPlayersFormatter.setValueClass(Integer.class);
			maxPlayersFormatter.setAllowsInvalid(false);
			maxPlayersFormatter.setMinimum(2);
			maxPlayersFormatter.setMaximum(9);
			
			maxPlayers = new JFormattedTextField(maxPlayersFormatter);
			maxPlayers.setFont(harambe32);
			
			maxPlayersLabel = new JLabel("Max Players: ", JLabel.TRAILING);
			maxPlayersLabel.setFont(harambe32);
			maxPlayersLabel.setForeground(Color.BLUE);
			maxPlayersLabel.setLabelFor(maxPlayers);
			
			winConditionPanel = new JPanel();
			winConditionPanel.setLayout(new FlowLayout());
			winConditionPanel.setOpaque(false);
			winConditionPanel.setAlignmentX(CENTER_ALIGNMENT);
			
			NumberFormatter winConditionArgFormatter = new CorrectNumberFormatter(NumberFormat.getIntegerInstance());
			winConditionArgFormatter.setValueClass(Integer.class);
			winConditionArgFormatter.setAllowsInvalid(false);
			winConditionArgFormatter.setMinimum(1);
			winConditionArgFormatter.setMaximum(5000);
			
			winConditionArg = new JFormattedTextField(winConditionArgFormatter);
			winConditionArg.setFont(harambe32);
			winConditionArg.setColumns(4);
			
			winConditionArgLabel = new JLabel("Points");
			winConditionArgLabel.setFont(harambe32);
			winConditionArgLabel.setForeground(Color.YELLOW);
			winConditionArgLabel.setLabelFor(winConditionArg);
			
			winCondition = new JComboBox<String>(Arrays.stream(SkropWinCondition.values()).map(c -> c.shortName).toArray(size -> new String[size]));
			winCondition.setFont(harambe32);
			winCondition.addActionListener(a -> {
				String condition = (String) winCondition.getSelectedItem();
				
				for (SkropWinCondition cond : SkropWinCondition.values()) {
					if (cond.shortName.equals(condition)) {
						if (condition.contains("Timer")) {
							winConditionArgLabel.setText("Seconds");
						} else {
							if (cond.countRects) {
								winConditionArgLabel.setText("Rectangles");
							} else {
								winConditionArgLabel.setText("Points");
							}
						}
					}
				}
			});
			
			host = new JButton("Host");
			host.setFont(harambe32);
			host.setAlignmentX(CENTER_ALIGNMENT);
			host.addActionListener(a -> {
				statusXPos = 0.5f;
				statusYPos = 0.4f;
				statusColor = Color.RED;
				
				if (!validateIP(infoserverFields.ipEntry())) {
					status = "Not a valid IP!";
					showStatus = true;
				} else if (infoserverFields.portEntry() == -1 || maxPlayers() == -1 || winConditionArg() == -1) {
					status = "Don't leave any fields blank!";
					showStatus = true;
				} else {
					gameState = GameState.HOSTING_GAME;
					updateGameState();
				}
			});
			
			back = new JButton("Back");
			back.setFont(harambe20);
			back.setAlignmentX(CENTER_ALIGNMENT);
			back.addActionListener(a -> {
				gameState = GameState.MAIN_MENU;
				updateGameState();
			});
			
			winConditionLabel = new JLabel("Win Condition: ");
			winConditionLabel.setFont(harambe32);
			winConditionLabel.setForeground(Color.BLUE);
			winConditionLabel.setLabelFor(winCondition);
			
			winConditionPanel.add(winCondition);
			winConditionPanel.add(winConditionArg);
			winConditionPanel.add(winConditionArgLabel);
			
			JPanel winConditionAlignmentPanel = new JPanel();
			winConditionAlignmentPanel.setOpaque(false);
			winConditionAlignmentPanel.setLayout(new BorderLayout());
			winConditionAlignmentPanel.setBorder(BorderFactory.createEmptyBorder());
			winConditionAlignmentPanel.add(winConditionPanel, BorderLayout.WEST);
			
			gameInfoFields.add(gameNameLabel, infoserverFields.left);
			gameInfoFields.add(gameName, infoserverFields.right);
			gameInfoFields.add(maxPlayersLabel, infoserverFields.left);
			gameInfoFields.add(maxPlayers, infoserverFields.right);
			gameInfoFields.add(winConditionLabel, infoserverFields.left);
			
			gameInfoFields.add(winConditionAlignmentPanel, infoserverFields.right);
			
			add(gameInfoFields);
			add(Box.createRigidArea(new Dimension(0, 15)));
			add(infoserverFields);
			add(Box.createRigidArea(new Dimension(0, 15)));
			add(host);
			add(Box.createRigidArea(new Dimension(0, 5)));
			add(back);
		}
		
		private String gameName() {
			return gameName.getText().isEmpty() ? "Skrop 2 Game" : gameName.getText();
		}
		
		private int maxPlayers() {
			int value = -1;
			
			try {
				maxPlayers.commitEdit();
				value = (int) maxPlayers.getValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return value;
		}
		
		private SkropWinCondition winCondition() {			
			for (var cond : SkropWinCondition.values()) {
				if (cond.shortName.equals(winCondition.getSelectedItem())) {
					return cond;
				}
			}
			
			return null;
		}
		
		private int winConditionArg() {
			int value = -1;
			
			try {
				winConditionArg.commitEdit();
				value = (int) winConditionArg.getValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return value;
		}
	}
	
	
	private class InfoserverFields extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6139913968660130114L;
		
		private final JLabel infoserverIPLabel;
		private final JTextField infoserverIP;
		
		private final JLabel infoserverPortLabel;
		private final JFormattedTextField infoserverPort;
		
		private final GridBagConstraints left;
		private final GridBagConstraints right;
		
		public InfoserverFields(final Font font) {
			setLayout(new GridBagLayout());
			setOpaque(false);
			
			infoserverIP = new JTextField(15);
			infoserverIP.setFont(font);
			
			infoserverIPLabel = new JLabel("Infoserver IP: ", JLabel.TRAILING);
			infoserverIPLabel.setFont(font);
			infoserverIPLabel.setForeground(new Color(0, 191, 255));
			infoserverIPLabel.setLabelFor(infoserverIP);
			
			NumberFormatter formatter = new CorrectNumberFormatter(NumberFormat.getIntegerInstance());
			formatter.setValueClass(Integer.class);
			formatter.setAllowsInvalid(false);
			formatter.setMinimum(0);
			formatter.setMaximum(65535);
			infoserverPort = new JFormattedTextField(formatter);
			infoserverPort.setFont(font);
			infoserverPort.setColumns(5);
			
			infoserverPortLabel = new JLabel("Infoserver Port: ", JLabel.TRAILING);
			infoserverPortLabel.setFont(font);
			infoserverPortLabel.setForeground(new Color(0, 191, 255));
			infoserverPortLabel.setLabelFor(infoserverPort);
			
			left = new GridBagConstraints();
			left.anchor = GridBagConstraints.EAST;
			
			right = new GridBagConstraints();
			right.weightx = 2.0;
			right.fill = GridBagConstraints.HORIZONTAL;
			right.gridwidth = GridBagConstraints.REMAINDER;
			
			add(infoserverIPLabel, left);
			add(infoserverIP, right);
			add(infoserverPortLabel, left);
			add(infoserverPort, right);
		}
		
		private String ipEntry() {
			return infoserverIP.getText();
		}
		
		private int portEntry() {
			int value = -1;
			
			try {
				infoserverPort.commitEdit();
				value = (int) infoserverPort.getValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return value;
		}
	}
	
	private class JoinMenu extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5507894302357476220L;
		
		private final InfoserverFields fields;
		
		private final JButton join;
		private final JButton back;
		
		private JoinMenu() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			setOpaque(false);
			
			fields = new InfoserverFields(harambe40);
			fields.setAlignmentX(CENTER_ALIGNMENT);
			
			join = new JButton("Join");
			join.setFont(harambe40);
			join.setAlignmentX(CENTER_ALIGNMENT);
			join.addActionListener(e -> {
				statusXPos = 0.5f;
				statusYPos = 0.4f;
				statusColor = Color.RED;
				
				if (!validateIP(fields.ipEntry())) {
					status = "Not a valid IP!";
					showStatus = true;
				} else if (fields.portEntry() == -1) {
					status = "Not a valid port!";
					showStatus = true;
				} else {
					gameState = GameState.JOINING_GAME;
					updateGameState();
				}
			});
			
			back = new JButton("Back");
			back.setFont(harambe20);
			back.setAlignmentX(CENTER_ALIGNMENT);
			back.addActionListener(a -> {
				gameState = GameState.MAIN_MENU;
				updateGameState();
			});
			
			add(fields);
			add(Box.createRigidArea(new Dimension(0, 15)));
			add(join);
			add(Box.createRigidArea(new Dimension(0, 5)));
			add(back);
		}		
	}
	
	private class MainMenuBackground extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2623219324374309666L;
		
		private final Font harambe100;		
		
		private MainMenuBackground() {
			harambe100 = Fonts.HARAMBE_8.deriveFont(Font.PLAIN, 100);
			setLayout(null);
		}
		
		@Override
		public void paintComponent(Graphics g) {			
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, getWidth(), getHeight());
			
			for (Rectangle r : mainMenuBackgroundWorld.rects) {
				int xPos = (int)(r.x * getWidth());
				int yPos = (int)(r.y * getHeight());
				
				g2.setColor(new Color(r.color));
				g2.fillRect(xPos - (int)(r.size/2.0f * getWidth()), yPos - (int)(r.size/2.0f * getHeight()), (int)(r.size * getWidth()), (int)(r.size * getHeight()));
			}
			
			if (gameState == GameState.WAITING_FOR_GAME_START) {
				g2.setFont(harambe40);
				
				if (game != null && game.players != null) {
					for (int i = 0; i < game.players.length; i++) {
						g2.setColor(game.players[i].color);
						placeTextAt(game.players[i].name, getWidth()/16, (getHeight()/16) + ((i + 1) * 50), g);
					}
					
					g2.setColor(Color.BLUE);
					placeTextAt(game.players.length + "/" + game.maxPlayers, getWidth()/16, (getHeight()/16) + ((game.players.length + 2) * 50), g);
				}
			}
			
			g.setColor(Color.RED);
			g.setFont(harambe40);
			
			placeTextAt("" + points, getWidth()/16, getHeight()/16, g);
			
			g.setColor(Color.GREEN);
			g.setFont(harambe100);
			
			placeTextAt("SKROP 2", getWidth()/2, getHeight()/16, g);
			
			if (showStatus) {
				g.setColor(statusColor);
				g.setFont(harambe40);
				
				placeTextAt(status, (int)(statusXPos * getWidth()), (int)(statusYPos * getHeight()), g);
			}
		}
	}
	
	private void placeTextAt(final String text, int x, int y, final Graphics g) {
		FontMetrics metrics = g.getFontMetrics();
		
		int xPos = x - (metrics.stringWidth(text)/2);
		int yPos = y - (metrics.getHeight() / 2) + metrics.getAscent();
		
		if (y < metrics.getHeight()) {
			y = metrics.getHeight();
		}
		
		((Graphics2D) g).drawString(text, xPos, yPos);
	}
	
	private class MainMenuButtons extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1212937226493579383L;
		
		private final JButton join;
		private final JButton host;
		private final JButton quit;
		
		private MainMenuButtons() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			quit = new JButton("Quit");
			quit.setFont(harambe40);
			quit.addActionListener(e -> {
				System.exit(0);
			});
			quit.setAlignmentX(CENTER_ALIGNMENT);
			
			host = new JButton("Host");
			host.setFont(harambe40);
			host.setAlignmentX(CENTER_ALIGNMENT);
			host.addActionListener(a -> {
				gameState = GameState.HOST_MENU;
				updateGameState();
			});
			
			join = new JButton("Join");
			join.setFont(harambe40);
			join.addActionListener(e -> {				
				gameState = GameState.JOIN_MENU;
				updateGameState();
			});
			join.setAlignmentX(CENTER_ALIGNMENT);
			
			add(join);
			add(Box.createRigidArea(new Dimension(0, 15)));
			add(host);
			add(Box.createRigidArea(new Dimension(0, 15)));
			add(quit);
			add(Box.createRigidArea(new Dimension(0, 15)));
			
			setOpaque(false);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		switch (gameState) {
		case MAIN_MENU:
		case JOIN_MENU:
		case HOST_MENU:
		case JOINING_GAME:
		case HOSTING_GAME:
		case WAITING_FOR_GAME_START:
			int pointsReceived = mainMenuBackgroundWorld.checkClick(e.getX()/(float)getWidth(), e.getY()/(float)getHeight());
			points += pointsReceived;
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Fixes a problem that occurs when using NumberFormatter in a JFormattedTextField; once the first character is entered it cannot be deleted.
	 * 
	 * @author Dezzmeister
	 *
	 */
	private class CorrectNumberFormatter extends NumberFormatter {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -4609324581668728238L;

		public CorrectNumberFormatter(final NumberFormat format) {
			super(format);
		}
		
		@Override
		public Object stringToValue(String text) throws ParseException {
			return text.equals("") ? null : super.stringToValue(text);
		}
	}
}
