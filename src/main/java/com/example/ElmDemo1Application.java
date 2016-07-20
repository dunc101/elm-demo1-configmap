package com.example;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ipacc.enterprise.elm.logging.InfinityLogger;
import com.ipacc.enterprise.elm.logging.InfinityLoggerFactory;
import com.ipacc.enterprise.elm.logging.InfinityTransaction;

@SpringBootApplication
@RestController
public class ElmDemo1Application {
	private InfinityLogger logger = InfinityLoggerFactory.getInstance();
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private Policy policy;
	
	@Autowired
	private User user;
		
	@RequestMapping("/test")
	public String ping() {
		return "pong " + policy.getPolicyNumber();
	}
	
	@RequestMapping("/validateUser")
	// this will look at the user object from the application.yml OR what is injected from the environment
	// this is just to show an example of OpenShift Secrets.  Obviously in a real world example we would
	// actually send the username and password as a parameter...
	public boolean validateUser() throws Exception {
		// Hard code an example user/pass that we excpect
		if (!user.getUsername().equalsIgnoreCase("infinity")) {
			throw new Exception("The user " + user.getUsername() + " is not valid!");
		}
		if (!user.getPassword().equals("1234567890")) {
			throw new Exception("The password specified is not valid!");
		}
		return true;
	}
	
	@Bean
	public RestTemplate resTemplate() {
		return new RestTemplate();
	}
	
	@RequestMapping("/begin")
	public String getExampleConsulConfigProperty() {
			String gtid = UUID.randomUUID().toString();
		    MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		    params.add("globalTransId", gtid);
		    params.add("clientId", "elm-demo1-discovery");
		    System.out.println("Transaction part 1 => Global Trans ID is " + gtid + " clientId starting transaction => elm-demo1");
		    String result = "";
			InfinityTransaction trans = null;
			try {
				trans = new InfinityTransaction.Builder()
					.setGlobalTransactionId(gtid)
					.setTransactionType("TransactionExample")
					.build();
				logger.startELMTransactionWithContext(trans);
				logger.info("message");
				Thread.sleep((long)(Math.random() * 1000));
				// we can get to elm_demo2 either from the injected environment variables or from the exposed Service that was created.
				String hostname = System.getenv("ELM_DEMO2_SERVICE_HOST");
				String port = System.getenv("ELM_DEMO2_SERVICE_PORT");
			    result = restTemplate.postForObject( "http://" + hostname + ":" + port + "/orchestration", params, String.class);
				//result = restTemplate.postForObject( "http://elm-demo2-configmap.10.19.3.57.xip.io/orchestration", params, String.class);
			    if (result.contains("Exception")) {
			    	trans.setResolutionFlag(false);
			    	logger.error(result);
			    } else {
					trans.setResolutionFlag(true);
			    }
			} catch (Exception e) {
				trans.setResolutionFlag(false);
				result = e.getMessage();
				logger.error(e.getMessage());
			}
			finally {
				logger.endELMTransactionWithContext(trans);
			}

		    return result;
	}

	@RequestMapping("/loop")
	public String loop() {
		int i = 0;
		while (true) {
			logger.info("test");
			i++;
			try {
				String result = getExampleConsulConfigProperty();
				System.out.println("Excecuted: " + i + " times => result: " + result);
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(ElmDemo1Application.class, args);
		ElmDemo1Application app = new ElmDemo1Application();
	}
}


