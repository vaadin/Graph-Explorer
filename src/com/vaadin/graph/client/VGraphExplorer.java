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

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;

public class VGraphExplorer extends Composite {

    /** Set the CSS class name to allow styling. */
    public static final String CSS_CLASSNAME = "v-graph-explorer";
    private static final String STYLE_CANVAS = "canvas"; 
    
    private final Panel root = new AbsolutePanel();
    protected final DrawingArea canvas = new DrawingArea(0, 0);

    public VGraphExplorer() {
        initWidget(root);
        canvas.getElement().getStyle().setPosition(Position.ABSOLUTE);
        root.add(canvas);
        root.getElement().getStyle().setPosition(Position.ABSOLUTE);
        canvas.setStyleName(STYLE_CANVAS);
        setStyleName(CSS_CLASSNAME);
    }

    void add(HTML widget) {
        root.add(widget);
    }

    void add(VectorObject widget) {
        canvas.add(widget);
    }

    public void remove(HTML widget) {
        widget.removeFromParent();
    }

    public void remove(VectorObject widget) {
        canvas.remove(widget);
    }
}
