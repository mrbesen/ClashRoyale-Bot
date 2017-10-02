package mrbesen.cr.auto.clicker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.istack.internal.Nullable;

public class UI implements ActionListener {

	private boolean isSelectionRunning = false; //true if an selection Thread is working, 
	
	private JFrame frame = new JFrame("Clash Royale Bot by MrBesen");;

	private JPanel root = new JPanel();
	private JPanel top = new JPanel();
	private JPanel middle = new JPanel();
	private JPanel sliderpanel = new JPanel();
	private JPanel bottom = new JPanel();

	private JMenuBar menubar = new JMenuBar();
	private JMenu file_ = new JMenu("File");
	private JMenuItem load = new JMenuItem();
	private JMenuItem save = new JMenuItem();

	private AutoPlayBox autoplay = new AutoPlayBox();
	private JCheckBox doubleplace = new JCheckBox("DoublePlace");
	private JCheckBox backfocus = new JCheckBox("BackFocus");

	private PosSelector[] posselctors = {
			new PosSelector(this, "Battle",true, 4),
			new PosColSelector(this, "End Battle",true, 5,0),
			new PosSelector(this, "Card1",false, 0),
			new PosSelector(this, "Card2",false, 1),
			new PosSelector(this, "Card3",false, 2),
			new PosSelector(this, "Card4", false, 3),
			new PosSelector(this, "Playout", false, 6),
			new PosColSelector(this, "Arena View", false, 7,1)
	};

	private JButton start = new JButton("START");
	private JButton skip = new JButton("SKIP"); // the button, to skip waiting
	private JButton pause = new JButton("Pause");
	private JButton exit = new JButton("EXIT");

	private JLabel info = new JLabel("Define positions, to start.");
	private JLabel time = new JLabel("0 s");

	private Slider[] slider = {
			new Slider("Waittime: ","s", 1,300,180,-1, null, new Updater() {
				@Override
				public void update(int nummber) {
					bot.setWait(nummber);
				}
			}, false),
			new Slider("Radius of Placement: ","px",0,40,15,0, null, new Updater() {
				@Override
				public void update(int nummber) {
					bot.setRandmones(nummber);
				}
			},false)
	};
	
	
	Clicker bot = new Clicker();

	
	private File file = new File(".profile");

	public UI() {
		Main.get().ui = this;
		//init screen
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(730, 180);

		save.setText("Save");
		save.addActionListener(this);
		load.setText("Load");
		load.addActionListener(this);

		file_.add(save);
		file_.add(load);
		menubar.add(file_);
		frame.setJMenuBar(menubar);

		skip.setEnabled(false);
		start.setEnabled(false);

		doubleplace.setSelected(true);
		doubleplace.setEnabled(false);

		skip.addActionListener(this);
		start.addActionListener(this);
		pause.addActionListener(this);
		exit.addActionListener(this);
		doubleplace.addActionListener(this);
		backfocus.addActionListener(this);

		for(PosSelector poss : posselctors) {//construct PosSelector Panel
			top.add(poss.button);
		}

		middle.add(start);//construct button panel
		middle.add(skip);
		middle.add(pause);
		middle.add(exit);
		middle.add(autoplay);
		middle.add(doubleplace);
		middle.add(backfocus);
		
		for(Slider s : slider) {//construct slider panel
			sliderpanel.add(s);
		}
		
		bottom.add(info);//construct bottom panel
		bottom.add(Box.createRigidArea(new Dimension(150, 5)));
		bottom.add(time);
		
		root.add(top);//add every panel
		root.add(middle);
		root.add(sliderpanel);
		root.add(bottom);
		
		frame.add(root);//create frame
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src instanceof JButton) {
			JButton srcb = (JButton) src;

			//check for the Position selectors
			for(PosSelector poss : posselctors) {
				if(poss.button.equals(srcb)) {
					if(!isSelectionRunning)
						new Thread(poss, "PositionSelector").start();
					break;
				}
			}

			if(srcb.equals(start)) {
				if(bot.isRunning()) {//stop!
					bot.stop();
					srcb.setText("START");
					skip.setEnabled(false);
					info("Stoped!");
				} else {
					bot.start();//start!
					srcb.setText("STOP");
					skip.setEnabled(true);
					info("Started!");
				}
			} else if(srcb.equals(skip)) 
				bot.skip();
			else if(srcb.equals(exit)) { 
				bot.stop();
				frame.setVisible(false);
				System.exit(0);
			} else if(srcb.equals(pause)) {
				if(bot.isPaused()) {//the bot is going to be unpaused
					pause.setText("Pause");
					info("Unpaused.");
				} else {//the bot is going to be paused.
					pause.setText("Unpause");
					info("Paused.");
				}
				bot.setPause(!bot.isPaused());
			}
		} else if(src instanceof JMenuItem) {
			JMenuItem srcI = (JMenuItem) src;
			if(srcI.equals(load)) {
				load(true);
			} else if(srcI.equals(save)) {
				save();
			}
		} else if(src instanceof JCheckBox) {
			JCheckBox srcb = (JCheckBox) src;
			if(srcb.equals(doubleplace)) {
				bot.setDoublePlay(srcb.isSelected());
				if(srcb.isSelected()) {//*2
					slider[0].setValue(slider[0].getValue()*2);
				} else {// /2
					slider[0].setValue(slider[0].getValue()/2);
				}
			} else if(srcb.equals(backfocus)) {
				bot.toggleBackfocus();
			}
		}
	}

	private void load(boolean info) {
		if(file.exists()) {
			try { 
				Scanner s = new Scanner(file);
				while(s.hasNextLine()) {
					String split[] = s.nextLine().split(" ");
					if(!split[1].equals("null")) {
						int num = Integer.parseInt(split[0]);
						if(num > 100) {//special settings (slider / checkboxes)
							if(num == 101) {//group wait
								int wait = Integer.parseInt(split[1]);
								slider[0].setValue(wait);
							} else if(num == 102) { // double playout
								boolean dp = Boolean.parseBoolean(split[1]);
								if(dp)
									autoplay.setSelected(true);	
								doubleplace.setSelected(dp);
								bot.setDoublePlay(dp);
							} else if(num == 103) {
								slider[1].setValue(Integer.parseInt(split[1]));
							} else if(num == 104) {
								Color c = new Color(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
								bot.setColor(c, 1,35);
							} else if(num == 105) {
								Color c = new Color(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
								bot.setColor(c, 0,35);
							}
						} else //standard Point object.
							bot.set(new Point(split[1]), num);
					}
				}
				s.close();
				refresh();
				if(info)
					info("Loaded!");
			} catch(IOException | NumberFormatException e) {
				e.printStackTrace();
				if(info)
					info("Error.");
			} 
		} else 
			if(info)
				info("no profile found.");
	}

	private void save() {
		try {
			if(!file.exists())
				file.createNewFile();
			else {
				/*ok == 0
				cancel = 2*/ 
				int choose = JOptionPane.showConfirmDialog(null, "You are going to override the old profile!", "Override", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if(choose == 2) {
					info("Canceled.");
					return;
				}
					
			}

			FileWriter fw = new FileWriter(file);
			fw.write(bot.serialize()+"\n101 "+ slider[0].getValue() + "\n102 " + doubleplace.isSelected()+"\n103 " + slider[1].getValue());
			fw.flush();
			fw.close();

			info("Saved!");
		} catch(IOException e) {
			e.printStackTrace();
			info("Error.");
		}
	}

	public void refresh() {
		//check if all required positions are set
		// & set the colors
		
		boolean allset = true;

		for(PosSelector poss : posselctors) {
			if(bot.isSet(poss.num))
				poss.green();
			else {
				poss.red();
				if(poss.isRequired())
					allset = false;
			}
		}
		
		if(allset)
			start.setEnabled(true);
	}

	public void info(String a) {
		info.setText(a);
	}

	/**
	 * Set the time label to this time.
	 * @param seconds in seconds.
	 * Negative time is set to zero.
	 */
	public void printTime(int seconds) {
		if(seconds < 0)//not allowed
			seconds = 0;
		
		StringBuilder out = new StringBuilder();
		
		int d = 0;
		while(seconds >= 86400) {
			seconds -= 86400;
			d ++;
		}
		
		if(d > 0) {
			out.append(d);
			out.append("d ");
		}
		
		int h = 0;//hours
		while(seconds >= 3600) {
			seconds -= 3600;
			h ++;
		}
		
		if(h > 0) {
			out.append(h);
			out.append("h ");
		}
		
		int m = 0;//min
		while(seconds >= 60) {
			seconds -= 60;
			m ++;
		}
		
		if(m > 0) {
			out.append(m);
			out.append("m ");
		}
		
		if(seconds > 0) {
			out.append(seconds);
			out.append("s");
		}
		
		if(out.length() == 0)
			out.append('-');//nothing
		
		time.setText(out.toString());
	}
	
	public void setPositionDone() {
		isSelectionRunning = false;
		
	}
	

	public interface Updater {
		public void update(int nummber);
	}

	public class AutoPlayBox extends JCheckBox implements ActionListener {//AutoPlayCheck Box (Extra Object because some updateing problems occour)
		private static final long serialVersionUID = 8957130982898848436L;

		public AutoPlayBox() {
			super("AutoPlay");
			addActionListener(this);
		}

		@Override
		public void setSelected(boolean b) {
			super.setSelected(b);
			update(b);
		}

		public void update(boolean b) {
			doubleplace.setEnabled(b);
			slider[0].setEnabled(b);
			slider[1].setEnabled(b);
			bot.setAutoPlay(b);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			update(this.isSelected());
		}
	}

	public class Slider extends JPanel implements ChangeListener{
		private static final long serialVersionUID = 1L;
		private JSlider slider;
		private JLabel label;

		private ChangeListener listener;
		private Updater updater;
		private String prefix = "", sufix = "";
		private int offset;
		/**
		 * @param prefix the text of the Label
		 * @param suffix  thge text of the Label
		 * @param minvalue start value
		 * @param maxvalue last value
		 * @param startvalue initial value
		 * @param comma 10^x offset for comma digits
		 */
		public Slider(String prefix, String suffix, int minvalue, int maxvalue, int startvalue, int comma, @Nullable ChangeListener cl,@Nullable Updater upd, boolean enabled) {//ChangeListener or Updater could be Null!
			slider = new JSlider(minvalue, maxvalue, startvalue);
			slider.addChangeListener(this);
			if(prefix != null)
				this.prefix = prefix;
			if(sufix != null) {
				this.sufix = sufix;
			}
			offset = comma;
			label = new JLabel(getLabelText());
			add(slider);
			add(label);
			listener = cl;
			updater = upd;
			slider.setEnabled(enabled);
		}
		
		public int getValue() {
			return slider.getValue();
		}

		public void setValue(int val) {
			slider.setValue(val);
		}

		private String getLabelText() {
			if(offset >= 0)
				return prefix + (slider.getValue() * (int) Math.pow(10, offset)) + sufix;	
			return prefix + (BigDecimal.valueOf(Math.pow(10, offset)).multiply(BigDecimal.valueOf(slider.getValue()))) + sufix;
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			slider.setEnabled(enabled);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			label.setText(getLabelText());//update information
			if(listener != null)
				listener.stateChanged(e);//forward event
			if(updater != null)
				updater.update(slider.getValue());
		}
	}
}