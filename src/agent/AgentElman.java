package agent;

import se.sics.tac.aw.*;
import se.sics.tac.util.ArgEnumerator;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

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
	
	private EntertainmentTracker haveEntertainment,wantEntertainment;
	private HotelTracker haveHotels,wantHotels;
	private FlightTracker haveFlights,wantFlights;
	
	ArrayList<Client> clients;

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
	}

	public void quoteUpdated(Quote quote) {
		int auction = quote.getAuction();
		int auctionCategory = agent.getAuctionCategory(auction);
		if (auctionCategory == TACAgent.CAT_HOTEL) {
			int alloc = agent.getAllocation(auction);
			float fear = 5.0f;
			if (alloc > 0 && quote.hasHQW(agent.getBid(auction))
					&& quote.getHQW() < alloc) {
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
				agent.submitBid(bid);
			} else if (quote.getAskPrice() + fear > lastBidPrice[auction]
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
					long power = 120 - Math.round(Math.pow(time, 3));
					if (power > 80) {
						prices[auction] = (new Float("" + power)).floatValue();
					}
				} else {
					float tempPrice = (float) Math.cbrt((double) agent
							.getGameTime() * 100f);
					// if(tempPrice < ){
					prices[auction] = 50f + (agent.getGameTime() * 100f) / 540000;
					// }
				}
				bid.addBidPoint(alloc, prices[auction]);
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(auction) + " own="
							+ agent.getOwn(auction));
				}
				agent.submitBid(bid);
			}
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
		

		// Set Clients
		log.fine("Game " + agent.getGameID() + " started!");
		for (int i = 0; i < 8; i++) {
			Client c = new Client(agent, i);
			clients.add(c);

			// flight
			wantFlights.add(1,c.getInFlight());
			wantFlights.add(2,c.getOutFlight());

			// hotel
			wantHotels.addDuration(1,c.getInFlight(), c.getOutFlight());

			// entertainment
			wantEntertainment.addDuration(c.getMaximumEntertainment(), c.getInFlight(), c.getOutFlight());
		}
		
		// Set things we own for entertainment
		for (int i = 1; i < 5; i++) {
			haveEntertainment.add(
					agent.getOwn(agent.getAuctionFor(agent.CAT_ENTERTAINMENT,
							agent.TYPE_ALLIGATOR_WRESTLING, i)),i);
			haveEntertainment.add(agent.getOwn(agent.getAuctionFor(
					agent.CAT_ENTERTAINMENT, agent.TYPE_AMUSEMENT, i)),i);
			haveEntertainment.add(agent.getOwn(agent.getAuctionFor(
					agent.CAT_ENTERTAINMENT, agent.TYPE_MUSEUM, i)),i);
		}
		
		
				
		calculateUtilities();		
		
		calculateAllocation();
		
		calculateRisk();
		
		calculateUtilOverRisk();
		
		sendBids();
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
					price = 201;
					prices[i] = 201f;
				}//if alloc = 1 or if = 0? //if do additional in non 0 bids remember re-bid rules
				else if(alloc = 0){
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

	private void updateBids() {
		float fear = 5.0f;
		float safety = 4.0f;
		for (int i = 8, n = 15; i < n; i++) {
			Quote quote = agent.getQuote(i);
			if (quote.getAskPrice() > lastAskPrice[i] && lastAskPrice[i] != 0) {
				diff[i] = (quote.getAskPrice() - lastAskPrice[i]) + safety;
			} else if (quote.getAskPrice() + fear > lastBidPrice[i]
					&& lastAskPrice[i] != 0) {
				diff[i] = (lastAskPrice[i] - lastAskPrice2[i]) + safety; // second
																			// order
																			// change
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
		for(int i = 8;i < 15;i++){
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
			if (hotel > 70) {
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

	// -------------------------------------------------------------------
	// Only for backward compatibility
	// -------------------------------------------------------------------

	public static void main(String[] args) {
		TACAgent.main(args);
	}

} // DummyAgent
