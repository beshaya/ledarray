package pattern;

import javax.swing.*;

import pattern.util.IntValue;


public interface Pattern {
	
	int[][][] getFrame();
	
	/**
	 * Records whether the pattern is currently being edited by the main program
	 * @return true IFF the pattern is being edited
	 */
	public boolean getState();
	public void setState(boolean state);
	public void updateControls();
	
	/**
	 * Records whether the pattern is being displayed onto the main program
	 * @return true IFF the pattern is being displayed
	 */
	public boolean isActive();
	public void setActive(boolean select);
	public IntValue getFreqMin();
	public IntValue getFreqMax();
	public boolean[][] getAffectedPixels();
	
	JPanel getControls();
	
	
	
}
