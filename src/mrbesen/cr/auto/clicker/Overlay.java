package mrbesen.cr.auto.clicker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class Overlay  extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4305002876609279070L;
	
	//	JFrame frame;
	Point spawn;
	Point cards[];
	Point ok;
	Point battle;
	Point arenaview;

	public Overlay() {
		/*Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
//				if(frame != null) {
					dispose();
//				}
			}
		}, "Shutdownhook-Overlaycloser"));*/
		init();
	}

	void set(Point spawn, Point[] cards, Point ok, Point battle, Point arenaview) {
		this.spawn = spawn;
		this.cards = cards;
		this.ok = ok;
		this.battle = battle;
		this.arenaview = arenaview;
	}

	public void init() {
		setTitle("Bot Overlay");
		setUndecorated(true);
		setBackground(new Color(0, 0, 0, 0));
		setOpacity(.5f);
		setAlwaysOnTop(true);
		//		frame.setOpacity(0.5f);

		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width=screenSize.width, height=screenSize.height;
		int x=0, y=0;
		System.out.println("x: " + x + " y: " + y + "  size: " + width + ", " + height);
		//		
		//		for(Point p : getlist()) {
		//			if(p != null) {
		//				if(p.x < x) {
		//					width += (x-p.x);//umsoviel weiter machen, wie nach links verschoben wird
		//					x = p.x;
		//				}
		//				if(x+width < p.x) {
		//					width += x+width-p.x;
		//				}
		//				if(p.y < y) {
		//					height += (y-p.y);
		//					y = p.y;
		//				}
		//				if(y+height < p.y) {
		//					height += y+height-p.y;
		//				}
		//			}
		//		}

		setSize(width, height);
		setLocation(x, y);
		
		setVisible(true);

//		frame.invalidate();
		System.out.println("Overlay is da!");
	}
/*	@Override
	public void paint(Graphics gra) {
//		gra.setPaintMode();
		gra.setColor(new Color(255, 255, 255, 0));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		gra.fillRect(0, 0, screenSize.width, screenSize.height);
		gra.setColor(new Color(255, 0, 0, 255));//red
		//		gra.setColor(new Color(1, 0, 0,0.5f));
		for(Point p : getlist()) {
			if(p != null) {
				gra.fillRect(p.x-2, p.y-2, 25, 25);
				System.out.println("draw: " + p.x +", " + p.y);
			}
		}
		System.out.println("paint!");
	}*/

	public void close() {
		/*if (frame != null) {
			frame.dispose();
			System.out.println("Closed Overlay");
		}*/
		dispose();
	}


	private Point[] getlist() {
		return new Point[] {spawn,ok,battle,arenaview, cards[0],cards[1],cards[2],cards[3]};
	}

}