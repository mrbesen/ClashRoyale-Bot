package mrbesen.cr.auto.clicker;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

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
			BufferedImage img = rob.createScreenCapture(new Rectangle(p.x-10, p.y-10, 20, 20));
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
			ui.bot.setColor(c,colornum);
		} catch(AWTException e) {
			e.printStackTrace();
		}
	}
}