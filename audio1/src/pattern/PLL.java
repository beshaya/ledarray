package pattern;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import pattern.util.*;
import core.PeakDetector;
import core.LightShow;

public class PLL implements Pattern{

	final protected Dimension d = new Dimension(0,0);
	protected double[] fftdata;
	protected IntValue minx=new IntValue(0);
	protected IntValue maxx=new IntValue(59);
	protected IntValue miny=new IntValue(0);
	protected IntValue maxy=new IntValue(24);
	protected IntValue fmax=new IntValue(100);
	protected IntValue fmin=new IntValue(20);
	protected IntValue cut= new IntValue(5);
	protected IntValue gain=new IntValue(20);
	protected Color myColor = new Color(255,0,0);

	PeakDetector peaks;
	//States whether or not the pattern's PatternVis is selected
	//Should be part of patternvis?
	public boolean selected;
	
	//Keeps track of whether or not the pattern is being added to the display frame.
	public boolean active;
	
	

	final JPanel control = new JPanel();
	
	final JButton setColor = new JButton("Pick Color");
	
	final TextSlider cutP = new TextSlider();
	final TextSlider gainP = new TextSlider();
	final TextSlider xmin = new TextSlider();
	final TextSlider xmax = new TextSlider();
	final TextSlider ymin = new TextSlider();
	final TextSlider ymax = new TextSlider();
	final TextSlider minfreq = new TextSlider();
	final TextSlider maxfreq = new TextSlider();
		
	
	public PLL(int xsize, int ysize, double[] fftdata, LightShow main){
		d.setSize(xsize,ysize);
		this.fftdata = fftdata;
		selected=false;
		active = false;
		minx.setValue(0);
		maxx.setValue(xsize-1);
		miny.setValue(0);
		maxy.setValue(ysize-1);
		setColor.setBackground(myColor);
		this.peaks = main.getpeakdetector();
		
		//Create the elements for the control panel and their listeners
		cutP.setup(cut, 0, 20, TextSlider.VERTICAL);
		gainP.setup(gain, 0, 100, TextSlider.VERTICAL);
		xmin.setup(minx,0,xsize-1, TextSlider.HORIZONTAL);
		xmax.setup(maxx,0,xsize-1, TextSlider.HORIZONTAL);
		ymin.setup(miny,0,ysize-1, TextSlider.HORIZONTAL);
		ymax.setup(maxy,0,ysize-1, TextSlider.HORIZONTAL);
		minfreq.setup(fmin,0,48000/2, TextSlider.HORIZONTAL);
		maxfreq.setup(fmax,0,48000/2, TextSlider.HORIZONTAL);
		
		setColor.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Color tempColor = JColorChooser.showDialog(control, "Pick a Color", myColor);
				if (tempColor != null)
					myColor = tempColor;
				setColor.setBackground(myColor);
			}
		});
		
		//put everything into the frick'n layout
		control.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		control.setPreferredSize(new Dimension(300,400));
		c.gridx=0;c.gridy=0;c.gridwidth=2;c.anchor=GridBagConstraints.FIRST_LINE_START;
		c.fill=GridBagConstraints.BOTH;
		control.add(new JLabel("x min"),c);
		c.gridx=0;c.gridy=1;c.gridwidth=2;
		control.add(xmin,c);
		c.gridx=0;c.gridy=2;
		control.add(new JLabel("x max"),c);
		c.gridx=0;c.gridy=3;
		control.add(xmax,c);
		c.gridx=0;c.gridy=4;
		control.add(new JLabel("y min"),c);
		c.gridx=0;c.gridy=5;
		control.add(ymin,c);
		c.gridx=0;c.gridy=6;
		control.add(new JLabel("y max"),c);
		c.gridx=0;c.gridy=7;
		control.add(ymax,c);
		c.gridx=0;c.gridy=8;
		control.add(new JLabel("min frequency"),c);
		c.gridx=0;c.gridy=9;
		control.add(minfreq,c);
		c.gridx=0;c.gridy=10;
		control.add(new JLabel("max frequency"),c);
		c.gridx=0;c.gridy=11;
		control.add(maxfreq,c);
		c.gridx=0;c.gridy=12;
		control.add(setColor,c);
		c.gridx=0;c.gridy=13;c.gridwidth=1;
		control.add(cutP,c);
		c.gridx=1;c.gridy=13;
		control.add(gainP,c);
		c.gridx=0;c.gridy=14;
		control.add(new JLabel("cut"),c);
		c.gridx=1;c.gridy=14;
		control.add(new JLabel("gain"),c);
		control.setVisible(true);
		
	}
	
	@Override
	public int[][][] getFrame() {
		int[][][] frame = new int[d.width][d.height][3];
		double pll = peaks.getpll() % 2;
		if(pll > 1){
			pll = 2-pll;
		}
		int col = (int)(pll*d.height);
		if(col >= d.height)
			col = d.height-1;
		if(col < 0)
			col = 0;
		for(int j=0;j<d.width;j++){
			frame[j][col][0] = 255;
		}
		
		return frame;
	}
	
	public boolean[][] getAffectedPixels() {
		boolean[][] frame = new boolean[d.width][d.height];

		for(int i=0;i<d.width;i++){
			if(i>=minx.getValue() && i<=maxx.getValue())
			for(int j=0;j<d.height;j++){
				if(j>=miny.getValue() && j<=maxy.getValue()){
					frame[i][j] = true;
				}
			}
		}
		return frame;
	}

	protected double integrateFFT(){
		double bass =0;
		//24000 should be bin 512
		int minbin = fmin.getValue()*fftdata.length/2/24000;
		int maxbin = fmax.getValue()*fftdata.length/2/24000;
       	for(int i=minbin;i<maxbin;i++){
       		bass += fftdata[i] *fftdata[i];
       		//System.out.print(i+" ");
       	}
       	double intensity = ((int)(Math.log(bass))-cut.getValue());
       	if((Math.log(bass)) == Double.NEGATIVE_INFINITY){
       		intensity = 0;
       	}
       	intensity = intensity * gain.getValue();
       	if(intensity > 255) intensity=255;
       	if(intensity <0) intensity =0;
       	intensity = intensity / 255;
       	return intensity;
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
		xmin.setFields(minx.getValue());
		xmax.setFields(maxx.getValue());
		ymin.setFields(miny.getValue());
		ymax.setFields(maxy.getValue());
		minfreq.setFields(fmin.getValue());
		maxfreq.setFields(fmax.getValue());
		gainP.setFields(gain.getValue());
		gainP.repaint();
	}

	public String toString(){
		return "RectPattern";
	}
	
	public IntValue getFreqMin(){
		return fmin;
	}
	
	public IntValue getFreqMax(){
		return fmax;
	}
}