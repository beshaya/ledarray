package pattern;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pattern.util.IntValue;

public class BouncingBall implements Pattern{

	final private Dimension d = new Dimension(0,0);
	private double[] fftdata;
	private int minx;
	private int maxx;
	private int miny;
	private int maxy;
	private int fmax=4;
	private int fmin=1;
	private int cut=10;
	private int gain=50;
	private Color myColor = new Color(255,0,0);
	
	//States whether or not the pattern's PatternVis is selected
	//Should be part of patternvis?
	public boolean selected;
	
	//Keeps track of whether or not the pattern is being added to the display frame.
	public boolean active;
	
	private double x;
	private double y;
	private double xdir;
	private double ydir;
	private long lastTime;
	
	final JTextField xmin = new JTextField("  0");
	final JTextField xmax = new JTextField(" 45");
	final JTextField ymin = new JTextField("  0");
	final JTextField ymax = new JTextField("  7");
	final JTextField minfreq = new JTextField("1");
	final JTextField maxfreq = new JTextField("4");
	final JPanel control = new JPanel();
	final JButton setColor = new JButton("Set Color");
	
	  final JSlider cutS = new JSlider(JSlider.VERTICAL, 0, 20, 5);
	  final JSlider gainS = new JSlider(JSlider.VERTICAL,0,100,10);

	public String toString(){
		return "BouncingBall";
	}
	public BouncingBall(int xsize, int ysize, double[] fftdata){
		d.setSize(xsize,ysize);
		this.fftdata = fftdata;
		selected=false;
		active = false;
		minx = 0;
		maxx = xsize-1;
		miny = 0;
		maxy = ysize-1;
		
		x = xsize/2;
		y = ysize/2;
		xdir = 1;
		ydir = 1;
		lastTime = System.currentTimeMillis();
		
		//Create the elements for the control panel and their listeners
		
		Dimension tbD = new Dimension(30,20);
		xmin.setPreferredSize(tbD);
		xmax.setPreferredSize(tbD);
		ymin.setPreferredSize(tbD);
		ymax.setPreferredSize(tbD);
		minfreq.setPreferredSize(tbD);
		maxfreq.setPreferredSize(tbD);
		xmin.addFocusListener(
			      new FocusListener(){
					public void focusGained(FocusEvent arg0) {
					}
					public void focusLost(FocusEvent arg0) {
			        	int val = 0;
			        	try{
			        		val = Integer.parseInt(xmin.getText());
			        	}catch(Exception ex){
			        		xmin.setText(1+"");
			        		val = 1;
			        	}
			        	if (val <0){
			        		xmin.setText(0+"");
			        		minx = 0;
			        	}else if (val >=d.width){
			        		xmin.setText(d.width-1+"");
			        		minx = d.width-1;
			        	}else{
			        		minx = val;
			        	}	
					}
			      });
		ymin.addFocusListener(
			      new FocusListener(){
					public void focusGained(FocusEvent arg0) {
					}
					public void focusLost(FocusEvent arg0) {
			        	int val = 0;
			        	try{
			        		val = Integer.parseInt(ymin.getText());
			        	}catch(Exception ex){
			        		ymin.setText(1+"");
			        		val = 1;
			        	}
			        	if (val <0){
			        		ymin.setText(0+"");
			        		miny = 0;
			        	}else if (val >=d.height){
			        		ymin.setText(d.height-1+"");
			        		miny = d.height-1;
			        	}else{
			        		miny = val;
			        	}	
					}
			      });
		
		xmax.addFocusListener(
			      new FocusListener(){
					public void focusGained(FocusEvent arg0) {
					}
					public void focusLost(FocusEvent arg0) {
			        	int val = 0;
			        	try{
			        		val = Integer.parseInt(xmax.getText());
			        	}catch(Exception ex){
			        		xmax.setText(1+"");
			        		val = 1;
			        	}
			        	if (val <0){
			        		xmax.setText(0+"");
			        		maxx = 0;
			        	}else if (val >=d.width){
			        		xmax.setText(d.width-1+"");
			        		maxx = d.width-1;
			        	}else{
			        		maxx = val;
			        	}	
					}
			      });
		ymax.addFocusListener(
			      new FocusListener(){
					public void focusGained(FocusEvent arg0) {
					}
					public void focusLost(FocusEvent arg0) {
			        	int val = 0;
			        	try{
			        		val = Integer.parseInt(ymax.getText());
			        	}catch(Exception ex){
			        		ymax.setText(1+"");
			        		val = 1;
			        	}
			        	if (val <0){
			        		ymax.setText(0+"");
			        		maxy = 0;
			        	}else if (val >=d.height){
			        		ymax.setText(d.height-1+"");
			        		maxy = d.height-1;
			        	}else{
			        		maxy = val;
			        	}	
					}
			      });
		
		minfreq.addFocusListener(
			      new FocusListener(){
					public void focusGained(FocusEvent arg0) {
					}
					public void focusLost(FocusEvent arg0) {
			        	int val = 0;
			        	try{
			        		val = Integer.parseInt(minfreq.getText());
			        	}catch(Exception ex){
			        		minfreq.setText(1+"");
			        		val = 1;
			        	}
			        	if (val <0){
			        		minfreq.setText(0+"");
			        		fmin = 1;
			        	}else if (val >=20000){
			        		minfreq.setText(20000+"");
			        		fmin = 20000-1;
			        	}else{
			        		fmin = val;
			        	}	
					}
			      });
		maxfreq.addFocusListener(
			      new FocusListener(){
					public void focusGained(FocusEvent arg0) {
					}
					public void focusLost(FocusEvent arg0) {
			        	int val = 0;
			        	try{
			        		val = Integer.parseInt(maxfreq.getText());
			        	}catch(Exception ex){
			        		maxfreq.setText(1+"");
			        		val = 20000;
			        	}
			        	if (val <0){
			        		maxfreq.setText(0+"");
			        		fmax = 1;
			        	}else if (val >=20000){
			        		maxfreq.setText(20000+"");
			        		fmax = 20000-1;
			        	}else{
			        		fmax = val;
			        	}	
					}
			      });
		
		cutS.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				cut = cutS.getValue();	
			}
		});
		gainS.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				gain = gainS.getValue();	
			}
		});
		
		setColor.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Color tempColor = JColorChooser.showDialog(control, "Pick a Color", myColor);
				if (tempColor != null)
					myColor = tempColor;
				setColor.setBackground(myColor);
			}
		});
		
		control.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		control.setPreferredSize(new Dimension(200,400));
		c.gridx=0;c.gridy=0;
		control.add(new JLabel("xmin"),c);
		c.gridx=1;c.gridy=0;
		control.add(xmin,c);
		c.gridx=0;c.gridy=1;
		control.add(new JLabel("xmax"),c);
		c.gridx=1;c.gridy=1;
		control.add(xmax,c);
		c.gridx=0;c.gridy=2;
		control.add(new JLabel("ymin"),c);
		c.gridx=1;c.gridy=2;
		control.add(ymin,c);
		c.gridx=0;c.gridy=3;
		control.add(new JLabel("ymax"),c);
		c.gridx=1;c.gridy=3;
		control.add(ymax,c);
		c.gridx=0;c.gridy=4;
		control.add(new JLabel("min frequency"),c);
		c.gridx=1;c.gridy=4;
		control.add(minfreq,c);
		c.gridx=0;c.gridy=5;
		control.add(new JLabel("max frequency"),c);
		c.gridx=1;c.gridy=5;
		control.add(maxfreq,c);
		c.gridx=0;c.gridy=6;
		control.add(setColor,c);
		c.gridx=0;c.gridy=7;
		control.add(cutS,c);
		c.gridx=1;c.gridy=7;
		control.add(gainS,c);
		c.gridx=0;c.gridy=8;
		control.add(new JLabel("cut"),c);
		c.gridx=1;c.gridy=8;
		control.add(new JLabel("gain"),c);
		control.setVisible(true);
		
	}
	
	@Override
	public int[][][] getFrame() {
		
		int[][][] frame = new int[d.width][d.height][3];
		
		double bass =0;
       	for(int i=fmin;i<fmax;i++) bass += fftdata[i]*fftdata[i];

       	int intensity = ((int)(Math.log(bass))-cut)*gain;
       	if(intensity > 255) intensity=255;
       	if(intensity <0) intensity =0;
		long time = System.currentTimeMillis();
		x+=xdir*(time-lastTime)/30.;
		y+=ydir*(time-lastTime)/30.;
		if(x >= maxx){
			x = 2*maxx - x;
			xdir = -xdir;
		}else if(x < minx){
			x = (minx-x) + minx;
			xdir = -xdir;
		}
		if(y >= maxy){
			y = 2*maxy - y;
			ydir = -ydir;
		}else if(y <miny){
			y = miny - y + miny;
			ydir = -ydir;
		}
		lastTime = time;
		int dispx = Math.min((int)x, maxx);
		dispx = Math.max(0,dispx);
		int dispy = Math.min((int)y, maxy);
		dispy = Math.max(0,dispy);
		frame[dispx][dispy][0] = 50;
		frame[dispx][dispy][1] = 50;
		frame[dispx][dispy][2] = 50;
		for(int i=0;i<d.width;i++){
			for(int j=0;j<d.height;j++){
				double distance = ((i-x)*(i-x) + (j-y)*(j-y)+.0001);
				double glow = (int)(intensity / distance);
				glow = Math.max(Math.min(glow, 205), 0);
				frame[i][j][0] += (glow * myColor.getRed()/255.);
				frame[i][j][1] += (glow * myColor.getGreen()/255.);
				frame[i][j][2] += (glow * myColor.getBlue()/255.);
			}
		}

		
		return frame;
	}
	
	public boolean[][] getAffectedPixels(){
		return new boolean[d.width][d.height];
	}

	@Override
	public JPanel getControls() {
		return control;
	}
	
	public void setState(boolean state){
		selected = state;
	}
	public boolean getState(){
		return selected;
	}
	
	public boolean isActive(){
		return active;
	}
	
	public void setActive(boolean set){
		active = set;
	}
	
	public void updateControls(){
		xmin.setText(minx+"");
		xmax.setText(maxx+"");
		ymin.setText(miny+"");
		ymax.setText(maxy+"");
		minfreq.setText(fmin+"");
		maxfreq.setText(fmax+"");
		System.out.println(cut);
		cutS.setValue(cut);
		gainS.setValue(gain);
		cutS.repaint();
		gainS.repaint();
	}

	public IntValue getFreqMin(){
		return new IntValue(fmin);
	}
	
	public IntValue getFreqMax(){
		return new IntValue(fmax);
	}
	
}
