package mrbesen.cr.auto.clicker;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;

public class Overlay {

	JFrame frame;
	Point spawn;
	Point cards[];
	Point ok;
	Point battle;
	Point arenaview;

	public Overlay() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				if(frame != null) {
					frame.dispose();
				}
			}
		}, "Shutdownhook-Overlaycloser"));
	}

	void set(Point spawn, Point[] cards, Point ok, Point battle, Point arenaview) {
		this.spawn = spawn;
		this.cards = cards;
		this.ok = ok;
		this.battle = battle;
		this.arenaview = arenaview;
	}

	public void init() {
		frame = new JFrame("Bot Overlay");
		frame.setUndecorated(true);
		frame.setOpacity(0.5f);

		int width=0, height=0;
		int x=spawn.x,y=spawn.y;
		for(Point p : getlist()) {
			if(p != null) {
				if(p.x < x) {
					width += (x-p.x);//umsoviel weiter machen, wie nach links verschoben wird
					x = p.x;
				}
				if(x+width < p.x) {
					width += x+width-p.x;
				}
				if(p.y < y) {
					height += (y-p.y);
					y = p.y;
				}
				if(y+height < p.y) {
					height += y+height-p.y;
				}
			}
		}

		frame.setSize(width, height);
		frame.setLocation(x, y);

		frame.setVisible(true);
		
		Graphics gra = frame.getGraphics();
		gra.setColor(new Color(255, 0, 0));//red
		for(Point p : cards) {
			if(p != null) {
				gra.drawRect(p.x-1, p.y-1, 300, 300);
			}
		}
		System.out.println("Overlay is da!");
	}

	public void close() {
		frame.dispose();
	}


	private Point[] getlist() {
		return new Point[] {spawn,ok,battle,arenaview, cards[0],cards[1],cards[2],cards[3]};
	}

}