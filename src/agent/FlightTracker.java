package agent;

public class FlightTracker {
	private int[] inFlight  = {0,0,0,0,0};
	private int[] outFlight  = {0,0,0,0,0};
	
	public void incrementInFlight(int day){
		inFlight[day-1]++;
	}
	
	public void incrementOutFlight(int day){
		outFlight[day-1]++;
	}
	
	public void addInFlight(int day,int number){
		inFlight[day-1]+=number;
	}
	
	public void addOutFlight(int day,int number){
		outFlight[day-1]+=number;
	}
	
	public int getInflightNumber(int day){
		return inFlight[day-1];
	}
	
	public int getOutFlightNumber(int day){
		return outFlight[day-1];
	}
	
}
