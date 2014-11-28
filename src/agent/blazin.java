/*
 *                                        TAC Agent BLAZIN
 *                              Authors  : Amit Kothari, Brian Ferguson
 *                                          ma0ak,cs2bcf
 *
 *
 * ---------------------------------------------------------------------------------------
 * Buying flights
 * ---------------------------------------------------------------------------------------
 * void buy_flights()
 *  - submits a single bid to each of the 8 flight auctions where:
 *	  
 *			bid contains: - quantity = flights needed for that auction. SEE: get_need(int,int,int) 
 *					      - price = auction.getAskprice().
 *
 *
 ---------------------------------------------------------------------------------------
 * Buying hotels
 * -------------------------------------------------------------------------------------
 * void buy_hotels()
 *  - submits a bid for every hotel_qoute object of each of the 8 hotel auctions. 
 *	  needed for that auction where:
 *
 *			bid contains: - quantity = hotels needed for that auction. SEE: get_need(int,int,int) 
 *			              - price = auction.getAskprice().
 *
 *
 * -------------------------------------------------------------------------------------
 * Buying and selling entertainment
 * -------------------------------------------------------------------------------------
 * void allocate_entertainment()
 *  - submits a single bid to each of the 12 entertainment auctions for amount to 
      buy or sell. If entertainment needed for that auction SEE: get_need(int,int,int) is more 
	  than enetrtainment owned for that auction i.e. We need more than we own:
 *
 *		    bid contains: - quantity = need - own. 
 *					      - price = buying price. SEE: ENTERTAINMENT_BUY_PRICE. 
 *	  
 *	  If we own more than we need: 		
 8
 *		    bid contains: - quantity = need - own. 
 *					      - price = selling price. SEE: ENTERTAINMENT_SELL_PRICE.
 *	  
 *	  Where quantity in the second bid is negative and thus sells.
 *
 *
 * ------------------------------------------------------------------------------------
 * Populating the details array
 * ------------------------------------------------------------------------------------
 * void fill_details_array()
 *  - loops through all flight, hotel and entertainment auctions seperately and creates a details object for
 *    each auction consisting of the ID, catagory, type, day, total needed, and total owned for that auction. 
 *    It also sets a boolean bidding_for attribute to each object, initially set to false.
 *	 
 *
 * int get_need(CATAGORY, DAY, TYPE) {
 *	- loops through all of the 8 clients and, for each client, increments the total need by one if they need 
 *	  the item being sold in the auction described by the three arguments.
 *
 *
 * boolean need_hotel(CURRENT_CLIENT, DAY, TYPE) {
 *	- returns true if the client described by the argument needs a hotel of type described by the argument on
 *	  the day described by the argument.
 *
 *
 * boolean need_entertainment(CURRENT_CLIENT, DAY, TYPE) {
 *	- returns true if the client described by the argument needs an entertainment of type described by the 
 *    argument on the day described by the argument.
 *
 *
 * int is_staying_in(CURRENT_CLIENT) {
 *   - returns the hotel that the client described in the argument has been allocated to as a result of their
 *     hotel bonus. (e.g. HotelBonus < 100; return CHEAP_HOTEL)
 *
 *
 * boolean already_got_entertainment_for (CURRENT_CLIENT, DAY) { 
 *   - returns true if a log object residing in the Log Array exists with attributes matching those described
 *     in the argument.(i.e. This client already has submitted a need for entertainment on this day) 
 *
 *
 * boolean already_got_entertainment_for (CURRENT_CLIENT, TYPE) { 
 *   - returns true if a log object residing in the Log Array exists with attributes matching those described
 *     in the argument.(i.e. This client already has submitted a need for entertainment of this type) 
 *
 *
 */
package agent;

import se.sics.tac.aw.*;
import se.sics.tac.util.ArgEnumerator;
import java.util.logging.*;
import java.util.*;

public class blazin extends AgentImpl {
	private static final Logger log = Logger.getLogger("se.sics.tac.aw.blazin");
	int TOTAL_AUCTIONS = 28;
	int logger_array_length = 96;
	Details DetailsArray[] = new Details[TOTAL_AUCTIONS];
	log loggerarray[] = new log[logger_array_length];
	int DETAILSINDEX = 0;
	int TOTAL_CLIENTS = 8;
	int FLIGHT = 0;
	int HOTEL = 1;
	int ENTERTAINMENT = 2;
	int IN_FLIGHT = 1;
	int OUT_FLIGHT = 0;
	int GOOD_HOTEL = 1;
	int CHEAP_HOTEL = 0;
	int E1 = 1;
	int E2 = 2;
	int E3 = 3;
	int DAY_1 = 0;
	int DAY_2 = 1;
	int DAY_3 = 2;
	int DAY_4 = 3;
	int DAY_5 = 4;
	int LOG_COUNTER = 0;















	/************************************************************************************	
	DETAILS OBJECT
	-------------------------------------------------------------------------------------
	CONTAINS: A unique auction ID and what we need and own for that auction and an indication of if we are 
	currently bidding in that auction or not. 
	
	CREATED WHEN: Total we need, total we own, and if we are currently bidding, has been accumulated for a 
	given auction. 
	
	STORED IN: DetailsArray[]
	
	RATIONALE: Simplifies buying methods as needs info is readily available.
	***********************************************************************************/
	public static class Details {
		private int AUCTION_ID, CAT, TYPE, DAY, NEED, OWN; 
		private boolean BIDDING_FOR;

		public Details(int AUCTION_ID, int CAT, int TYPE, int DAY, int NEED, int OWN, boolean BIDDING_FOR) {
			this.AUCTION_ID = AUCTION_ID;
			this.CAT = CAT;
			this.TYPE = TYPE;
			this.DAY = DAY;
			this.NEED = NEED;
			this.OWN = OWN;
			this.BIDDING_FOR = BIDDING_FOR;
		}
		public int get_auction_ID() {return this.AUCTION_ID;}
		public int get_category() {return this.CAT;}
		public int get_type() {return this.TYPE;}
		public int get_day() {return this.DAY;}
		public int get_need() {return this.NEED;}
		public int get_own() {return this.OWN;}
		public boolean bidding_for() {return this.BIDDING_FOR;}
		public void set_need(int need) { this.NEED = need;}
		public void set_own(int set) {this.OWN = set;}
		public void set_bidding_for(boolean toset) { this.BIDDING_FOR = toset;}
	}	
    /************************************************************************************	
	LOG OBJECT
	-------------------------------------------------------------------------------------
	CONTAINS: A record of what entertainment a particular client has for a particular day.
	
	CREATED WHEN: A customer has submitted an individual need for a particular entertainment for a particular 
	day. 

	STORED IN: LogArray[]
	
	RATIONALE:We need to keep a record of the needs that our clients have submitted for particular days and
	particular types of entertainment, or they will either:
				- submit a need for more than one entertainment for the same day
				- submit a need for more than one entertainment of the same type 
	Both of these possiblities must be eliminated as no additional utility can be gained from either.
	*********************************************************************************/
	public static class log {
		
		private int CURRENT_CLIENT, TYPE, DAY; 
		
		public log (int CURRENT_CLIENT, int TYPE, int DAY) {
			this.CURRENT_CLIENT = CURRENT_CLIENT;
			this.TYPE = TYPE;
			this.DAY = DAY;
		}
		public int get_current_client() {return this.CURRENT_CLIENT;}
		public int get_type() {return this.TYPE;}
		public int get_day() {return this.DAY;}
		public void set_current_client(int client) {this.CURRENT_CLIENT = client;}
		public void set_type(int type) {this.TYPE = type;}
		public void set_day(int day) {this.DAY = day;}
	}	


    /*********************FLIGHT BUYING METHOD*************************************/
    public void buy_flights(Quote flight_quote){
		for (DETAILSINDEX = 0; DETAILSINDEX < 8; DETAILSINDEX++){
			if(DetailsArray[DETAILSINDEX].get_auction_ID() == flight_quote.getAuction()){
				if (!DetailsArray[DETAILSINDEX].bidding_for()){
					if(DetailsArray[DETAILSINDEX].get_own() != DetailsArray[DETAILSINDEX].get_need()){  
						
						Bid NewBid  = new Bid(DetailsArray[DETAILSINDEX].get_auction_ID());	
						NewBid.addBidPoint(DetailsArray[DETAILSINDEX].get_need(), flight_quote.getAskPrice());
						agent.submitBid(NewBid);
						DetailsArray[DETAILSINDEX].set_bidding_for(true);
					}
				}
			}
		}
	}

    /*****************HOTEL BUYING METHOD*****************************************/
	public void buy_hotels(Quote hotel_quote) {
		
		for (DETAILSINDEX = 8; DETAILSINDEX < 16; DETAILSINDEX++){
			if(DetailsArray[DETAILSINDEX].get_auction_ID() == hotel_quote.getAuction()){
				
					int current_own = agent.getOwn(hotel_quote.getAuction());
					int previous_own = DetailsArray[DETAILSINDEX].get_own();
					int previous_need = DetailsArray[DETAILSINDEX].get_need();

					if (current_own != previous_own) {
						int current_need = previous_need - current_own;
						DetailsArray[DETAILSINDEX].set_need(current_need);
						DetailsArray[DETAILSINDEX].set_own(current_own);
					}
					
					if (DetailsArray[DETAILSINDEX].get_need() != 0){ 
						if(DetailsArray[DETAILSINDEX].get_own() != DetailsArray[DETAILSINDEX].get_need()){
							if(!DetailsArray[DETAILSINDEX].bidding_for()){
								float boost = get_boost_price(DetailsArray[DETAILSINDEX].get_need());
								Bid NewBid  = new Bid(DetailsArray[DETAILSINDEX].get_auction_ID());
								NewBid.addBidPoint(DetailsArray[DETAILSINDEX].get_need(), hotel_quote.getAskPrice() + boost);
								agent.submitBid(NewBid);
								DetailsArray[DETAILSINDEX].set_bidding_for(true);
							}
							else{
								float boost = get_boost_price(DetailsArray[DETAILSINDEX].get_need());
								Bid ReplacingBid  = new Bid(hotel_quote.getAuction());
								ReplacingBid.addBidPoint(DetailsArray[DETAILSINDEX].get_need(), hotel_quote.getAskPrice() + boost);
								agent.submitBid(ReplacingBid);
							}
						}
					}
			}
		}
	}



/***************ENTERTAINMENT BUYING AND SELLING METHOD*******************************/
	public void allocate_entertainment(Quote entertainment_quote){ 
		
		for (DETAILSINDEX = 16; DETAILSINDEX < 28; DETAILSINDEX++){
			if(DetailsArray[DETAILSINDEX].get_auction_ID() == entertainment_quote.getAuction()){
				
				if (DetailsArray[DETAILSINDEX].get_need() > DetailsArray[DETAILSINDEX].get_own()) {
					int buy = DetailsArray[DETAILSINDEX].get_need() - DetailsArray[DETAILSINDEX].get_own();
					if(!DetailsArray[DETAILSINDEX].bidding_for()){
						Bid NewBid  = new Bid(DetailsArray[DETAILSINDEX].get_auction_ID());
						NewBid.addBidPoint(buy, 100.0f);
						agent.submitBid(NewBid);
						DetailsArray[DETAILSINDEX].set_bidding_for(true);
					}
				}
				else if (DetailsArray[DETAILSINDEX].get_need() < DetailsArray[DETAILSINDEX].get_own()) {
					int sell = DetailsArray[DETAILSINDEX].get_own() - DetailsArray[DETAILSINDEX].get_need();
					if(!DetailsArray[DETAILSINDEX].bidding_for()){
						Bid NewBid  = new Bid(DetailsArray[DETAILSINDEX].get_auction_ID());
						NewBid.addBidPoint(0 - sell, 60.0f);
						agent.submitBid(NewBid);
						DetailsArray[DETAILSINDEX].set_bidding_for(true);
					}
				}
			}
		}
	}














/************************POPULATE DETAILS ARRAY METHODS******************************/
	public void fill_logger_array() {
		for (int x=0; x<logger_array_length; x++ ){
			log dummylog = new log(10,10,10);
			loggerarray[x] = dummylog;
		}
		log.fine("AGENT BLAZIN SUCCESFULLY FILLED LOGARRAY WITH DUMMY ENTRIES");
	}
	
	public void fill_details_array() {
		
		int AUCTION_ID, CAT, TYPE, DAY, NEED, OWN;
		int arraypos = 0;
		boolean BIDDING_FOR = false;
		
		for (DAY = 1; DAY < 5; DAY++){
			AUCTION_ID = agent.getAuctionFor(0,1,DAY);
			NEED = get_need(0, 1, DAY);
			OWN = agent.getOwn(AUCTION_ID);
			Details CurrentDetails = new Details(AUCTION_ID, 0, 1, DAY, NEED, OWN, BIDDING_FOR);
			DetailsArray[arraypos++] = CurrentDetails;		
		}
		for (DAY = 2; DAY < 6; DAY++) {
			AUCTION_ID = agent.getAuctionFor(0,0,DAY);
			NEED = get_need(0,0,DAY);
			OWN = agent.getOwn(AUCTION_ID);
			Details CurrentDetails = new Details(AUCTION_ID, 0, 0, DAY, NEED, OWN, BIDDING_FOR);
			DetailsArray[arraypos++] = CurrentDetails;		
		}
		for(TYPE = 0; TYPE < 2; TYPE++){
			for (DAY = 1; DAY < 5; DAY++) {
				AUCTION_ID = agent.getAuctionFor(1,TYPE,DAY);
				NEED = get_need(1,TYPE,DAY);
				OWN = agent.getOwn(AUCTION_ID);
				Details CurrentDetails = new Details(AUCTION_ID, 1, TYPE, DAY, NEED, OWN, BIDDING_FOR);
				DetailsArray[arraypos++] = CurrentDetails;		
			}
		}
		for(TYPE = 1; TYPE < 4; TYPE++){
			for (DAY = 1; DAY < 5; DAY++){
				AUCTION_ID = agent.getAuctionFor(2,TYPE,DAY);
				NEED = get_need(2,TYPE,DAY);
				OWN = agent.getOwn(AUCTION_ID);
				Details CurrentDetails = new Details(AUCTION_ID, 2, TYPE, DAY, NEED, OWN, BIDDING_FOR);
				DetailsArray[arraypos++] = CurrentDetails;
			}
		}
		log.fine("AGENT BLAZIN SUCCESFULLY CREATED " + arraypos + " DETAILS OBJECTS IN DETAILS ARRAY");
	}		
									
	public int get_need(int CAT, int TYPE, int DAY) {
		
		int NEED = 0;
		int CURRENT_CLIENT;
		
		for (CURRENT_CLIENT = 0; CURRENT_CLIENT < 8; CURRENT_CLIENT++) {
			
			if (CAT == FLIGHT && TYPE == IN_FLIGHT){
				if (agent.getClientPreference(CURRENT_CLIENT , TACAgent.ARRIVAL) == DAY){
					NEED++;
			}}
			if (CAT == FLIGHT && TYPE == OUT_FLIGHT){
				if (agent.getClientPreference(CURRENT_CLIENT , TACAgent.DEPARTURE) == DAY){
					NEED++;
			}}
			if (CAT == HOTEL && TYPE == GOOD_HOTEL){
				if (need_hotel(CURRENT_CLIENT,DAY,GOOD_HOTEL) == true){
					NEED++;
			}}
			if (CAT == HOTEL && TYPE == CHEAP_HOTEL){
				if (need_hotel(CURRENT_CLIENT,DAY,CHEAP_HOTEL) == true){
					NEED++;
			}}
			if (CAT == ENTERTAINMENT && TYPE == E1){			
				if (need_entertainment(CURRENT_CLIENT,DAY,E1) == true){
					NEED++;
			}}
			if (CAT == ENTERTAINMENT && TYPE == E2){			
				if (need_entertainment(CURRENT_CLIENT,DAY,E2) == true){
					NEED++;
			}}
			if (CAT == ENTERTAINMENT && TYPE == E3){			
				if (need_entertainment(CURRENT_CLIENT,DAY,E3) == true){
					NEED++;
			}}
		}	
		return NEED;
	}



	public boolean need_entertainment(int CURRENT_CLIENT, int DAY, int TYPE){
	
		int ARRIVAL_DAY = agent.getClientPreference(CURRENT_CLIENT , TACAgent.ARRIVAL);
		int DEPARTURE_DAY = agent.getClientPreference(CURRENT_CLIENT , TACAgent.DEPARTURE);
		boolean need = false;				

		if (DAY >= ARRIVAL_DAY && DAY < DEPARTURE_DAY){
			if (already_got_entertainment_for(CURRENT_CLIENT,DAY) == false) {
				if (already_got_entertainment_of(CURRENT_CLIENT,TYPE) == false) {
						loggerarray[LOG_COUNTER].set_current_client(CURRENT_CLIENT);
						loggerarray[LOG_COUNTER].set_type(TYPE);
						loggerarray[LOG_COUNTER].set_day(DAY);
						LOG_COUNTER = LOG_COUNTER + 1;
						need = true;
				}
			}
		}
		return need;
	}
			
	public boolean need_hotel(int CURRENT_CLIENT, int DAY, int TYPE){
		
		int ARRIVAL_DAY = agent.getClientPreference(CURRENT_CLIENT , TACAgent.ARRIVAL);
		int DEPARTURE_DAY = agent.getClientPreference(CURRENT_CLIENT , TACAgent.DEPARTURE);
		boolean need = false;
					
		if (DAY >= ARRIVAL_DAY && DAY < DEPARTURE_DAY){
			if (staying_in(CURRENT_CLIENT) == TYPE){
				return true;
			}
		}
		return need;
	}


	public boolean already_got_entertainment_for(int CURRENT_CLIENT, int DAY) {
		boolean found = false;	
		
		for (int x=0; x<logger_array_length ; x++ ){

			if (loggerarray[x].get_current_client() == CURRENT_CLIENT && loggerarray[x].get_day() == DAY) {
				found = true;
			}
		}
	return found;
	}
	

	public boolean already_got_entertainment_of(int CURRENT_CLIENT, int TYPE) {
		boolean found = false;	
		for (int x=0; x<logger_array_length ; x++ ){

			if (loggerarray[x].get_current_client() == CURRENT_CLIENT && loggerarray[x].get_type() == TYPE) {
				found = true;
			}
		}
	return found;
	}

	
	public int staying_in(int CURRENT_CLIENT) {
		
		int cutoff = 120;
		if (agent.getClientPreference(CURRENT_CLIENT , TACAgent.HOTEL_VALUE) > cutoff){ 
			return GOOD_HOTEL;
		}
		else {
			return CHEAP_HOTEL;
		}
	}



	public float get_boost_price(int need) {		
		float static_boost = 85;
		float time_boost = (agent.getGameTime() / 72000);
		float quantity_boost = (need/2)*(need/2);
		float boost = time_boost * quantity_boost * static_boost;
		return boost;
	}




	









/*********************************TACAgent methods***********************************/
	public void gameStarted() {
		log.fine("Game " + agent.getGameID() + " started!");
		fill_logger_array();
		print_log_array();
		fill_details_array();
		print_details_array();
		print_log_array();
	}
	protected void init(ArgEnumerator args) {
    }	
	public void quoteUpdated(int auctionCategory) {
    log.fine("All quotes for "
	     + agent.auctionCategoryToString(auctionCategory)
	     + " has been updated");
	}
	public void bidUpdated(Bid bid) {
    log.fine("Bid Updated: id=" + bid.getID() + " auction="
	     + bid.getAuction() + " state="
	     + bid.getProcessingStateAsString());
    log.fine("       Hash: " + bid.getBidHash());
	}
	public void bidRejected(Bid bid) {
		log.warning("Bid Rejected: " + bid.getID());
		log.warning("      Reason: " + bid.getRejectReason()
		+ " (" + bid.getRejectReasonAsString() + ')');
	}
	public void bidError(Bid bid, int status) {
		log.warning("Bid Error in auction " + bid.getAuction() + ": " + status
		+ " (" + agent.commandStatusToString(status) + ')');
	}
	public void gameStopped() {
		log.fine("Game Stopped!");
	}
	public void auctionClosed(int auction) {
		log.fine("*** Auction " + auction + " closed!");
	}
	public void quoteUpdated(Quote quote){
		int AUCTION_ID = quote.getAuction();
		int CAT = agent.getAuctionCategory(AUCTION_ID);
		
		if (CAT == TACAgent.CAT_FLIGHT){
      		buy_flights(quote);
		}
		else if (CAT == TACAgent.CAT_HOTEL){
		  	buy_hotels(quote);
		}
		else {
			allocate_entertainment(quote);
		}
	}
	
	/*!!!!!!!!!!!!!!!!!BUGTRACKING METHODS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
	public void print_log_array() {
		for (int x=0; x<logger_array_length ; x++) {
			log.fine("THIS IS LOG " + x);
			log.fine("CLIENT = " + loggerarray[x].get_current_client());
			log.fine("DAY = " + loggerarray[x].get_day());
			log.fine("TYPE = " + loggerarray[x].get_type());
		}
	}


	
	public void print_details_array(){
		for (int i = 0; i < 28; i ++) {
			log.fine(" INDEX " + i + " STARTS HERE ____ > ");
			log.fine("Auctionid:\t"+ DetailsArray[i].get_auction_ID() + " | ");
			log.fine("Cat:\t" +DetailsArray[i].get_category() + " | ");
			log.fine("Type:\t"+DetailsArray[i].get_type() + " | ");
			log.fine("Day:\t"+DetailsArray[i].get_day() + " | ");
			log.fine("Need:\t"+DetailsArray[i].get_need() + " | ");
			log.fine("Own:\t"+DetailsArray[i].get_own() + " | ");
			log.fine("Bidding For:\t"+DetailsArray[i].bidding_for() + " | ");
		}
	}
	/////////////////////////////////////////////////////////////////////////////

	public static void main (String[] args) {
		TACAgent.main(args);
	}
}