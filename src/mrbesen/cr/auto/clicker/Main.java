package mrbesen.cr.auto.clicker;

import javax.swing.*;

public class Main {
	
	private static Main main;
	public UI ui;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		main = this;
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new UI();
	}
	
	public static Main get() {
		return main;
	}
}
