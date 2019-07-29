package com.dezzy.skrop2_client.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import com.dezzy.skrop2_client.assets.Config;
import com.dezzy.skrop2_client.assets.Fonts;
import com.dezzy.skrop2_client.net.tcp.Client;

public class GUI extends JFrame implements ComponentListener, MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4348592485678370253L;
	
	private final MainMenuButtons mainMenuButtons;
	private final MainMenuBackground mainMenuBackground;
	private World mainMenuBackgroundWorld;
	private int points = 0;
	
	private final JoinMenu joinMenu;
	
	private Client tcpClient;
	private Thread tcpThread;
	
	private GameState gameState = GameState.MAIN_MENU;
	public GUI(int width, int height) {
		super("Skrop 2");
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
		
		setVisible(true);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ActionListener gameTickListener = e -> gameTick();
		new Timer(60, gameTickListener).start();
	}
	
	private String serverName = "";
	private int gamePort = -1;
	
	public void processServerEvent(final String event) {
		String header = event.substring(0, event.indexOf(" "));
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
							tcpClient.sendString("join-game");
						}
					}
				}
			} else if (header.equals("port")) {
				gamePort = Integer.parseInt(body);
				tcpClient.sendString("quit");
				tcpClient.stop();
				tcpClient.waitUntilStopped();
				
				gameState = GameState.WAITING_FOR_GAME_START;
				updateGameState();
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
		case JOINING_GAME:
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
		case JOIN_MENU:
			remove(mainMenuButtons);
			add(joinMenu, BorderLayout.PAGE_END);
			revalidate();
			break;
		case JOINING_GAME:
			remove(joinMenu);
			postStatus("Connecting to infoserver...", Color.BLUE, 0.5f, 0.4f);
			revalidate();
			try {
				tcpClient = new Client(this, joinMenu.ipEntry(), Integer.parseInt(joinMenu.portEntry())); //IP and port have already been validated
				tcpThread = new Thread(tcpClient);
				tcpThread.start();
				tcpClient.sendString("server-info-request");
			} catch (Exception e) {				
				gameState = GameState.JOIN_MENU;
				updateGameState();
				postStatus("Connection failed!", Color.RED, 0.5f, 0.4f);
				
				e.printStackTrace();
			}
		case WAITING_FOR_GAME_START:
			try {
				tcpClient = new Client(this, joinMenu.ipEntry(), gamePort);
				tcpThread = new Thread(tcpClient);
				tcpThread.start();
				tcpClient.sendString("init-player name:" + Config.name + " color:" + Config.color);
			} catch (Exception e) {
				gameState = GameState.JOIN_MENU;
				updateGameState();
				postStatus("Connection failed!", Color.RED, 0.5f, 0.4f);
				
				e.printStackTrace();
			}
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
	
	/**
	 * Returns true if the String is a valid port (5 digit int).
	 * 
	 * @param port String entry to be tested
	 * @return true if <code>port</code> is a valid TCP port number
	 */
	private int validatePort(final String port) {
		int out = -1;
		
		try {
			out = Integer.parseInt(port);
			
			if (port.length() > 5 || out > 65535 || out <= 0) {
				System.err.println("Enter a valid port number!");
				return -1;
			}
		} catch (Exception e) {
			System.err.println("Enter a valid port number!");
			e.printStackTrace();
		}
		
		return out;
	}
	
	private class JoinMenu extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5507894302357476220L;
		
		private final Font harambe40;
		
		private final JTextField infoserverIP;
		private final JTextField infoserverPort;
		private final JButton join;
		
		private JoinMenu() {
			harambe40 = Fonts.HARAMBE_8.deriveFont(Font.PLAIN, 40);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			infoserverIP = new JTextField("Infoserver IP", 15);
			infoserverIP.setFont(harambe40);
			infoserverIP.setAlignmentX(CENTER_ALIGNMENT);
			
			infoserverPort = new JTextField("Infoserver Port", 5);
			infoserverPort.setFont(harambe40);
			infoserverPort.setAlignmentX(CENTER_ALIGNMENT);
			
			join = new JButton("Join");
			join.setFont(harambe40);
			join.addActionListener(e -> {
				statusXPos = 0.5f;
				statusYPos = 0.4f;
				statusColor = Color.RED;
				
				if (!validateIP(ipEntry())) {
					status = "Not a valid IP!";
					showStatus = true;
				} else if (validatePort(portEntry()) == -1) {
					status = "Not a valid port!";
					showStatus = true;
				} else {
					gameState = GameState.JOINING_GAME;
					updateGameState();
				}
			});
			join.setAlignmentX(CENTER_ALIGNMENT);
			
			add(infoserverIP);
			add(Box.createRigidArea(new Dimension(0, 15)));
			add(infoserverPort);
			add(Box.createRigidArea(new Dimension(0, 15)));
			add(join);
			add(Box.createRigidArea(new Dimension(0, 15)));
			
			setOpaque(false);
		}
		
		private String ipEntry() {
			return infoserverIP.getText();
		}
		
		private String portEntry() {
			return infoserverPort.getText();
		}
		
	}
	
	private class MainMenuBackground extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2623219324374309666L;
		
		private final Font harambe40;
		private final Font harambe100;		
		
		private MainMenuBackground() {
			harambe40 = Fonts.HARAMBE_8.deriveFont(Font.PLAIN, 40);
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
			
			Font harambe40 = Fonts.HARAMBE_8.deriveFont(Font.PLAIN, 40);
			
			quit = new JButton("Quit");
			quit.setFont(harambe40);
			quit.addActionListener(e -> {
				System.exit(0);
			});
			quit.setAlignmentX(CENTER_ALIGNMENT);
			
			host = new JButton("Host");
			host.setFont(harambe40);
			host.setAlignmentX(CENTER_ALIGNMENT);
			
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
		case JOINING_GAME:
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
}
