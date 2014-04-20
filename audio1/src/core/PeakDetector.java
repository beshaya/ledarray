package core;
import pattern.util.IntValue;
import pattern.util.TextSlider;

public class PeakDetector {
	
	final int SEARCHING = 0;
	final int RISING = 1;
	int min;
	int max;
	double rest;
	double[] data;
	double peak;
	long peaktime;
	long lastpeak;
	double lastpeakval;
	int state;
	double power;
	IntValue decay = new IntValue(900);
	
	/**
	 * Peak detector that fires when  
	 * @param fftdata 
	 * @param low lowest fft point to use
	 * @param high highest fft point to use
	 */
	public PeakDetector(double[] fftdata, int low, int high){
		this.data = fftdata;
		this.min = low;
		this.max = high;
		peak = 0.;
		rest = 0;
		state = SEARCHING;
		decayrate.setup(decay, 700, 1000, TextSlider.HORIZONTAL);
	}
	
	private double peakdecay = .9;
	private final TextSlider decayrate = new TextSlider();
	
	public long process(){
		//Start by combining power in bounds
		power = 0.;
		for(int i=min;i<max;i++){
			power += data[i];
		}
		//if(rest < 10)
		//	rest += 10;
		if(state == SEARCHING){
			if(power > peak && System.currentTimeMillis() > peaktime + 400){
				peak = power;
				state = RISING;
				lastpeak = peaktime;
				peaktime = System.currentTimeMillis();
				updatepll(true);
				//System.out.println("peak"+ " " + power);
			}else{
				rest = (rest * 49 + power) / 50;
				//System.out.println(peak +" " +  power);
				peak = peak * decay.getValue()/1000.;//(peak-rest) * .95 + rest;
				if(peak < (1.1*rest))
					peak = 1.1*rest;
				if(peak < 5){
					peak = 5;
				}
				updatepll(false);
			}
		}else if(state == RISING){
			updatepll(false);
			if(power > peak){
				peak = power;
			}else if (power < peak){
				lastpeakval = peak;
				state = SEARCHING;
				//System.out.println("peak!" + peak);
				peak = peak * 1.2;
			}
	
		}
		return peaktime;
	}
	
	public long getpeak(){
		return peaktime;
	}
	
	double pllvalue = 0;
	double plldisplay = 0;
	double rate = .015;//initialize for 1 beat per minute
		//1/s * .015s/frame 
	double gain = .003;
	
	private double updatepll(boolean ispeak){
		if(ispeak){
			double error = 0;
			if(pllvalue < 1){
				error = 1-pllvalue; //speed up
				//pllvalue = pllvalue - (int)pllvalue;
			}else {
				double tempval = pllvalue - (int)pllvalue;
				if(tempval < .5){
					error = -tempval; //slow down
					plldisplay = (int)plldisplay;
				}else{
					error = 1-tempval; //speed up
					plldisplay = (int)plldisplay + 1;
				}
				pllvalue = 0;// pllvalue - (int)pllvalue;
				
			}
			if (error < -1)
				error = -1;
			//if(pllvalue > .5)
			//	pllvalue = 0;
			rate = Math.max(rate + error * gain,0);
			rate = Math.min(rate,0.1);
			//System.out.println(rate);
		}
		pllvalue += rate;
		plldisplay += rate;
		//pllvalue = pllvalue%2;
		//if(pllvalue >= 1){
		//	pllvalue = 0;
		//}
		return pllvalue;
	}
	
	public double getpll(){
		return plldisplay;
	}
	
	public double getPower(){
		if(power < 5) return 0;
		return power/rest;
	}
	
	public TextSlider getControl(){
		return decayrate;
	}
}
