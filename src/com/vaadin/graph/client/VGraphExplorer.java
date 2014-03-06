/*
 * Copyright 2011-2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License. 
 */
package com.vaadin.graph.client;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.VectorObject;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class VGraphExplorer extends Composite {

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-mycomponent"; //TODO style

    private final Panel root = new AbsolutePanel();
    protected final DrawingArea canvas = new DrawingArea(0, 0);
    private final GraphProxy graph = new GraphProxy();

    public VGraphExplorer() {
        initWidget(root);
        RootPanel.getBodyElement().getStyle().setBackgroundColor("green"); //TODO style
        Style canvasStyle = canvas.getElement().getStyle();
        canvasStyle.setPosition(Position.ABSOLUTE);
        canvasStyle.setBackgroundColor("white"); //TODO style
        root.add(canvas);
        root.getElement().getStyle().setPosition(Position.ABSOLUTE);
        setStyleName(CLASSNAME);
    }

    void add(HTML widget) {
        root.add(widget);
    }

    void add(VectorObject widget) {
        canvas.add(widget);
    }

    GraphProxy getGraph() {
        return graph;
    }

    public void remove(HTML widget) {
        widget.removeFromParent();
    }

    public void remove(VectorObject widget) {
        canvas.remove(widget);
    }
}
