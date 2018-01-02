package kr.ac.kaist.server.commcentr.trust.history;

import java.util.ArrayList;

public class Experience extends ArrayList<Evidence> {
	private static final long serialVersionUID = -5635067318152545125L;
	
	public int countPositiveInteractions() {
		int nPosInt = 0;
		
		for (Evidence ev : this) {
			nPosInt += ev.getNumOfPositiveInteractions();
		}
		
		return (nPosInt);
	}
	
	public int countNegativeInteractions() {
		int nNegInt = 0;
		
		for (Evidence ev : this) {
			nNegInt += ev.getNumOfNegativeInteractions();
		}
		
		return (nNegInt);
	}
	
	public Evidence findMatchingEvidence(Features feats) {
		Features _feats = null;
		
		for (Evidence ev : this) {
			_feats = ev.getFeatures();
			if (_feats.get("id").equals(feats.get("id"))) {
		
				return (ev);
			}
		}
		
		return (null);
	}
	
	public void print() {
		for (Evidence ev : this) {
			System.out.println(ev);
		}
		
		return;
	}
}
