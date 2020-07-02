package com.mrdougz.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import com.mrdougz.entities.Enemy;
import com.mrdougz.entities.Entity;
import com.mrdougz.entities.Player;
import com.mrdougz.entities.Shoot;
import com.mrdougz.graphics.HUD;
import com.mrdougz.graphics.Spritesheet;
import com.mrdougz.world.World;

public class Game extends Canvas implements Runnable, KeyListener, MouseListener{
	static final long serialVersionUID = 1L;
	
	public static JFrame frame;
	private Thread thread;
	private boolean isRunning;
	public static final int WIDTH = 240;
	public static final int HEIGHT = 160;
	public static final int SCALE = 3;
	
	private int CUR_LEVEL = 1, MAX_LEVEL = 2;
	private BufferedImage image;
	
	
	public static List<Entity> entities;
	public static List<Enemy> enemies;
	public static List<Shoot> shoots;
	public static Spritesheet spritesheet;
	
	public static World world;
	
	public static Player player;
	
	public static Random rand;
	
	public HUD hud;
	
	public static String gameState = "MENU";
	private boolean showMessageGameOver = false;
	private int framesGameOver = 0;
	private boolean restartGame = false;
	
	public Menu menu;
	
	public boolean saveGame = false;
	
	public Game() {
//		Sound.musicBackground.loop();
		rand = new Random();
		addKeyListener(this);
		addMouseListener(this);
		setPreferredSize(new Dimension(WIDTH*SCALE,HEIGHT*SCALE));
		initFrame();
		hud = new HUD();
		image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		entities = new ArrayList<Entity>();
		enemies = new ArrayList<Enemy>();
		shoots = new ArrayList<Shoot>();
		spritesheet = new Spritesheet("/spritesheet.png");
		player = new Player(0,0,16,16,spritesheet.getSprite(32, 0, 16, 16));
		entities.add(player);
		world = new World("/level1.png");
		menu = new Menu();
	}
	public void initFrame() {
		frame = new JFrame("Jooj");
		frame.add(this);
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public synchronized void start() {
		thread = new Thread(this);
		isRunning = true;
		thread.start();
	}
	
	public synchronized void stop() {
		isRunning = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		Game game = new Game();
		game.start();
	}
	
	public void tick() {
		if(gameState == "NORMAL") {
			if(this.saveGame) {
				this.saveGame = false;
				String[] opt1 = {"level"};
				int[] opt2 = {this.CUR_LEVEL};
				Menu.saveGame(opt1, opt2, 10);
				System.out.println("Game saved!");
			}
			this.restartGame = false;
			for(int i = 0; i < entities.size(); i++) {
				Entity e = entities.get(i);
				e.tick();
			}
			
			for(int i = 0; i < shoots.size(); i++) {
				shoots.get(i).tick();
			}
			
			if(enemies.size() == 0) {
				CUR_LEVEL++;
				if(CUR_LEVEL > MAX_LEVEL) {
					CUR_LEVEL = 1;
				}
				String newWorld = "level"+CUR_LEVEL+".png";
				World.restartGame(newWorld);
			}
		} else if(gameState == "GAME_OVER") {
			framesGameOver++;
			if(framesGameOver == 32) {
				framesGameOver = 0;
				if(showMessageGameOver) {
					showMessageGameOver = false;
				} else if(!showMessageGameOver) {
					showMessageGameOver = true;
				}
			}
			if(restartGame) {
				restartGame = false;
				gameState = "NORMAL";
				CUR_LEVEL = 1;
				String newWorld = "level"+CUR_LEVEL+".png";
				World.restartGame(newWorld);
			}
		} else if(gameState == "MENU") {
			menu.tick();
		}
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = image.getGraphics();
		g.setColor(new Color(0, 0, 0));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		world.render(g);
		
		for(int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			e.render(g);
		}
		for(int i = 0; i < shoots.size(); i++) {
			shoots.get(i).render(g);
		}
		
		hud.render(g);
		g.dispose();
		g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0,WIDTH*SCALE,HEIGHT*SCALE,null);
		g.setFont(new Font("Arial", Font.CENTER_BASELINE, 20));
		g.setColor(Color.white);
		g.drawString("Ammo: " + player.ammo, 620, 12*SCALE);
		if(gameState == "GAME_OVER") {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(new Color(0, 0, 0, 100));
			g2.fillRect(0, 0, WIDTH*SCALE, HEIGHT*SCALE);
			g.setFont(new Font("Arial", Font.BOLD, 50));
			g.setColor(Color.white);
			g.drawString("Game Over", (WIDTH*SCALE)/2-38*SCALE, (HEIGHT*SCALE)/2);
			g.setFont(new Font("Arial", Font.CENTER_BASELINE, 26));
			if(showMessageGameOver) {
				g.drawString("> Press Enter to restart <", (WIDTH*SCALE)/2-44*SCALE, (HEIGHT*SCALE)/2 + 40);
			}
		} else if(gameState == "MENU") {
			menu.render(g);
		}
		bs.show();
	}

	public void run(){
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000/amountOfTicks;
		double delta = 0;
		int frames = 0;
		double timer = System.currentTimeMillis();
		requestFocus();
		while(isRunning) {
			long now = System.nanoTime();
			delta+=(now-lastTime) / ns;
			lastTime = now;
			if(delta >= 1) {
				tick();
				render();
				frames++;
				delta--;
			}
			if(System.currentTimeMillis() - timer >= 1000){
				System.out.println("Fps:"+frames);
				frames = 0;
				timer+=1000;
			}
		}
		
		stop();
		
	}
	@Override
	public void keyTyped(KeyEvent e) {
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		
		if(e.getKeyCode() == KeyEvent.VK_Z) {
			player.jump = true;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_RIGHT ||
				e.getKeyCode() == KeyEvent.VK_D) {
			player.right = true;
		} else if(e.getKeyCode() == KeyEvent.VK_LEFT ||
				e.getKeyCode() == KeyEvent.VK_A){
			player.left = true;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_UP ||
				e.getKeyCode() == KeyEvent.VK_W) {
			player.up = true;
			
			if(gameState == "MENU") {
				menu.up = true;
			}
		} else if(e.getKeyCode() == KeyEvent.VK_DOWN ||
				e.getKeyCode() == KeyEvent.VK_S){
			player.down = true;
			
			if(gameState == "MENU") {
				menu.down = true;
			}
		}
		
		if(e.getKeyCode() == KeyEvent.VK_X ||
				e.getKeyCode() == KeyEvent.VK_SHIFT) {
			player.shoot = true;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			restartGame = true;
			
			if(gameState == "MENU") {
				menu.enter = true;
			}
		}
		
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			gameState = "MENU";
		}
		
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			this.saveGame = true;
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
			player.right = false;
		} else if(e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A){
			player.left = false;
		}
		if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
			player.up = false;
		} else if(e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
			player.down = false;
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		player.mouseShoot = true;
		player.mx = (e.getX() / SCALE);
		player.my = (e.getY() / SCALE);
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
