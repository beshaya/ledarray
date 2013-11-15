package pattern.util;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class TextSlider extends JPanel{

	public static final int HORIZONTAL = SwingConstants.HORIZONTAL;
	public static final int VERTICAL = SwingConstants.VERTICAL;
	final JSlider mySlider = new JSlider();
	final JTextField myText = new JTextField();
	
	public TextSlider(){
		super();
	}
	
	public void setup(final IntValue value, final int min, final int max, int orientation){
		this.setLayout(new GridBagLayout());
		mySlider.setOrientation(orientation);
		mySlider.setMinimum(min);
		mySlider.setMaximum(max);
		mySlider.setValue(value.getValue());
		myText.setText(value.toString());//make a text field
		
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
		myText.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				if ((int)arg0.getKeyChar()==10){ //allow return presses cause the value to be parsed
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
			}
			
		});
		mySlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				value.setValue(mySlider.getValue());
				myText.setText(value.toString());
			}
		});
		
		myText.setPreferredSize(new Dimension(50,20));
		myText.setMinimumSize(new Dimension(40,20));
		GridBagConstraints c = new GridBagConstraints();
		if(orientation == HORIZONTAL){
			mySlider.setMinimumSize(new Dimension(100,20));
			c.gridx=0;c.gridy=0;
			this.add(myText);
			c.gridx=1;c.gridy=0;
			this.add(mySlider);
		}else if(orientation == VERTICAL){
			mySlider.setMinimumSize(new Dimension(30,80));
			c.gridx=0;c.gridy=0;
			this.add(mySlider,c);
			c.gridx=0;c.gridy=1;
			this.add(myText,c);
		}
	}
	
	public void setFields(int i){
		myText.setText(i+"");
		mySlider.setValue(i);
		this.repaint();
	}
}
