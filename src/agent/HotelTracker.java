package agent;

public class HotelTracker {
	private int[] towers = {0,0,0,0};
	private int[] shanty  = {0,0,0,0};
	
	public void incrementTowers(int day){
		towers[day]++;
	}
	
	public void addTowers(int day,int number){
		towers[day]+=number;
	}
	
	public void incrementShanty(int day){
		shanty[day]++;
	}
	
	public void addShanty(int day,int number){
		shanty[day]+=number;
	}
	
	public int getTowerNumber(int day){
		return towers[day];
	}
	
	public int getShantyNumber(int day){
		return shanty[day];
	}
}
