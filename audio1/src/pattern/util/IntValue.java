package pattern.util;

public class IntValue {
	int myInt;
	
	public IntValue(int init){
		myInt = init;
	}
	
	public int getValue(){
		return myInt;
	}
	
	public void setValue(int newint){
		myInt = newint;
	}
	
	public String toString(){
		return myInt +"";
	}
}
