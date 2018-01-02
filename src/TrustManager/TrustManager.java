package TrustManager;

import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeSet;

import kr.ac.kaist.server.commcentr.trust.history.Evidence;
import kr.ac.kaist.server.commcentr.trust.history.Experience;
import kr.ac.kaist.server.commcentr.trust.history.Features;
import kr.ac.kaist.server.commcentr.trust.history.Feedback;
import kr.ac.kaist.server.commcentr.trust.history.InteractionHistoryManager;
import kr.ac.kaist.server.commcentr.trust.util.Configuration;
import kr.ac.kaist.server.commcentr.trust.util.Getch;



public class TrustManager {
	/* Number[0] = pEvId, Number[1] = nEvId, Number[3] = pTrVal */
	
	static InteractionHistoryManager pIhm;
	static InteractionHistoryManager nIhm;
	static Experience nExper;
	static Experience pExper;
	
	
	
	public static double computeTrustValue(String trustor, String trustee) throws Exception {
		
		Features feats = new Features();
		
		feats.put("id", trustee);
		
		Configuration conf = Configuration.getInstance();
		
		/* compute a personal trust value */
		TreeSet<Object> pSrchKeyFNames = conf.getTreeSet("pIntHist.srchKeyFNames");
		//	pSrchKeyFNames = personal search key feature names
		String pSrchKey = "";
		int i = 0;
		for (Object pSrchKeyFName : pSrchKeyFNames) {
			pSrchKey += pSrchKeyFName + "=" + feats.get(pSrchKeyFName);
			if (i < pSrchKeyFNames.size() - 1) {
				pSrchKey += ",";
			}
			i++;
		}
		
		HashSet<Object> pTargetFNames = conf.getHashSet("pTr.targetFNames");
		//	pTargetFNames = personal target feature names

		
		
		
		pExper = pIhm.extractExperience(pSrchKey, pTargetFNames);

		/*System.out.print("personal experience with [" + pSrchKey + "]:");

		for (Evidence ev : pExper) {
			//System.out.println("\t" + ev);
		}
		System.out.println("------------------------------------------------------------------------");
				*/
		double pTrVal = TrustCalculator.computeTrustValue(feats, pExper, pTargetFNames);
		//System.out.print("personal trust value:");

		//System.out.println("\tv=" + pTrVal);
		//System.out.println("------------------------------------------------------------------------");
		
		/* compute a non-personal trust value */
		TreeSet<Object> nSrchKeyFNames = conf.getTreeSet("nIntHist.srchKeyFNames");
		String nSrchKey = "";
		int j = 0;
		for (Object nSrchKeyFName : nSrchKeyFNames) {
			nSrchKey += nSrchKeyFName + "=" + feats.get(nSrchKeyFName);
			if (j < nSrchKeyFNames.size() - 1) {
				nSrchKey += ",";
			}
		}
		
		HashSet<Object> nKeyFNames = conf.getHashSet("nTr.keyFNames");
		//	nKeyFNames = non-personal key feature names
		/*
		 * The above line must be changed later as follows.
		 * 1. nKeyFNames must be extracted from a situation-aware ontology, given some place features.
		 * 2. After a trust value computation (or an interaction), the ontology must be updated, given the place features and some device features.
		 * */
				
		
		
		nExper = nIhm.extractExperience(nSrchKey, nKeyFNames);
		//System.out.print("non-personal experience in [" + nSrchKey + "]:");

		/*for (Evidence ev : nExper) {
			System.out.println("\t" + ev);
		}
		System.out.println("------------------------------------------------------------------------");
		*/
		double nTrVal = TrustCalculator.computeTrustValue(feats, nExper, null);
		//System.out.print("non-personal trust value:");

		//System.out.println("\ta=" + nTrVal);
		//System.out.println("------------------------------------------------------------------------");
		
		/* combine the personal and non-personal trust value into a new personal trust value */
		int nPosInt = pExper.countPositiveInteractions();
		int nNegInt = pExper.countNegativeInteractions();
		pTrVal = TrustManager.computeTrustValue(pTrVal, nPosInt, nNegInt, nTrVal);
		//!!!!System.out.print(nSrchKey + ", pos: " + nPosInt + " neg: " + nNegInt + "\n");

		/*System.out.println("\tr=" + nPosInt + ",s=" + nNegInt);
		System.out.println("\tu=1/(r+s+1)=" + 1.0 / (nPosInt + nNegInt + 1.0));
		System.out.println("\tP(w)=b + a*u=" + pTrVal);

		System.out.println("------------------------------------------------------------------------");*/

		/* combine the personal and non-personal trust value into a new non-personal trust value */
		nPosInt = nExper.countPositiveInteractions();
		nNegInt = nExper.countNegativeInteractions();
		nTrVal = TrustManager.computeTrustValue(nTrVal, nPosInt, nNegInt, 0.5);

		/* update the personal interaction history */
		
		//!!!!!System.out.println(pTrVal + ":" + nTrVal);
		
		double finaltrust = 0;
		Evidence pEv = pExper.findMatchingEvidence(feats);
		if(pEv == null){
			finaltrust = nTrVal;
			
		}
		else{
			finaltrust = pTrVal;
			pIhm.updateTrustValue(pEv.getId(), pTrVal);
		}
		/* close interaction history managers */
		/* clear collections */
		
		//pIhm.close();
		//nIhm.close();


		
		
		return finaltrust;
	}
	
	private static double computeTrustValue(double beliefWeight, int nPosInt, int nNegInt, double base) throws Exception {

		//System.out.println(beliefWeight + " vs " + nPosInt/(nPosInt+nNegInt+2.0) +": " + base);

		//TODO

		double trVal = nPosInt/(nPosInt+nNegInt+2.0) + (2.0 / (nPosInt + nNegInt + 2.0)) * base;
		
		return (trVal);
	}
	
	public static void connect(int i){
		
		try {
			nIhm = new InteractionHistoryManager();
			pIhm = new InteractionHistoryManager();
			Configuration conf = Configuration.getInstance();
			
			if(i==1){
				nIhm.connect(
							conf.getString("nIntHist.ip"), conf.getInt("nIntHist.port"),
							conf.getString("nIntHist.id"), conf.getString("nIntHist.pw"),
							conf.getString("nIntHist.db"), conf.getString("nIntHist.table")
				);
				
				
				
				pIhm.connect(
							conf.getString("pIntHist.ip"), conf.getInt("pIntHist.port"),
							conf.getString("pIntHist.id"), conf.getString("pIntHist.pw"),
							conf.getString("pIntHist.db"), conf.getString("pIntHist.table")
				);
			}
			else{
				nIhm.connect(
						conf.getString("nIntHist.ip"), conf.getInt("nIntHist.port"),
						conf.getString("nIntHist.id"), conf.getString("nIntHist.pw"),
						conf.getString("nIntHist.db"), "new"+conf.getString("nIntHist.table")
				);
			
			
			
				pIhm.connect(
						conf.getString("pIntHist.ip"), conf.getInt("pIntHist.port"),
						conf.getString("pIntHist.id"), conf.getString("pIntHist.pw"),
						conf.getString("pIntHist.db"), "new"+conf.getString("pIntHist.table")
				);
				
			}
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	public static void close(){
		
		try {
			pIhm.close();
			nIhm.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public static void putInteraction(String trustor, String trustee, Feedback recvfeed, double trVal){
		
		Features feats = new Features();
		
		feats.put("id", trustee);
		
		
		Evidence pEv = pExper.findMatchingEvidence(feats);
		if(pEv == null){
			try {
				pIhm.insertInteractions("id=" + trustee, feats, recvfeed, trVal);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			//pIhm.updateTrustValue(pEv.getId(), pTrVal);
			try {
				pIhm.increaseNumOfInteractions(pEv.getId(), recvfeed);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		
		/* update the non-personal interaction history */
		
		
		
	}

	/*public static Features featsExpander(String id) {

		Features feats = new Features();

		if (id == "P1") {
			feats.put("id", "P1");
			feats.put("name", "Bumjin Gwak");
			feats.put("type", "Rental");
			feats.put("location", "36.34");
			feats.put("yearOfBirth", "1983");
			feats.put("gender", "MALE");
			feats.put("devices", "TV,Hairdryer");
			feats.put("interests", "Basketball,ChildCare,Research");
		} else if (id.equals("2")) {
			feats.put("id", "2");
			
		}
		



		return feats;


	}*/

}
