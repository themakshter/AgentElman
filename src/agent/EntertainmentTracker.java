package agent;

public class EntertainmentTracker extends Tracker {
	private int[] alligator = { 0, 0, 0, 0 };
	private int[] amusement = { 0, 0, 0, 0 };
	private int[] museum = { 0, 0, 0, 0 };

	public int subtract(int type, int day, int amount) {
		switch (type) {
		case 1:
			return subtract(alligator, day, amount);
		case 2:
			return subtract(amusement, day, amount);
		case 3:
			return subtract(museum, day, amount);
		}
		return 0;
	}
	
	
	public int getTicket(int type, int day) {
		switch (type) {
		case 1:
			return alligator[day - 1];
		case 2:
			return amusement[day - 1];
		case 3:
			return museum[day - 1];
		}
		return 0;
	}

	@Override
	public void add(int type, int day) {
		switch (type) {
		case 1:
			alligator[day - 1]++;
		case 2:
			amusement[day - 1]++;
		case 3:
			museum[day - 1]++;
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
	public int getAmount(int type, int day) {
		switch (type) {
		case 1:
			return alligator[day - 1];
		case 2:
			return amusement[day - 1];
		case 3:
			return museum[day - 1];
		}
		return 0;
	}


	public int[] getAlligator() {
		return alligator;
	}


	public void setAlligator(int[] alligator) {
		this.alligator = alligator;
	}


	public int[] getAmusement() {
		return amusement;
	}


	public void setAmusement(int[] amusement) {
		this.amusement = amusement;
	}


	public int[] getMuseum() {
		return museum;
	}


	public void setMuseum(int[] museum) {
		this.museum = museum;
	}

}
