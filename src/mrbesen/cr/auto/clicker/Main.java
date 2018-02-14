package mrbesen.cr.auto.clicker;

import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;

public class Main {
	
	private static Main main;
	public UI ui;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		
		GraphicsEnvironment ge = 
	            GraphicsEnvironment.getLocalGraphicsEnvironment();
	        GraphicsDevice gd = ge.getDefaultScreenDevice();

	        //If translucent windows aren't supported, exit.
	        if (!gd.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
	            System.err.println(
	                "Translucency is not supported");
	                System.exit(0);
	        }
	        
		main = this;
		new UI();
	}
	
	public static Main get() {
		return main;
	}
}
