package kr.ac.kaist.server.commcentr.trust.history;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.util.HashSet;

import org.json.JSONArray;

public class InteractionHistoryManager {
	private String table = null;
	private Connection conn = null;
	
	public InteractionHistoryManager() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		
		return;
	}
	
	public void connect(String ip, int port, String id, String pw, String db, String table) throws Exception {
		this.table = table;
		this.conn = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port, id, pw);
		
		Statement stmt = this.conn.createStatement();
		
		stmt.execute("CREATE DATABASE IF NOT EXISTS " + db);
		
		stmt.execute("USE " + db);
		
		stmt.execute(
			"CREATE TABLE IF NOT EXISTS " +
				this.table + " (" +
					"evId INT NOT NULL AUTO_INCREMENT, " +	//	evidence id
					"srchKey VARCHAR(128) NOT NULL, " +		//	search key
					"feats TEXT NOT NULL, " +				//	features
					"nPosInt INT NOT NULL, " +				//	number of positive interactions
					"nNegInt INT NOT NULL, " +				//	number of negative interactions
					"trVal DOUBLE NOT NULL, " +				//	trust value
					"UNIQUE (evId), " +
					"PRIMARY KEY (evId), " +
					"INDEX (srchKey)" +
				")"
		);
		
		stmt.close();
		
		
		return;
	}
	
	public Experience extractExperience(String srchKey, HashSet<Object> keyFNames) throws Exception {
		Statement stmt = this.conn.createStatement();
		ResultSet rsSet = 
			stmt.executeQuery(
				"SELECT evId, feats, nPosInt, nNegInt, trVal " + 
				"FROM " + this.table + " " +
				"WHERE srchKey = '" + srchKey + "'"
			);
		
		Experience exper = new Experience();
		Features feats = null;
		Evidence ev = null;
		
		while (rsSet.next()) {
			feats = new Features(new JSONArray(rsSet.getString(2)));
			if (feats.containsKeys(keyFNames)) {
				ev = new Evidence(rsSet.getInt(1), feats, rsSet.getInt(3), rsSet.getInt(4), rsSet.getDouble(5));
				exper.add(ev);
			}
			else {
				feats.clear();
			}
		}
		
		rsSet.close();
		stmt.close();
			
		return (exper);
	}
	
	public void updateTrustValue(int evId, double trVal) throws Exception {
		Statement stmt = this.conn.createStatement();
		
		stmt.execute(
			"UPDATE " + this.table + " " +
			"SET trVal = " + trVal + " " + 
			"WHERE evId = " + evId
		);
		
		stmt.close();
			
		return;
	}

	public Evidence insertEvidence(String srchKey, Features feats, int nPosInt, int nNegInt, double trVal) throws Exception {
		Statement stmt = this.conn.createStatement();
		stmt.execute(
			"INSERT INTO " + this.table + " " +
			"VALUES(" +
				"evId = -1, '" + srchKey + "', '" + feats.toJSON() + "', " + 
				nPosInt + ", " + nNegInt + ", " + trVal + 
			")"
		);
		
		ResultSet rsSet = stmt.executeQuery("SELECT max(evId) FROM " + this.table);
		rsSet.next();
		int evId = rsSet.getInt(1);
		
		rsSet.close();
		stmt.close();
		
		return (new Evidence(evId, feats, nPosInt, nNegInt, trVal));
	}
	
	public void insertInteractions(String srchKey, Features feats, Feedback feedback, double trVal) throws Exception {
		
		
		Statement stmt = this.conn.createStatement();
		
		ResultSet rsSet = stmt.executeQuery("SELECT max(evId) FROM " + this.table);
		rsSet.next();
		int evId = rsSet.getInt(1);
		
		evId++;
		//System.out.println(evId);
		if(feedback == Feedback.POSITIVE){
			stmt.execute(
				"INSERT INTO " + this.table + " " +
				"VALUES(" +
					evId + ", '" + srchKey + "', '" + feats.toJSON() + "', " + 
					1 + ", " + 0 + ", " + trVal + 
				")"
			);
		}
		else{
			stmt.execute(
					"INSERT INTO " + this.table + " " +
					"VALUES(" +
						evId + ", '" + srchKey + "', '" + feats.toJSON() + "', " + 
						0 + ", " + 1 + ", " + trVal + 
					")"
				);
			
		}
		
		
		
		rsSet.close();
		stmt.close();
		
		return;
		
	}
	
	
	
	public void increaseNumOfInteractions(int evId, Feedback feedback) throws Exception {
		Statement stmt = this.conn.createStatement();
		
		if (feedback == Feedback.POSITIVE) {
			stmt.execute(
				"UPDATE " + this.table + " " +
				"SET nPosInt = nPosInt + 1 " +
				"WHERE evId = " + evId
			);
		}
		else if (feedback == Feedback.NEGATIVE) {
			stmt.execute(
				"UPDATE " + this.table + " " +
				"SET nNegInt = nNegInt + 1 " +
				"WHERE evId = " + evId
			);
		}
		
		stmt.close();
			
		return;
	}
	
	public void clear() throws Exception {
		Statement stmt = this.conn.createStatement();
		
		stmt.execute("DELETE FROM " + this.table);
		
		return;
	}
	
	public void close() throws Exception {
		this.table = null;
		this.conn.close();
		
		return;
	}
}
