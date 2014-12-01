package agent;

public class HotelTracker extends Tracker {
	private int[] towers = {0,0,0,0};
	private int[] shanty  = {0,0,0,0};
	
	@Override
	public void add(int type, int day) {
		if(type ==1){
			towers[day-1]++;
		}else if(type == 2){
			shanty[day-1]++;
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
	public void subtract(int type, int day) {
		
	}

	@Override
	public void subtractAmount(int type, int day, int amount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subtractDuration(int type, int inDate, int outDate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getAmount(int type, int day) {
		if(type == 1){
			return towers[day-1];
		}else if(type == 2){
			return shanty[day-1];
		}
		return 0;
	}
}
