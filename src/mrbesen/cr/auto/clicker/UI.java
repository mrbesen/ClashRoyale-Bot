package mrbesen.cr.auto.clicker;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.istack.internal.Nullable;

public class UI implements ActionListener {

	private boolean isSelectionRunning = false; //true if an selection Thread is working, 
	
	private JFrame frame = new JFrame("Clash Royale Bot · by MrBesen");;

	private JPanel root = new JPanel();
	private JPanel top = new JPanel();
	private JPanel bottom = new JPanel();
	//JPanel slider = new JPanel();

	private JMenuBar menubar = new JMenuBar();
	private JMenu file_ = new JMenu("File");
	private JMenuItem load = new JMenuItem();
	private JMenuItem save = new JMenuItem();

	private AutoPlayBox autoplay = new AutoPlayBox();
	private JCheckBox doubleplace = new JCheckBox("DoublePlace");

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

	private JButton skip = new JButton("SKIP"); // the button, to skip waiting
	private JButton start = new JButton("START");
	private JButton exit = new JButton("EXIT");

	private JLabel info = new JLabel("Define positions, to start.");

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
		exit.addActionListener(this);
		doubleplace.addActionListener(this);

		for(PosSelector poss : posselctors) {
			top.add(poss.button);
		}

		bottom.add(start);
		bottom.add(skip);
		bottom.add(exit);
		bottom.add(autoplay);
		bottom.add(doubleplace);
		bottom.add(info);

		root.add(top);
		root.add(bottom);
		
		for(Slider s : slider) {
			root.add(s);
		}

		frame.add(root);

		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src instanceof JButton) {
			JButton srcb = (JButton) src;

			//check for the Posselectors
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
			/*		if(srcb.equals(autoplay)) {
				bot.setAutoPlay(srcb.isSelected());
				if(srcb.isSelected()) {
					slider[0].setEnabled(true);
					doubleplace.setEnabled(true);
				} else {
					slider[0].setEnabled(false);
					doubleplace.setEnabled(false);
				}
			} else */
			if(srcb.equals(doubleplace)) {
				bot.setDoublePlay(srcb.isSelected());
				if(srcb.isSelected()) {//*2
					slider[0].setValue(slider[0].getValue()*2);
				} else {// /2
					slider[0].setValue(slider[0].getValue()/2);
				}
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
							if(num == 101) {//truppenwait
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
						} else //standard Point Obj.
							bot.set(new Point(split[1]), num);
					}
				}
				s.close();
				refresh();
				if(info)
					info("loaded!");
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

			FileWriter fw = new FileWriter(file);
			fw.write(bot.serialize()+"\n101 "+ slider[0].getValue() + "\n102 " + doubleplace.isSelected()+"\n103 " + slider[1].getValue());
			fw.flush();
			fw.close();

			info("saved!");
		} catch(IOException e) {
			e.printStackTrace();
			info("Error.");
		}
	}

	public void refresh() {
		if(bot.bothset()) 
			start.setEnabled(true);

		for(PosSelector poss : posselctors) {
			if(bot.isSet(poss.num))
				poss.green();
			else
				poss.red();
		}
	}

	public void info(String a) {
		info.setText(a);
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
		 * @param sufix  thge text of the Label
		 * @param minvalue start vlaue
		 * @param maxvalue last value
		 * @param startvalue inital value
		 * @param komma 10^x offset for komma digits
		 */
		public Slider(String prefix, String sufix, int minvalue, int maxvalue, int startvalue, int komma, @Nullable ChangeListener cl,@Nullable Updater upd, boolean enabled) {//ChangeListener or Updater could be Null!
			slider = new JSlider(minvalue, maxvalue, startvalue);
			slider.addChangeListener(this);
			if(prefix != null)
				this.prefix = prefix;
			if(sufix != null) {
				this.sufix = sufix;
			}
			offset = komma;
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
			label.setText(getLabelText());//update info
			if(listener != null)
				listener.stateChanged(e);//forward Event
			if(updater != null)
				updater.update(slider.getValue());
		}
	}
}