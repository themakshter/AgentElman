package agent;

public class EntertainmentTracker {
	private int[] alligator = {0,0,0,0};
	private int[] amusement = {0,0,0,0};
	private int[] museum = {0,0,0,0};
	
	
	public void addEntertainment(int type,int day){
		switch(type){
		case 1:
			alligator[day-1]++;
		case 2:
			amusement[day-1]++;
		case 3:
			museum[day-1]++;
		}
	}
	
	public void addEntertainmentNumber(int type,int day, int number){
		for(int i = 0; i < number;i++){
			addEntertainment(type,day);
		}
	}
	
	public void addEntertainmentDuration(int type, int inDate, int outDate){
		for(int i=inDate;i< outDate;i++){
			addEntertainment(type,i);
		}
	}
	
	public void subtract(int type,int day,int amount){
	int[] required;
	switch(type){
	case 1:
		required = alligator;
		break;
	case 2:
		required = amusement;
		break;
	case 3:
		required = museum;
		break;
	}
	
	
	
	}
	
	public void subtractEntertainment(int type,int day){
		switch(type){
		case 1:
			alligator[day-1]--;
		case 2:
			amusement[day-1]--;
		case 3:
			museum[day-1]--;
		}
	}
	
	public void subtractEntertainmentAmount(int type,int day,int number){
		for(int i = 0; i < number;i++){
			subtractEntertainment(type,day);
		}
	}
	
	public void subtractEntertainmentDuration(int type, int inDate, int outDate){
		for(int i=inDate;i< outDate;i++){
			subtractEntertainment(type, i);
		}
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
