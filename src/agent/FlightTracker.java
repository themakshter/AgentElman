package agent;

public class FlightTracker extends Tracker {
	private int[] inFlight  = {0,0,0,0,0};
	private int[] outFlight  = {0,0,0,0,0};
	@Override
	public void add(int type, int day) {
		if(type == 1){
			inFlight[day-1]++;
		}else if(type == 2){
			outFlight[day-1]++;
		}
	}
	@Override
	public void addAmount(int type, int day, int amount) {
		for(int i = 0; i < amount;i++){
			add(type,day);
		}
	}

	@Override
	public void addDuration(int type, int inDate, int outDate) {
		for(int i=inDate;i< outDate;i++){
			add(type,i);
		}
	}
	
	@Override
	public int subtract(int type, int day, int amount) {
		if(type == 1){
			return subtract(inFlight, day, amount);
		}else if(type == 2){
			return subtract(outFlight, day, amount);
		}
		return 0;		
	}
	
	@Override
	public int getAmount(int type, int day) {
		if(type == 1){
			return inFlight[day-1];
		}else if(type == 2){
			return outFlight[day-1];
		}
		return 0;
	}
	
	
	
}
