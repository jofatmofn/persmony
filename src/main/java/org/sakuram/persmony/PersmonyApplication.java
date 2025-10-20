package org.sakuram.persmony;

import org.sakuram.persmony.service.MiscService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;

@Push
@SpringBootApplication(scanBasePackages = {"org.sakuram.persmony.service", "org.sakuram.persmony.view"})
public class PersmonyApplication implements AppShellConfigurator {

	private static final long serialVersionUID = 1L;

	public PersmonyApplication(ApplicationContext applicationContext) {
		applicationContext.getBean(MiscService.class).loadCache();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(PersmonyApplication.class, args);
	}

}
