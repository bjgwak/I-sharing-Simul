package SimModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TrustManager.TrustCalculator;
import TrustManager.TrustManager;
import kr.ac.kaist.server.commcentr.trust.history.Feedback;
import java.util.Random;



public class Main {
	
	private static final transient Logger log = LoggerFactory.getLogger(Main.class.getName());
	
	static double maliciousrate = 0.2; 
	static double maliciousactingrate = 0.1;
	static int learninguser = 200;
	static int learninground = 100;
	static int targetuser = 100;
	static int targetround = 1;
	static int groupnum = 10;
	
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
			if(i > (1-maliciousrate) * targetuser)
				newusers[i] = new User(i, i/(targetuser/groupnum), true, "d", 0.5);
			else
				newusers[i] = new User(i, i/(targetuser/groupnum), false, "d", 0.5);
		}
		
		for(int j = 0; j < targetround; j++){
			
			TrustManager.connect(2);
			
			for(int i = 0; i < targetuser; i++){
				
				newusers[i].putTrustValue(TrustManager.computeTrustValue("P1", String.valueOf(i)));
				if(newusers[i].getTrustValue() >= 0.5 && newusers[i].getMaliciousness()){
					falsepositive++;
				}
				else if(newusers[i].getTrustValue() < 0.5 && !newusers[i].getMaliciousness()){
					falsenegative++;
				}
				else
					correct++;
				
				if(newusers[i].getMaliciousness() && oRandom.nextFloat() < maliciousactingrate){
					TrustManager.putInteraction("P1",String.valueOf(i), Feedback.NEGATIVE, newusers[i].getTrustValue());
					counter_correct++;
					}
				else if(newusers[i].getMaliciousness()){
					TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, newusers[i].getTrustValue());
					counter_falsepositive++;
				}
				else if(!newusers[i].getMaliciousness()){
					TrustManager.putInteraction("P1",String.valueOf(i), Feedback.POSITIVE, newusers[i].getTrustValue());
					counter_correct++;
				}
			}
			
			log.info(counter_falsepositive + ":" + counter_correct);
			log.info(falsepositive + ":" + correct);
			
			TrustManager.close();
			Thread.sleep(100);
			
		}
		
		
		//TODO attackrate를 기반으로 어택을 했으면 권한을 하나하나 줄이는 방식. (CUDRN)
		//TODO 반대로 attack이 쭉 없었으면 상향시키는 개념이 추가되어야 하는가?
		//TODO maliciousness 안에 레벨을 나눠야 되지 않을까..
		//TODO 유저에 대한 experience와 그룹 멤버들로부터 가져온 것과의 밸런싱이 현재 없음.
		//TODO MAPE-K는 syntixi를 기반으로 해야겠지만..
		
		
		
		log.info("done!");
		System.exit(0);
	}

}
