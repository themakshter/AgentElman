package agent;


import se.sics.tac.aw.*;
import se.sics.tac.util.ArgEnumerator;

import java.util.logging.*;


public class AgentElman extends AgentImpl {

  private static final Logger log =
    Logger.getLogger(AgentElman.class.getName());

  private static final boolean DEBUG = false;

  private float[] prices;
  
  private int[] utilities;

  protected void init(ArgEnumerator args) {
    prices = new float[agent.getAuctionNo()];
    utilities = new int[8];
  }

  public void quoteUpdated(Quote quote) {
    int auction = quote.getAuction();
    int auctionCategory = agent.getAuctionCategory(auction);
    if (auctionCategory == TACAgent.CAT_HOTEL) {
      int alloc = agent.getAllocation(auction);
      if (alloc > 0 && quote.hasHQW(agent.getBid(auction)) &&
	  quote.getHQW() < alloc) {
	Bid bid = new Bid(auction);
	// Can not own anything in hotel auctions...
	prices[auction] = quote.getAskPrice() + 50;
	bid.addBidPoint(alloc, prices[auction]);
	if (DEBUG) {
	  log.finest("submitting bid with alloc="
		     + agent.getAllocation(auction)
		     + " own=" + agent.getOwn(auction));
	}
	agent.submitBid(bid);
      }
    } else if (auctionCategory == TACAgent.CAT_ENTERTAINMENT) {
      int alloc = agent.getAllocation(auction) - agent.getOwn(auction);
      if (alloc != 0) {
	Bid bid = new Bid(auction);
	if (alloc < 0)
	  prices[auction] = 200f - (agent.getGameTime() * 120f) / 720000;
	else
	  prices[auction] = 50f + (agent.getGameTime() * 100f) / 720000;
	bid.addBidPoint(alloc, prices[auction]);
	if (DEBUG) {
	  log.finest("submitting bid with alloc="
		     + agent.getAllocation(auction)
		     + " own=" + agent.getOwn(auction));
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
    log.warning("      Reason: " + bid.getRejectReason()
		+ " (" + bid.getRejectReasonAsString() + ')');
  }

  public void bidError(Bid bid, int status) {
    log.warning("Bid Error in auction " + bid.getAuction() + ": " + status
		+ " (" + agent.commandStatusToString(status) + ')');
  }

  public void gameStarted() {
    log.fine("Game " + agent.getGameID() + " started!");
    calculateUtilities();
    calculateAllocation();
    sendBids();
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
	  price = 200;
	  prices[i] = 200f;
	}
	break;
      case TACAgent.CAT_ENTERTAINMENT:
	if (alloc < 0) {
	  price = 200;
	  prices[i] = 200f;
	} else if (alloc > 0) {
	  price = 50;
	  prices[i] = 50f;
	}
	break;
      default:
	break;
      }
      if (price > 0) {
	Bid bid = new Bid(i);
	bid.addBidPoint(alloc, price);
	if (DEBUG) {
	  log.finest("submitting bid with alloc=" + agent.getAllocation(i)
		     + " own=" + agent.getOwn(i));
	}
	agent.submitBid(bid);
      }
    }
  }
  
  //TODO: Fabrice, parallelise
  //TODO: Max achievable utility
  public void calculateUtilities(){
	  for(int i = 0; i < 8;i++){
		 int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
	     int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
	     int hotel = agent.getClientPreference(i, TACAgent.HOTEL_VALUE);
	     int travelPenalty = 0;
	     int hotelBonus = hotel;
	     int funBonus = agent.getClientPreference(i, TACAgent.E1) + agent.getClientPreference(i, TACAgent.E2) + agent.getClientPreference(i, TACAgent.E3);
	      utilities[i] = 1000 + travelPenalty + hotelBonus + funBonus;
	      System.out.println("Stay duration : " + inFlight +  " to " + outFlight+ "\nHotel Bonus : " + hotelBonus + "\nFun Bonus : " + funBonus + "\nMax util : " + utilities[i]);
	  }
  }

  private void calculateAllocation() {
    for (int i = 0; i < 8; i++) {
      int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
      int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
      int hotel = agent.getClientPreference(i, TACAgent.HOTEL_VALUE);
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
      while((eType = nextEntType(i, eType)) > 0) {
	auction = bestEntDay(inFlight, outFlight, eType);
	log.finer("Adding entertainment " + eType + " on " + auction);
	agent.setAllocation(auction, agent.getAllocation(auction) + 1);
      }
    }
  }

  private int bestEntDay(int inFlight, int outFlight, int type) {
    for (int i = inFlight; i < outFlight; i++) {
      int auction = agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
					type, i);
      if (agent.getAllocation(auction) < agent.getOwn(auction)) {
	return auction;
      }
    }
    // If no left, just take the first...
    return agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
			       type, inFlight);
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

  public static void main (String[] args) {
    TACAgent.main(args);
  }

} // DummyAgent
