package com.mrdougz.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.mrdougz.main.Game;
import com.mrdougz.world.Camera;

public class Shoot extends Entity{

	private float dx;
	private float dy;
	private float spd = 4f;
	
	private int life = 30, currLife = 0;
	
	public Shoot(int x, int y, int width, int height, BufferedImage sprite, float dx, float dy) {
		super(x, y, width, height, sprite);
		this.dx = dx;
		this.dy = dy;
	}

	public void tick() {
		x+=dx*spd;
		y+=dy*spd;
		currLife++;
		if(currLife == life) {
			Game.shoots.remove(this);
			return;
		}
	}
	
	public void render(Graphics g) {
		g.setColor(Color.yellow);
		g.fillOval(this.getX() - Camera.x, this.getY() - Camera.y, width, height);
	}
	
}
