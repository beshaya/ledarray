import javax.sound.sampled.*;

public class Listener {

	/*
		public Listener(){
			TargetDataLine line;
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object
			if (!AudioSystem.isLineSupported(info)) {
			    // Handle the error ... 

			}
			// Obtain and open the line.
			try {
			    line = (TargetDataLine) AudioSystem.getLine(info);
			    line.open(format);
			} catch (LineUnavailableException ex) {
			    // Handle the error ... 
			}
		}
		*/
		public static void main(String[] args){
			Mixer.Info[] info = AudioSystem.getMixerInfo();
			for(int i=0;i<info.length;i++){
				System.out.println(info[i]);
			}
		}
}
