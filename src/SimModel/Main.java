package SimModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TrustManager.TrustCalculator;
import TrustManager.TrustManager;
import kr.ac.kaist.server.commcentr.trust.history.Feedback;
import java.util.Random;



public class Main {
	
	
	
	static double maliciousrate = 0.2; 
	static double maliciousactingrate = 0.2;
	
	static int learninguser = 200;
	static int learninground = 30;
	static int targetuser = 100;
	static int targetround = 31;
	static int groupnum = 10;
	static double trustthreshold = 0.50;
	static double baserate = 0.5;
	
	public static void main(String[] args) throws Exception {
		
		 Random oRandom = new Random();

		
		
		User[] users = new User[learninguser]; 	//learning for previous users (I-sharing only)
		
		for(int i = 0; i < learninguser; i++){
			
			if(i > (1 - maliciousrate) * learninguser)
				users[i] = new User(i, i/(learninguser/groupnum), 1, "U", baserate);
			else
				users[i] = new User(i, i/(learninguser/groupnum), 0, "U", baserate);
		}
		
		
		
		for(int j = 0; j < learninground; j++){
			
			TrustManager.connect(1);
			
			for(int i = 0; i < learninguser; i++){
				
				if(oRandom.nextBoolean()){		//P-interaction = 0.5
					users[i].putTrustValue(TrustManager.computeTrustValue("P1", String.valueOf(i))); 
					
					if(i > (1- maliciousrate) * learninguser){ //malicious
						if(oRandom.nextFloat() < maliciousactingrate){		//sensing malicious actions
							if(users[i].getAccessRights() == "U" || users[i].getAccessRights() == "D" ||users[i].getAccessRights() == "R"){ 	//Access rights are too high	
								TrustManager.putInteraction("P1",String.valueOf(i), Feedback.NEGATIVE, users[i].getTrustValue()); 
								
								//System.out.println(i + "th user is demoted");
							}
							else{
								TrustManager.putInteraction("P1",String.valueOf(i), Feedback.NEGATIVE, users[i].getTrustValue()); 
							}
						}
						else{
							//sensing benign actions
							TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, users[i].getTrustValue()); 
						}
							
					}
					else{		//benign user
						TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, users[i].getTrustValue()); 

					}
				}
			}
			
			TrustManager.close();
			Thread.sleep(100);
			if(j % 10 == 0)
				System.out.println(j +" round passed");
		}
		
		System.out.println("==========EOT==========");
		
		User[] newusers = new User[targetuser]; 
		int falsepositive = 0;
		int falsenegative = 0;
		int truepositive = 0;
		int truenegative = 0;
		
		int counter_falsepositive = 0;
		int counter_falsenegative = 0;
		int counter_truepositive = 0;
		int counter_truenegative = 0;
		
		for(int i = 0; i < targetuser; i++){
			if(i > (1-maliciousrate) * targetuser)
				newusers[i] = new User(i, i/(targetuser/groupnum), 1, "U", baserate);
			else
				newusers[i] = new User(i, i/(targetuser/groupnum), 0, "U", baserate);
		}
		
		
		for(int j = 0; j < targetround; j++){
			
			//TrustManager.connect(2);
			
			for(int i = 0; i < targetuser; i++){
				
				String AR = newusers[i].getAccessRights();
				
				
				if(i > (1-maliciousrate) * targetuser){ //malicious
					if(oRandom.nextFloat() < maliciousactingrate){		//sensing malicious actions
						if(AR.equals("C")|| AR.equals("U") || AR.equals("D") || AR.equals("R")){ 	//Access rights are too high	
							counter_falsepositive++;
							demoteAR(newusers[i]);
							//System.out.println(i + "th user is demoted");
						}
						else{
							counter_truepositive++;		//Access rights are appropriate
						}
					}
					else{//sensing benign actions
						if(AR.equals("C")|| AR.equals("U") || AR.equals("D") || AR.equals("R")){ 	//Access rights are too high	
							counter_falsepositive++;
							//System.out.println(i + "th user is demoted");
						}
						else{
							counter_truepositive++;		//Access rights are appropriate
						}
					}
						
				}
				else{		//benign user
					if(AR.equals("C")|| AR.equals("U") || AR.equals("D") || AR.equals("R")){
						counter_truenegative++;
					}
					else
						counter_falsenegative++;
				}
			}
			if(j % 5 == 0){
				//System.out.println(j + "PPV: " + (double)counter_truepositive/(counter_falsepositive+ counter_truepositive));
				//System.out.println(j + "NPV: " + (double)counter_truenegative/(counter_falsenegative+ counter_truenegative));
				System.out.println(j + "ACC: " + (double)(counter_truepositive+counter_truenegative)/(counter_falsepositive+ counter_truepositive+counter_falsenegative+counter_truenegative));
			}
			//TrustManager.close();
			//Thread.sleep(10);
			
		}
		System.out.println("========EOB==========");
		//end of bailey
		
		
		
		for(int i = 0; i < targetuser; i++){		//initialization for I-sharing simulation
			if(i > (1-maliciousrate) * targetuser)
				newusers[i] = new User(i, i/(targetuser/groupnum), 1, "U", baserate);
			else
				newusers[i] = new User(i, i/(targetuser/groupnum), 0, "U", baserate);
		}
		
		
		
		for(int j = 0; j < targetround; j++){		//Trust 기준을 변경하기
			
			TrustManager.connect(2);
			
			for(int i = 0; i < targetuser; i++){
				
				newusers[i].putTrustValue(TrustManager.computeTrustValue("P1", String.valueOf(i))); 
				
				if(newusers[i].getTrustValue() == baserate){
					
					double AR = calculateAR(newusers[i], users);//unknown user
					
					if(i > (1- maliciousrate) * targetuser){ //malicious
						if(oRandom.nextFloat() < maliciousactingrate){		//sensing malicious actions
							
							if(AR > trustthreshold){		//action is too high
								falsepositive++;
								TrustManager.putInteraction("P1",String.valueOf(i), Feedback.NEGATIVE, users[i].getTrustValue()); 
							}
							else
								truepositive++;
						}
						else//sensing benign actions
						{
							AR = calculateAR(newusers[i], users);
							if(AR > trustthreshold){		//action is too high
								falsepositive++;
							}
							else{
								truenegative++;
							}
							TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, users[i].getTrustValue()); 
							
						}
					}
					else{		//benign user
						if(AR > trustthreshold){
						truenegative++;
						}
						else{
							falsenegative++;
						}
							
						TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, users[i].getTrustValue()); 
					}
				}
				else {//known user
					if(i > (1- maliciousrate) * targetuser){ //malicious
						
						double AR = calculateAR(newusers[i], users);//unknown user
						
						if(oRandom.nextFloat() < maliciousactingrate){		//sensing malicious actions
							
							
							if(newusers[i].getTrustValue() > trustthreshold){		
								falsepositive++;
							}
							else{
								truepositive++;
							}
							
							TrustManager.putInteraction("P1",String.valueOf(i), Feedback.NEGATIVE, users[i].getTrustValue()); 
						}
						else//sensing benign actions
						{
							if(newusers[i].getTrustValue() > trustthreshold){		
								falsepositive++;
							}
							else{
								truepositive++;
							}
							
							TrustManager.putInteraction("P1",String.valueOf(i), Feedback.NEGATIVE, users[i].getTrustValue()); 
						}
					}
					else{		//benign user
						if(newusers[i].getTrustValue() > trustthreshold){		
							truenegative++;
						}
						else{
							falsenegative++;
						}
						
						TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, users[i].getTrustValue()); 
					}
				}
			}
			if(j % 5 == 0){
				//System.out.println(j + "PPV: " + (double) truepositive/(falsepositive + truepositive));
				System.out.println(j + "ACC: " + (double)(truepositive+truenegative)/(falsepositive+ truepositive+falsenegative+truenegative));
				
			}
			TrustManager.close();
			//Thread.sleep(10);
			
		}
		
		
		
		
		
		//TODO 반대로 attack이 쭉 없었으면 상향시키는 개념이 추가되어야 하는가?
		//TODO magic numbers
		//TODO 유저에 대한 experience와 그룹 멤버들로부터 가져온 것과의 밸런싱이 현재 없음.
		
		
		
		System.out.println("==================");
		System.exit(0);
	}
	
	public static double calculateAR(User target, User[] groupmembers){
		
		double risk = 0;
		int count = 0;
		
		for(int i = 0; i < groupmembers.length; i++){
			if(target.getGroupNum() == groupmembers[i].getGroupNum()){
				
				risk += groupmembers[i].getTrustValue();
				count++;
			}
		}
		
		
		double finalrisk = risk /count;
		//System.out.println(target.getId() + "'s risk = " + finalrisk);
		//System.out.println(target.getId() + "," + risk + "," + finalrisk);
		return finalrisk;
	}
	
	
	public static void demoteAR(User user){
		if(user.getAccessRights().equals("C")){
			user.putAccessRights("N");
		}
		else if(user.getAccessRights().equals("U")){
			user.putAccessRights("N");
		}
		else if(user.getAccessRights().equals("D")){
			user.putAccessRights("N");
		}
		else if(user.getAccessRights().equals("R")){
			user.putAccessRights("N");
		}
		else if(user.getAccessRights().equals("N")){
			user.putAccessRights("N");
		}
		else{
			
		}
	}

}
