package mrbesen.cr.auto.clicker;

import java.util.Random;

public class Point {

	int x = 0, y = 0;
	
	public String serialize() {
		return (x +"x"+y);
	}
	
	public Point(String deserialize) {
		String[] split = deserialize.split("x",2);
		x = Integer.parseInt(split[0]);
		y = Integer.parseInt(split[1]);
	}
	
	public Point(int x, int y) {
		this.x = x; this.y = y;
	}

	public Point add(int x, int y) {
		return new Point(this.x+x, this.y+y);
	}

	public Point(int random) {
		Random rand = new Random();
		x = rand.nextInt(random) - (random/2);
		y = rand.nextInt(random) - (random/2);
	}
	
	public Point add(Point a) {
		return new Point(a.x+x, a.y+y);
	}
}