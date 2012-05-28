package com.vaadin.graph;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.*;

final class CancelHandler implements ClickListener {
    private static final long serialVersionUID = 1L;

    private final Window dialog;

    CancelHandler(Window dialog) {
        this.dialog = dialog;
    }

    public void buttonClick(ClickEvent event) {
        dialog.getParent().removeWindow(dialog);
    }
}