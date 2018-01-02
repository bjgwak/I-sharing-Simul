package kr.ac.kaist.server.commcentr.trust.history;

public class Evidence {
	private int id = -1;
	private Features feats = null;
	private int nPosInt = 0;
	private int nNegInt = 0;
	private double trVal = 0.0;
	
	public Evidence(int id, Features feats, int nPosInt, int nNegInt, double trVal) {
		this.id = id;
		this.feats = feats;
		this.nPosInt = nPosInt;
		this.nNegInt = nNegInt;
		this.trVal = trVal;
		
		return;
	}
	
	public int getId() {
		return (this.id);
	}
	
	public Features getFeatures() {
		return (this.feats);
	}
	
	public int getNumOfPositiveInteractions() {
		return (this.nPosInt);
	}
	
	public int getNumOfNegativeInteractions() {
		return (this.nNegInt);
	}
	
	public double getTrustValue() {
		return (this.trVal);
	}
	
	public String toString() {
		String str = "";
		
		str += "evId=" + this.id + ",feats=" + this.feats + ",\n";
		str += "\t\t#posInt=" + this.nPosInt + ",#negInt=" + this.nNegInt + ",trVal=" + this.trVal;
		
		return(str);
	}
}
