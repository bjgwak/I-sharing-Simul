package SimModel;

import kr.ac.kaist.server.commcentr.trust.history.Features;

public class User {
	
	private int id = 0;
	private int groupNum = 0;
	private int maliciousness = 0;
	private String accessright; 
	private double trVal = 0.0;
	
	public User(int id, int groupnum, int malicious, String access, double trVal) {
		this.id = id;
		this.groupNum = groupnum;
		this.maliciousness = malicious;
		this.accessright = access;
		this.trVal = trVal;
		return;
	}
	
	public int getId() {
		return (this.id);
	}
	
	public int getGroupNum() {
		return (this.groupNum);
	}
	
	public int getMaliciousness() {
		return (this.maliciousness);
	}
	
	public double getTrustValue() {
		return (this.trVal);
	}
	
	public String getAccessRights() {
		return (this.accessright);
	}
	
	public void putAccessRights(String ar) {
		this.accessright = ar;
	}
	
	public void putTrustValue(double tr) {
		this.trVal = tr;
	}

}
