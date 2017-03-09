package mrbesen.cr.auto.clicker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

public class UI implements ActionListener, ChangeListener{

	JFrame frame = new JFrame("Clash Royale Bot · by MrBesen");;

	JPanel root = new JPanel();
	JPanel top = new JPanel();
	JPanel bottom = new JPanel();
	JPanel slider = new JPanel();

	JMenuBar menubar = new JMenuBar();
	JMenu file_ = new JMenu("File");
	JMenuItem load = new JMenuItem();
	JMenuItem save = new JMenuItem();
	
	JCheckBox autoplay = new JCheckBox("AutoPlay");
	JCheckBox doubleplace = new JCheckBox("DoublePlace");

	PosSelector[] posselctors = {
			new PosSelector(this, "Battle",true, 4),
			new PosSelector(this, "End Battle",true, 5),
			new PosSelector(this, "Card1",false, 0),
			new PosSelector(this, "Card2",false, 1),
			new PosSelector(this, "Card3",false, 2),
			new PosSelector(this, "Card4", false, 3),
			new PosSelector(this, "Playout", false, 6)
	};

	JButton skip = new JButton("SKIP"); // the button, to skip waiting
	JButton start = new JButton("START");
	JButton exit = new JButton("EXIT");

	JLabel info = new JLabel("Define positions, to start.");

	JSlider truppenwait = new JSlider(JSlider.HORIZONTAL, 1, 200, 180);
	JLabel wait = new JLabel("18.0");
	
	Clicker bot = new Clicker();

	File file = new File(".profile");

	public UI() {
		Main.get().ui = this;
		//init screen
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(620, 140);

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
		
		skip.addActionListener(this);
		start.addActionListener(this);
		exit.addActionListener(this);
		autoplay.addActionListener(this);
		doubleplace.addActionListener(this);
		truppenwait.addChangeListener(this);

		for(PosSelector poss : posselctors) {
			top.add(poss.button);
		}
		
		bottom.add(start);
		bottom.add(skip);
		bottom.add(exit);
		bottom.add(autoplay);
		bottom.add(doubleplace);
		bottom.add(info);
		
		slider.add(truppenwait);
		slider.add(wait);
		
		root.add(top);
		root.add(bottom);
		root.add(slider);

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
			if(srcb.equals(autoplay))
				bot.setAutoPlay(srcb.isSelected());
			else if(srcb.equals(doubleplace)) {
				bot.setDoublePlay(srcb.isSelected());
				if(srcb.isSelected()) {//*2
					truppenwait.setValue(truppenwait.getValue()*2);
				} else {// /2
					truppenwait.setValue(truppenwait.getValue()/2);
				}
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object o = e.getSource();
		if(o instanceof JSlider) {
			JSlider slider = (JSlider) o;
			if(slider.equals(truppenwait)) {
				bot.setWait(slider.getValue());
				wait.setText(""+(slider.getValue()/10f));
			}
		}
	}
	
	private void load(boolean info) {
		if(file.exists()) {
			try { 
				Scanner s = new Scanner(file);
				while(s.hasNextLine()) {
					String split[] = s.nextLine().split(" ",2);
					if(!split[1].equals("null"))
						bot.set(new Point(split[1]), Integer.parseInt(split[0]));
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
			fw.write(bot.serialize());
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


}