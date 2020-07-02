package com.mrdougz.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.mrdougz.main.Game;
import com.mrdougz.world.Camera;
import com.mrdougz.world.World;

public class Player extends Entity{

	public boolean right, up, left, down;
	public int right_dir = 0, left_dir = 1;
	public int dir = right_dir;
	public double speed = 1.4;
	
	private int frames = 0, maxFrames = 5, index = 0, maxIndex = 3;
	private boolean moved = false;	
	private BufferedImage[] rightPlayer;
	private BufferedImage[] leftPlayer;
	
	private BufferedImage playerDamage;
	
	private boolean hasGun;
	
	public int ammo = 0;
	
	public boolean isDamaged = false;
	private int damagedFrames = 0;
	
	public static float life = 100f, maxLife = 100f;
	public int mx, my;
	public boolean shoot = false, mouseShoot = false;
	
	public boolean jump = false;
	public boolean isJumpping = false;
	public int jumpFrames = 32, jumpCur = 0;
	public int jumpSpeed = 2;
	
	public boolean jumpUp = false, jumpDown = false;
	
	public int z = 0;
	
	public Player(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
		
		rightPlayer = new BufferedImage[4];
		leftPlayer = new BufferedImage[4];
		playerDamage = Game.spritesheet.getSprite(0, 16, 16, 16);
		for(int i = 0; i < 4; i++) {
			rightPlayer[i] = Game.spritesheet.getSprite(32 + (i*16), 0, 16, 16);
		}
		for(int i = 0; i < 4; i++) {
			leftPlayer[i] = Game.spritesheet.getSprite(32 + (i*16), 16, 16, 16);
		}
		
	}
	
	public void tick() {
		if(jump) {
			if(isJumpping == false) {
				jump = false;
				isJumpping = true;
				jumpUp = true;
			}
		}
		
		if(isJumpping) {
			if(jumpUp) {
				jumpCur+=jumpSpeed;					
			} else if(jumpDown) {
				jumpCur-=jumpSpeed;
				if(jumpCur <= 0) {
					isJumpping = false;
					jumpDown = false;
					jumpUp = false;
				}
			}
			z = jumpCur;
			if(jumpCur >= jumpFrames) {
				jumpUp = false;
				jumpDown = true;
			}
		}
		
		moved = false;
		if(right && World.isFree((int)(this.getX()+(int)speed),this.getY(), z)) {
			moved = true;
			dir = right_dir;
			x+=speed;
		}
		else if(left && World.isFree((int)(this.getX()-speed),this.getY(), z)) {
			moved = true;
			dir = left_dir;
			x-=speed;
		}
		if(up && World.isFree(this.getX(),(int)(this.getY()-speed), z)) {
			moved = true;
			y-=speed;
		}
		else if(down && World.isFree(this.getX(),(int)(this.getY()+speed), z)) {
			moved = true;
			y+=speed;
		}
		
		if(moved) {
			frames++;
			if(frames == maxFrames) {
				frames = 0;
				index++;
				if(index > maxIndex) {
					index = 0;
				}
			}
		}
		
		if(mouseShoot) {
			mouseShoot = false;
			float angle = (float) Math.atan2(my - (this.getY()+8 - Camera.y), mx - (this.getX()+8 - Camera.x));
			if(hasGun && ammo > 0) {
				ammo--;
				float dx = (float) Math.cos(angle);
				float dy = (float) Math.sin(angle);
				int px = 0, py = 8;
				Shoot bullet = new Shoot(this.getX()+px, this.getY()+py, 3, 3, null, dx, dy);
				Game.shoots.add(bullet);
			}
		}
		
		if(life <= 0) {
			Game.gameState = "GAME_OVER";
		}
		
		checkCollisionGun();
		checkCollisionLifepack();
		checkCollisionAmmo();
		
		if(isDamaged) {
			this.damagedFrames++;
			if(this.damagedFrames == 8) {
				this.damagedFrames = 0;
				isDamaged = false;
			}
			
		}
		
		if(shoot) {
			shoot = false;
			if(hasGun && ammo > 0) {
				ammo--;
				int dx = 0;
				int px = 0;
				int py = 6;
				if(dir == right_dir) {
					px = 18;
					dx = 1;
				} else {
					px = -8;
					dx = -1;
				}
				
				Shoot bullet = new Shoot(this.getX()+px, this.getY()+py, 3, 3, null, dx, 0);
				Game.shoots.add(bullet);
			}
		}
		updateCamera();
	}
	
	public void updateCamera() {
		Camera.x = Camera.clamp(this.getX() - (Game.WIDTH/2), 0,World.WIDTH*16 - Game.WIDTH);
		Camera.y = Camera.clamp(this.getY() - (Game.HEIGHT/2), 0,World.HEIGHT*16 - Game.HEIGHT);
	}
	
	public void checkCollisionGun() {
		for(int i = 0; i < Game.entities.size(); i++) {
			Entity e = Game.entities.get(i);
			if(e instanceof Weapon) {
				if(Entity.isColliding(this, e)) {
					hasGun = true;
					Game.entities.remove(i);
				}
			}
		}
	}
	
	public void checkCollisionAmmo() {
		for(int i = 0; i < Game.entities.size(); i++) {
			Entity e = Game.entities.get(i);
			if(e instanceof Bullet) {
				if(Entity.isColliding(this, e)) {
					ammo+=10;
					Game.entities.remove(i);
				}
			}
		}
	}
	
	public void checkCollisionLifepack() {
		for(int i = 0; i < Game.entities.size(); i++) {
			Entity e = Game.entities.get(i);
			if(e instanceof Lifepack) {
				if(Entity.isColliding(this, e)) {
					life+=8;
					if(life > 100) life = 100;
					Game.entities.remove(i);
					return;
				}
			}
		}
	}
	
	public void render(Graphics g) {
		if(!isDamaged) {
			if(dir == right_dir) {
				g.drawImage(rightPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y - z, null);
				if(hasGun) {
					g.drawImage(Entity.GUN_LEFT, this.getX()+8 - Camera.x, this.getY() - Camera.y - z, null);
				}
			} else if(dir == left_dir) {
				g.drawImage(leftPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y - z, null);
				if(hasGun) {
					g.drawImage(Entity.GUN_RIGHT, this.getX()-8 - Camera.x, this.getY() - Camera.y - z, null);
				}
			}
		} else {
			g.drawImage(playerDamage, this.getX() - Camera.x, this.getY() - Camera.y - z, null);
			if(hasGun) {
				if(dir == left_dir) {
					g.drawImage(Entity.GUN_DAMAGED_LEFT, this.getX() - 8 - Camera.x, this.getY() - Camera.y - z, null);
				} else {
					g.drawImage(Entity.GUN_DAMAGED_RIGHT, this.getX() + 8 - Camera.x, this.getY() - Camera.y - z, null);
				}
			}
		}
		
		if(isJumpping) {
			g.setColor(Color.black);
			g.fillOval(this.getX() - Camera.x + 4, this.getY() - Camera.y + 16, 8, 8);
		}
	}

}
