package org.sakuram.persmony.view;

import java.util.List;
import java.util.function.Supplier;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.StreamResource;

public class MultiDownloadButton extends Composite<Button> {

    private final Div hidden = new Div();

    public MultiDownloadButton(String caption,
                               Supplier<List<StreamResource>> resources) {

        getContent().setText(caption);
        hidden.getStyle().set("display", "none");
        getContent().getElement().appendChild(hidden.getElement());

        getContent().addClickListener(e -> {
            hidden.removeAll();

            for (StreamResource resource : resources.get()) {
                Anchor a = new Anchor(resource, caption);
                a.getElement().setAttribute("download", true);
                hidden.add(a);
                a.getElement().callJsFunction("click");
            }
        });
    }
}
