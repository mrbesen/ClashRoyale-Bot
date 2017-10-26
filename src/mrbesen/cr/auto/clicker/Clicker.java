package mrbesen.cr.auto.clicker;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Clicker implements Runnable{

	private boolean running = false;
	private boolean should_run = false;
	private boolean inbattle = false;
	private boolean skipbattle = false;
	private Thread thread;

	private Point battle;
	private Point end;

	private Point arena_switch;

	private Point[] cardslots = new Point[4]; 
	private Point playout;

	private Color ok_button = new Color(85, 170, 254);
	private Color arena_view = new Color(85, 170, 254);//<-not the correct color!

	private boolean autoplay;
	private boolean doubleplayout = true;
	private int truppenwait = 180;
	private int randomness = 15;
	private boolean paused = false;
	private boolean backfocus = false;
	private final int waittime = 50;//time between mouse teleports and clicks

	private int mincolordistance = 35;
	private Overlay ov = null;

	OSType os;

	long started = -1;

	private void sleep( int ms) {
		//update ui
		Main.get().ui.printTime((int) ( (System.currentTimeMillis()-started) / 1000 ));

		if(skipbattle)
			return;
		try {
			if(ms > 1000) {
				Thread.sleep(1000);
				sleep(ms-1000);
			} else
				Thread.sleep(ms);
			while(paused & should_run) {
				Thread.sleep(75);
			}
		} catch (InterruptedException e) { } //when skip is applyed, or the bot gets stopped
	}

	public void start() {
		should_run = true;
		if(!running) {
			running = true;
			thread = new Thread(this, "BOT");
			thread.start();
			started = System.currentTimeMillis();
		}
	}

	public void stop() {
		should_run = false;
		skipbattle = true;
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

		//determine os
		String oss = System.getProperty("os.name").toLowerCase();
		if(oss.contains("nix") | oss.contains("nux") | oss.contains("aix")) 
			os = OSType.Linux;
		else if(oss.contains("win"))
			os = OSType.Windows;
		else if(oss.contains("mac")) 
			os = OSType.OSX;
		else 
			Main.get().ui.info("OS not supported for backfocus: " + oss);

		int card = 0;
		try {
			Robot rob = new Robot();
			while(should_run) {
				sleep(500);
				clickL(rob, battle);//smash the start button
				sleep(1000);
				clickL(rob, battle);//press start again (if there is an alert poping up)
				backfocus(rob);
				//battle is starting up
				sleep(9000);//wait for the battle to start (loading screen)
				Main.get().ui.info("Battle started.");
				inbattle = true;
				float modifier = 1;
				long start = System.currentTimeMillis();
				long lastwait = start;//actions like moving mouse and do stuff gets messured and subtracted of the wait's
				while( ((System.currentTimeMillis() - start) / 6000) < 41 & should_run & !skipbattle) {

					//check für ok-button
					if(round(start) > 20) {//game is older then 20 seconds
						if(checkOK(end, rob,ok_button)) {//check
							Main.get().ui.info("OK-button detected!");
							skipbattle = true;
							break;
						}
					}

					//try to play out a card
					if(autoplay) {
						playout(card, rob);//try to play a card
						card = (card +1) % 4;//move card pointer to the next
						if(doubleplayout) {
							sleep(waittime * 2);
							playout(card, rob);
							card = (card +1) % 4;//next
						}
						backfocus(rob);
					}

					if(round(start) >= 115) //game older than 2 minutes -> speed the playout process up!
						modifier = 2;
					else if(round(start) >= (115 - (truppenwait / 2))) //remove half waittime and do half speed.
						modifier = 1.5f;
					//        eingestellter wert (0.1 sec) ggf. durch 2 teilen   vergangene zeit abziehen (zeit fürs setztem der letzten truppen)   
					int waittime = ( (int) (((truppenwait * 100) / modifier) - (System.currentTimeMillis()- lastwait)) );//how long to wait?
					Main.get().ui.info("Waiting for: " + (waittime / 1000) + "s");
					while (waittime > 1500 & !skipbattle & should_run) {//check for the ok-button every 3 seconds
						long startwait = System.currentTimeMillis();//record needed time
						if(checkOK(end, rob, ok_button)) {//check
							Main.get().ui.info("OK-button detected!");
							skipbattle = true;
							break;
						} 
						sleep((int) (1500 - (System.currentTimeMillis() - startwait)));//sleep the rest of 3 seconds, that was not gone for checking
						waittime = (int) (waittime - (System.currentTimeMillis() - startwait));//calculate waittime that is left
					}
					sleep(waittime);//wait

					lastwait = System.currentTimeMillis();//restart the messurement of time used by the actions
				}
				skipbattle = false;
				inbattle = false;
				clickL(rob, end);//ok button
				backfocus(rob);
				Main.get().ui.info("Battle ended.");
				sleep(9000);//9 sec-loading screen
				//checken, ob Arena wechsel pop-up
				while(checkOK(arena_switch, rob,arena_view) & should_run) {
					System.out.println("Arena found, clicking");
					clickL(rob, arena_switch);
					backfocus(rob);
					sleep(2000);
				}
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}
		running= false;//remove the running flag
	}

	private float round(long start) {//returns how old the round is in 0.1 seconds
		return ((System.currentTimeMillis() - start) / 1000);
	}

	/**
	 * Try to play out an Card. fakes 2 mouse clicks. One at the card, and one at the defined playout spot. 
	 * @param card card nummber (0-3)
	 * @param rob the Robot Object to use
	 */
	private void playout(int card, Robot rob) {
		Main.get().ui.info("Playout: " + (card+1));
		if(cardslots[card] != null) {//card is selectable
			clickL(rob, cardslots[card]);//click on the card slot
			sleep(waittime);//lets Teamviewer transmit the data to the phone and let the phone some time zto sumbit the data to supercell.
			if(playout != null)//a specified playout spot
				clickL(rob, playout.add(new Point(randomness)));//click on the playout location
			else 
				clickL(rob, battle.add(new Point(randomness)));//non specified playout spot (the battle start button is a good position to play out cards)
		}
	}

	private void backfocus(Robot bot) {
		if(backfocus) {
			if(os == OSType.Windows | os == OSType.Linux)
				bot.keyPress(KeyEvent.VK_ALT);
			else//osx / unsupported
				bot.keyPress(KeyEvent.VK_META);

			bot.keyPress(KeyEvent.VK_TAB);
			sleep(waittime);
			bot.keyRelease(KeyEvent.VK_TAB);
			if(os == OSType.Windows | os == OSType.Linux)
				bot.keyRelease(KeyEvent.VK_ALT);
			else//osx / unsupported
				bot.keyRelease(KeyEvent.VK_META);
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
		else if(num == 7)
			arena_switch = a;
	}

	public boolean isBackfocus() {
		return backfocus;
	}

	public void toggleBackfocus() {
		backfocus = !backfocus;
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
		else if(num == 7)
			return arena_switch;

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

	public void setRandmones(int rand) {
		randomness = rand;
	}

	public boolean bothset() {
		return (end != null & battle != null);
	}

	private void clickL(Robot b, Point a) {
		if(!should_run)
			return;
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
		sleep(waittime);
		b.mouseRelease(InputEvent.BUTTON1_MASK);
		sleep(waittime);
	}

	private int getMousex() {
		return MouseInfo.getPointerInfo().getLocation().x;
	}
	private int getMousey() {
		return MouseInfo.getPointerInfo().getLocation().y;
	}

	/**
	 * This method checks a squared radius of 10 px around the Point and compares the screen color with the color of the ok-button, that ends an game. 
	 * @param p the point to scann
	 * @param bot the Robot object to use
	 * @return true, if there are more then 70px alike enough
	 */
	private boolean checkOK(Point p, Robot bot, Color goalcolor) {
		if(p == null | bot == null | goalcolor == null)
			return false;
		//long start = System.currentTimeMillis();
		int count = 0;
		BufferedImage img = bot.createScreenCapture(getRect(p.x, p.y));//smile
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int color = img.getRGB(x, y);
				int red = (color & 0x00ff0000) >> 16;
				int green = (color & 0x0000ff00) >> 8;
				int blue = color & 0x000000ff;
				double distance = Math.sqrt(Math.pow((blue - goalcolor.getBlue()), 2)
				+ Math.pow((red - goalcolor.getRed()), 2) + Math.pow((green - goalcolor.getGreen()), 2));//calculate the distance between the goalcolor and the test color
				if (distance < mincolordistance)
					count++;
			}
		}

		//		System.out.println("counts: " + count);//some performance checking
		return count > 4;//engough pixel have the right color
	}

	public Point getMouse() {
		return new Point(getMousex(), getMousey());
	}

	public String serialize() {
		String out = "";
		for(int i = 0; i < 8; i++) {
			Point p = get(i);
			String ps = "null";
			if(p != null)
				ps = p.serialize();
			out += i + " " +  ps + "\n";
		}
		out = out + "104 " + arena_view.getRed() + " " + arena_view.getGreen() + " " + arena_view.getBlue();
		out = out + "\n105 " + ok_button.getRed() + " " + ok_button.getGreen() + " " + ok_button.getBlue();
		return out ;
	}


	/**
	 * Set the avg Color of an Button
	 * @param c Color
	 * @param colornum nummber (0=ok-button, 1=arena_view-button)
	 */
	public void setColor(Color c, int colornum, int minimumdistance) {
		switch(colornum) {
		case 0:
			ok_button = c;
			break;
		case 1:
			arena_view = c;
			break;
		}
		if(mincolordistance < minimumdistance)
			mincolordistance = minimumdistance;
		System.out.println(colornum + ": "+c.getRed() + " " + c.getGreen() + " " + c.getBlue());
	}


	public boolean isPaused() {
		return paused;
	}

	public void setPause(boolean b) {
		paused = b;
	}

	private enum OSType {
		Linux,
		Windows,
		OSX,
		unsupported
	}


	public static Rectangle getRect(int x, int y) {
		return new Rectangle(x-2, y-2, 5, 5);
	}
	
	public void toggleOverlay() {
		if(ov == null) {
			try {
				ov = new Overlay();
				ov.set(playout, cardslots, end, battle, arena_switch);
				ov.init();
			} catch(Exception e) {
				System.out.println("Catched Exception, while inflateing Overlay: ");
				e.printStackTrace();
			}
		} else {
			ov.close();
			ov = null;
		}
	}
}