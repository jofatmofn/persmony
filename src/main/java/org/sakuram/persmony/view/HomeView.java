package org.sakuram.persmony.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value="home", layout=PersMonyLayout.class)
@PageTitle("Home")
public class HomeView extends Div {

	private static final long serialVersionUID = 1443163123285038924L;

	public HomeView() {
		add(new H3("Welcome"));
	}
}
