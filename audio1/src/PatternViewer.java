import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import pattern.*;
import java.lang.reflect.Constructor;

/**
 * Provides a JPanel for showing all patterns being used as a sidebar.
 * @author bshaya
 *
 */
public class PatternViewer extends JPanel{
	
	final int frametime = 15;
	Vector<Pattern> patternList;
	Vector<PatternVis> subpanes;
	final JPanel scrollarea = new JPanel();
	final JScrollPane scroll = new JScrollPane(scrollarea);
	String[] testStrings = {"Pattern 1", "Pattern 2"};
	final JComboBox pTypes = new JComboBox(testStrings);
	final JButton addPat = new JButton("add");
	final AudioCapture02 mainProgram;
	
	public PatternViewer(final AudioCapture02 mainProgram){
		super();
		this.mainProgram = mainProgram;
		patternList = new Vector<Pattern>();
		this.setPreferredSize(new Dimension(300,400));

		addPat.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Class selected = (Class)pTypes.getSelectedItem();
				System.out.println(selected);
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
		JLabel label = new JLabel("Patterns");
		scrollarea.setPreferredSize(new Dimension(250,800));
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
		if(patternList.size() > 0)
			mainProgram.attachPatternPaneListeners();
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
	}
	

}