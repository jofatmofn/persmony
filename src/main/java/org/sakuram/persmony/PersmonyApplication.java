package org.sakuram.persmony;

import org.sakuram.persmony.service.MiscService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages="org.sakuram.persmony.service")
public class PersmonyApplication {

	public PersmonyApplication(ApplicationContext applicationContext) {
		applicationContext.getBean(MiscService.class).loadCache();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(PersmonyApplication.class, args);
	}

}
