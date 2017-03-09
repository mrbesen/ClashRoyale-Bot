package mrbesen.cr.auto.clicker;

public class Main {
	
	private static Main main;
	public UI ui;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		main = this;
		new UI();
	}
	
	public static Main get() {
		return main;
	}
}
