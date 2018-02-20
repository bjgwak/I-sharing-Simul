package SimModel;



import TrustManager.TrustCalculator;
import TrustManager.TrustManager;
import kr.ac.kaist.server.commcentr.trust.history.Feedback;

import java.util.Arrays;
import java.util.Random;
import java.util.Collections;

public class Main {
	
	static double maliciousrate = 0.2; 
	static double maliciousactingrate = 0.2;
	static int maliciousinterval = 5;
	static int benigninterval = 10;
	static int maliciouslength = 20;
	static int benignlength = 5;
	
	
	static int learninguser = 200;
	static int learninground = 20;
	static int targetuser = 100;
	static int timeslot = 1001;
	static int groupnum = 10;
	static double trustthreshold = 0.5;
	static double interactionprob = 0.5;
	static double baserate = 0.5;
	static int queuesize=1;
	
	public static void main(String[] args) throws Exception {
		
		Random oRandom = new Random();

		User[] users = new User[learninguser]; 	//learning for previous users (I-sharing only)
		
		for(int i = 0; i < learninguser; i++){
			
			if(i > (1 - maliciousrate) * learninguser)
				users[i] = new User(i, i/(learninguser/groupnum), 1, "U", baserate, maliciousinterval, maliciouslength);
			else
				users[i] = new User(i, i/(learninguser/groupnum), 0, "U", baserate, benigninterval, benignlength);
		}
		
		
		TrustManager.connect(1);
		for(int j = 0; j < learninground; j++){
			
			
			
			for(int i = 0; i < learninguser; i++){
				
				if(oRandom.nextBoolean()){		//P-interaction = 0.5
					users[i].putTrustValue(TrustManager.computeTrustValue("P1", String.valueOf(i), baserate)); 
					
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
			
			
			
			if(j % 10 == 0)
				System.out.println(j +" round passed");
		}
		TrustManager.close();
		System.out.println("==========EOT==========");
		
		User[] newusers = new User[targetuser]; 
		int[] status = new int[targetuser];
		int[] markedasmalicious = new int[targetuser];
		int[] settimeclock = new int[targetuser];
		int[] authenticated = new int[targetuser];
 		int queueindex= 0;
		int falsepositive = 0;
		int falsenegative = 0;
		int truepositive = 0;
		int truenegative = 0;
		
		int counter_falsepositive = 0;
		int counter_falsenegative = 0;
		int counter_truepositive = 0;
		int counter_truenegative = 0;
		
		int final_counter_falsepositive = 0;
		int final_counter_falsenegative = 0;
		int final_counter_truepositive = 0;
		int final_counter_truenegative = 0;
		
		int bailey_avail = 0;
		int TARAS_avail = 0;
		
		for(int i = 0; i < targetuser; i++){
			if(i > (1-maliciousrate) * targetuser)
				newusers[i] = new User(i, i/(targetuser/groupnum), 1, "U", baserate, maliciousinterval, maliciouslength);
			else
				newusers[i] = new User(i, i/(targetuser/groupnum), 0, "U", baserate, benigninterval, benignlength);
		}
		
		Collections.shuffle(Arrays.asList(newusers));		//shuffle the order of users to interact
		
		Loop1: for(int j = 1; j < timeslot; j++){
			
			//TrustManager.connect(2);
			
			Loop2: for(int i = 0; i < targetuser; i++){
				
				if(markedasmalicious[i] == 0) {		//not malicious
						if(settimeclock[i] == 0) {	//interaction interval is ready
							if(oRandom.nextFloat() < interactionprob || status[i] != 0) {		//reflects interaction probability			
								if(queueindex == queuesize && status[i] == 0) {	//queue is full and not interact with him
									continue;
								}
								else if(queueindex < queuesize && status[i] == 0) {		//queue is not full
									queueindex++;
									status[i] = j;
								}
								
								//System.out.println(i + "is in the queue!");
								if(newusers[i].getInteractionLength() > j - status[i]) {		//in the interaction time
									String AR = newusers[i].getAccessRights();
									//System.out.println(i + "tries to interact!" + j + ":" + status[i]);
									if(newusers[i].getMaliciousness() == 1){ //malicious
										if(oRandom.nextFloat() < maliciousactingrate){		//sensing malicious actions
											if(AR.equals("C")|| AR.equals("U") || AR.equals("D")){ 	//Access rights are too high	
												counter_falsenegative++;
												demoteAR(newusers[i]);
												//System.out.println(i + "th user is demoted");
												if(AR.equals("R") || AR.equals("N")) {
													markedasmalicious[i] = 1;
													queueindex--;
												}
												//System.out.println(i + "is marked as malicious: " + j);
											}
											else{
												counter_truepositive++;		//Access rights are appropriate
											}
										}
										else{//sensing benign actions
											if(AR.equals("C")|| AR.equals("U") || AR.equals("D")){ 	//Access rights are too high	
												counter_falsenegative++;
												//System.out.println(i + "th user is demoted");
											}
											else{
												counter_truepositive++;		//Access rights are appropriate
											}
										}
											
									}
									else{		//benign user
										if(AR.equals("C")|| AR.equals("U") || AR.equals("D")){
											counter_truenegative++;
											bailey_avail++;
										}
										else
											counter_falsepositive++;
									}
								}
								else {	//all interaction is done
									queueindex--;
									status[i] = 0;
									settimeclock[i] = newusers[i].getInterval();
									
								}
							}
							else {	//decide to not interact 
								
							}
						}
						else {		//time interval is working
							settimeclock[i]--;
						}
				}
				else {		//marked as malicious
					counter_truepositive++;
				}
			}
			if(j % 100 == 0){
				System.out.println(j + "TP: " + (double)counter_truepositive/(counter_falsenegative+ counter_truepositive) + ": " + (double) bailey_avail / j);
				//System.out.println(j + "TN: " + (double)counter_truenegative/(counter_falsepositive+ counter_truenegative) + ": " + (double) bailey_avail / j);
				
			}
			//TrustManager.close();
			//Thread.sleep(10);
			
		}
		//System.out.println((double)bailey_avail / timeslot);
		System.out.println("\n========EOB==========\n");
		//end of bailey
		
		newusers = new User[targetuser]; 
		status = new int[targetuser];
		markedasmalicious = new int[targetuser];
		settimeclock = new int[targetuser];
		authenticated = new int[targetuser];
 		queueindex= 0;
		
		
		
		for(int i = 0; i < targetuser; i++){
			if(i > (1-maliciousrate) * targetuser)
				newusers[i] = new User(i, i/(targetuser/groupnum), 1, "U", baserate, maliciousinterval, maliciouslength);
			else
				newusers[i] = new User(i, i/(targetuser/groupnum), 0, "U", baserate, benigninterval, benignlength);
		}
		
		Collections.shuffle(Arrays.asList(newusers));
		
		TrustManager.connect(2);
		for(int j = 1; j < timeslot; j++){
			
			for(int i = 0; i < targetuser; i++){
				if(markedasmalicious[i] == 0) {		//not malicious
						if(settimeclock[i] == 0) {	//interaction interval is ready
							if(oRandom.nextFloat() < interactionprob || status[i] != 0) {		//reflects interaction probability			
								if(queueindex == queuesize && status[i] == 0) {	//queue is full and not interact with him
									continue;
								}
								else if(queueindex < queuesize && status[i] == 0) {		//queue is not full
									queueindex++;
									status[i] = j;
								}
			
								//System.out.println(newusers[i].getId() + "is in the queue!");
								if(newusers[i].getInteractionLength() > j - status[i]) {		//in the interaction time
									
									//double AR = calculateAR(newusers[i], users);//unknown user
									double AR = calculateAR(newusers[i], users);
									//newusers[i].putTrustValue(TrustManager.computeTrustValue("P1", String.valueOf(i), AR)); 
									
									newusers[i].putTrustValue(AR); //only reputation
									
									//System.out.println(i + "tries to interact!" + j + ":" + status[i]);
									if(newusers[i].getMaliciousness() == 1){ //malicious
										if(oRandom.nextFloat() < maliciousactingrate){		//sensing malicious actions
											if(newusers[i].getTrustValue() > trustthreshold){ 	//Access rights are too high	
												falsenegative++;
												
												//System.out.println(i + "th user is demoted");
											}
											else{
												//System.out.println(newusers[i].getId() + "is an attacker! " + AR );
												truepositive++;
												queueindex--;
												status[i] = 0;
												settimeclock[i] = newusers[i].getInterval();
												markedasmalicious[i] = 1;
												TARAS_avail++;
												//System.out.println(i + "'s trust value is too low: " + j);
											}
											TrustManager.putInteraction("P1",String.valueOf(i), Feedback.NEGATIVE, users[i].getTrustValue()); 
										}
										else{//sensing benign actions
											if(newusers[i].getTrustValue() > trustthreshold){ 	//Access rights are too high	
												falsenegative++;
												
												//System.out.println(i + "th user is demoted");
											}
											else{
												//System.out.println(i + "is an attacker2!" + AR);
												truepositive++;		//Access rights are appropriate
												queueindex--;
												status[i] = 0;
												settimeclock[i] = newusers[i].getInterval();
												TARAS_avail++;
												markedasmalicious[i] = 1;
												//System.out.println(i + "'s trust value is too low: " + j);
											}
											TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, users[i].getTrustValue()); 
										}
									}
									else{		//benign user
										if(newusers[i].getTrustValue() > trustthreshold){
											truenegative++;
											TARAS_avail++;
										}
										else {
											falsepositive++;
											TARAS_avail++;
										}
											
										TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, users[i].getTrustValue()); 
									}
								}
								else {	//all interaction is done
									queueindex--;
									status[i] = 0;
									settimeclock[i] = newusers[i].getInterval();
									
								}
							}
							else {	//decide to not interact 
								
							}
						}
						else {		//time interval is working
							settimeclock[i]--;
						}
				}
				else {		//marked as malicious
					truepositive++;
				}
			}
			if(j % 100 == 0){
				System.out.println(j + "TP: " + (double)truepositive/(falsenegative+ truepositive) + ": " + (double)TARAS_avail / j);
				//System.out.println(j + "TN: " + (double)truenegative/(falsepositive+ truenegative) + ": " + (double)TARAS_avail / j);
				
				//System.out.println(j + "NPV: " + (double)counter_truenegative/(counter_falsenegative+ counter_truenegative));
				//System.out.println(j + "ACC: " + (double)(counter_truepositive+counter_truenegative)/(counter_falsepositive+ counter_truepositive+counter_falsenegative+counter_truenegative));
			}
			
			//TrustManager.close();
			//Thread.sleep(10);
			
		}
		System.out.println("SEN: " + (double)truepositive/(falsenegative+ truepositive));
		System.out.println("DOS: " + (double)TARAS_avail / timeslot);
		
		TrustManager.reset();
		TrustManager.connect(1);
		TrustManager.reset();
		TrustManager.close();
		
		//TODO 반�?�? attack?�� �? ?��?��?���? ?��?��?��?��?�� 개념?�� 추�??��?��?�� ?��?���??
		//TODO magic numbers
		//TODO ?��???�� ???�� experience?? 그룹 멤버?��로�??�� �??��?�� 것과?�� 밸런?��?�� ?��?�� ?��?��.
		
		
		
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
		if(target.getMaliciousness() == 1)
			System.out.println(target.getId() + "'s risk = " + finalrisk);
		
		return finalrisk;
	}
	
	
	public static void demoteAR(User user){
		if(user.getAccessRights().equals("C")){
			user.putAccessRights("U");
		}
		else if(user.getAccessRights().equals("U")){
			user.putAccessRights("D");
		}
		else if(user.getAccessRights().equals("D")){
			user.putAccessRights("R");
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