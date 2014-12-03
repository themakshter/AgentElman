package agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import se.sics.tac.aw.TACAgent;

public class Client {

	private int index, inFlight, outFlight, hotel, alligator, amusement,
	museum, maxUtility, risk, calcualtedUtility;
	
	private ClientPackage clientPackage;

	public Client(TACAgent agent,int index) {
		setIndex(index);
		setInFlight(agent.getClientPreference(index, TACAgent.ARRIVAL));
		setOutFlight(agent.getClientPreference(index, TACAgent.DEPARTURE));
		setHotel(agent.getClientPreference(index, TACAgent.HOTEL_VALUE));
		setAlligator(agent.getClientPreference(index, TACAgent.E1));
		setAmusement(agent.getClientPreference(index, TACAgent.E2));
		setMuseum(agent.getClientPreference(index, TACAgent.E3));
		clientPackage = new ClientPackage(this);
		calculateMaxUtility();
		calculateRisk();
	}

	public void calculateMaxUtility() {
		int travelPenalty = 0;
		int stayDuration = outFlight - inFlight;
		int funBonus = 0;
		int[] entertainmentBonuses = { alligator, amusement, museum };
		if (stayDuration >= 3) {
			funBonus = alligator + amusement + museum;
		} else {
			Arrays.sort(entertainmentBonuses);
			for (int j = 0; j < 3; j++) {
				if (j < stayDuration) {
					funBonus = funBonus + entertainmentBonuses[2 - j];
				} else if (entertainmentBonuses[2 - j] > 100) {
					funBonus = funBonus + entertainmentBonuses[2 - j];
					travelPenalty = travelPenalty + 100;
				}
			}
		}

		maxUtility = 1000 - travelPenalty + hotel + funBonus;

		System.out.println("Stay duration : " + inFlight + " to " + outFlight
				+ "\nHotel Bonus : " + hotel + "\nFun Bonus : " + funBonus
				+ "\nMax util : " + maxUtility);

	}

	public void calculateRisk() {
		int stayDuration = outFlight - inFlight;
		risk = 0;

		for (int j = inFlight; j < outFlight; j++) {
			switch (j) {
			case 1:
				risk += 1;
				break;
			case 2:
				risk += 2;
				break;
			case 3:
				risk += 2;
				break;
			case 4:
				risk += 1;
				break;
			default:
				break;
			}
		}
		System.out.println("Risk value : " + risk);
	}
	
	public boolean validDay(int day) {
		
		if(day >= inFlight && day < outFlight) {
			return true;
		} else {
			return false;
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getHotel() {
		return hotel;
	}

	public void setHotel(int hotel) {
		this.hotel = hotel;
	}

	public int getAlligator() {
		return alligator;
	}

	public void setAlligator(int alligator) {
		this.alligator = alligator;
	}

	public int getAmusement() {
		return amusement;
	}

	public void setAmusement(int amusement) {
		this.amusement = amusement;
	}

	public int getMuseum() {
		return museum;
	}

	public void setMuseum(int museum) {
		this.museum = museum;
	}

	public int getInFlight() {
		return inFlight;
	}

	public void setInFlight(int inFlight) {
		this.inFlight = inFlight;
	}

	public int getOutFlight() {
		return outFlight;
	}

	public void setOutFlight(int outFlight) {
		this.outFlight = outFlight;
	}

	public int getMaxUtility() {
		return maxUtility;
	}

	public void setMaxUtility(int maxUtility) {
		this.maxUtility = maxUtility;
	}

	public int getRisk() {
		return risk;
	}

	public void setRisk(int risk) {
		this.risk = risk;
	}

	public int getCalcualtedUtility() {
		return calcualtedUtility;
	}

	public void setCalcualtedUtility(int calcualtedUtility) {
		this.calcualtedUtility = calcualtedUtility;
	}

	public int getMaximumEntertainment(){
		int max = 0;
		int[] entertainmentBonuses = { alligator, amusement, museum };
		for(int i = 1; i < entertainmentBonuses.length;i++){
			if(entertainmentBonuses[i] > entertainmentBonuses[i-1]){
				max = i;
			}
		}
		return max+1;
	}

	public ClientPackage getClientPackage() {
		return clientPackage;
	}

	public void setClientPackage(ClientPackage clientPackage) {
		this.clientPackage = clientPackage;
	}
}

class ClientComparator implements Comparator<Client> {
	@Override
	public int compare(Client c1, Client c2) {
		return c1.getMaxUtility() - c2.getMaxUtility();
	}

	public int compareHotel(Client c1, Client c2) {
		return c1.getHotel() - c2.getHotel();
	}

	public int compareAlligator(Client c1, Client c2) {
		return c1.getAlligator() - c2.getAlligator();
	}

	public int compareAmusement(Client c1, Client c2) {
		return c1.getAmusement() - c2.getAmusement();

	}

	public int compareMuseum(Client c1, Client c2) {
		return c1.getMuseum() - c2.getMuseum();
	}

	//Sorts descending order.
	/*Types:
	 * 1 - Max Utility
	 * 2 - Alligator Wrestling Utility
	 * 3 - Amusement Utility
	 * 4 - Museum Utility
	 */
	public ArrayList<Client> sort(ArrayList<Client> unsortedList,int type) {
		ArrayList<Client> sortedList = new ArrayList<Client>();

		for(Client c1: unsortedList) {

			if(sortedList.isEmpty()) {

				sortedList.add(c1);

			} else {

				int i = 0;
				int compareResult = 0;
				switch(type) {
				case 1 : 	
					compareResult = compare(c1,sortedList.get(i));
					break;
				case 2 : 	
					compareResult = compareAlligator(c1,sortedList.get(i));
					break;
				case 3 : 	
					compareResult = compareAmusement(c1,sortedList.get(i));
					break;
				case 4 : 	
					compareResult = compareMuseum(c1,sortedList.get(i));
					break;
				}			

				while (compareResult < 0) {
					i++;			
					if (i == sortedList.size()) {

						break;
					}

					switch(type) {
					case 1 : 	
						compareResult = compare(c1,sortedList.get(i));
						break;
					case 2 : 	
						compareResult = compareAlligator(c1,sortedList.get(i));
						break;
					case 3 : 	
						compareResult = compareAmusement(c1,sortedList.get(i));
						break;
					case 4 : 	
						compareResult = compareMuseum(c1,sortedList.get(i));
						break;
					}	
				}

				if (i == sortedList.size()) {
					sortedList.add(c1);
				} else {
					sortedList.add(i,c1);
				}

			}
		}

		return sortedList;
	}
}
