package TrustManager;

import java.util.HashMap;
import java.util.HashSet;


import kr.ac.kaist.server.commcentr.trust.history.Evidence;
import kr.ac.kaist.server.commcentr.trust.history.Experience;
import kr.ac.kaist.server.commcentr.trust.history.Features;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;


public class TrustCalculator {
	private static final Attribute TR_VAL_ATTR = new Attribute("trVal");
	
	public static double computeTrustValue(Features feats, Experience exper, HashSet<Object> targetFNames) throws Exception {
		if (exper.isEmpty()) {
			return (0.0);
		}
		
		HashMap<String, HashSet<String>> fName2fVals = TrustCalculator.getFeatureName2FeatureValues(feats, exper, targetFNames);
		//	fName2fVals = map of feature name to feature values
		
		HashMap<String, Attribute> fName2Attr = TrustCalculator.getFeatureName2Attribute(fName2fVals);
		//	fName2Attr = map of feature name to attribute
		
		Instances trainingSet = TrustCalculator.getTrainingSet(exper, fName2Attr);
		
		M5P classifier = new M5P();
		classifier.buildClassifier(trainingSet);
		
		Instance testInst = TrustCalculator.getInstance(feats, fName2Attr);
		
		double trVal = classifier.classifyInstance(testInst);
		
		if (trVal < 0.0) {
			trVal = 0.0;
		}
		else if (trVal > 1.0) {
			trVal = 1.0;
		}
		
		for (String fName : fName2fVals.keySet()) {
			fName2fVals.get(fName).clear();
		}
		fName2fVals.clear();
		fName2Attr.clear();
		
		return (trVal);
	}
	
	private static HashMap<String, HashSet<String>> getFeatureName2FeatureValues(Features feats, Experience exper, HashSet<Object> targetFNames) throws Exception {
		HashMap<String, HashSet<String>> fName2fVals = new HashMap<String, HashSet<String>>();
		
		TrustCalculator.putFeatureName2FeatureValues(feats, targetFNames, fName2fVals);
		
		for (Evidence ev : exper) {
			TrustCalculator.putFeatureName2FeatureValues(ev.getFeatures(), targetFNames, fName2fVals);
		}
		
		return (fName2fVals);
	}
	
	private static void putFeatureName2FeatureValues(Features feats, HashSet<Object> targetFNames, HashMap<String, HashSet<String>> fName2fVals) throws Exception {
		HashSet<String> fVals = null;
		Object fVal = null;
		
		for (String fName : feats.keySet()) {
			if (targetFNames != null && !targetFNames.contains(fName)) {
				continue;
			}
			fVal = feats.get(fName);
			if (fName2fVals.containsKey(fName)) {
				fVals = fName2fVals.get(fName);
				if (fVal instanceof Number) {
					if (!fVals.isEmpty()) {
						throw (new Exception("Inconsistent feature values"));
					}
				}
				else if (fVal instanceof String) {
					if (!fVals.isEmpty()) {
						fVals.add((String)fVal);
					}
					else {
						throw (new Exception("Inconsistent feature values"));
					}
				}
			}
			else {
				fVals = new HashSet<String>();
				fName2fVals.put(fName, fVals);
				if (fVal instanceof String) {
					fVals.add((String)fVal);
				}
			}
		}
		
		return;
	}
	
	private static HashMap<String, Attribute> getFeatureName2Attribute(HashMap<String, HashSet<String>> fName2fVals) {
		HashMap<String, Attribute> fName2Attr = new HashMap<String, Attribute>(fName2fVals.size() + 1);
		Attribute attr = null;
		FastVector attrVals = null;
		HashSet<String> fVals = null;
		
		for (String fName : fName2fVals.keySet()) {
			fVals = fName2fVals.get(fName);
			if (fVals.isEmpty()) {
				attr = new Attribute(fName);
			}
			else {
				attrVals = new FastVector(fVals.size());
				for (String fVal : fVals) {
					attrVals.addElement(fVal);
				}
				attr = new Attribute(fName, attrVals);
			}
			fName2Attr.put(fName, attr);
		}
		fName2Attr.put(TrustCalculator.TR_VAL_ATTR.name(), TrustCalculator.TR_VAL_ATTR);
		
		return (fName2Attr);
	}
	
	private static Instances getTrainingSet(Experience exper, HashMap<String, Attribute> fName2Attr) {
		FastVector attrs = new FastVector(fName2Attr.size());
		
		for (Attribute attr : fName2Attr.values()) {
			attrs.addElement(attr);
		}
		
		Instances trainingSet = new Instances("TrainingSet", attrs, exper.size());
		Instance trainingInst = null;
		
		for (Evidence ev : exper) {
			trainingInst = TrustCalculator.getInstance(ev.getFeatures(), fName2Attr);
			trainingInst.setValue(TrustCalculator.TR_VAL_ATTR, ev.getTrustValue());
			trainingSet.add(trainingInst);
		}
		trainingSet.setClass(TrustCalculator.TR_VAL_ATTR);
		
		return (trainingSet);
	}
	
	private static Instance getInstance(Features feats, HashMap<String, Attribute> fName2Attr) {
		Instance inst = new Instance(fName2Attr.size());
		Object fVal = null;
		
		for (String fName : feats.keySet()) {
			if (!fName2Attr.containsKey(fName)) {
				continue;
			}
			fVal = feats.get(fName);
			if (fVal instanceof Number) {
				inst.setValue(fName2Attr.get(fName), (Double)fVal);
			}
			else if (fVal instanceof String) {
				inst.setValue(fName2Attr.get(fName), (String)fVal);
			}
		}
		
		return (inst);
	}
}
