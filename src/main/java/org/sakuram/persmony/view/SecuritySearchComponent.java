package org.sakuram.persmony.view;

import java.util.List;
import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinCriteriaVO;
import org.sakuram.persmony.valueobject.IsinVO;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter(AccessLevel.NONE)
public class SecuritySearchComponent extends CustomField<String> {
	private static final long serialVersionUID = 1L;
	
	private HorizontalLayout layout;
	private TextField isinTextField;
	@Getter(AccessLevel.NONE)
	private Button searchButton;
	
	public SecuritySearchComponent(DebtEquityMutualService debtEquityMutualService, MiscService miscService) {
		
		layout = new HorizontalLayout();
		isinTextField = new TextField();
		layout.add(isinTextField);
		isinTextField.addValueChangeListener(e -> setModelValue(e.getValue(), true));
		
		searchButton = new Button("Search");
		layout.add(searchButton);
		searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchButton.setDisableOnClick(true);
		// On click of Fetch
		searchButton.addClickListener(event -> {
			Dialog dialog;
			Button closeButton;
			VerticalLayout verticalLayout;
			TextField isinTextField, companyNameTextField, securityNameTextField;
			Select<IdValueVO> isinOperatorSelect, companyNameOperatorSelect, securityNameOperatorSelect;
			Select<IdValueVO> securityTypeDvSelect;
			HorizontalLayout hLayout;
			Button fetchButton;
			Grid<IsinVO> isinsGrid;
			
			try {
				dialog = new Dialog();
				dialog.setHeaderTitle("Security Search");
				closeButton = new Button(new Icon("lumo", "cross"),
				        (e) -> {
				        	dialog.close();
				        });
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				dialog.getHeader().add(closeButton);
				verticalLayout = new VerticalLayout();
				verticalLayout.getStyle().set("width", "75rem");
				dialog.add(verticalLayout);
				
				isinOperatorSelect = ViewFuncs.newSelect(FieldSpecVO.getTxtOperatorList(), "Operator", true, false);
				isinTextField = new TextField("Value");
				hLayout = new HorizontalLayout();
				verticalLayout.add(new FormLayout().addFormItem(hLayout, "ISIN"));
				hLayout.add(isinOperatorSelect, isinTextField);
				isinOperatorSelect.addValueChangeListener(valueChangEevent -> {
					if (isinOperatorSelect.getValue() == null) {
						isinTextField.setValue("");
					}
				});
				
				companyNameOperatorSelect = ViewFuncs.newSelect(FieldSpecVO.getTxtOperatorList(), "Operator", true, false);
				companyNameTextField = new TextField("Value");
				hLayout = new HorizontalLayout();
				verticalLayout.add(new FormLayout().addFormItem(hLayout, "Company Name"));
				hLayout.add(companyNameOperatorSelect, companyNameTextField);
				companyNameOperatorSelect.addValueChangeListener(valueChangEevent -> {
					if (companyNameOperatorSelect.getValue() == null) {
						companyNameTextField.setValue("");
					}
				});
				
				securityNameOperatorSelect = ViewFuncs.newSelect(FieldSpecVO.getTxtOperatorList(), "Operator", true, false);
				securityNameTextField = new TextField("Value");
				hLayout = new HorizontalLayout();
				verticalLayout.add(new FormLayout().addFormItem(hLayout, "Security Name"));
				hLayout.add(securityNameOperatorSelect, securityNameTextField);
				securityNameOperatorSelect.addValueChangeListener(valueChangEevent -> {
					if (securityNameOperatorSelect.getValue() == null) {
						securityNameTextField.setValue("");
					}
				});
				
				securityTypeDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_SECURITY_TYPE, null, true, false);
				verticalLayout.add(new FormLayout().addFormItem(securityTypeDvSelect, "Security Type"));
				
				fetchButton = new Button("Fetch");
				verticalLayout.add(fetchButton);
				fetchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
				fetchButton.setDisableOnClick(true);
				
				isinsGrid = new Grid<>(IsinVO.class);
				isinsGrid.setColumns("isin", "companyName", "securityName", "securityType");
				for (Column<IsinVO> column : isinsGrid.getColumns()) {
					column.setResizable(true);
				}
				verticalLayout.add(isinsGrid);
				
				// On click of Fetch
				fetchButton.addClickListener(clickEvent -> {
					IsinCriteriaVO isinCriteriaVO;
					List<IsinVO> isinVOList = null;

					try {
						// Validation
						if (!isinTextField.isEmpty() && isinOperatorSelect.getValue() == null) {
							ViewFuncs.showError("ISIN: Non-matching Operator and Value");
							return;
						}
						if (isinTextField.isEmpty() && isinOperatorSelect.getValue() != null) {
							ViewFuncs.showError("Specify Value for ISIN");
							return;
						}
						if (!companyNameTextField.isEmpty() && companyNameOperatorSelect.getValue() == null) {
							ViewFuncs.showError("Company Name: Non-matching Operator and Value");
							return;
						}
						if (companyNameTextField.isEmpty() && companyNameOperatorSelect.getValue() != null) {
							ViewFuncs.showError("Specify Value for Company Name");
							return;
						}
						if (!securityNameTextField.isEmpty() && securityNameOperatorSelect.getValue() == null) {
							ViewFuncs.showError("Security Name: Non-matching Operator and Value");
							return;
						}
						if (securityNameTextField.isEmpty() && securityNameOperatorSelect.getValue() != null) {
							ViewFuncs.showError("Specify Value for Security Name");
							return;
						}
						
						// Back-end Call
						isinCriteriaVO = new IsinCriteriaVO(
								isinTextField.getValue().equals("") ? null : isinTextField.getValue(),
								isinOperatorSelect.getValue() == null ? null : isinOperatorSelect.getValue().getValue(),
								companyNameTextField.getValue().equals("") ? null : companyNameTextField.getValue(),
								companyNameOperatorSelect.getValue() == null ? null : companyNameOperatorSelect.getValue().getValue(),
								securityNameTextField.getValue().equals("") ? null : securityNameTextField.getValue(),
								securityNameOperatorSelect.getValue() == null ? null : securityNameOperatorSelect.getValue().getValue(),
								securityTypeDvSelect.getValue() == null ? null : securityTypeDvSelect.getValue().getId()
								);
						try {
							isinVOList = debtEquityMutualService.searchSecurities(isinCriteriaVO);
							Notification.show("No. of ISINs fetched: " + isinVOList.size())
								.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
							isinsGrid.setItems(isinVOList);
						} catch (Exception e) {
							ViewFuncs.showError(UtilFuncs.messageFromException(e));
							return;
						}
					} finally {
						fetchButton.setEnabled(true);
					}
				});
				
				isinsGrid.addItemDoubleClickListener(dcEvent -> {
					this.isinTextField.setValue(dcEvent.getItem().getIsin());
		        	dialog.close();
				});
				dialog.open();
					
			} finally {
				searchButton.setEnabled(true);
			}
				
		});
	}

	@Override
	protected String generateModelValue() {
		return isinTextField.getValue();
	}

	@Override
	protected void setPresentationValue(String newPresentationValue) {
		isinTextField.setValue(newPresentationValue == null ? "" : newPresentationValue);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		isinTextField.setEnabled(enabled);
		searchButton.setEnabled(enabled);
	}
}
