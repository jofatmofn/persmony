package org.sakuram.persmony.view;

import java.util.concurrent.atomic.AtomicReference;

import org.sakuram.persmony.valueobject.InvestmentTransaction2VO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
@Scope("prototype")
public class DTInvestmentTransactionDetailComponent {

	InvestmentDetailComponent investmentDetailComponent;

	FormLayout formLayout;
	NativeLabel transactionTypeLabel, dueAmountLabel, statusLabel, investorLabel, productProviderLabel, productTypeLabel;
	Button investmentIdButton;
	AtomicReference<Long> investmentTransactionIdAr;
	
	public DTInvestmentTransactionDetailComponent(InvestmentDetailComponent investmentDetailComponent) {
		this.investmentDetailComponent = investmentDetailComponent;
		investmentTransactionIdAr = new AtomicReference<Long>(0L);
	}
	
	public FormLayout template() {
		FormItem formItem;
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 3));
		
		investmentIdButton = new Button("");
		investmentIdButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
		investmentIdButton.addClickListener(e -> {
			investmentDetailComponent.showDetail(investmentTransactionIdAr.get());
		});
		formLayout.addFormItem(investmentIdButton, "Investment Id");
		investorLabel = new NativeLabel("");
		formLayout.addFormItem(investorLabel, "Investor");
		dueAmountLabel = new NativeLabel("");
		formLayout.addFormItem(dueAmountLabel, "Due Amount");

		productTypeLabel = new NativeLabel("");
		formItem = formLayout.addFormItem(productTypeLabel, "Product Type");
		formLayout.setColspan(formItem, 2);
		transactionTypeLabel = new NativeLabel("");
		formLayout.addFormItem(transactionTypeLabel, "Transaction Type");
		
		productProviderLabel = new NativeLabel("");
		formItem = formLayout.addFormItem(productProviderLabel, "Product Provider");
		formLayout.setColspan(formItem, 2);
		statusLabel = new NativeLabel("");
		formLayout.addFormItem(statusLabel, "Status");
		
		return formLayout;
	}
	
	public FormLayout showDetail(InvestmentTransaction2VO investmentTransaction2VO) {
		investmentTransactionIdAr.set(investmentTransaction2VO.getInvestmentId());
		investmentIdButton.setText(String.valueOf(investmentTransaction2VO.getInvestmentId()));
		investorLabel.setText(investmentTransaction2VO.getInvestor());
		dueAmountLabel.setText(investmentTransaction2VO.getDueAmount() == null ? " " : investmentTransaction2VO.getDueAmount().toString());
		productTypeLabel.setText(investmentTransaction2VO.getProductType());
		transactionTypeLabel.setText(investmentTransaction2VO.getTransactionType());
		statusLabel.setText(investmentTransaction2VO.getStatus());
		productProviderLabel.setText(investmentTransaction2VO.getProductProvider());
		return formLayout;
	}
	
}
