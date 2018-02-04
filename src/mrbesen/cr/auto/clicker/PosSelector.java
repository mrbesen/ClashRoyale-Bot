package mrbesen.cr.auto.clicker;

import java.awt.Color;

import javax.swing.JButton;

public class PosSelector implements Runnable {

	JButton button;
	int num;
	UI ui;
	private boolean required;
	
	
	public boolean isRequired() {
		return required;
	}

	PosSelector(UI ui, String text, boolean required, int num) {
		this.ui = ui;
		this.required = required;
		this.num = num;
		button = new JButton(text);
		button.addActionListener(ui);
		button.setOpaque(true);
		red();
	}

	@Override
	public void run() {
		button.setBackground(Color.MAGENTA);
		try {
			for(int i = 5; i > 0; i--) {//countdown
				ui.info(i + "");
				Thread.sleep(1000);
			}
		} catch(InterruptedException e) {}
		ui.bot.set(ui.bot.getMouse(), num);//get and save the position
		ui.info("Position saved!");
		green();
		ui.setPositionDone();
	}
	
	public void green() {
		button.setBackground(Color.GREEN);
	}
	
	public void red() {
		if(required)
			button.setBackground(Color.RED);
		else
			button.setBackground(Color.ORANGE);
	}
}