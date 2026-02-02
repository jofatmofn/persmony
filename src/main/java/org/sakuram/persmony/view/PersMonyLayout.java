package org.sakuram.persmony.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;

@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
public class PersMonyLayout extends AppLayout {
	private static final long serialVersionUID = 1L;

    public PersMonyLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("PersMony");
        logo.getStyle()
            .set("margin", "0")
            .set("font-size", "var(--lumo-font-size-l)");

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo
        );
        header.setDefaultVerticalComponentAlignment(
                FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassName("py-s");

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink searchRL =
                new RouterLink("Investments Search", SearchView.class);
        RouterLink operationRL =
                new RouterLink("Investments Operations", OperationView.class);
        RouterLink reportRL =
                new RouterLink("Reports", ReportView.class);
        RouterLink sbAcTxnOperationRL =
                new RouterLink("Savings Account Transactions", SbAcTxnOperationView.class);
        RouterLink debtEquityMutualRL =
                new RouterLink("Debt Equity MF", DebtEquityMutualView.class);

        VerticalLayout menu = new VerticalLayout(
                searchRL, operationRL, reportRL, sbAcTxnOperationRL, debtEquityMutualRL);
        menu.setPadding(false);
        menu.setSpacing(false);

        addToDrawer(menu);
    }
}

