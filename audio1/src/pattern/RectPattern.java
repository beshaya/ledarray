package pattern;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pattern.util.IntValue;
import core.LightShow;
public class RectPattern extends SolidRectPattern{


	public RectPattern(int xsize, int ysize, double[] fftdata, LightShow main) {
		super(xsize, ysize, fftdata,main);
		this.control.remove(this.setColor);
	}

	@Override
	public int[][][] getFrame() {
		
		int[][][] frame = new int[d.width][d.height][3];
		
		Double intensity = integrateFFT();
		intensity *= 255;

		for(int i=0;i<d.width;i++){
			if(i>=minx.getValue() && i<=maxx.getValue())
			for(int j=0;j<d.height;j++){
				if(j>=miny.getValue() && j<=maxy.getValue()){
					frame[i][j][0] = (int)(intensity * i / d.width);
					frame[i][j][1] = (int)(intensity * (d.width-i) / d.width);
					frame[i][j][2] = (int)(intensity * j / d.height);
				}
			}
		}
		return frame;
	}
}