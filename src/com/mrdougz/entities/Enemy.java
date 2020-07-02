package com.mrdougz.entities;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.mrdougz.main.Game;
import com.mrdougz.main.Sound;
import com.mrdougz.world.Camera;
import com.mrdougz.world.World;

public class Enemy extends Entity{
	
	private float speed = 1f;
	private int frames = 0, maxFrames = 25, index = 0, maxIndex = 1;
	private BufferedImage[] sprites;
	private int maskx = 8, masky = 8, maskw = 10, maskh = 10;
	
	private int life = 2;

	
	private boolean isDamaged = false;
	private int damagedFrames = 10, damagedCurrent = 0;
	public Enemy(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, null);
		sprites = new BufferedImage[2];
		sprites[0] = Game.spritesheet.getSprite(112, 16, 16, 16);
		sprites[1] = Game.spritesheet.getSprite(128, 16, 16, 16);
	}
	
	public void tick() {
		if(isCollidingWithPlayer() == false) {
			if((int)x < Game.player.getX() && World.isFree((int)(x+speed), this.getY(), Game.player.z) &&
					!isColliding((int)(x+speed), this.getY())) {
				x+=speed;
			} else if((int)x > Game.player.getX() && World.isFree((int)(x-speed), this.getY(), Game.player.z) &&
					!isColliding((int)(x-speed), this.getY())) {
				x-=speed;
			}
			if((int)y < Game.player.getY() && World.isFree(this.getX(), (int)(y+speed), Game.player.z) &&
					!isColliding(this.getX(), (int)(y+speed))) {
				y+=speed;
			} else if((int)y > Game.player.getY() && World.isFree(this.getX(), (int)(y-speed), Game.player.z) &&
					!isColliding(this.getX(), (int)(y-speed))) {
				y-=speed;
			}
		}else {
			if(Game.rand.nextInt(100) < 10) {
				Sound.hurtSound.play();
				Player.life-=Game.rand.nextInt(4);
				Game.player.isDamaged = true;
				
			}
		}
		frames++;
		if(frames == maxFrames) {
			frames = 0;
			index++;
			if(index > maxIndex) {
				index = 0;
			}
		}
		
		collidingBullet();
		
		if(life <= 0) {
			destroySelf();
			return;
		}
		
		if(isDamaged) {
			damagedCurrent++;
			if(damagedCurrent == damagedFrames) {
				damagedCurrent = 0;
				isDamaged = false;
			}
		}
		
	}
	
	public void collidingBullet() {
		for(int i = 0; i < Game.shoots.size(); i++) {
			Entity e = Game.shoots.get(i);
			if(e instanceof Shoot){
				if(Entity.isColliding(this, e)) {
					isDamaged = true;
					this.life--;
					Game.shoots.remove(e);
					return;
				}
			}
		}
	}
	
	public void destroySelf() {
		Game.enemies.remove(this);
		Game.entities.remove(this);
	}
	
	public boolean isCollidingWithPlayer() {
		Rectangle enemyCurrent = new Rectangle(this.getX() + maskx, this.getY() + masky, maskw, maskh);
		Rectangle player = new Rectangle(Game.player.getX(), Game.player.getY(), 16, 16);
		return enemyCurrent.intersects(player);
	}
	
	public boolean isColliding(int xNext, int yNext) {
		Rectangle enemyCurrent = new Rectangle(xNext + maskx, yNext + masky, maskw, maskh);
		for(int i = 0; i < Game.enemies.size(); i++) {
			Enemy e = Game.enemies.get(i);
			if(e == this) continue;
		
			Rectangle targetEnemy = new Rectangle(e.getX() + maskx, e.getY() + masky, maskw, maskh);
			if(enemyCurrent.intersects(targetEnemy)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void render(Graphics g) {
		if(!isDamaged) {
			g.drawImage(sprites[index], this.getX() - Camera.x, this.getY() - Camera.y, null);
		} else {
			g.drawImage(Entity.ENEMY_FEEDBACK, this.getX() - Camera.x, this.getY() - Camera.y, null);
		}
	}
}
