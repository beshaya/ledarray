package pattern;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import pattern.util.*;
import core.LightShow;
import core.PeakDetector;
public class Text implements Pattern{

	final protected Dimension d = new Dimension(0,0);
	protected double[] fftdata;
	protected IntValue fmax=new IntValue(100);
	protected IntValue fmin=new IntValue(20);
	protected IntValue cut= new IntValue(5);
	protected IntValue gain=new IntValue(20);
	protected Color myColor = new Color(255,0,0);

	//States whether or not the pattern's PatternVis is selected
	//Should be part of patternvis?
	public boolean selected;
	
	//Keeps track of whether or not the pattern is being added to the display frame.
	public boolean active;
	
	

	final JPanel control = new JPanel();
	
	final JButton setColor = new JButton("Pick Color");
	
	final TextSlider cutP = new TextSlider();
	final TextSlider gainP = new TextSlider();
	final TextSlider minfreq = new TextSlider();
	final TextSlider maxfreq = new TextSlider();
	final JCheckBox source = new JCheckBox();
	final JCheckBox fade = new JCheckBox();
	final JCheckBox sparkles = new JCheckBox();
	final JTextField txt = new JTextField();
	final JButton scroll = new JButton("Send!");
	PeakDetector peaks;	
	
	long sendTime;
	int rate = 40;
	String myString = "";
	
	public Text(int xsize, int ysize, double[] fftdata, LightShow main){
		d.setSize(xsize,ysize);
		this.fftdata = fftdata;
		selected=false;
		active = false;
		setColor.setBackground(myColor);
		this.peaks = main.getpeakdetector();
				
		//Create the elements for the control panel and their listeners
		cutP.setup(cut, 0, 20, TextSlider.VERTICAL);
		gainP.setup(gain, 0, 100, TextSlider.VERTICAL);
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
		
		txt.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				if ((int)arg0.getKeyChar()==10){ //allow return presses cause the value to be parsed
					myString = txt.getText();
					sendTime = System.currentTimeMillis();
				}
			}
			
		});
		
		scroll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				sendTime = System.currentTimeMillis();
				//System.out.println("Start");
				myString = txt.getText();
			}
		});
		
		//put everything into the frick'n layout
		control.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		control.setPreferredSize(new Dimension(300,500));
		c.gridx=0;c.gridy=0;c.gridwidth=2;c.anchor=GridBagConstraints.FIRST_LINE_START;
		c.fill=GridBagConstraints.BOTH;
		c.gridy++;
		control.add(new JLabel("Scrolling Text"),c);
		c.gridy++;
		control.add(txt,c);
		c.gridx=0;c.gridy++;
		control.add(scroll,c);
		
		c.gridx=0;c.gridy++;
		control.add(maxfreq,c);
		c.gridx=0;c.gridy++;
		control.add(setColor,c);
		c.gridx=0;c.gridy++;c.gridwidth=1;
		control.add(new JLabel("Use Beat Detector?"),c);
		c.gridx=1;
		control.add(source,c);
		
		c.gridx=0;c.gridy++;
		control.add(new JLabel("Constant fade"),c);
		c.gridx=1;
		control.add(fade,c);
		
		c.gridx=0;c.gridy++;
		control.add(new JLabel("Sparkles?"),c);
		c.gridx=1;
		control.add(sparkles,c);
		
		c.gridx=0;c.gridy++;
		control.add(cutP,c);
		c.gridx=1;
		control.add(gainP,c);
		c.gridx=0;c.gridy++;
		control.add(new JLabel("cut"),c);
		c.gridx=1;
		control.add(new JLabel("gain"),c);
		control.setVisible(true);
		
	}
	
	Color[] colors = {Color.blue, Color.red,new Color(144,0,144), Color.yellow, Color.green, Color.cyan, Color.orange};
	int lastpll=0;
	long beattime=0; 
	
	@Override
	public int[][][] getFrame() {
		int[][][] frame = new int[d.width][d.height][3];

		//want to display text from txt on the screen offset by time * rate
		
	    BufferedImage outputFrame = new BufferedImage(d.width,d.height,BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2d = outputFrame.createGraphics();
	    int fontsize = 6;
	    
        //g2d.drawImage(old, 0, 0, null);
        g2d.setFont(new Font("Serif", Font.BOLD, 20));
        
	    g2d.setPaint(myColor);
	    long now = System.currentTimeMillis();
	    int xpos = (int) (60 - (now-sendTime)/1000. * rate);
	    g2d.drawString(myString, xpos, 24);
	    g2d.dispose();
	    
	    /*
	    try {
	        File outputfile = new File("D:/javat.png");
	        outputfile.mkdirs();
	        if(outputfile.canWrite()){
	        	ImageIO.write(outputFrame, "png", outputfile);
	        }
	    } catch (IOException e) {
	    	System.out.println("ioexception");
	    } catch (NullPointerException e){
	    	System.out.println("write collision?");
	    }*/
	    	    
	    //copy the g2d object back.
	    for(int i=0;i<d.width;i++){
			for(int j=0;j<d.height;j++){
				Color c = new Color(outputFrame.getRGB(i,j));
				frame[i][j][0] = c.getRed();
				frame[i][j][1] = c.getGreen();
				frame[i][j][2] = c.getBlue();
			}
		}
		return frame;
	}
	
	public boolean[][] getAffectedPixels() {
		boolean[][] frame = new boolean[d.width][d.height];
		double bass =0;
		//24000 should be bin 512
		int minbin = fmin.getValue()*fftdata.length/2/24000;
		int maxbin = fmax.getValue()*fftdata.length/2/24000;
       	for(int i=minbin;i<maxbin;i++){
       		bass += fftdata[i];// *fftdata[i];
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
		for(int i=0;i<d.width;i++){
			for(int j=0;j<d.height;j++){
				int mag = (i-30)*(i-30)+(j-16)*(j-16);
				double angle = Math.atan2(j-16, i-30) / Math.PI * 3;
				frame[i][j] = (mag < 144) && ((angle + 6) % 2 > 1);
			}
		}
		return frame;
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
