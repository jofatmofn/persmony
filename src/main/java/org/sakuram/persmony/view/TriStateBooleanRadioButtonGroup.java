package org.sakuram.persmony.view;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;

public class TriStateBooleanRadioButtonGroup extends CustomField<Boolean> {

	private static final long serialVersionUID = 3945643079355015951L;

	public enum TriState {
        BOTH, YES, NO
    }

    private final RadioButtonGroup<TriState> rbg = new RadioButtonGroup<>();

    public TriStateBooleanRadioButtonGroup() {

        rbg.setItems(TriState.BOTH, TriState.YES, TriState.NO);
        rbg.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        rbg.setItemLabelGenerator(item -> {
            switch (item) {
                case BOTH: return "Both";
                case YES:  return "Yes";
                case NO:   return "No";
                default:   return "";
            }
        });

        rbg.setValue(TriState.BOTH); // default

        // Propagate value changes to CustomField
        rbg.addValueChangeListener(e -> updateValue());

        // Wrap (optional, but clean for layout control)
        Div wrapper = new Div(rbg);
        wrapper.setWidthFull();

        add(wrapper);
    }

    // --- Core mapping logic ---

    @Override
    protected Boolean generateModelValue() {
        TriState value = rbg.getValue();
        if (value == null) return null;

        switch (value) {
            case BOTH: return null;
            case YES:  return Boolean.TRUE;
            case NO:   return Boolean.FALSE;
            default:   return null;
        }
    }

    @Override
    protected void setPresentationValue(Boolean value) {
        if (value == null) {
            rbg.setValue(TriState.BOTH);
        } else {
            rbg.setValue(value ? TriState.YES : TriState.NO);
        }
    }

    public void setLabel(String label) {
        rbg.setLabel(label);
    }

    public void setVertical(boolean vertical) {
        if (vertical) {
            rbg.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        } else {
            rbg.removeThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        }
    }
}
