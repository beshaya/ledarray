import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import pattern.*;

import java.io.File;
import java.lang.reflect.Constructor;

/**
 * Provides a JPanel for showing all patterns being used as a sidebar.
 * @author bshaya
 *
 */
public class PatternViewer extends JPanel{
	
	int frametime = 15;
	Vector<Pattern> patternList;
	Vector<PatternVis> subpanes;
	final JPanel scrollarea = new JPanel();
	final JScrollPane scroll = new JScrollPane(scrollarea);
	JLabel label = new JLabel("Patterns");
	
	String[] testStrings = {"Pattern 1", "Pattern 2"};
	final JComboBox pTypes = new JComboBox(testStrings);
	final JButton addPat = new JButton("add");
	final AudioCapture02 mainProgram;
	int xsize;
	int ysize;
	
	public PatternViewer(final AudioCapture02 mainProgram){
		super();
		this.mainProgram = mainProgram;
		this.frametime = mainProgram.frametime;
		xsize = mainProgram.xsize;
		ysize = mainProgram.ysize;
		patternList = new Vector<Pattern>();
		this.setPreferredSize(new Dimension(250,400));

		addPat.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Class selected = (Class)pTypes.getSelectedItem();
				Constructor[] ctors = selected.getDeclaredConstructors();
				Constructor ctor = null;
				for (int i = 0; i < ctors.length; i++) {
				    ctor = ctors[i];
				    if (ctor.getGenericParameterTypes().length == 3)
					break;
				}
				try{
					ctor.setAccessible(true);
					Pattern newp = (Pattern)ctor.newInstance(mainProgram.xsize, mainProgram.ysize, mainProgram.fftdata);
					System.out.println("Adding a new "+newp.toString());
					patternList.add(newp);
					updatePatterns();
				}catch (Exception E){
					E.printStackTrace();
				}
			}
		});
		
		//Find all available patterns and put in pTypes.
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
		  this.pTypes.setModel(new DefaultComboBoxModel(patternNames));
		  
		
		scrollarea.setPreferredSize(new Dimension(200,800));
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setPreferredSize(new Dimension(250,400));
		addPat.setText("add");
		JPanel addPanel = new JPanel();
		addPanel.add(pTypes);
		addPanel.add(addPat);
	    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.add(label);
		this.add(scroll);
		this.add(addPanel);
		pTypes.setVisible(true);
		updatePatterns();
		
		
	}

	public void setPatternList(Vector<Pattern> newList){
		patternList = newList;
		updatePatterns();
	}
	
	public void updatePatterns(){
		scrollarea.removeAll();
		//subpanes.removeAllElements();
		subpanes = new Vector<PatternVis>();
		for (Pattern p : patternList){
			PatternVis pvis = new PatternVis(p);
			subpanes.add(pvis);
			scrollarea.add(pvis);
		}
		int preferedHeight = 800;
		if(patternList.size() > 0){
			mainProgram.attachPatternPaneListeners(subpanes); //tell the main program to reattach listeners
			//calculate how large the scroll area should now be.
			preferedHeight = patternList.size() * (subpanes.get(0).getPreferredSize().height+6);
		}
		scrollarea.setPreferredSize(new Dimension(250,preferedHeight));
		this.validate();
	}
	
	public Vector<PatternVis> getSubpanes(){
		return subpanes;
	}
	public void repaint(){
		super.repaint();
	}
	
	
	public void setSelected(PatternVis p){
		for(PatternVis q : subpanes){
			if (q == p){
				q.getPattern().setState(true);
			}else{
				q.getPattern().setState(false);
			}
		}
		this.repaint();
	}
	
	int[][][] getFrame(){
		int[][][] frame = new int[xsize][ysize][3];
		double[][] maxLuma = new double[xsize][ysize]; //keep track of the largest luminocity from any pattern;
		//the combined pattern will not exceed any constituent's luminocity.
		double luma;
		
		for(int i=0;i<patternList.size();i++){
			if(patternList.get(i).isActive()){
				int[][][] contribution = patternList.get(i).getFrame();
				for(int j=0;j<contribution.length;j++)
					for(int k=0;k<contribution[0].length;k++){
						luma=.3*contribution[j][k][0] + .59*contribution[j][k][1] + .11*contribution[j][k][2];
						frame[j][k][0] += contribution[j][k][0];
						frame[j][k][1] += contribution[j][k][1];
						frame[j][k][2] += contribution[j][k][2];
						if(luma > maxLuma[j][k]) maxLuma[j][k] = luma;
					}
			}
		}
		
		//now normalize luminocity
		for(int j=0;j<frame.length;j++){
			for(int k=0;k<frame[0].length;k++){
				luma=.3*frame[j][k][0] + .59*frame[j][k][1] + .11*frame[j][k][2];
				frame[j][k][0] = (int)(frame[j][k][0] * maxLuma[j][k] / luma);
				frame[j][k][1] = (int)(frame[j][k][1] * maxLuma[j][k] / luma);
				frame[j][k][2] = (int)(frame[j][k][2] * maxLuma[j][k] / luma);
			}
		}
		return frame;
	}
	
	int[][] getAffected(){
		int[][] affected = new int[xsize][ysize];
		for (int i=0;i<patternList.size();i++){
			if (patternList.get(i).isActive() || patternList.get(i).getState()){
				int enabledness =0; //1 if isActive, 2 if getState; lets us see which pattern is being edited
				if(patternList.get(i).getState()) enabledness = 2;
				else enabledness = 1;
				boolean[][] contribution = patternList.get(i).getAffectedPixels();
				for(int j=0;j<contribution.length;j++){
					for(int k=0;k<contribution[0].length;k++){
						affected[j][k] = Math.max(contribution[j][k]?enabledness:0, affected[j][k]);
					}
				}
			}
		}
		return affected;
	}
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
	
	/**
	 * Set the label that appears above the pattern
	 * @param s the new label
	 */
	public void setLabel(String s){
		label.setText(s);
	}

}