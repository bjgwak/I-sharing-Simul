package SimModel;

import kr.ac.kaist.server.commcentr.trust.history.Features;

public class User {
	
	private int id = 0;
	private int groupNum = 0;
	private boolean maliciousness = false;
	private String accessright; 
	private double trVal = 0.0;
	
	public User(int id, int groupnum, boolean malicious, String access, double trVal) {
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
	
	public boolean getMaliciousness() {
		return (this.maliciousness);
	}
	
	public double getTrustValue() {
		return (this.trVal);
	}
	
	public String getAccessRights() {
		return (this.accessright);
	}
	
	public void putTrustValue(double tr) {
		this.trVal = tr;
	}

}
