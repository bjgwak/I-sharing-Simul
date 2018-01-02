package kr.ac.kaist.server.commcentr.trust.util;


import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class Configuration extends JSONObject {
	private final static String CONF_FILE = "conf/conf";
	
	private static Configuration _instance = null;
	
	private Configuration(String str) throws Exception {
		super(str);		
				
		return;
	}
	
	public static Configuration getInstance() throws Exception {
		if (Configuration._instance == null) {
			Scanner in = new Scanner(new File(Configuration.CONF_FILE));
			String str = "";
			
			while (in.hasNext()) {
				str += in.nextLine();
			}
			in.close();
			
			Configuration._instance = new Configuration(str);
		}
		
		return (Configuration._instance);
	}
	
	public TreeSet<Object> getTreeSet(String key) throws Exception {
		TreeSet<Object> treeSet = new TreeSet<Object>();
		JSONArray jArr = this.getJSONArray(key);
		
		for (int i = 0; i < jArr.length(); i++) {
			treeSet.add(jArr.get(i));
		}
		
		return (treeSet);
	}
	
	public HashSet<Object> getHashSet(String key) throws Exception {
		HashSet<Object> hashSet = new HashSet<Object>();
		JSONArray jArr = this.getJSONArray(key);
		
		for (int i = 0; i < jArr.length(); i++) {
			hashSet.add(jArr.get(i));
		}
		
		return (hashSet);
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = Configuration.getInstance();
		
		System.out.println(conf.getTreeSet(""));
	}
}
