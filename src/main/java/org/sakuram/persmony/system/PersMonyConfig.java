package org.sakuram.persmony.system;

import org.sakuram.persmony.util.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;

@Configuration
public class PersMonyConfig {
	@Bean
	public DatePickerI18n isoDatePickerI18n() {
		DatePickerI18n i18n = new DatePickerI18n();
		i18n.setDateFormat(Constants.ISO_LOCAL_DATE);
		return i18n;
	}
}
