package com.mrdougz.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import com.mrdougz.entities.Player;

public class HUD {

	public void render(Graphics g) {
		g.setColor(new Color(123,32,22));
		g.fillRect(8, 6, 50,8);
		g.setColor(new Color(12,111,15));
		g.fillRect(8, 6, (int)((Player.life/Player.maxLife)*50),8);
		g.setColor(Color.white);
		g.setFont(new Font("Arial", Font.CENTER_BASELINE, 8));
		g.drawString((int)Player.life + "/"+(int)Player.maxLife ,10, 22);
	}
	
}
