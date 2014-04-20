package pattern;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.*;


import javax.swing.*;

/**
 * JPanel for displaying a pattern in the sidebar
 * Provides "enabled" for knowing whether or not the pattern should be added to others
 * and "remove" to remove it from the list.
 * @author bshaya
 *
 */
public class PatternVis extends JPanel{
	
	Pattern myPattern;
	final JCheckBox enabled = new JCheckBox();
	final JButton remove = new JButton("X");
	
	public PatternVis(Pattern p){
		super();
		myPattern = p;
		this.setPreferredSize(new Dimension (150,80));
		remove.setPreferredSize(new Dimension (20,20));
		remove.setFont(new Font("Dialog", 1, 10));
		remove.setBorder(null);
		enabled.setSelected(p.isActive());
		this.add(enabled);
		this.add(remove);
		
		enabled.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent a) {
				myPattern.setActive(enabled.isSelected());
			}
		});
	}
	
	public Pattern getPattern(){
		return myPattern;
	}
	
	public JButton getRemoveButton(){
		return remove;
	}
	
	public void paintComponent ( Graphics g ) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        Dimension d = getSize();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, d.width, d.height);
        int[][][] frame = myPattern.getFrame();
        //figure out how wide I can make teh pixelz
        int xsize = frame.length;
        int ysize = frame[0].length;
        Dimension mySize = this.getSize();
        int dotwidth = Math.max(Math.min((mySize.height-30)/ysize,mySize.width/xsize),2);
        for(int i=0;i<xsize;i++){
        	for(int j=0;j<ysize;j++){
        		g2.setColor(makeColor(frame[i][j][0],frame[i][j][1],frame[i][j][2]));
        		g2.fillRect(i*dotwidth+15,30+j*dotwidth,dotwidth-1,dotwidth-1);
        	}
        }
        if(myPattern.getState()){
        	g2.setColor(Color.RED);
        	g2.drawRect(1, 1, d.width-2, d.height-2);
        	g2.drawRect(2, 2, d.width-4, d.height-4);
        }
        
	}
	
	private Color makeColor(int r, int g, int b){
		r = Math.min(Math.max(r, 0),255);
		g = Math.min(Math.max(g, 0),255);
		b = Math.min(Math.max(b, 0),255);
		return new Color(r,g,b);
	}
	
}
