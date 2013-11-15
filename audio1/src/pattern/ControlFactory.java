package pattern;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pattern.util.*;

public abstract class ControlFactory {
	
	public static final int HORIZONTAL = SwingConstants.HORIZONTAL;
	public static final int VERTICAL = SwingConstants.VERTICAL;
	
	public static JPanel textSlider(final IntValue value, final int min, final int max, int init, int orientation){
		final JPanel myPanel = new JPanel();
		myPanel.setLayout(new GridBagLayout());
		final JSlider mySlider = new JSlider(orientation,min,max,init); //make and set up a slider
		final JTextField myText = new JTextField(init+"");//make a text field
		
		//set up event handlers
		myText.addFocusListener(
			      new FocusListener(){
					public void focusGained(FocusEvent arg0) {
					}
					public void focusLost(FocusEvent arg0) {
			        	int val = 0;
			        	try{
			        		val = Integer.parseInt(myText.getText());
			        	}catch(Exception ex){
			        		myText.setText(value.toString()); //if parsing fails, reset
			        	}
			        	if (val < min){
			        		myText.setText(min+"");
			        		value.setValue(min);
			        	}else if (val > max){
			        		myText.setText(max+"");
			        		value.setValue(max);
			        	}else{
			        		value.setValue(val);
			        	}
			        	mySlider.setValue(value.getValue());
					}
			      });
		mySlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				value.setValue(mySlider.getValue());
				myText.setText(value.toString());
			}
		});
		
		GridBagConstraints c = new GridBagConstraints();
		if(orientation == HORIZONTAL){
			c.gridx=0;c.gridy=0;
			myPanel.add(myText);
			c.gridx=1;c.gridy=0;
			myPanel.add(mySlider);
		}else if(orientation == VERTICAL){
			c.gridx=0;c.gridy=0;
			myPanel.add(mySlider);
			c.gridx=0;c.gridy=1;
			myPanel.add(myText);
		}
		return myPanel;
	}
	
	public static JPanel textSlider(IntValue value, int min, int max, int init){
		return textSlider(value,min,max,init, HORIZONTAL);
	}
}
