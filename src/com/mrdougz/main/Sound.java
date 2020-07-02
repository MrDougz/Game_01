package com.mrdougz.main;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Sound {

	private Clip clip;
	
	public static final Sound musicBackground = new Sound("/music.wav");
	public static final Sound hurtSound = new Sound("/hurt.wav");
	
	private Sound(String name) {
		try {
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(Sound.class.getResource(name)));
		} catch(Throwable e){
			
		}
	}
	
	public void play() {
		try {
			new Thread() {
				public void run() {
					clip.loop(1);
				}
			}.start();
		} catch(Throwable e){
			
		}
	}
	
	public void loop() {
		try {
			new Thread() {
				public void run() {
					clip.loop(Clip.LOOP_CONTINUOUSLY);;
				}
			}.start();
		} catch(Throwable e){
			
		}
	}
	
}
