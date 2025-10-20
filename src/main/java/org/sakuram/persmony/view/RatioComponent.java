package org.sakuram.persmony.view;

import org.javatuples.Pair;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter(AccessLevel.NONE)
public class RatioComponent extends CustomField<Pair<Short, Short>> {
	private static final long serialVersionUID = 1L;
	
	private HorizontalLayout layout;
	@Getter(AccessLevel.NONE)
	private IntegerField newValueIntegerField, oldValueIntegerField;

	public RatioComponent() {
		layout = new HorizontalLayout();
		newValueIntegerField = new IntegerField();
		newValueIntegerField.setMin(1);
		newValueIntegerField.setMax(100);
		newValueIntegerField.setValue(1);
		oldValueIntegerField = new IntegerField();
		oldValueIntegerField.setMin(1);
		oldValueIntegerField.setMax(100);
		oldValueIntegerField.setValue(1);
		layout.add(newValueIntegerField);
		layout.add(new Label(":"));
		layout.add(oldValueIntegerField);
		newValueIntegerField.addValueChangeListener(e -> {
			if (newValueIntegerField.getValue() == null) {
				newValueIntegerField.setValue(1);
			}
			setModelValue(Pair.with(e.getValue().shortValue(), oldValueIntegerField.getValue().shortValue()), true);
		});
		oldValueIntegerField.addValueChangeListener(e -> {
			if (oldValueIntegerField.getValue() == null) {
				oldValueIntegerField.setValue(1);
			}
			setModelValue(Pair.with(newValueIntegerField.getValue().shortValue(), e.getValue().shortValue()), true);
		});
		
	}

	@Override
	protected Pair<Short, Short> generateModelValue() {
		if (newValueIntegerField.getValue() == 1 && oldValueIntegerField.getValue() == 1) {
			return null;
		} else {
			return Pair.with(newValueIntegerField.getValue().shortValue(), oldValueIntegerField.getValue().shortValue());
		}
	}

	@Override
	protected void setPresentationValue(Pair<Short, Short> newPresentationValue) {
		if (newPresentationValue == null) {
			newValueIntegerField.setValue(1);
			oldValueIntegerField.setValue(1);
		} else {
			newValueIntegerField.setValue(newPresentationValue.getValue0().intValue());
			oldValueIntegerField.setValue(newPresentationValue.getValue1().intValue());
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		newValueIntegerField.setEnabled(enabled);
		oldValueIntegerField.setEnabled(enabled);
	}
}
