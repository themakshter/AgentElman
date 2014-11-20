package agent;

import java.util.Arrays;

import se.sics.tac.aw.TACAgent;

public class CalculateUtilities implements Runnable {
	
	 int inFlight, outFlight, hotelBonus, utility;
     int[] entertainmentBonuses;
	
	public CalculateUtilities(int inFlight, int outFlight, int hotelBonus, int[] entertainmentBonuses) {
		this.inFlight = inFlight;
		this.outFlight = outFlight;
		this.hotelBonus = hotelBonus;
		this.entertainmentBonuses = entertainmentBonuses;
	}

	@Override
	public void run() {
		int stayDuration = outFlight - inFlight;
	     int travelPenalty = 0;
	     int funBonus = 0;
	     
	     if (stayDuration >=3 ) {
		     funBonus = entertainmentBonuses[0] + entertainmentBonuses[1] + entertainmentBonuses[2];
	     } else {
	    	 
	    	 Arrays.sort(entertainmentBonuses);
	    	 
	    	 for(int j = 0;j<3;j++) {
	    		 if (j < stayDuration) {
	    			 funBonus = funBonus + entertainmentBonuses[2-j];
	    		 } else if(entertainmentBonuses[2-j] > 100) {
	    			 funBonus = funBonus + entertainmentBonuses[2-j];
	    			 travelPenalty = travelPenalty + 100;
	    		 }
	    	 }
	     }

	     utility = 1000 + travelPenalty + hotelBonus + funBonus;
	     System.out.println("Travel Penalty : "+ travelPenalty+ "\nHotel Bonus : " + hotelBonus + "\nFun Bonus : " + funBonus + "\nMax util : " + utility);
	     System.out.println();
	     Thread.currentThread().interrupt();
	}

}
