package agent;

public class EntertainmentTracker {
	private int[] alligator = {0,0,0,0};
	private int[] amusement = {0,0,0,0};
	private int[] museum = {0,0,0,0};
	
	public void incrementAlligator(int day){
		alligator[day-1]++;
	}
	
	public void addAlligator(int day,int number){
		alligator[day-1]+=number;
	}
	
	public void incrementAmusement(int day){
		amusement[day-1]++;
	}
	
	public void addAmusement(int day,int number){
		amusement[day-1]+= number;
	}
	
	public void incrementMuseum(int day){
		museum[day-1]++;
	}
	
	public void addMuseum(int day,int number){
		museum[day-1]+=number;
	}
	
	public int getTicket(int type,int day){
		switch(type){
		case 1:
			return alligator[day-1];
		case 2:
			return amusement[day-1];
		case 3:
			return museum[day-1];
		}
		return 0;
	}
	
	
}
