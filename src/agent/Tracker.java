package agent;

public abstract class Tracker {

	public abstract void add(int type, int day);
	public abstract void addAmount(int type, int day, int amount);
	public abstract void addDuration(int type, int inDate, int outDate);
	public abstract int subtract(int type, int day, int amount);
	public abstract int getAmount(int type,int day);
	
	public int subtract(int[] required, int day, int amount){
		if(required[day] > amount){
			required[day] -= amount;
			return amount;
		}else{
			int subtracted = required[day];
			required[day] = 0;
			return subtracted;		
		}
	}

	
}
