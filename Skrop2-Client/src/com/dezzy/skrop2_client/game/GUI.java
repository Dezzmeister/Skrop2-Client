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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.dezzy.skrop2_client.assets.Fonts;

public class GUI extends JFrame implements ComponentListener, MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4348592485678370253L;
	
	private final JPanel mainMenuButtons;
	private final JPanel mainMenuBackground;
	private World mainMenuBackgroundWorld;
	private int points = 0;
	
	private final JPanel joinMenu;
	
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
			mainMenuBackgroundWorld.update();
			mainMenuBackground.repaint();
			break;
		default:
			break;
		}
	}
	
	private void updateGameState() {
		switch(gameState) {
		case JOIN_MENU:
			
		}
	}
	
	private class JoinMenu extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5507894302357476220L;
		
		
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
