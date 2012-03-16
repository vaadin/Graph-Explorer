package com.vaadin.graph;

import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

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