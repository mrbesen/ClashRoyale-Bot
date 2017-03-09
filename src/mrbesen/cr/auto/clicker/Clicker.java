package mrbesen.cr.auto.clicker;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class Clicker implements Runnable{

	private boolean running = false;
	private boolean should_run = false;
	private boolean inbattle = false;
	private boolean skipbattle = false;
	private Thread thread;

	private Point battle;
	private Point end;

	private Point[] cardslots = new Point[4]; 
	private Point playout;
	private boolean autoplay;
	private boolean doubleplayout =false;
	private int truppenwait = 2;

	public Clicker() {
	}

	private void sleep( int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {//when skip is applyed
			;
		}
	}

	public void start() {
		should_run = true;
		if(!running) {
			running = true;
			thread = new Thread(this, "BOT");
			thread.start();
		}
	}

	public void stop() {
		should_run = false;
		while(running) {
			thread.interrupt();//stop that shit (its maybe sleeping)
		}
	}

	public void skip() {
		if(isRunning())
			if(inbattle)
				skipbattle = true;
		
			thread.interrupt();
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	public void run() {
		sleep(1000);//chill ma
		int card = 0;
		try {
			Robot rob = new Robot();
			while(should_run) {
				clickL(rob, battle);//start knopf
				sleep(1000);
				if(!should_run)
					break;
				clickL(rob, battle);//start knopf bestätigen
				//battle running
				sleep(9000);//wait for the battle to start (loading screen)
				Main.get().ui.info("Battle started.");
				inbattle = true;
				int modifier = 1;
				long start = System.currentTimeMillis();
				long lastwait = start;//das delay der runde mit ein rechnen
				while( ((System.currentTimeMillis() - start) / 6000) < 41 & should_run & !skipbattle) {
					if(((System.currentTimeMillis() - start) / 60000f) > 2) //speed up! (nach her durch 2 teilen)
						modifier = 2;
					//try to play a round
					if(autoplay) {
						playout(card, rob);
						card = (card +1) % 4;//next
						if(doubleplayout) {
							sleep(750);
							playout(card, rob);
							card = (card +1) % 4;//next
						}
					}
					//   eingestellter wert (0.1 sec) ggf. durch 2 teilen    vergangene zeit abziehen (zeit fürs setztem der letzten truppen)   
					sleep((int) (((truppenwait * 100) / modifier) - (System.currentTimeMillis()- lastwait)));//chillen
					lastwait = System.currentTimeMillis();
				}
				skipbattle = false;
				inbattle = false;
				Main.get().ui.info("Battle ended.");
				
				if(!should_run)
					break;
				clickL(rob, end);//ok knopf
				if(!should_run)
					break;
				sleep(10000);//10 sec-lade screen
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}
		running = false;
	}

	private void playout(int card, Robot rob) {
		if(cardslots[card] != null) {//card is selectable
			clickL(rob, cardslots[card]);//click on the card slot
			sleep(450);
			if(playout != null)//a specified playout spot
				clickL(rob, playout);//click on the playout location
			else 
				clickL(rob, battle);//non specified playout spot
		}
	}

	public void set(Point a, int num) {
		if(num < 4) 
			cardslots[num] = a;
		else if(num == 5) {
			end = a;
			Main.get().ui.refresh();
		} else if(num == 4) {
			battle = a;
			Main.get().ui.refresh();
		} else if(num == 6)
			playout = a;
	}

	public boolean isSet(int num) {
		if(num < 0 ) throw new IllegalArgumentException("num >= 0 !");

		return get(num) != null;
	}

	private Point get(int num) {
		if(num < 4)
			return cardslots[num];
		else if(num == 5)
			return end ;
		else if(num == 4)
			return battle;
		else if(num == 6)
			return playout;

		return null;
	}

	public void setWait(int i) {
		truppenwait = i;
	}

	public void setDoublePlay(boolean a) {
		doubleplayout = a;
	}

	public void setAutoPlay(boolean a) {
		autoplay = a;
	}

	public boolean bothset() {
		return (end != null & battle != null);
	}

	private void clickL(Robot b, Point a) {
		Point old = getMouse();
		b.mouseMove(a.x, a.y);
		sleep(50);
		clickL(b);
		sleep(50);
		b.mouseMove(old.x, old.y);
		sleep(50);
	}

	private void clickL(Robot b) {//40 ms delay
		b.mousePress(InputEvent.BUTTON1_MASK);
		sleep(40);
		b.mouseRelease(InputEvent.BUTTON1_MASK);
		sleep(10);
	}

	private int getMousex() {
		return MouseInfo.getPointerInfo().getLocation().x;
	}
	private int getMousey() {
		return MouseInfo.getPointerInfo().getLocation().y;
	}

	public Point getMouse() {
		return new Point(getMousex(), getMousey());
	}

	public String serialize() {
		String out = "";
		for(int i = 0; i < 7; i++) {
			Point p = get(i);
			String ps = "null";
			if(p != null)
				ps = p.serialize();
			out += i + " " +  ps + "\n";
		}
		return out.substring(0, out.length()-1);
	}
}