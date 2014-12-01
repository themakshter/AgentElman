package agent;

public class EntertainmentTracker extends Tracker {
	private int[] alligator = { 0, 0, 0, 0 };
	private int[] amusement = { 0, 0, 0, 0 };
	private int[] museum = { 0, 0, 0, 0 };

	public void subtract(int type, int day, int amount) {
		int[] required;
		switch (type) {
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

	public void subtractEntertainment(int type, int day) {

	}

	public void subtractEntertainmentAmount(int type, int day, int number) {
		for (int i = 0; i < number; i++) {
			subtractEntertainment(type, day);
		}
	}

	public void subtractEntertainmentDuration(int type, int inDate, int outDate) {
		for (int i = inDate; i < outDate; i++) {
			subtractEntertainment(type, i);
		}
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
	public void subtract(int day, int type) {
		switch (type) {
		case 1:
			alligator[day - 1]--;
		case 2:
			amusement[day - 1]--;
		case 3:
			museum[day - 1]--;
		}
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
	public void addAmount(int type, int day, int amount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDuration(int type, int inDate, int outDate) {
		// TODO Auto-generated method stub

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

}
