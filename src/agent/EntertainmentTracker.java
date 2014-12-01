package agent;

public class EntertainmentTracker {
	private int[] alligator = {0,0,0,0};
	private int[] amusement = {0,0,0,0};
	private int[] museum = {0,0,0,0};
	
	public void incrementAlligator(int day){
		alligator[day]++;
	}
	
	public void addAlligator(int day,int number){
		alligator[day]+=number;
	}
	
	public void incrementAmusement(int day){
		amusement[day]++;
	}
	
	public void addAmusement(int day,int number){
		amusement[day]+= number;
	}
	
	public void incrementMuseum(int day){
		museum[day]++;
	}
	
	public void addMuseum(int day,int number){
		museum[day]+=number;
	}
	
	public int getTicket(int type,int day){
		switch(type){
		case 1:
			return alligator[day];
		case 2:
			return amusement[day];
		case 3:
			return museum[day];
		}
		return 0;
	}
	
	
}
