package pattern;

import javax.swing.*;


public interface Pattern {
	
	int[][][] getFrame();
	
	public boolean getState();
	public void setState(boolean state);
	public void updateControls();
	public boolean isActive();
	public void setActive(boolean select);
	
	JPanel getControls();
	
}
