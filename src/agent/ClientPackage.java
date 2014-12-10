package agent;

public class ClientPackage {

	private Client owner;
	private Integer inFlight,outFlight;
	private boolean[] hotelDays = new boolean[4];
	private boolean tampaTowers;
	private int[] entertainments = new int[4]; //0 - none; 1 - Alligator wrestling; 2 - Amusement; 3 - Museum
	private float packageCost;

	public ClientPackage (Client c) {
		owner = c;
		setPackageCost(0);
	}

	public void addToPackageCost(int price) {
		packageCost += price;
	}
	
	public void addToPackageCost(float price) {
		packageCost += price;
	}
	
	public boolean isFeasible() {

		if(inFlight != null && outFlight != null) {
			for(int i = inFlight;i<outFlight-2;i++) {
				System.out.println("day " + (i) + " : "+hotelDays[i-1]);
				if(!hotelDays[i-1]) {
					return false;
				}
			}
			return true;
		}else{ 
			return false;
		}
	}
	
	public boolean canCompletePackage(boolean[] closedGoodAuctions,boolean[] closedCheapAuctions) {
		
		if(inFlight != null && outFlight != null) {
			for(int i = inFlight;i<outFlight-2;i++) {
				if(!hotelDays[i-1]) {
					if(tampaTowers) {
						if(closedGoodAuctions[i-1]) {
							return false;
						}
					} else {
						if(closedCheapAuctions[i-1]) {
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}
	
	//Returns 0 if no hotels in package for at least in flight day.
	public int calculateLastPossibleOutFlightForCurrentHotels() {
		
		int index = inFlight-1;
		boolean hasHotel = hotelDays[index];
		
		while(hasHotel) {
			index++;
			hasHotel = hotelDays[index];
		}
		
		if (index + 1 == inFlight) {
			return 0;
		}
		else {
			return index + 1; 
		}
	}
	
	//Returns 0 if no hotels in package for at least in flight day.
	public int calculateLastPossibleInFlightForCurrentHotels() {
		
		int index = outFlight-2;
		boolean hasHotel = hotelDays[index];
		
		while(hasHotel) {
			index--;
			hasHotel = hotelDays[index];
		}
		 
		if (index + 1 == outFlight) {
			return 0;
		}
		else {
			return index + 1; 
		}
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
	
	public void addHotel (int day,int type) {
		switch(type) {
		case 0 :
			tampaTowers = false;
			hotelDays[day-1] = true;
			break;
		case 1 :
			tampaTowers = true;
			hotelDays[day-1] = true;
			break;
		}
	}
	
	public void addFlight(int day, int type) {
		switch(type) {
		case 0 :
			inFlight = day;

		case 1 :
			outFlight = day;
		}
		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Client: " + owner.getIndex() + "\n");
		sb.append("Inflight: " + inFlight + "\n");
		sb.append("Outflight: " + outFlight + "\n");
		
		for(int i = 0; i<hotelDays.length;i++) {
			sb.append(hotelDays[i] + " ");	
		}
		sb.append("\n");
		sb.append("Tampa Towers: "+tampaTowers + "\n");
		
		for(int i = 0; i<entertainments.length;i++) {
			sb.append(entertainments[i] + " ");
		}
		
		return sb.toString();
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
	
	public int getEntertainmentsAt(int day) {
		return entertainments[day-1];
	}
	
	public void setEntertainmentsAt(int day,int value) {
		entertainments[day-1] = value;
	}

	public float getPackageCost() {
		return packageCost;
	}

	public void setPackageCost(float packageCost) {
		this.packageCost = packageCost;
	}





}
