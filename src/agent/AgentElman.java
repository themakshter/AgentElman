package agent;

import se.sics.tac.aw.*;
import se.sics.tac.util.ArgEnumerator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

import javax.swing.Timer;

public class AgentElman extends AgentImpl {

	private static ExecutorService executorService = Executors
			.newCachedThreadPool();

	private static final Logger log = Logger.getLogger(AgentElman.class
			.getName());

	private static final boolean DEBUG = false;

	private float[] prices, diff, lastAskPrice, lastAskPrice2, lastBidPrice,
	lastBidPrice2;
	private int[] utilities, risks;

	private double[] calculatedUtility;
	private int[][] clientEntertainment;

	private EntertainmentTracker haveEntertainment,wantEntertainment, unallocatedEntertainment;
	private HotelTracker haveHotels,wantHotels, unallocatedHotels;
	private FlightTracker haveFlights,wantFlights, unallocatedFlights;
	
	private boolean[] closedGood,closedCheap;

	ArrayList<Client> clients;
	ClientComparator cc = new ClientComparator();
	
	Timer updateTimer;
	
	
	private int[][] entertainVal;
	
	private int[] lastAlloc;
	
	private float [] fear;
	private float openingPrice;

	protected void init(ArgEnumerator args) {
		prices = new float[agent.getAuctionNo()];
		utilities = new int[8];
		risks = new int[8];
		calculatedUtility = new double[8];
		diff = new float[28];
		lastAskPrice = new float[28];
		lastAskPrice2 = new float[28];
		lastBidPrice = new float[28];
		lastBidPrice2 = new float[28];
		clientEntertainment = new int[8][3];
		clients = new ArrayList<Client>();
		haveEntertainment = new EntertainmentTracker();
		wantEntertainment = new EntertainmentTracker();
		haveHotels = new HotelTracker();
		wantHotels = new HotelTracker();
		haveFlights = new FlightTracker();
		wantFlights = new FlightTracker();
		

		unallocatedEntertainment = new EntertainmentTracker();
		unallocatedHotels = new HotelTracker();
		
		entertainVal = new int[13][8];
		
		lastAlloc = new int[28];
		fear = new float[28];

		closedCheap = new boolean[4];
		closedGood = new boolean[4];
		
		openingPrice = 90.0f;
	}

	public void quoteUpdated(Quote quote) {
		int auction = quote.getAuction();
		int auctionCategory = agent.getAuctionCategory(auction);
		if (auctionCategory == TACAgent.CAT_HOTEL) {
			int alloc = agent.getAllocation(auction);
			//float fear[auction] = 5.0f;
			//if(alloc > 2){fear = fear + 10} //something like this - maybe 3,20?
			if (alloc > 0 && quote.hasHQW(agent.getBid(auction))
					&& quote.getHQW() < alloc) {
				Bid bid = new Bid(auction);
				// Can not own anything in hotel auctions...
				updateBids();
				System.out.println("Price : " + prices[auction]);
				System.out.println("Ask price: " + quote.getAskPrice());
				prices[auction] = quote.getAskPrice() + diff[auction];
				System.out.println("New Price : " + prices[auction]);
				bid.addBidPoint(alloc, prices[auction]);
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(auction) + " own="
							+ agent.getOwn(auction));
				}
				agent.submitBid(bid);
			} else if (quote.getAskPrice() + fear[auction] > lastBidPrice[auction]
					&& lastAskPrice[auction] != 0) {// change here?
				Bid bid = new Bid(auction);
				// Can not own anything in hotel auctions...
				updateBids();
				prices[auction] = quote.getAskPrice() + diff[auction];
				bid.addBidPoint(alloc, prices[auction]);
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(auction) + " own="
							+ agent.getOwn(auction));
				}
			}		
		} else if (auctionCategory == TACAgent.CAT_ENTERTAINMENT) {
			int alloc = agent.getAllocation(auction) - agent.getOwn(auction);
			if (alloc != 0) {
				Bid bid = new Bid(auction);
				if (alloc < 0) {
					double time = ((double) agent.getGameTime())
							/ (60.0 * 1000.0);
					time = time * 0.4873;
					long power = 130 - Math.round(Math.pow(time, 3));
					if (power > 80) {
						prices[auction] = (new Float("" + power)).floatValue();
					}
				} else {
					//prices[auction] = 50f + (agent.getGameTime() * 100f) / 720000;
				//}
					float tempMax = 0;
					int tempMaxIndex = 0;
					for(int a = 0; a<8; a++){
						//auction -16
						if(entertainVal[auction-16][a] > tempMax){
							tempMax = entertainVal[auction-16][a];
							tempMaxIndex = a;
						}						
					}
					float tempPrice = (float) Math.cbrt( Math.pow(tempMax, 3) * agent.getGameTime() / 420000);
					if(tempPrice < tempMax - 5){
						prices[auction] = tempPrice;
					}else{
						prices[auction] = Math.max(0, tempMax - 5);
					}
						if(lastAlloc[auction] > alloc){
							//TODO: fix this because we need to turn this zero only once we get ticket
							entertainVal[auction - 16][tempMaxIndex] = 0;
						}
					lastAlloc[auction] = alloc;
//					
//					//float tempPrice = (float) Math.cbrt((double) agent
//					//		.getGameTime() * 100f);
//					// if(tempPrice < ){
//					//prices[auction] = 50f + (agent.getGameTime() * 100f) / 540000;
//					// }
				}
				bid.addBidPoint(alloc, prices[auction]);
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(auction) + " own="
							+ agent.getOwn(auction));
				}
				if(agent.getGameTime()<50 && agent.getGameTime() > 100){
					//something like this
				}
				
				agent.submitBid(bid);
			}
		} else if (auctionCategory == TACAgent.CAT_FLIGHT) {
			//System.out.println("Flights updated " + quote.getBidPrice());
			
			//System.out.println(agent.getOwn(quote.getAuction()));
			
		}
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
		
		if(bid.getAuction()>7 && bid.getAuction() < 16) {
			System.out.println("Bid Price " + bid.getAuction());
		}
		
	}

	public void bidRejected(Bid bid) {
		log.warning("Bid Rejected: " + bid.getID());
		log.warning("      Reason: " + bid.getRejectReason() + " ("
				+ bid.getRejectReasonAsString() + ')');
	}

	public void bidError(Bid bid, int status) {
		log.warning("Bid Error in auction " + bid.getAuction() + ": " + status
				+ " (" + agent.commandStatusToString(status) + ')');
	}

	public void gameStarted() {

		init(null);

		clients = new ArrayList<Client>();
		entertainVal = new int[13][8];


		// Set Clients
		log.fine("Game " + agent.getGameID() + " started!");
		for (int i = 0; i < 8; i++) {
			Client c = new Client(agent, i);
			clients.add(c);

			// flight
			wantFlights.add(1,c.getInFlight());
			wantFlights.add(2,c.getOutFlight());
			
			c.addFlightToPackage(c.getInFlight(), 0);
			c.addFlightToPackage(c.getOutFlight(), 1);

			// hotel
			wantHotels.addDuration(1,c.getInFlight(), c.getOutFlight());

			// entertainment
			//wantEntertainment.addDuration(c.getMaximumEntertainment(), c.getInFlight(), c.getOutFlight());
			for (int j = 1; j < 5; j++) {
				wantEntertainment.addAmount(agent.TYPE_ALLIGATOR_WRESTLING, j,1);
				
				if(c.getInFlight() <= j && c.getOutFlight() > j){
					entertainVal[j][i] = agent.getClientPreference(i, TACAgent.E1);
					entertainVal[j+4][i] = agent.getClientPreference(i, TACAgent.E2);
					entertainVal[j+8][i] = agent.getClientPreference(i, TACAgent.E3);
				}
				
			}
		}

		// Set things we own for entertainment
		for (int i = 1; i < 5; i++) {
			haveEntertainment.addAmount(1,i,
					agent.getOwn(agent.getAuctionFor(agent.CAT_ENTERTAINMENT,
							agent.TYPE_ALLIGATOR_WRESTLING, i)));
			haveEntertainment.addAmount(2,i,agent.getOwn(agent.getAuctionFor(
					agent.CAT_ENTERTAINMENT, agent.TYPE_AMUSEMENT, i)));
			haveEntertainment.addAmount(3,i,agent.getOwn(agent.getAuctionFor(
					agent.CAT_ENTERTAINMENT, agent.TYPE_MUSEUM, i)));
		}
		
		for (int i = 1; i < 5; i++) {
			unallocatedEntertainment.addAmount(1,i,
					agent.getOwn(agent.getAuctionFor(agent.CAT_ENTERTAINMENT,
							agent.TYPE_ALLIGATOR_WRESTLING, i)));
			
			unallocatedEntertainment.addAmount(2,i,agent.getOwn(agent.getAuctionFor(
					agent.CAT_ENTERTAINMENT, agent.TYPE_AMUSEMENT, i)));
			
			unallocatedEntertainment.addAmount(3,i,agent.getOwn(agent.getAuctionFor(
					agent.CAT_ENTERTAINMENT, agent.TYPE_MUSEUM, i)));
		}
		
		allocateStartingEnt();

		/*for(Client c: clients) {
			System.out.println("Entertainments: " + c.getClientPackage().getEntertainments()[0]+","
					+ c.getClientPackage().getEntertainments()[1]+","
					+ c.getClientPackage().getEntertainments()[2]+","
					+ c.getClientPackage().getEntertainments()[3]+
					" Index: " + c.getIndex());
		}*/


		calculateAllocation();
		sendBids();

		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				System.out.println("Timer called");
			}
		};
		updateTimer = new Timer(1 * 59 * 1000, taskPerformer);
		updateTimer.start();
		
		calculateUtilities();	
		calculateRisk();	
		calculateUtilOverRisk();
		
		
		for (int i = 0, n = agent.getAuctionNo(); i < n; i++) {
			lastAlloc[i] = agent.getAllocation(i) - agent.getOwn(i);
		}
		
		
		for (int i = 0; i < 28; i++) {
			fear[i] = 5.0f;
			if (agent.getAllocation(i) > 2) {
				fear[i] += 10f;
			}
			if (agent.getAllocation(i) > 3) {
				fear[i] +=10f;
			}
			if (agent.getAllocation(i) > 4) {
				fear[i]+= 15f;
			}
			if(agent.getAllocation(i) > 5){
				fear[i] += 15f;
			}
			if(agent.getAllocation(i) > 6){
				fear[i] += 10f;
			}
		}
		
	}

	public void updateTrackers(){
		int amountLeft;
		for(int i = 0;i < 4;i++){
			for(int j = 1; j < 4;j++){
				amountLeft = wantEntertainment.subtract(j, i, haveEntertainment.getTicket(j, i));
			}
		}

		//TODO: sort out haveStuff and wantStuff


	}

	public void gameStopped() {
		log.fine("Game Stopped!");
	}

	public void auctionClosed(int auction) {
		log.fine("*** Auction " + auction + " closed!");
		
		updateClosedHotelAuctions(auction);
		
		int type = agent.getAuctionType(auction);
		int numOwned = agent.getOwn(auction);
		int day = agent.getAuctionDay(auction);
		
		haveHotels.addAmount(type, day, numOwned);
		unallocatedHotels.addAmount(type, day, numOwned);
		
		ArrayList<Client> sortedHotelUtil =  cc.sort(clients, 5);
		if (type == 1) {
			
			int noToAllocate = numOwned;
			
			for( Client c : sortedHotelUtil) {	
				if( noToAllocate <=0 ) {
					break;
				}
				
				if(c.validDay(day)) {
					c.addHotelToPackage(day, type);
					unallocatedHotels.subtract(type, day, 1);
					noToAllocate--;
				}				
			}
		} else if (type == 0) {
			int noToAllocate = numOwned;
			
			for(int i = sortedHotelUtil.size()-1; i>=0;i--) {
				if( noToAllocate <=0 ) {
					break;
				}
				
				Client c = sortedHotelUtil.get(i);
				
				if(c.validDay(day)) {
					c.addHotelToPackage(day, type);
					unallocatedHotels.subtract(type, day, 1);
					noToAllocate--;
				}	
			}
			
		}
		
		
		for(Client c : clients) {
			ClientPackage clientPackage = c.getClientPackage();		
			if (clientPackage.calculateStayDuration() == 1 && clientPackage.validDay(day)) {
				if(!clientPackage.getHotelDays()[day-1]) {
					System.out.println("Changed hotel type");
					switch(auction) {
					case 8:
						if(!agent.getQuote(12).isAuctionClosed()) {
							agent.setAllocation(12, agent.getAllocation(12) + 1);
						}
						break;
					case 9:
						if(!agent.getQuote(13).isAuctionClosed()) {
							agent.setAllocation(13, agent.getAllocation(14) + 1);
						}
						break;
					case 10:
						if(!agent.getQuote(14).isAuctionClosed()) {
							agent.setAllocation(14, agent.getAllocation(14) + 1);
						}
						break;
					case 11:
						if(!agent.getQuote(15).isAuctionClosed()) {
							agent.setAllocation(15, agent.getAllocation(15) + 1);
						}
						break;
					case 12:
						if(!agent.getQuote(8).isAuctionClosed()) {
							agent.setAllocation(8, agent.getAllocation(8) + 1);
						}
						break;
					case 13:
						if(!agent.getQuote(9).isAuctionClosed()) {
							agent.setAllocation(9, agent.getAllocation(9) + 1);
						}
						break;
					case 14:
						if(!agent.getQuote(10).isAuctionClosed()) {
							agent.setAllocation(10, agent.getAllocation(10) + 1);
						}
						break;
					case 15:
						if(!agent.getQuote(11).isAuctionClosed()) {
							agent.setAllocation(11, agent.getAllocation(11) + 1);
						}
						break;	
					}
				}
			}

		}
		
		
	/*	for(Client c : clients) {
			System.out.println(c.getClientPackage().toString());
			System.out.println(c.getClientPackage().canCompletePackage(closedGood, closedCheap));
			System.out.println();
			
		}*/
		
		if(checkAllHotelAuctionsClosed()) {
			for(Client c: clients) {
				ClientPackage clientPackage = c.getClientPackage();
				if(!clientPackage.isFeasible()) {
					int wantedOutFlight = clientPackage.calculateLastPossibleOutFlightForCurrentHotels();
					int wantedInFlight = clientPackage.calculateLastPossibleInFlightForCurrentHotels();		
					
					if (wantedOutFlight - clientPackage.getInFlight() > clientPackage.getOutFlight() - wantedInFlight && wantedOutFlight != 0) {
						Bid bid = new Bid(agent.getAuctionFor(TACAgent.CAT_FLIGHT, TACAgent.DEPARTURE, wantedOutFlight));
						bid.addBidPoint(1, 700);
						agent.submitBid(bid);
					} else if (wantedInFlight != 0) {
						Bid bid = new Bid(agent.getAuctionFor(TACAgent.CAT_FLIGHT, TACAgent.DEPARTURE, wantedInFlight));
						bid.addBidPoint(1, 700);
						agent.submitBid(bid);
					}
				}	
			}
			
		}
		
		float tempValue = 0;
	//	System.out.println("Time : " + agent.getGameTime());
		if (agent.getGameTime() > 59 * 1000 && agent.getGameTime() < 69 * 1000) {
			System.out.println("Time in loop : " + agent.getGameTime());
			for (int i = 8; i < 16; i++) {
				tempValue += agent.getQuote(i).getAskPrice();
			
			}
			tempValue /= 8;
			openingPrice = (openingPrice + tempValue)/2;
			System.out.println("Opening price:" + openingPrice);
		}
	}
	
	private void updateClosedHotelAuctions(int auction) {
		switch(auction) {
		case 8:
			closedCheap[0] = true;
			break;
		case 9:
			closedCheap[1] = true;
			break;
		case 10:
			closedCheap[2] = true;
			break;
		case 11:
			closedCheap[3] = true;
			break;
		case 12:
			closedGood[0] = true;
			break;
		case 13:
			closedGood[1] = true;
			break;
		case 14:
			closedGood[2] = true;
			break;
		case 15:
			closedGood[3] = true;
			break;	
		}
	}
	
	private boolean checkAllHotelAuctionsClosed() {
		
		for (boolean b : closedCheap) {
			if (!b) {
				return false;
			}
		}
		for (boolean b : closedGood) {
			if (!b) {
				return false;
			}
		}
		
		return true;
	}

	private void sendBids() {
		for (int i = 0, n = agent.getAuctionNo(); i < n; i++) {
			int alloc = agent.getAllocation(i) - agent.getOwn(i);
			float price = -1f;
			switch (agent.getAuctionCategory(i)) {
			case TACAgent.CAT_FLIGHT:
				if (alloc > 0) {
					price = 1000;
				}
				break;
			case TACAgent.CAT_HOTEL:
				if (alloc > 0) {
					price = 251;
					prices[i] = 251f;
				}//if alloc = 1 or if = 0? //if do additional in non 0 bids remember re-bid rules
				else if(alloc == 0){
					price = 20;
					prices[i] = 20; 
					Bid bid = new Bid(i); //make bid here?
					bid.addBidPoint(1, price);
					if (DEBUG) {
						log.finest("submitting bid with alloc="
								+ agent.getAllocation(i) + " own="
								+ agent.getOwn(i));
					}
					agent.submitBid(bid);
					System.out.println();
					price = 0;
					prices[i] = 0;
				}

				break;
			case TACAgent.CAT_ENTERTAINMENT:
				if (alloc < 0) {
					price = 120;
					prices[i] = 120f;
				} else if (alloc > 0) {
					price = 0;
					prices[i] = 0f;//needs change
				}
				break;
			default:
				break;
			}
			if (price > 0) {
				Bid bid = new Bid(i);
				bid.addBidPoint(alloc, price);//alloc + 1 ?
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(i) + " own="
							+ agent.getOwn(i));
				}
				agent.submitBid(bid);
				lastBidPrice2[i] = lastBidPrice2[i]; //this always 0?
				lastBidPrice[i] = price;
			}
		}
	}
	
	private void updateBids() { //may want to pass fear here if changed
		//float fear = 15.0f;
		float safety = 10.0f;
		for (int i = 8, n = 16; i < n; i++) {
			safety = fear[i];
			Quote quote = agent.getQuote(i);
			if (quote.getAskPrice() > lastAskPrice[i] && lastAskPrice[i] != 0) {
				diff[i] = (quote.getAskPrice() - lastAskPrice[i]) + safety;
			} else if (quote.getAskPrice() + fear[i] > lastBidPrice[i]
					&& lastAskPrice[i] != 0) {
				//diff[i] = (lastAskPrice[i] - lastAskPrice2[i]) + safety; // second order change? [want + fear??]
				diff[i] = (quote.getAskPrice() - lastAskPrice[i]) + safety; //doing this way would rebid for top?
			} else if (lastAskPrice[i] == 0) {
				diff[i] = 50f;
			}
			lastAskPrice2[i] = lastAskPrice[i];
			lastAskPrice[i] = quote.getAskPrice();
		}
	}

	// TODO: Fabrice, parallelise
	public void calculateUtilities() {

		for (int i = 0; i < 8; i++) {

			int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
			int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
			int hotelBonus = agent.getClientPreference(i, TACAgent.HOTEL_VALUE);
			int[] entertainmentBonuses = {
					agent.getClientPreference(i, TACAgent.E1),
					agent.getClientPreference(i, TACAgent.E2),
					agent.getClientPreference(i, TACAgent.E3) };
			int stayDuration = outFlight - inFlight;
			int travelPenalty = 0;
			int funBonus = 0;

			if (stayDuration >= 3) {
				funBonus = entertainmentBonuses[0] + entertainmentBonuses[1]
						+ entertainmentBonuses[2];
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

			utilities[i] = 1000 - travelPenalty + hotelBonus + funBonus;
			System.out.println("Stay duration : " + inFlight + " to "
					+ outFlight + "\nHotel Bonus : " + hotelBonus
					+ "\nFun Bonus : " + funBonus + "\nMax util : "
					+ utilities[i]);
			System.out.println();
		}
	}

	public void calculateRisk() {
		for (int i = 0; i < 8; i++) {
			int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
			int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
			int stayDuration = outFlight - inFlight;
			int risk = 0;

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
			risks[i] = risk;
			System.out.println("Risk value : " + risk);
		}
	}

	public void calculateUtilOverRisk() {

		// int[][] gotTickets = new int[5][3];
		int[][] gotTickets = new int[12][2];

		/*
		 * for(int i = 1; i < 5;i++){ gotTickets[i][0] =
		 * agent.getOwn(agent.getAuctionFor(agent.CAT_ENTERTAINMENT,
		 * agent.TYPE_ALLIGATOR_WRESTLING, i)); gotTickets[i][1] =
		 * agent.getOwn(agent.getAuctionFor(agent.CAT_ENTERTAINMENT,
		 * agent.TYPE_AMUSEMENT, i)); gotTickets[i][2] =
		 * agent.getOwn(agent.getAuctionFor(agent.CAT_ENTERTAINMENT,
		 * agent.TYPE_MUSEUM, i)); }
		 */

		int pointer = 0;

		for (int i = 1; i < 5; i++) {
			for (int j = 0; j < agent
					.getOwn(agent.getAuctionFor(agent.CAT_ENTERTAINMENT,
							agent.TYPE_ALLIGATOR_WRESTLING, i)); j++) {
				gotTickets[pointer][0] = i;
				gotTickets[pointer][1] = agent.TYPE_ALLIGATOR_WRESTLING;
				pointer++;
			}
			for (int j = 0; j < agent.getOwn(agent.getAuctionFor(
					agent.CAT_ENTERTAINMENT, agent.TYPE_AMUSEMENT, i)); j++) {
				gotTickets[pointer][0] = i;
				gotTickets[pointer][1] = agent.TYPE_AMUSEMENT;
				pointer++;
			}
			for (int j = 0; j < agent.getOwn(agent.getAuctionFor(
					agent.CAT_ENTERTAINMENT, agent.TYPE_MUSEUM, i)); j++) {
				gotTickets[pointer][0] = i;
				gotTickets[pointer][1] = agent.TYPE_MUSEUM;
				pointer++;
			}
		}

		int[][] bestClients = new int[12][2];

		int[] tempStore = new int[4];

		for (int i = 0; i < 8; i++) {
			int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
			int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);

			for (int j = 0; j < 12; j++) {
				if (inFlight <= gotTickets[j][0]
						&& gotTickets[j][0] < outFlight) {
					if (bestClients[j][1] < agent.getClientPreference(i,
							gotTickets[j][1])) {
						tempStore[0] = gotTickets[j][0];
						tempStore[1] = gotTickets[j][1];
						tempStore[2] = bestClients[j][0];
						tempStore[3] = bestClients[j][1];

						bestClients[j][0] = i;
						bestClients[j][1] = agent.getClientPreference(i,
								gotTickets[j][1]);

						pushDown(tempStore[0], tempStore[1], tempStore[2],
								tempStore[3], j, bestClients, gotTickets,
								tempStore);

					}
				}
			}
		}

		for (int j = 0; j < 8; j++) {
			int ticketUtil = getTicketUtilityForClient(bestClients, gotTickets,
					j);
			calculatedUtility[j] = ((double) utilities[j])
					/ ((double) risks[j]) + ticketUtil;
			System.out.println("Calculated utility : " + calculatedUtility[j]);
		}
	}

	public int getTicketUtilityForClient(int[][] bestClients,
			int[][] gotTickets, int clientNumber) {
		boolean[] used = { false, false, false, false, false };
		int ticketUtility = 0;
		for (int i = 0; i < bestClients.length; i++) {
			if (bestClients[i][0] == clientNumber) {
				if (!used[gotTickets[i][0]]) {
					ticketUtility += bestClients[i][1];
					used[gotTickets[i][0]] = true;
				}
			}
		}
		return ticketUtility;
	}

	private void pushDown(int previousValue1, int previousValue2,
			int previousValue3, int previousValue4, int j, int[][] bestClients,
			int[][] gotTickets, int[] tempStore) {
		for (int k = j + 1; k < 12; k++) {
			if (previousValue1 == gotTickets[k][0]
					&& previousValue2 == gotTickets[k][1]) {
				if (bestClients[k][1] < previousValue3) {
					tempStore[0] = gotTickets[k][0];
					tempStore[1] = gotTickets[k][1];
					tempStore[2] = bestClients[k][0];
					tempStore[3] = bestClients[k][1];

					bestClients[k][0] = previousValue3;
					bestClients[k][1] = previousValue4;

					pushDown(tempStore[0], tempStore[1], tempStore[2],
							tempStore[3], j, bestClients, gotTickets, tempStore);
				}
			} else {
				return;
			}
		}
	}

	/*
	 * private void calculateAllocation() { for (int i = 0; i < 8; i++) { int
	 * inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL); int outFlight
	 * = agent.getClientPreference(i, TACAgent.DEPARTURE); int hotel =
	 * agent.getClientPreference(i, TACAgent.HOTEL_VALUE); int type;
	 * 
	 * 
	 * // Get the flight preferences auction and remember that we are // going
	 * to buy tickets for these days. (inflight=1, outflight=0) int auction =
	 * agent.getAuctionFor(TACAgent.CAT_FLIGHT, TACAgent.TYPE_INFLIGHT,
	 * inFlight); agent.setAllocation(auction, agent.getAllocation(auction) +
	 * 1); auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
	 * TACAgent.TYPE_OUTFLIGHT, outFlight); agent.setAllocation(auction,
	 * agent.getAllocation(auction) + 1);
	 * 
	 * // if the hotel value is greater than 70 we will select the // expensive
	 * hotel (type = 1) if (hotel > 70) { type = TACAgent.TYPE_GOOD_HOTEL; }
	 * else { type = TACAgent.TYPE_CHEAP_HOTEL; } // allocate a hotel night for
	 * each day that the agent stays for (int d = inFlight; d < outFlight; d++)
	 * { auction = agent.getAuctionFor(TACAgent.CAT_HOTEL, type, d);
	 * log.finer("Adding hotel for day: " + d + " on " + auction);
	 * agent.setAllocation(auction, agent.getAllocation(auction) + 1); }
	 * 
	 * int eType = -1; while((eType = nextEntType(i, eType)) > 0) { auction =
	 * bestEntDay(inFlight, outFlight, eType); log.finer("Adding entertainment "
	 * + eType + " on " + auction); agent.setAllocation(auction,
	 * agent.getAllocation(auction) + 1); } } }
	 */

	//TODO: complete method
	private void updateAllocation(){
		for(int i = 8;i < 16;i++){
			int own = agent.getOwn(i);
			int allocated = agent.getAllocation(i);
			Quote q = agent.getQuote(i);
			//if auction closes and we don't have enough, we target the other auction
			if(own < allocated && q.isAuctionClosed()){

			}
		}
	}



	private void calculateAllocation() {
		for (Client c : clients) {
			int inFlight = c.getInFlight();
			int outFlight = c.getOutFlight();
			int duration = outFlight - inFlight;
			int hotel = c.getHotel();
			int type;

			// Get the flight preferences auction and remember that we are
			// going to buy tickets for these days. (inflight=1, outflight=0)		
			int auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
					TACAgent.TYPE_INFLIGHT, inFlight);
			agent.setAllocation(auction, agent.getAllocation(auction) + 1);

			auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
					TACAgent.TYPE_OUTFLIGHT, outFlight);
			agent.setAllocation(auction, agent.getAllocation(auction) + 1);

			// if the hotel value is greater than 70 we will select the
			// expensive hotel (type = 1)		
			if (hotel > openingPrice && duration < 4) {
				type = TACAgent.TYPE_GOOD_HOTEL;
			} else {
				type = TACAgent.TYPE_CHEAP_HOTEL;
			}

			// allocate a hotel night for each day that the agent stays
			for (int d = inFlight; d < outFlight; d++) {
				auction = agent.getAuctionFor(TACAgent.CAT_HOTEL, type, d);
				log.finer("Adding hotel for day: " + d + " on " + auction);
				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
			}

			int eType = -1;
			while ((eType = nextEntType(c.getIndex(), eType)) > 0) {
				//				clientEntertainment[c.getIndex()][eType] = agent
				//						.getClientPreference(c.getIndex(), eType);
				auction = bestEntDay(inFlight, outFlight, eType);
				log.finer("Adding entertainment " + eType + " on " + auction);
				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
			}

		}
		
	}

	private int bestEntDay(int inFlight, int outFlight, int type) {
		for (int i = inFlight; i < outFlight; i++) {
			int auction = agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT, type,
					i);
			if (agent.getAllocation(auction) < agent.getOwn(auction)) {
				return auction;
			}
		}
		// If no left, just take the first...
		return agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT, type, inFlight);
	}

	private int nextEntType(int client, int lastType) {
		int e1 = agent.getClientPreference(client, TACAgent.E1);
		int e2 = agent.getClientPreference(client, TACAgent.E2);
		int e3 = agent.getClientPreference(client, TACAgent.E3);

		// At least buy what each agent wants the most!!!
		if ((e1 > e2) && (e1 > e3) && lastType == -1)
			return TACAgent.TYPE_ALLIGATOR_WRESTLING;
		if ((e2 > e1) && (e2 > e3) && lastType == -1)
			return TACAgent.TYPE_AMUSEMENT;
		if ((e3 > e1) && (e3 > e2) && lastType == -1)
			return TACAgent.TYPE_MUSEUM;
		return -1;
	}

	private void allocateStartingEnt() {
		
		ArrayList<Client> sortedClients = cc.sort(clients,2);

		for(Client c : sortedClients) {
			for(int i = 1;i <= unallocatedEntertainment.getAlligator().length; i++) {
				if(c.validDay(i) && unallocatedEntertainment.getAlligator()[i-1] > 0 && c.getClientPackage().getEntertainmentsAt(i) == 0) {
					c.getClientPackage().setEntertainmentsAt(i,1);
					unallocatedEntertainment.subtract(1, i, 1);
					break;
				}
			}
		}
		
		sortedClients = cc.sort(clients,3);
		
		for(Client c : sortedClients) {
			for(int i = 1;i <= unallocatedEntertainment.getAmusement().length; i++) {
				if(c.validDay(i) && unallocatedEntertainment.getAmusement()[i-1] > 0 && c.getClientPackage().getEntertainmentsAt(i) == 0) {
					c.getClientPackage().setEntertainmentsAt(i,2);
					unallocatedEntertainment.subtract(2, i, 1);
					break;
				}
			}
		}
		
		sortedClients = cc.sort(clients,4);
		
		for(Client c : sortedClients) {
			for(int i = 1;i <= unallocatedEntertainment.getMuseum().length; i++) {
				if(c.validDay(i) && unallocatedEntertainment.getMuseum()[i-1] > 0 && c.getClientPackage().getEntertainmentsAt(i) == 0) {
					c.getClientPackage().setEntertainmentsAt(i,3);
					unallocatedEntertainment.subtract(3, i, 1);
					break;
				}
			}
		}

	}


	// -------------------------------------------------------------------
	// Only for backward compatibility
	// -------------------------------------------------------------------

	public static void main(String[] args) {
		TACAgent.main(args);
	}

} // DummyAgent
