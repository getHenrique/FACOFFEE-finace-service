package facom.ufms.ScoreAPIProxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScoreAPIProxyApplication {

    public static void main(String[] args) {
        
        SpringApplication.run(ScoreAPIProxyApplication.class, args);
        ScoreClient client = new ControlledClient( new CachedClient(new APIClient()));

        for(int i = 0; i < 5; i++){

            System.out.println(client.score("123.456.789-10"));

        }

    }

}
