package kr.ac.kaist.server.commcentr.trust.history;

import java.util.Collection;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import weka.core.Attribute;

public class Features extends HashMap<String, Object> {
	private static final long serialVersionUID = -6215045371597578862L; 
	
	public Features() {
		return;
	}
	
	public Features(JSONArray jArr) throws Exception {
		JSONObject jObj = null;
		
		for (int i = 0; i < jArr.length(); i++) {
			jObj = jArr.getJSONObject(i);
			switch (jObj.getInt("fType")) {
				case (Attribute.NUMERIC):
					this.put(jObj.getString("fName"), jObj.getDouble("fVal"));
					break;
				case (Attribute.NOMINAL):
					this.put(jObj.getString("fName"), jObj.getString("fVal"));
					break;
			}
		}
		
		return;
	}
	
	public JSONArray toJSON() throws Exception {
		JSONArray jArr = new JSONArray();
		JSONObject jObj = null;
		Object fVal = null;
		
		for (String fName : this.keySet()) {
			jObj = new JSONObject();
			jObj.put("fName", fName);
			fVal = this.get(fName);
			if (fVal instanceof Number) {
				jObj.put("fType", Attribute.NUMERIC);
				jObj.put("fVal", (Double)fVal);
			}
			else if (fVal instanceof String) {
				jObj.put("fType", Attribute.NOMINAL);
				jObj.put("fVal", (String)fVal);
			}
			jArr.put(jObj);
		}
		
		return (jArr);
	}
	
	public boolean containsKeys(Collection<Object> keys) {
		for (Object key : keys) {
			if (this.containsKey(key)) {
				return (true);
			}
		}
		
		return (false);
	}
	
	public static void main(String[] args) throws Exception {
		JSONArray jArr = new JSONArray();
		JSONObject jObj = null;
		
		jObj = new JSONObject();
		jObj.put("fType", 0);
		jObj.put("fName", "name1");
		jObj.put("fVal", 1);
		jArr.put(jObj);
		
		jObj = new JSONObject();
		jObj.put("fType", 1);
		jObj.put("fName", "name2");
		jObj.put("fVal", "val2");
		jArr.put(jObj);
		
		Features feats = new Features(jArr);
		
		feats.put("name3", 4);
		
		System.out.println(feats);
	}
}
