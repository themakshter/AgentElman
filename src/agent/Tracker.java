package agent;

public abstract class Tracker {

	public abstract void add(int type, int day);
	public abstract void addAmount(int type, int day, int amount);
	public abstract void addDuration(int type, int inDate, int outDate);
	public abstract void subtract(int type, int day);
	public abstract void subtractAmount(int type, int day, int amount);
	public abstract void subtractDuration(int type, int inDate, int outDate);
	public abstract int getAmount(int type,int day);
	
}
