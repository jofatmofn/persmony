package org.sakuram.persmony.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route(value="", layout=PersMonyLayout.class)
public class RootRedirectView extends Div implements BeforeEnterObserver {

	private static final long serialVersionUID = 12218453719913087L;

	@Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo(HomeView.class);
    }
}