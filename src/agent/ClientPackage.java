package agent;

public class ClientPackage {

	private Client owner;
	private Integer inFlight,outFlight;
	private boolean[] hotelDays = new boolean[4];
	private boolean tampaTowers;
	private int[] entertainments = new int[4]; //0 - none; 1 - Alligator wrestling; 2 - Amusement; 3 - Museum

	public ClientPackage (Client c) {
		owner = c;
	}

	public boolean isFeasible() {

		if(inFlight != null && outFlight != null) {

			for(int i = inFlight;i<outFlight;i++) {
				if(!hotelDays[i-1]) {
					return false;
				}
			}
			return true;
		} 

		return false;
	}

	public int calculateUtility() {
		if(isFeasible()) {
			int utility;

			int travelPenalty = 100*(Math.abs(inFlight - owner.getInFlight()) + Math.abs(outFlight - owner.getOutFlight()));

			int hotelBonus;
			if (tampaTowers) {
				hotelBonus = owner.getHotel(); 
			} else {
				hotelBonus = 0;
			}

			int funBonus = 0;
			for (int i = 0;i<entertainments.length;i++) {
				int entertainmentIndex = entertainments[i];

				switch(entertainmentIndex) {
				case 0 :
					break;
				case 1 :
					funBonus += owner.getAlligator();
				case 2 :
					funBonus += owner.getAmusement();
				case 3 :
					funBonus += owner.getMuseum();
				default : 
					break;
				}

			}


			utility = 1000 - travelPenalty + hotelBonus + funBonus;

			return utility;
		} else {
			return 0;
		}
	}

	public boolean validDay(int day) {

		if(inFlight != null && outFlight != null) {
			if(day >= inFlight && day < outFlight) {
				return true;
			} else {
				return false;
			}
		} 
		return false;
		
	}


	//GETTERS AND SETTERS
	public Client getOwner() {
		return owner;
	}

	public void setOwner(Client owner) {
		this.owner = owner;
	}

	public Integer getInFlight() {
		return inFlight;
	}

	public void setInFlight(Integer inFlight) {
		this.inFlight = inFlight;
	}

	public Integer getOutFlight() {
		return outFlight;
	}

	public void setOutFlight(Integer outFlight) {
		this.outFlight = outFlight;
	}

	public boolean[] getHotelDays() {
		return hotelDays;
	}

	public void setHotelDays(boolean[] hotelDays) {
		this.hotelDays = hotelDays;
	}

	public boolean isTampaTowers() {
		return tampaTowers;
	}

	public void setTampaTowers(boolean tampaTowers) {
		this.tampaTowers = tampaTowers;
	}

	public int[] getEntertainments() {
		return entertainments;
	}

	public void setEntertainments(int[] entertainments) {
		this.entertainments = entertainments;
	}
	
	public void setEntertainmentsAt(int day,int value) {
		entertainments[day-1] = value;
	}



}
