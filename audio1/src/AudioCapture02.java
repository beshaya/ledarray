/*File AudioCapture02.java
This program demonstrates the capture and 
subsequent playback of audio data.

A GUI appears on the screen containing the 
following buttons:
Capture
Stop
Playback

Input data from a microphone is captured and 
saved in a ByteArrayOutputStream object when the
user clicks the Capture button.

Data capture stops when the user clicks the Stop 
button.

Playback begins when the user clicks the Playback
button.

This version of the program gets and  displays a
list of available mixers, producing the following
output:

Available mixers:
Java Sound Audio Engine
Microsoft Sound Mapper
Modem #0 Line Record
ESS Maestro

Thus, this machine had the four mixers listed 
above available at the time the program was run.

Then the program gets and uses one of the 
available mixers instead of simply asking for a 
compatible mixer as was the case in a previous 
version of the program.

Either of the following two mixers can be used in
this program:

Microsoft Sound Mapper
ESS Maestro

Neither of the following two mixers will work in
this program.  The mixers fail at runtime for 
different reasons:

Java Sound Audio Engine
Modem #0 Line Record

The Java Sound Audio Engine mixer fails due to a 
data format compatibility problem.

The Modem #0 Line Record mixer fails due to an 
"Unexpected Error"

Tested using SDK 1.4.0 under Win2000
************************************************/

import javax.swing.*;
import javax.swing.Timer;
import javax.imageio.*;

import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.io.*;

import javax.sound.sampled.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import pattern.*;
import pattern.util.IntValue;
import pattern.util.TextSlider;

import java.util.*;
import java.net.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AudioCapture02 extends JFrame{

	boolean enableoutput = false; //enables file writing for communication with processing.
  boolean stopCapture = false;
  ByteArrayOutputStream byteArrayOutputStream;
  double[] samples;
  double[] fftdata;
  
  final int samplelength = 44000;
  final int bufferlength = 1000;
  final int frametime = 15;
  final int fftpoints = 8192/8;
  
  AudioFormat audioFormat;
  TargetDataLine targetDataLine;
  AudioInputStream audioInputStream;
  SourceDataLine sourceDataLine;
  
  final int xsize = 60;
  final int ysize = 24;
  
  private IntValue myGain = new IntValue(10);
  
  final JButton captureBtn =   new JButton("Capture");
  final JButton stopBtn = new JButton("Stop");
  final JButton switchBtn = new JButton("Switch!");
  final TextSlider globalGain = new TextSlider();
  
  final PatternViewer livePatView = new PatternViewer(this);
  final PatternViewer workPatView = new PatternViewer(this);
  final Visualizer vis = new Visualizer();
  final FFTVisualizer fftwindow = new FFTVisualizer();
  final DisplayMirror mirror = new DisplayMirror(); //Displays a copy of what goes to the array
  final DisplayMirror sandbox = new DisplayMirror();
  final FFT fft = new FFT(fftpoints);
  final double[] window = fft.getBlackman4();
  final JPanel gain = new JPanel();
  final PatternCombiner pComb = new PatternCombiner();
  
  //Woo server shitz
  ServerSocket srvr;
  Socket skt;
  PrintWriter out;
  
  Vector<Pattern> livePat = new Vector<Pattern>();
  Vector<Pattern> workPat = new Vector<Pattern>();
  
  public static void main(String args[]){
    new AudioCapture02();
  }//end main

  final JSlider basscut = new JSlider(JSlider.VERTICAL, 0, 20, 5);
  final JSlider bassgain = new JSlider(JSlider.VERTICAL,0,100,10);
private Timer myTimer;
  
  public AudioCapture02(){//constructor

	  /*
	  try {
		srvr = new ServerSocket(5432);
		skt = srvr.accept();
        out = new PrintWriter(skt.getOutputStream(), true);
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}*/

	  Set<Class> s = getClassesInPackage("pattern");
	  Iterator<Class> itr = s.iterator();
	  Set<Class> patternClasses = new HashSet<Class>();
	  while(itr.hasNext()){
		  Class c = itr.next();
		  if(Pattern.class.isAssignableFrom(c) && !Pattern.class.equals(c)){
			  //Filter out all the classes that are not proper subclasses of pattern
			  System.out.println(c.toString());
			  patternClasses.add(c);
		  }else{}
	  }
	  Class[] patternNames = new Class[patternClasses.size()];
	  int j=0;
	  for(Iterator<Class> iter=patternClasses.iterator();iter.hasNext();){
		  patternNames[j++] = iter.next();
		  
	  }
	  
	  globalGain.setup(myGain, 0, 100, TextSlider.HORIZONTAL);
	  
	samples = new double[samplelength];
	fftdata = new double[fftpoints];
    captureBtn.setEnabled(true);
    stopBtn.setEnabled(false);
    basscut.setName("basscut");
    bassgain.setName("bassgain");
    
    //Do things on every new frame
    FrameDriver newframe = new FrameDriver();
    
    myTimer = new Timer(frametime, newframe);
    //Register anonymous listeners
    captureBtn.addActionListener(
      new ActionListener(){
        public void actionPerformed(ActionEvent e){
          captureBtn.setEnabled(false);
          stopBtn.setEnabled(true);

          vis.repaint();
          //Capture input data from the
          // microphone until the Stop button is
          // clicked.
          captureAudio();
        }//end actionPerformed
      }//end ActionListener
    );//end addActionListener()
    
    stopBtn.addActionListener(
      new ActionListener(){
        public void actionPerformed(
        	                 ActionEvent e){
            captureBtn.setEnabled(true);
            stopBtn.setEnabled(false);
          //Terminate the capturing of input data
          // from the microphone.
          stopCapture = true;
        }//end actionPerformed
      }//end ActionListener
    );//end addActionListener()
    
    //When this button is hit, move all the patterns from sandbox to live and vice versa.
    switchBtn.addActionListener(
    	      new ActionListener(){
    	        public void actionPerformed( ActionEvent e){
    	        	System.out.println("switch!");
    	          Vector<Pattern> tempList = new Vector<Pattern>();
    	          for(Pattern p : livePat){
    	        	  tempList.add(p);
    	          }
    	          livePat.removeAllElements();
    	          for(Pattern p : workPat){
    	        	  livePat.add(p);
    	          }
    	          workPat.removeAllElements();
    	          for(Pattern p : tempList){
    	        	  workPat.add(p);
    	          }
    	          livePatView.updatePatterns();
    	          workPatView.updatePatterns();
    	        }//end actionPerformed
    	      }//end ActionListener
    	    );//end addActionListener()


    //put everything into the GUI
    getContentPane().setLayout(new BorderLayout());
    JSplitPane audioData = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            fftwindow,vis);
    JPanel displays = new JPanel();
    displays.setLayout(new GridLayout(2,1));
    displays.add(mirror);
    displays.add(sandbox);
    mirror.setBackground(Color.black);
    sandbox.setBackground(new Color(0,0,0));
    JSplitPane mainDisplay = new JSplitPane(JSplitPane.VERTICAL_SPLIT,displays,audioData);
    audioData.setOneTouchExpandable(true);
    audioData.setDividerLocation(100);
    mainDisplay.setOneTouchExpandable(true);
    mainDisplay.setDividerLocation(500);
    JPanel buttonpanel = new JPanel();
    buttonpanel.add(captureBtn);
    buttonpanel.add(stopBtn);
    buttonpanel.add(switchBtn);
    buttonpanel.add(new JLabel("Gain: "));
    buttonpanel.add(globalGain);
    gain.setPreferredSize(new Dimension(200,800));

    livePat= new Vector<Pattern>();
    livePat.add(new SolidRectPattern(xsize,ysize,fftdata));
    livePat.add(new SolidRectPattern(xsize,ysize,fftdata));
    livePatView.setPatternList(livePat);
    livePatView.setLabel("Live Patterns");

    workPat = new Vector<Pattern>();
    workPat.add(new SolidRectPattern(xsize,ysize,fftdata));
    workPat.add(new Border(xsize,ysize,fftdata));
    workPatView.setPatternList(workPat);
    workPatView.setLabel("Sandbox");
    
    JPanel workspace = new JPanel();
    GridLayout workLayout = new GridLayout(1,2);
    workspace.setLayout(workLayout);
    workspace.add(gain);
    workspace.add(workPatView);
    
    getContentPane().add(buttonpanel, BorderLayout.NORTH);
    getContentPane().add(mainDisplay, BorderLayout.CENTER);
    getContentPane().add(workspace,BorderLayout.LINE_START);
    getContentPane().add(livePatView,BorderLayout.EAST);

    setTitle("Capture/Playback Demo");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(1250,800);
    
    myTimer.start();
    
    setVisible(true);
  }//end constructor

  //This method captures audio input from a
  // microphone and saves it in a
  // ByteArrayOutputStream object.
  private void captureAudio(){
    try{
      //Get and display a list of
      // available mixers.
      Mixer.Info[] mixerInfo = 
                      AudioSystem.getMixerInfo();
      System.out.println("Available mixers:");
      int mixerinx = 0;
      for(int cnt = 0; cnt < mixerInfo.length;
                                          cnt++){
      	System.out.println(mixerInfo[cnt].
      	                              getName());
      	if(mixerInfo[cnt].getName().contains("Primary Sound Capture")) mixerinx = cnt;
      }//end for loop

      //Get everything set up for capture
      audioFormat = getAudioFormat();

      DataLine.Info dataLineInfo =
                            new DataLine.Info(
                            TargetDataLine.class,
                            audioFormat);

      //Select one of the available
      // mixers.
      System.out.println("Attempting to use: "+ mixerInfo[mixerinx].getName());
      Mixer mixer = AudioSystem.
                          getMixer(mixerInfo[mixerinx]);
      
      //Get a TargetDataLine on the selected
      // mixer.
      targetDataLine = (TargetDataLine)
                     mixer.getLine(dataLineInfo);
      //Prepare the line for use.
      targetDataLine.open(audioFormat);
      targetDataLine.start();

      //Create a thread to capture the microphone
      // data and start it running.  It will run
      // until the Stop button is clicked.
      Thread captureThread = new CaptureThread();
      captureThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){ 
          public void uncaughtException(Thread t, Throwable e) {
              captureBtn.setEnabled(true);
              stopBtn.setEnabled(false);
              targetDataLine.close();
              //samples = convertSamples();
              //vis.repaint();
           }
      });
      
      captureThread.start();
    } catch (Exception e) {
      System.out.println(e);
      System.exit(0);
    }//end catch
  }//end captureAudio method

  //This method plays back the audio data that
  // has been saved in the ByteArrayOutputStream
  private void playAudio() {
    try{
      //Get everything set up for playback.
      //Get the previously-saved data into a byte
      // array object.
      byte audioData[] = byteArrayOutputStream.
                                   toByteArray();
      //Get an input stream on the byte array
      // containing the data
      InputStream byteArrayInputStream =
             new ByteArrayInputStream(audioData);
      AudioFormat audioFormat = getAudioFormat();
      audioInputStream = new AudioInputStream(
                    byteArrayInputStream,
                    audioFormat,
                    audioData.length/audioFormat.
                                 getFrameSize());
      DataLine.Info dataLineInfo = 
                            new DataLine.Info(
                            SourceDataLine.class,
                            audioFormat);
      sourceDataLine = (SourceDataLine)
               AudioSystem.getLine(dataLineInfo);
      sourceDataLine.open(audioFormat);
      sourceDataLine.start();

      //Create a thread to play back the data and
      // start it  running.  It will run until
      // all the data has been played back.
      Thread playThread = new PlayThread();
      playThread.start();
    } catch (Exception e) {
      System.out.println(e);
      System.exit(0);
    }//end catch
  }//end playAudio

  //This method creates and returns an
  // AudioFormat object for a given set of format
  // parameters.  If these parameters don't work
  // well for you, try some of the other
  // allowable parameter values, which are shown
  // in comments following the declartions.
  private AudioFormat getAudioFormat(){
    float sampleRate = 44100.0F;
    //8000,11025,16000,22050,44100
    int sampleSizeInBits = 16;
    //8,16
    int channels = 1;
    //1,2
    boolean signed = true;
    //true,false
    boolean bigEndian = false;
    //true,false
    return new AudioFormat(
                      sampleRate,
                      sampleSizeInBits,
                      channels,
                      signed,
                      bigEndian);
  }//end getAudioFormat
  
  private short[] convertSamples(){
	  int bytes = byteArrayOutputStream.size();
	  byte[] data = byteArrayOutputStream.toByteArray();
	  short[] samples = new short[bytes/2];
	  for(int i=0;i<bytes;i+=2){
		  samples[i/2] = (short)
				  (  (data[i + 0] & 0xFF)
				   | (data[i + 1] << 8)  );
	  }
	  return samples;
  }
//=============================================//

//Inner class to capture data from microphone
class CaptureThread extends Thread{
  //An arbitrary-size temporary holding buffer
  byte tempBuffer[] = new byte[2*bufferlength];
  public void run(){
    byteArrayOutputStream =
                     new ByteArrayOutputStream();
    stopCapture = false;
    try{//Loop until stopCapture is set by
        // another thread that services the Stop
        // button.
      while(!stopCapture ){
        //Read data from the internal buffer of
        // the data line.
        int cnt = targetDataLine.read(tempBuffer,
                              0,
                              tempBuffer.length);
        if(cnt > 0){
          //Save data in output stream object.
          byteArrayOutputStream.write(tempBuffer,0,cnt);
          //move the old samples left [bufferlength] positions
          //to make room for new samples
          for(int i=bufferlength;i < samplelength;i++){
        	  samples[i-bufferlength]=samples[i];
          }
          //convert new data and put into samples
          double gain = myGain.getValue() / 10.;
          for(int i=0;i<bufferlength;i++){
        	  samples[i+samplelength-bufferlength] = (double)
            		  (  (tempBuffer[i*2 + 0] & 0xFF)
                   		   | (tempBuffer[i*2 + 1] << 8)  )/32768.0 * gain;

          }
        }//end if
      }//end while
      byteArrayOutputStream.close();

    }catch (Exception e) {
      System.out.println(e);
      System.exit(0);
    }//end catch
    throw new RuntimeException();
  }//end run
}//end inner class CaptureThread

//===================================//
//Inner class to play back the data
// that was saved.
class PlayThread extends Thread{
  byte tempBuffer[] = new byte[10000];

  public void run(){
    try{
      int cnt;
      //Keep looping until the input read method
      // returns -1 for empty stream.
      while((cnt = audioInputStream.read(
      	              tempBuffer, 0,
                      tempBuffer.length)) != -1){
        if(cnt > 0){
          //Write data to the internal buffer of
          // the data line where it will be
          // delivered to the speaker.
          sourceDataLine.write(tempBuffer,0,cnt);
        }//end if
      }//end while
      //Block and wait for internal buffer of the
      // data line to empty.
      sourceDataLine.drain();
      sourceDataLine.close();


    }catch (Exception e) {
      System.out.println(e);
      System.exit(0);
    }//end catch
  }//end run
}//end inner class PlayThread
//=============================================//

/**
 * Sums all active patterns and displays the pattern on the screen.
 * @author bshaya
 *
 */
class Visualizer extends JPanel{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void paintComponent ( Graphics g ) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        Dimension d = getSize();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, d.width, d.height);
        
        g2.setColor(Color.blue);
        if(samples != null){
        	int numsamp = samples.length;
        	for(int i=0;i< numsamp;i++)
        	g2.drawRect(d.width*i/numsamp, (int)(d.height*(samples[i]+1.)/2.),1,1);
        }
    }
	
}


class FFTVisualizer extends JPanel{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IntValue fmin = new IntValue(0);
	private IntValue fmax = new IntValue(0);
	
	public void setFPmin(IntValue i){
		fmin = i;
	}
	public void setFPmax(IntValue i){
		fmax = i;
	}
	public void paintComponent ( Graphics g ) {
		double div = 6.;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        Dimension d = getSize();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, d.width, d.height);
        
        g2.setColor(Color.DARK_GRAY);
        int fLow = fmin.getValue()*fftpoints / 2 / 24000;
        int fHigh = fmax.getValue()*fftpoints / 2 / 24000;
        g2.fillRect(d.width*fLow*2/fftpoints,0,d.width*(fHigh-fLow)*2/fftpoints,d.height);
        g2.setColor(Color.blue);
        double max=0;
       	for(int i=0;i< fftpoints/2;i++){
       	g2.drawRect(d.width*i*2/fftpoints, d.height-(int)(d.height*Math.log(fftdata[i])/div),1,(int)(d.height*Math.log(fftdata[i])/div));
       	}
       	double bass =0;
       	for(int i=0;i<4;i++) bass += fftdata[i]*fftdata[i];
    }

	public void setFPmin(int freqMin) {
		// TODO Auto-generated method stub
		
	}

	public void setFPmax(int freqMax) {
		// TODO Auto-generated method stub
		
	}
	
}

class DisplayMirror extends JPanel{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int[][][] frame;
	private int[][] affectedPix;
	private Color bg;
	
	public DisplayMirror(){
		super();
		frame = new int[1][1][3];
		affectedPix = new int[1][1];
		bg = Color.black;
	}
	public void setFrame(int[][][] newFrame, int[][] newAffected){
		this.frame = newFrame;
		this.affectedPix = newAffected;
		this.repaint();
	}
	
	public void setBackground(Color bg){
		this.bg = bg;
	}
	
	public void paintComponent ( Graphics g ) {
		//System.out.println(patternList.size());
		double div = 6.;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        Dimension d = getSize();
        g2.setColor(bg);
        g2.fillRect(0, 0, d.width, d.height);
        int bwidth = Math.min(d.width/frame.length, d.height/frame[0].length);
        for(int i=0;i<frame.length;i++){
        	for(int j=0;j<frame[0].length;j++){
        		if(affectedPix[i][j] == 1){
        			g2.setColor(new Color(50,50,50));
        			g2.drawRect(i*bwidth,j*bwidth,bwidth,bwidth);
        		}
          		g2.setColor(new Color(frame[i][j][0],frame[i][j][1],frame[i][j][2]));
           		g2.fillRect(i*bwidth,j*bwidth,bwidth-1,bwidth-1);
           		if (affectedPix[i][j] ==2){
        			g2.setColor(new Color(80,80,80));
        			g2.drawRect(i*bwidth,j*bwidth,bwidth,bwidth);
        		}
           	}
        }
    }
	
}

class PatternCombiner{
	
	
}

/**
 * Given a package name, attempts to reflect to find all classes within the package
 * on the local file system.
 * 
 * @param packageName
 * @return
 */
private static Set<Class> getClassesInPackage(String packageName) {
	Set<Class> classes = new HashSet<Class>();
	String separator = System.getProperty("file.separator");
	String packageNameSlashed = separator + packageName.replace(".", separator);
	// Get a File object for the package

	String directoryString = System.getProperty("java.class.path")+packageNameSlashed;
	System.out.println(directoryString);

	File directory = new File(directoryString);
	if (directory.exists()) {
		// Get the list of the files contained in the package
		String[] files = directory.list();
		for (String fileName : files) {
			// We are only interested in .class files
			if (fileName.endsWith(".class")) {
				// Remove the .class extension
				fileName = fileName.substring(0, fileName.length() - 6);
				try {
					classes.add(Class.forName(packageName + "." + fileName));
				} catch (ClassNotFoundException e) {
					System.out.println(packageName + "." + fileName + " does not appear to be a valid class.");
				}
			}
		}
	} else {
		System.out.println(packageName + " does not appear to exist as a valid package on the file system.");
	}
	return classes;
}

public void attachPatternPaneListeners(Vector<PatternVis> patternPanes){
    //add listeners to each PatternVis so we know when a pattern is selected
    //When the pattern is selected, put its controls into the control frame

    for(PatternVis ppane : patternPanes){
    	ppane.addMouseListener(new MouseListener(){
    		
			public void mouseClicked(MouseEvent e) {		
				PatternVis component = (PatternVis)e.getComponent();
				livePatView.setSelected(component);
				workPatView.setSelected(component);
				gain.removeAll();
				gain.validate();
				gain.repaint();
				gain.add(component.getPattern().getControls());
				gain.validate();		
				component.getPattern().updateControls();
				fftwindow.setFPmin(component.getPattern().getFreqMin());
				fftwindow.setFPmax(component.getPattern().getFreqMax());
			}

			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
    		public void mousePressed(MouseEvent e){}
    	});
    	
    	//Attach an action listener to remove the pattern
    	ppane.getRemoveButton().addActionListener(new ActionListener(){
    		
    		public void actionPerformed(ActionEvent e){
				PatternVis component= (PatternVis)((JButton)e.getSource()).getParent();
    			livePat.remove(component.getPattern());
    			workPat.remove(component.getPattern());
    			livePatView.updatePatterns();
    			workPatView.updatePatterns();
    		}
    	});
    }
}

class FrameDriver implements ActionListener{
	  public void actionPerformed(ActionEvent evt) {
	    vis.repaint();
	    
	    //calculate the fft
	    double[] fftinput = new double[fftpoints];
	    for(int i=0;i<fftpoints;i++){
	    	fftinput[i] = samples[i+samplelength-fftpoints] * window[i];
	    }
	    double[] zeroes = new double[fftpoints];
	    fft.fft(fftinput, zeroes);
	    //combine data into power
	    for(int i=0;i<fftpoints/2;i++){
	    	fftdata[i] = fftinput[i]*fftinput[i] + zeroes[i]*zeroes[i];
	    }
	    //display the fft
	    fftwindow.repaint();
	    livePatView.repaint();
	    workPatView.repaint();
	    
	    //COMBINE!
	    int[][][] combinedFrame= livePatView.getFrame();
	    int[][] affectedPix = livePatView.getAffected();
	    
	    BufferedImage outputFrame = new BufferedImage(xsize,32,BufferedImage.TYPE_INT_RGB);
	    for(int i=0;i<xsize;i++){
	    	for(int j=0;j<ysize;j++){
	    		Color c = new Color(combinedFrame[i][j][0]>>2,combinedFrame[i][j][1]>>2,combinedFrame[i][j][2]>>2);
	    		outputFrame.setRGB(i,j,c.getRGB());
	    	}
	    }
	    
	    //Also combine what we're working on in the sandbox
	    int[][][] sandFrame= workPatView.getFrame();
	    int[][] sandAffected = workPatView.getAffected();
	    
	    
	    
	    //output the combined pattern to a .png
	    //maybe in future versions, this will be sent across a network!
	    //TODO: Make server code work...
	    /*
	      try {

	          //System.out.print("Server has connected!\n");

			  out.print((char)255);
	          
			  for(int i=0;i<xsize;i++){
			   	for(int j=0;j<ysize;j++){
			   		out.print((char)combinedFrame[i][j][0]);
			   		out.print((char)combinedFrame[i][j][1]);
			   		out.print((char)combinedFrame[i][j][2]);
			   	}
		   		//out.print((char)255);
			  }
			  
	          //skt.close();
	          //srvr.close();
	       }
	       catch(Exception e) {
	          e.printStackTrace();
	       }
	    */
	      
	    /*
	    try {
			PrintWriter writer = new PrintWriter("C:/Users/bshaya/Desktop/OctoWS2811/examples/VideoDisplay/Processing/file2serial/java.txt", "UTF-8");
		    for(int i=0;i<xsize;i++){
		    	for(int j=0;j<ysize;j++){
		    		writer.print(combinedFrame[i][j][0]);
		    		writer.print(combinedFrame[i][j][1]);
		    		writer.print(combinedFrame[i][j][2]);
		    	}
		    }
		    writer.close();
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    */
	    if(enableoutput && !stopCapture)
	    try {
	        File outputfile = new File("D:/java.png");
	        outputfile.mkdirs();
	        if(outputfile.canWrite()){
	        	ImageIO.write(outputFrame, "png", outputfile);
	        }
	    } catch (IOException e) {
	    	System.out.println("ioexception");
	    } catch (NullPointerException e){
	    	System.out.println("write collision?");
	    }
	    mirror.setFrame(combinedFrame,affectedPix);
	    sandbox.setFrame(sandFrame, sandAffected);
	  }
	}
}//end outer class AudioCapture02.java