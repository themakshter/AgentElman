package agent;

public class FlightTracker {
	private int[] inFlight  = {0,0,0,0};
	private int[] outFlight  = {0,0,0,0};
	
	public void incrementInFlight(int day){
		inFlight[day]++;
	}
	
	public void incrementOutFlight(int day){
		outFlight[day]++;
	}
	
	public void addInFlight(int day,int number){
		inFlight[day]+=number;
	}
	
	public void addOutFlight(int day,int number){
		outFlight[day]+=number;
	}
	
	public int getInflightNumber(int day){
		return inFlight[day];
	}
	
	public int getOutFlightNumber(int day){
		return outFlight[day];
	}
	
}
