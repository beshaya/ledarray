package pattern;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import pattern.util.*;
import core.LightShow;



public class Fireworks extends Border{
	
	int numsplode = 10;
	Sploder[] sploderz = new Sploder[numsplode];
	int nextwork = 0;
	
	public Fireworks(int xsize, int ysize, double[] fftdata, LightShow main){
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
       	
       	long now = System.currentTimeMillis();
		double pll = peaks.getpll();
		if(pll >= lastpll+1){
			if(peaks.getPower() > 1.2){ //only generate a new firework if the peak detector sees above average power
				lastpll = (int)pll;
				beattime = System.currentTimeMillis();
				Color thisColor = colors[(int)pll%colors.length];
				int x = (int) (Math.random()*(maxx.getValue()-minx.getValue()-2*thick.getValue()) + minx.getValue()+thick.getValue());
				int y = (int) (Math.random()*(maxy.getValue()-miny.getValue()-2*thick.getValue()) + miny.getValue() + thick.getValue());
				sploderz[nextwork] = new Sploder(x,y,thisColor,now);
				nextwork = (nextwork+1)%numsplode;
			}
		}
		
		//pll = Math.max(lastpll,pll);

		intensity = Math.min(Math.max(1.5 - (now-beattime) * .005,0),1);
		
		for(int i=0;i<numsplode;i++){
			if(sploderz[i] != null){
				double[][] pellets = sploderz[i].tic(now);
				Color sColor = sploderz[i].myColor;
				for(int j=0;j<pellets.length;j++){
					int x = (int)pellets[j][0];
					int y = (int)pellets[j][1];
					if(x < maxx.getValue() && x > minx.getValue() && y < maxy.getValue() && y > miny.getValue()){
						frame[x][y][0] += sColor.getRed() >> 4;
						frame[x][y][1] += sColor.getGreen() >> 4;
						frame[x][y][2] += sColor.getBlue() >> 4;
					}
				}
			}
			
			
		}
/*		for(int i=0;i<d.width;i++){
			boolean xborder = (i>=minx.getValue() && i<= minx.getValue() + thick.getValue() || i<=maxx.getValue() && i >= maxx.getValue() - thick.getValue());
			for(int j=0;j<d.height;j++){
				boolean yborder = (j>=miny.getValue() && j<= miny.getValue() + thick.getValue() || j<=maxy.getValue() && j >= maxy.getValue() - thick.getValue());
				if(yborder || xborder){
					frame[i][j][0] = (int)(intensity * myColor.getRed());
					frame[i][j][1] = (int)(intensity * myColor.getGreen());
					frame[i][j][2] = (int)(intensity * myColor.getBlue());
				}
			}
		}*/
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
		for(int i=0;i<d.width;i++){
			boolean xborder = (i>=minx.getValue() && i<= minx.getValue() + thick.getValue() || i<=maxx.getValue() && i >= maxx.getValue() - thick.getValue());
			for(int j=0;j<d.height;j++){
				boolean yborder = (j>=miny.getValue() && j<= miny.getValue() + thick.getValue() || j<=maxy.getValue() && j >= maxy.getValue() - thick.getValue());
				frame[i][j] = (yborder || xborder);
			}
		}
		return frame;
	}
	
	class Sploder{
		int x;
		int y;
		Color myColor;
		long origin;
		long lasttic;
		double[][] pellets;
		double[][] velocities;
		double gravity = 5;
		double damping = 0.01;
		
		public Sploder(int x,int y, Color myColor, long startTime){
			this.x = x;
			this.y = y;
			this.myColor = myColor;
			this.origin = startTime;
			int numPellets = (int)(Math.random()*10 + 10);
			
			//initialize the pellets
			pellets = new double[numPellets*2][2];
			velocities = new double[numPellets*2][2];
			for(int i=0;i<numPellets;i++){
				pellets[i][0] = (double) x;
				pellets[i][1] = (double) y;
				pellets[i+numPellets][0] = (double) x;
				pellets[i+numPellets][1] = (double) y;
				double mag = Math.random()/2 + 8;
				double dir = 2 * Math.PI * i / numPellets; //Math.random() * 2 * Math.PI;
				velocities[i][0] = mag * Math.cos(dir);
				velocities[i][1] = mag * Math.sin(dir);
				velocities[i+numPellets][0] = mag * Math.cos(dir)/2;
				velocities[i+numPellets][1] = mag * Math.sin(dir)/2;
			}
			lasttic = startTime;
		}
		
		public double[][] tic(long time){
			//newton's method physics update
			for(int i =0;i<pellets.length;i++){
				pellets[i][0] += velocities[i][0] * (time-lasttic)/1000.;
				pellets[i][1] += velocities[i][1] * (time-lasttic)/1000.;
				velocities[i][1] += (-damping * velocities[i][1] + gravity) * (time-lasttic)/1000.;
				velocities[i][0] -= damping * velocities[i][0] * (time-lasttic)/1000.;
			}
			lasttic = time;
			return pellets;
		}
	}
}