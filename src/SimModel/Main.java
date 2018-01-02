package SimModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TrustManager.TrustCalculator;
import TrustManager.TrustManager;
import kr.ac.kaist.server.commcentr.trust.history.Feedback;
import java.util.Random;



public class Main {
	
	private static final transient Logger log = LoggerFactory.getLogger(Main.class.getName());
	
	static double highlymaliciousrate = 0.1;
	static double maliciousrate = 0.1; 
	static double maliciousactingrate = 0.1;
	
	static int learninguser = 200;
	static int learninground = 100;
	static int targetuser = 100;
	static int targetround = 200;
	static int groupnum = 10;
	
	static double baserate = 0.5;
	
	public static void main(String[] args) throws Exception {
		
		 Random oRandom = new Random();

		log.info("hello!");
		
		/*User[] users = new User[learninguser];
		
		for(int i = 0; i < learninguser; i++){	//I-sharing grouping
			if(i > (1-maliciousrate) * learninguser)
				users[i] = new User(i, i/(learninguser/groupnum), true, "d", 0.5);
			else
				users[i] = new User(i, i/(learninguser/groupnum), false, "d", 0.5);
		}
		
		
		
		for(int j = 0; j < learninground; j++){
			
			TrustManager.connect(1);
			
			for(int i = 0; i < learninguser; i++){
				if(oRandom.nextBoolean()){ //interaction possibility==0.5
					users[i].putTrustValue(TrustManager.computeTrustValue("P1", String.valueOf(i)));
					if(users[i].getMaliciousness() && oRandom.nextFloat() < maliciousactingrate)
						TrustManager.putInteraction("P1",String.valueOf(i), Feedback.NEGATIVE, users[i].getTrustValue());
					else
						TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, users[i].getTrustValue());	
				}
			}
			
			TrustManager.close();
			Thread.sleep(100);
			
		}
		*/
		User[] newusers = new User[targetuser]; 
		int falsepositive = 0;
		int falsenegative = 0;
		int correct = 0;
		
		int counter_falsepositive = 0;
		int counter_falsenegative = 0;
		int counter_correct = 0;
		
		
		for(int i = 0; i < targetuser; i++){
			if(i > (1-highlymaliciousrate) * targetuser)
				newusers[i] = new User(i, i/(targetuser/groupnum), 2, "U", baserate);
			else if(i > (1-(highlymaliciousrate + maliciousrate)) * targetuser)
				newusers[i] = new User(i, i/(targetuser/groupnum), 1, "U", baserate);
			else
				newusers[i] = new User(i, i/(targetuser/groupnum), 0, "U", baserate);
		}
		
		
		for(int j = 0; j < targetround; j++){
			
			TrustManager.connect(2);
			
			for(int i = 0; i < targetuser; i++){
				if(i > (1-highlymaliciousrate) * targetuser){ //highlymalicious
					if(oRandom.nextFloat() < maliciousactingrate*3){		//sensing malicious actions
						if(newusers[i].getAccessRights() == "U" || newusers[i].getAccessRights() == "D" || newusers[i].getAccessRights() == "R"){ 	//Access rights are too high	
							counter_falsepositive++;
							demoteAR(newusers[i]);
							//System.out.println(i + "th user is demoted");
						}
						else{
							counter_correct++;		//Access rights are appropiriate
						}
					}
					else//sensing benign actions
						counter_falsepositive++;
				}
				else if(i > (1-(highlymaliciousrate + maliciousrate)) * targetuser){ //malicious
					if(oRandom.nextFloat() < maliciousactingrate){		//sensing malicious actions
						if(newusers[i].getAccessRights() == "U" || newusers[i].getAccessRights() == "D" ||newusers[i].getAccessRights() == "R"){ 	//Access rights are too high	
							counter_falsepositive++;
							demoteAR(newusers[i]);
							//System.out.println(i + "th user is demoted");
						}
						else{
							counter_correct++;		//Access rights are appropiriate
						}
					}
					else//sensing benign actions
						counter_falsepositive++;
				}
				else{		//benign user
					counter_correct++;
				}
			}
			if(j % 10 == 0){
				log.info(j + ": " + (double)counter_falsepositive/(counter_falsepositive + counter_correct));
			}
			TrustManager.close();
			//Thread.sleep(10);
			
		}
		
		
		
		
		
		//TODO 반대로 attack이 쭉 없었으면 상향시키는 개념이 추가되어야 하는가?
		//TODO maliciousness 안에 레벨을 나눠야 되지 않을까..
		//TODO 유저에 대한 experience와 그룹 멤버들로부터 가져온 것과의 밸런싱이 현재 없음.
		//TODO MAPE-K는 syntixi를 기반으로 해야겠지만..
		
		
		
		log.info("done!");
		System.exit(0);
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
			log.error("unknown Access rights in demoteAR");
		}
	}

}
