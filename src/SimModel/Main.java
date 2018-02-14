package SimModel;



import TrustManager.TrustCalculator;
import TrustManager.TrustManager;
import kr.ac.kaist.server.commcentr.trust.history.Feedback;
import java.util.Random;



public class Main {
	
	
	
	static double maliciousrate = 0.2; 
	static double maliciousactingrate = 0.05;
	static int maliciousinterval = 1;
	static int benigninterval = 10;
	static int maliciouslength = 20;
	static int benignlength = 3;
	
	static int learninguser = 200;
	static int learninground = 1;
	static int targetuser = 100;
	static int timeslot = 500;
	static int groupnum = 10;
	static double trustthreshold = 0.5;
	static double interactionprob = 0.1;
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
		
		for(int i = 0; i < targetuser; i++){
			if(oRandom.nextFloat() < maliciousrate)
				newusers[i] = new User(i, i/(targetuser/groupnum), 1, "U", baserate, maliciousinterval, maliciouslength);
			else
				newusers[i] = new User(i, i/(targetuser/groupnum), 0, "U", baserate, benigninterval, benignlength);
		}
		
		
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
											if(AR.equals("C")|| AR.equals("U") || AR.equals("D") || AR.equals("R")){ 	//Access rights are too high	
												counter_falsenegative++;
												demoteAR(newusers[i]);
												//System.out.println(i + "th user is demoted");
												markedasmalicious[i] = 1;
												//System.out.println(i + "is marked as malicious: " + j);
												queueindex--;
											}
											else{
												counter_truepositive++;		//Access rights are appropriate
											}
										}
										else{//sensing benign actions
											if(AR.equals("C")|| AR.equals("U") || AR.equals("D") || AR.equals("R")){ 	//Access rights are too high	
												counter_falsenegative++;
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
				System.out.println(j + "SEN: " + (double)counter_truepositive/(counter_falsenegative+ counter_truepositive));
				//System.out.println(j + "NPV: " + (double)counter_truenegative/(counter_falsenegative+ counter_truenegative));
				//System.out.println(j + "ACC: " + (double)(counter_truepositive+counter_truenegative)/(counter_falsepositive+ counter_truepositive+counter_falsenegative+counter_truenegative));
			}
			//TrustManager.close();
			//Thread.sleep(10);
			
		}
		
		System.out.println("========EOB==========");
		//end of bailey
		
		
		
		for(int i = 0; i < targetuser; i++){
			if(oRandom.nextFloat() < maliciousrate)
				newusers[i] = new User(i, i/(targetuser/groupnum), 1, "U", baserate, maliciousinterval, maliciouslength);
			else
				newusers[i] = new User(i, i/(targetuser/groupnum), 0, "U", baserate, benigninterval, benignlength);
		}
		
		
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
								
								newusers[i].putTrustValue(TrustManager.computeTrustValue("P1", String.valueOf(i))); 
								
																
								//System.out.println(i + "is in the queue!");
								if(newusers[i].getInteractionLength() > j - status[i]) {		//in the interaction time
									
									//double AR = calculateAR(newusers[i], users);//unknown user
									
									double AR = newusers[i].getTrustValue();
									
									if(AR == baserate) {
										AR = calculateAR(newusers[i], users);
									}
								
									//System.out.println(i + "tries to interact!" + j + ":" + status[i]);
									if(newusers[i].getMaliciousness() == 1){ //malicious
										if(oRandom.nextFloat() < maliciousactingrate){		//sensing malicious actions
											if(AR > trustthreshold){ 	//Access rights are too high	
												counter_falsenegative++;
												
												//System.out.println(i + "th user is demoted");
											}
											else{
												counter_truepositive++;
												queueindex--;
												status[i] = 0;
												settimeclock[i] = newusers[i].getInterval();
												//System.out.println(i + "'s trust value is too low: " + j);
											}
											TrustManager.putInteraction("P1",String.valueOf(i), Feedback.NEGATIVE, users[i].getTrustValue()); 
										}
										else{//sensing benign actions
											if(AR > trustthreshold){ 	//Access rights are too high	
												counter_falsenegative++;
												//System.out.println(i + "th user is demoted");
											}
											else{
												counter_truepositive++;		//Access rights are appropriate
												queueindex--;
												status[i] = 0;
												settimeclock[i] = newusers[i].getInterval();
												//System.out.println(i + "'s trust value is too low: " + j);
											}
											TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, users[i].getTrustValue()); 
										}
									}
									else{		//benign user
										if(AR > trustthreshold){
											counter_truenegative++;
										}
										else
											counter_falsepositive++;
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
					counter_truepositive++;
				}
			}
			if(j % 100 == 0){
				System.out.println(j + "SEN: " + (double)counter_truepositive/(counter_falsenegative+ counter_truepositive));
				//System.out.println(j + "NPV: " + (double)counter_truenegative/(counter_falsenegative+ counter_truenegative));
				//System.out.println(j + "ACC: " + (double)(counter_truepositive+counter_truenegative)/(counter_falsepositive+ counter_truepositive+counter_falsenegative+counter_truenegative));
			}
			//TrustManager.close();
			//Thread.sleep(10);
			
		}
		
		
		TrustManager.reset();
		TrustManager.connect(1);
		TrustManager.reset();
		TrustManager.close();
		
		//TODO 諛섎�濡� attack�씠 彛� �뾾�뿀�쑝硫� �긽�뼢�떆�궎�뒗 媛쒕뀗�씠 異붽��릺�뼱�빞 �븯�뒗媛�?
		//TODO magic numbers
		//TODO �쑀���뿉 ���븳 experience�� 洹몃９ 硫ㅻ쾭�뱾濡쒕��꽣 媛��졇�삩 寃껉낵�쓽 諛몃윴�떛�씠 �쁽�옱 �뾾�쓬.
		
		
		
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
