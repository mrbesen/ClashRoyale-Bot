package mrbesen.cr.auto.clicker;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class PosColSelector extends PosSelector {

	int colornum;
	
	public PosColSelector(UI ui, String text, boolean required, int num, int colornum) {
		super(ui, text, required, num);
		this.colornum = colornum;
	}

	@Override
	public void run() {
		super.run();
		try {
			Robot rob = new Robot();
			Point p = ui.bot.getMouse();
			BufferedImage img = rob.createScreenCapture(Clicker.getRect(p.x, p.y));
			//calculate avg color;
			int red = 0;
			int green = 0;
			int blue = 0;
			int count = 0;
			for (int x = 0; x < 20; x++) {
				for (int y = 0; y < 20; y++) {
					int color = img.getRGB(x, y);
					red += (color & 0x00ff0000) >> 16;
					green += (color & 0x0000ff00) >> 8;
					blue += color & 0x000000ff;
					count ++;
				}
			}
			red /= count;
			blue /= count;
			green /= count;
			Color c = new Color(red, green, blue);
			
			//calculate distances:
			List<Integer> dist = new LinkedList<Integer>();
			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					int color = img.getRGB(x, y);
					int redf = (color & 0x00ff0000) >> 16;
					int greenf = (color & 0x0000ff00) >> 8;
					int bluef = color & 0x000000ff;
					double distance = Math.sqrt(Math.pow((bluef - c.getBlue()), 2)
					+ Math.pow((redf - c.getRed()), 2) + Math.pow((greenf - c.getGreen()), 2));
					dist.add((int) distance);
//					System.out.println(distance);
				}
			}
			
			dist.sort(new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					if(o1 < o2)
						return -1;
					if(o1== o2)
						return 0;
					if(o1 > o2)
						return 1;
					return 0;
				}
			});
			
			int miniumdistance = dist.get((int) (dist.size()*0.3f));//at least the first third tests should fit
//			int maximumdistance = dist.get(dist.size()-1);
			System.out.println("minimum distance: " + miniumdistance );
			ui.bot.setColor(c,colornum, miniumdistance);
			ui.bot.set(p, num);
		} catch(AWTException e) {
			e.printStackTrace();
		}
	}
}