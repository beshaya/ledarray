package pattern;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import pattern.util.*;
import core.LightShow;
public class HashTag extends Border{
	public HashTag(int xsize, int ysize, double[] fftdata, LightShow main){
		super(xsize,ysize,fftdata, main);
	}
	
	@Override
	public int[][][] getFrame() {
		int[][][] frame = new int[d.width][d.height][3];
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
			boolean xborder = (i>=minx.getValue() && i<= minx.getValue() + thick.getValue() || i<=maxx.getValue() && i >= maxx.getValue() - thick.getValue());
			for(int j=0;j<d.height;j++){
				boolean yborder = (j>=miny.getValue() && j<= miny.getValue() + thick.getValue() || j<=maxy.getValue() && j >= maxy.getValue() - thick.getValue());
				if(yborder || xborder){
					frame[i][j][0] = (int)(intensity * myColor.getRed());
					frame[i][j][1] = (int)(intensity * myColor.getGreen());
					frame[i][j][2] = (int)(intensity * myColor.getBlue());
				}
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
			boolean xborder = (i>=minx.getValue() && i<= minx.getValue() + thick.getValue() || i<=maxx.getValue() && i >= maxx.getValue() - thick.getValue());
			for(int j=0;j<d.height;j++){
				boolean yborder = (j>=miny.getValue() && j<= miny.getValue() + thick.getValue() || j<=maxy.getValue() && j >= maxy.getValue() - thick.getValue());
				frame[i][j] = (yborder || xborder);
			}
		}
		return frame;
	}
}