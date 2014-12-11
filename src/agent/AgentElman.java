package agent;

import se.sics.tac.aw.*;
import se.sics.tac.util.ArgEnumerator;

import java.util.ArrayList;
import java.util.logging.*;

import javax.swing.Timer;

public class AgentElman extends AgentImpl {


	private static final Logger log = Logger.getLogger(AgentElman.class
			.getName());

	private static final boolean DEBUG = false;

	private float[] prices, diff, lastAskPrice, lastAskPrice2, lastBidPrice,
	lastBidPrice2, lastFlightPrice, initialFlightPrice;

	private EntertainmentTracker unallocatedEntertainment;
	private HotelTracker unallocatedHotels;

	private boolean[] closedGood,closedCheap;

	ArrayList<Client> clients;
	ClientComparator cc = new ClientComparator();

	Timer updateTimer;

	private int[][] entertainVal;

	private int[] lastAlloc;

	private float [] fear;
	private float openingPrice;

	private int noOfFlightPerturbations;


	protected void init(ArgEnumerator args) {
		prices = new float[28];
		diff = new float[28];
		lastAskPrice = new float[28];
		lastAskPrice2 = new float[28];
		lastBidPrice = new float[28];
		lastBidPrice2 = new float[28];
		clients = new ArrayList<Client>();

		unallocatedEntertainment = new EntertainmentTracker();
		unallocatedHotels = new HotelTracker();

		entertainVal = new int[13][8];

		lastAlloc = new int[28];
		fear = new float[28];

		closedCheap = new boolean[4];
		closedGood = new boolean[4];

		openingPrice = 90.0f;

		clients = new ArrayList<Client>();
		entertainVal = new int[13][8];

		lastFlightPrice = new float[8];
		initialFlightPrice = new float[8];
		noOfFlightPerturbations = 0;
	}

	public void quoteUpdated(Quote quote) {
		int auction = quote.getAuction();
		int auctionCategory = TACAgent.getAuctionCategory(auction);
		if (auctionCategory == TACAgent.CAT_HOTEL) {
			int alloc = agent.getAllocation(auction);
			if (alloc > 0 && quote.hasHQW(agent.getBid(auction))
					&& quote.getHQW() < alloc) {
				Bid bid = new Bid(auction);
				// Can not own anything in hotel auctions...
				updateBids();
				prices[auction] = quote.getAskPrice() + diff[auction];
				if(prices[auction] > 650){
					prices[auction] = 650;
				}
				bid.addBidPoint(alloc, prices[auction]);
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(auction) + " own="
							+ agent.getOwn(auction));
				}
				agent.submitBid(bid);
			} else if (alloc > 0 && quote.getAskPrice() + fear[auction] > lastBidPrice[auction]
					&& lastAskPrice[auction] != 0) {// change here?
				Bid bid = new Bid(auction);
				// Can not own anything in hotel auctions...
				updateBids();
				prices[auction] = quote.getAskPrice() + diff[auction] + fear[auction];
				if(prices[auction] > 650){
					prices[auction] = 650;
				}
				bid.addBidPoint(alloc, prices[auction]);
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(auction) + " own="
							+ agent.getOwn(auction));
				}
				agent.submitBid(bid);
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
					float tempMax = 0;
					int tempMaxIndex = 0;
					for(int a = 0; a<8; a++){
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
				}
				bid.addBidPoint(alloc, prices[auction]);
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(auction) + " own="
							+ agent.getOwn(auction));
				}

				agent.submitBid(bid);
			}
		} else if (auctionCategory == TACAgent.CAT_FLIGHT) {

			bidOnFlights(auction,quote);

		}
	}


	public void quoteUpdated(int auctionCategory) {
		log.fine("All quotes for "
				+ TACAgent.auctionCategoryToString(auctionCategory)
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

	public void initialiseClients() {
		for (int i = 0; i < 8; i++) {
			Client c = new Client(agent, i);
			clients.add(c);

			c.addFlightToPackage(c.getInFlight(), 0);
			c.addFlightToPackage(c.getOutFlight(), 1);

			// entertainment
			for (int j = 1; j < 5; j++) {
				if(c.getInFlight() <= j && c.getOutFlight() > j){
					entertainVal[j][i] = agent.getClientPreference(i, TACAgent.E1);
					entertainVal[j+4][i] = agent.getClientPreference(i, TACAgent.E2);
					entertainVal[j+8][i] = agent.getClientPreference(i, TACAgent.E3);
				}
			}
		}
	}

	public void gameStarted() {

		init(null);

		log.fine("Game " + agent.getGameID() + " started!");

		initialiseClients();

		allocateStartingEnt();

		calculateAllocation();
		sendBids();

		initLastAlloc();
		initFear();

		for (int i = 0;i<8;i++) {
			initialFlightPrice[i] = agent.getQuote(i).getAskPrice();
		}
	}

	public void initLastAlloc() {
		for (int i = 0, n = TACAgent.getAuctionNo(); i < n; i++) {
			lastAlloc[i] = agent.getAllocation(i) - agent.getOwn(i);
		}
	}

	public void initFear() {
		for (int i = 0; i < 28; i++) {
			fear[i] = 5.0f;
			if (agent.getAllocation(i) > 2) {
				fear[i] += 20f;
			}
			if (agent.getAllocation(i) > 3) {
				fear[i] +=15f;
			}
			if (agent.getAllocation(i) > 4) {
				fear[i]+= 10f;
			}
			if(agent.getAllocation(i) > 5){
				fear[i] += 10f;
			}
		}
	}


	public void gameStopped() {
		System.out.println("Opening Price: " + openingPrice);
		log.fine("Game Stopped!");
	}

	public void auctionClosed(int auction) {
		log.fine("*** Auction " + auction + " closed!");


		updateClosedHotelAuctions(auction);

		int type = TACAgent.getAuctionType(auction);
		int numOwned = agent.getOwn(auction);
		int day = TACAgent.getAuctionDay(auction);

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
							agent.setAllocation(13, agent.getAllocation(13) + 1);
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

		if(checkAllHotelAuctionsClosed()) {
			for(Client c: clients) {
				ClientPackage clientPackage = c.getClientPackage();
				if(!clientPackage.isFeasible()) {
					int wantedOutFlight = clientPackage.calculateLastPossibleOutFlightForCurrentHotels();
					int wantedInFlight = clientPackage.calculateLastPossibleInFlightForCurrentHotels();		

					//TODO if equal pick cheaper
					if (wantedOutFlight - clientPackage.getInFlight() > clientPackage.getOutFlight() - wantedInFlight && wantedOutFlight != 0) {
						Bid bid = new Bid(TACAgent.getAuctionFor(TACAgent.CAT_FLIGHT, TACAgent.TYPE_OUTFLIGHT, wantedOutFlight));
						bid.addBidPoint(1, 800);
						agent.submitBid(bid);
					} else if (wantedInFlight != 0) {
						Bid bid = new Bid(TACAgent.getAuctionFor(TACAgent.CAT_FLIGHT, TACAgent.TYPE_INFLIGHT, wantedInFlight));
						bid.addBidPoint(1, 800);
						agent.submitBid(bid);
					}
				}	
			}

		}

		float tempValue = 0;
		if (agent.getGameTime() > 57 * 1000 && agent.getGameTime() < 69 * 1000) {

			int count = 0;
			for (int i = 8; i < 16; i++) {
				if(agent.getQuote(i).getAskPrice() > 50){
					count++;
				}
				tempValue += agent.getQuote(i).getAskPrice();

			}
			tempValue /= count;
			openingPrice = (openingPrice + tempValue)/2;

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

	private void bidOnFlights(int auction, Quote quote) {

		noOfFlightPerturbations++;

		if(lastFlightPrice[auction] == 0) {
			lastFlightPrice[auction] = quote.getAskPrice();
		}

		if(quote.getAskPrice() - lastFlightPrice[auction] >= 10 && agent.getAllocation(auction) > agent.getOwn(auction)) {
			Bid bid = new Bid(auction);
			bid.addBidPoint(agent.getAllocation(auction) - agent.getOwn(auction), 1000);
			if (DEBUG) {
				log.finest("submitting bid with alloc="
						+ agent.getAllocation(auction) + " own="
						+ agent.getOwn(auction));
			}
			agent.submitBid(bid);
		} else if(noOfFlightPerturbations >= 20 && agent.getAllocation(auction) > agent.getOwn(auction)) {
			if(quote.getAskPrice() - initialFlightPrice[auction] > 0) {
				Bid bid = new Bid(auction);
				bid.addBidPoint(agent.getAllocation(auction) - agent.getOwn(auction), 1000);
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(auction) + " own="
							+ agent.getOwn(auction));
				}
				agent.submitBid(bid);
			}
		}
		
		lastFlightPrice[auction] = quote.getAskPrice();



		if(agent.getGameTime() > 6.5f*60f*1000f && agent.getAllocation(auction) > agent.getOwn(auction)) {
			Bid bid = new Bid(auction);
			bid.addBidPoint(agent.getAllocation(auction) - agent.getOwn(auction), 1000);
			if (DEBUG) {
				log.finest("submitting bid with alloc="
						+ agent.getAllocation(auction) + " own="
						+ agent.getOwn(auction));
			}
			agent.submitBid(bid);
		}

	}

	private void sendBids() {
		for (int i = 0, n = TACAgent.getAuctionNo(); i < n; i++) {
			int alloc = agent.getAllocation(i) - agent.getOwn(i);
			float price = -1f;
			switch (TACAgent.getAuctionCategory(i)) {
			case TACAgent.CAT_FLIGHT:
				if (alloc > 0) {
					price = 0;
				}
				break;
			case TACAgent.CAT_HOTEL:
				if (alloc > 0) {
					price = 251;
					prices[i] = 251f;
				}
				else if(alloc == 0){
					price = 20;
					prices[i] = 20; 
					Bid bid = new Bid(i);
					bid.addBidPoint(1, price);
					if (DEBUG) {
						log.finest("submitting bid with alloc="
								+ agent.getAllocation(i) + " own="
								+ agent.getOwn(i));
					}
					agent.submitBid(bid);
					price = 0;
					prices[i] = 0;
				}

				break;
			case TACAgent.CAT_ENTERTAINMENT:
				if (alloc < 0) {
					price = 130;
					prices[i] = 130f;
				} else if (alloc > 0) {
					price = 0;
					prices[i] = 0f;
				}
				break;
			default:
				break;
			}
			if (price > 0) {
				Bid bid = new Bid(i);
				bid.addBidPoint(alloc, price);
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
		float safety = 10.0f;
		for (int i = 8, n = 16; i < n; i++) {
			//safety = fear[i];
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


	private void calculateAllocation() {
		for (Client c : clients) {
			int inFlight = c.getInFlight();
			int outFlight = c.getOutFlight();
			int duration = outFlight - inFlight;
			int hotel = c.getHotel();
			int type;

			// Get the flight preferences auction and remember that we are
			// going to buy tickets for these days. (inflight=1, outflight=0)		
			int auction = TACAgent.getAuctionFor(TACAgent.CAT_FLIGHT,
					TACAgent.TYPE_INFLIGHT, inFlight);
			agent.setAllocation(auction, agent.getAllocation(auction) + 1);

			auction = TACAgent.getAuctionFor(TACAgent.CAT_FLIGHT,
					TACAgent.TYPE_OUTFLIGHT, outFlight);
			agent.setAllocation(auction, agent.getAllocation(auction) + 1);

			// if the hotel value is greater than 70 we will select the
			// expensive hotel (type = 1)		
			if (hotel > openingPrice+10 && duration < 4) {
				type = TACAgent.TYPE_GOOD_HOTEL;
			} else {
				type = TACAgent.TYPE_CHEAP_HOTEL;
			}

			// allocate a hotel night for each day that the agent stays
			for (int d = inFlight; d < outFlight; d++) {
				auction = TACAgent.getAuctionFor(TACAgent.CAT_HOTEL, type, d);
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
			int auction = TACAgent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT, type,
					i);
			if (agent.getAllocation(auction) < agent.getOwn(auction)) {
				return auction;
			}
		}
		// If no left, just take the first...
		return TACAgent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT, type, inFlight);
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

		for (int i = 1; i < 5; i++) {
			unallocatedEntertainment.addAmount(1,i,
					agent.getOwn(TACAgent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
							TACAgent.TYPE_ALLIGATOR_WRESTLING, i)));

			unallocatedEntertainment.addAmount(2,i,agent.getOwn(TACAgent.getAuctionFor(
					TACAgent.CAT_ENTERTAINMENT, TACAgent.TYPE_AMUSEMENT, i)));

			unallocatedEntertainment.addAmount(3,i,agent.getOwn(TACAgent.getAuctionFor(
					TACAgent.CAT_ENTERTAINMENT, TACAgent.TYPE_MUSEUM, i)));
		}


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
