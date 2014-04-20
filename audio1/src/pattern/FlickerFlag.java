package pattern;

//written by alyssa!

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pattern.util.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import core.LightShow;
public class FlickerFlag extends Border{
	public FlickerFlag(int xsize, int ysize, double[] fftdata, LightShow main){
		super(xsize,ysize,fftdata, main);
		minx.setValue(30);
		miny.setValue(16);
		myColor = Color.white;
	}
	
	int[][][] frame;
	long lasttime = 0; //time of the last frame
	int delay = 300; //milliseconds
	
	@Override
	public int[][][] getFrame() {
		double bass =0;
		//24000 should be bin 512
		int minbin = fmin.getValue()*fftdata.length/2/24000;
		int maxbin = fmax.getValue()*fftdata.length/2/24000;
       	for(int i=minbin;i<maxbin;i++){
       		bass += fftdata[i];// *fftdata[i];
       	}
       	double intensity = ((int)(Math.log(bass))-cut.getValue());
       	if((Math.log(bass)) == Double.NEGATIVE_INFINITY){
       		intensity = 0;
       	}
       	intensity = intensity * gain.getValue();
       	if(intensity > 255) intensity=255;
       	if(intensity <0) intensity =0;
       	intensity = intensity / 255;
       	Random randomGenerator = new Random();
       	long time = System.currentTimeMillis();
       	if(time > lasttime + delay){
    		frame = new int[d.width][d.height][3];

       		for (int k = 0; k< 1800; k++){
       			int i = (int)(Math.random()*d.width);
	       		int j = (int)(Math.random()*d.height);
	       		if((i-minx.getValue())*(i-minx.getValue())+(j-miny.getValue())*(j-miny.getValue())<=36){
	       			frame[i][j][0] = (int) (255*intensity); //(int)(1*myColor.getRed());
	       		}
	       		else{
	       			frame[i][j][0]  = (int) (myColor.getRed()*intensity);
	       			frame[i][j][1] = (int) (myColor.getGreen()*intensity);//(int)(1 * myColor.getGreen());
	       			frame[i][j][2] = (int) (myColor.getBlue()*intensity);
	       		}
       		}
       		lasttime = time;
       	}
       	/*
		for(int i=0;i<d.width;i++){
			for(int j=0;j<d.height;j++){
				if(i == 30 && j == 16){
					frame[i][j][0] = (int)(1 * myColor.getRed());
					frame[i][j][1] = (int)(1 * myColor.getGreen());
					frame[i][j][2] = (int)(1 * myColor.getBlue());
				}
			}
		}
		*/
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