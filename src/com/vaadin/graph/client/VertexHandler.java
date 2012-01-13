/*
 * Copyright 2011 Vaadin Ltd.
 *
 * Licensed under the GNU Affero General Public License, Version 3.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/agpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.graph.client;

import java.util.Collection;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;

/**
 * Presenter/controller for a vertex in a graph.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
class VertexHandler implements MouseDownHandler, MouseMoveHandler,
        MouseUpHandler, Handler {
    final VGraphExplorer parent;
    final HTML widget;
    int dragStartX;
    int dragStartY;
    protected boolean mouseDown;
    private final ClientVertex node;

    VertexHandler(VGraphExplorer parent, ClientVertex node) {
        this.parent = parent;
        this.node = node;
        widget = new HTML();
        widget.setTitle(node.getId());
        parent.add(widget);

        Style style = widget.getElement().getStyle();
        style.setLeft(node.getX(), Unit.PX);
        style.setTop(node.getY(), Unit.PX);

        node.setObserver(this);
        widget.addDomHandler(this, MouseDownEvent.getType());
        widget.addDomHandler(this, MouseMoveEvent.getType());
        widget.addDomHandler(this, MouseUpEvent.getType());
    }

    public void onMouseDown(MouseDownEvent event) {
        mouseDown = true;
        updateCSS();
        DOM.setCapture(widget.getElement());
        dragStartX = event.getX();
        dragStartY = event.getY();
        event.preventDefault();
    }

    public void onMouseMove(MouseMoveEvent event) {
        if (mouseDown) {
            node.setDragging(true);
            updateCSS();
            node.setX(event.getX() + node.getX() - dragStartX);
            node.setY(event.getY() + node.getY() - dragStartY);
            update();
            int clientX = event.getClientX();
            int clientY = event.getClientY();
            if (clientX < 0 || clientY < 0 || clientX > Window.getClientWidth()
                    || clientY > Window.getClientHeight()) {
                parent.save(node, true);
                node.setDragging(false);
            }
        }
        event.preventDefault();
    }

    public void onMouseUp(MouseUpEvent event) {
        Element element = widget.getElement();
        if (!node.isDragging()) {
            updateCSS();
            reposition();
            if (ClientVertex.EXPANDED.equals(node.getState())) {
                parent.collapse(node);
            }
            parent.toggle(node);
        } else {
            parent.save(node, true);
            node.setDragging(false);
        }
        mouseDown = false;
        DOM.releaseCapture(element);
        event.preventDefault();
    }

    public void remove() {
        node.setObserver(null);
        widget.removeFromParent();
        VIndexedGraph graph = parent.getGraph();
        remove(graph.getInEdges(node));
        remove(graph.getOutEdges(node));
        graph.removeVertex(node.getId());
    }

    private void reposition() {
        Element element = widget.getElement();
        Style style = widget.getElement().getStyle();

        int width = element.getOffsetWidth();
        node.setWidth(width);
        int halfwidth = width / 2;
        int left = limit(0, node.getX() - halfwidth, parent.getOffsetWidth()
                - width);
        node.setX(left + halfwidth);
        style.setLeft(left, Unit.PX);

        int height = element.getOffsetHeight();
        node.setHeight(height);
        int halfHeight = height / 2;
        int top = limit(0, node.getY() - halfHeight, parent.getOffsetHeight()
                - height);
        node.setY(top + halfHeight);
        style.setTop(top, Unit.PX);
    }

    public void update() {
        widget.setHTML("<div class='label'>" + node.getLabel() + "</div>");
        reposition();
        updateCSS();
        updateRelationships();
    }

    private void updateCSS() {
        widget.getElement().setClassName(
                "node " + node.getState() + ' ' + node.getKind()
                        + (mouseDown ? ' ' + "down" : ""));
    }

    void updateRelationships() {
        VIndexedGraph graph = parent.getGraph();
        update(graph.getInEdges(node));
        update(graph.getOutEdges(node));
    }

    /** Limits value to [min, max], so that min <= value <= max. */
    private static int limit(int min, int value, int max) {
        return Math.min(Math.max(min, value), max);
    }

    private static void remove(Collection<ClientEdge> edges) {
        if (edges != null) {
            for (ClientEdge edge : edges) {
                edge.notifyRemove();
            }
        }
    }

    private static void update(Collection<ClientEdge> edges) {
        if (edges != null) {
            for (ClientEdge edge : edges) {
                edge.notifyUpdate();
            }
        }
    }
}
