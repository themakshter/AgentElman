package agent;

public class HotelTracker {
	private int[] towers = {0,0,0,0};
	private int[] shanty  = {0,0,0,0};
	
	public void incrementTowers(int day){
		towers[day-1]++;
	}
	
	public void incrementTowers(int inDate,int outDate){
		for(int i = inDate; i < outDate;i++){
			towers[i-1]++;
		}
	}
	
	public void addTowers(int day,int number){
		towers[day-1]+=number;
	}
	
	public void incrementShanty(int day){
		shanty[day-1]++;
	}
	
	public void incrementShanty(int inDate,int outDate){
		for(int i = inDate; i < outDate;i++){
			shanty[i-1]++;
		}
	}
	
	public void addShanty(int day,int number){
		shanty[day-1]+=number;
	}
	
	public int getTowerNumber(int day){
		return towers[day-1];
	}
	
	public int getShantyNumber(int day){
		return shanty[day-1];
	}
}
