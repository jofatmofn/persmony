package org.sakuram.persmony.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;

import java.io.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GridCsvDownloaderComponent<T> extends HorizontalLayout {

	private static final long serialVersionUID = -1656762337639025866L;
	
	private final Grid<T> grid;
    private final String fileName;
    private final Function<T, String> rowMapper;
    private final Supplier<String> headerSupplier;

    public GridCsvDownloaderComponent(Grid<T> grid,
                             String fileName,
                             Supplier<String> headerSupplier,
                             Function<T, String> rowMapper) {

        this.grid = grid;
        this.fileName = fileName;
        this.rowMapper = rowMapper;
        this.headerSupplier = headerSupplier;

        Button downloadBtn = new Button("Download CSV");
        add(downloadBtn);

        downloadBtn.addClickListener(e -> triggerDownload());
    }

    private void triggerDownload() {

        List<T> snapshot = grid.getGenericDataView()
                               .getItems()
                               .toList();

        StreamResource resource = new StreamResource(fileName, () -> {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try (PrintWriter writer = new PrintWriter(baos)) {

                if (headerSupplier != null) {
                    writer.println(headerSupplier.get());
                }

                for (T item : snapshot) {
                    writer.println(rowMapper.apply(item));
                }
            }

            return new ByteArrayInputStream(baos.toByteArray());
        });

        resource.setContentType("text/csv");

        Anchor download = new Anchor(resource, "");
        download.getElement().setAttribute("download", true);

        add(download);
        download.getElement().callJsFunction("click");
        // remove(download);
    }
}