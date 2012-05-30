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

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.*;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * Presenter/controller for a node in a graph.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
class NodeController implements MouseDownHandler, MouseMoveHandler,
        MouseUpHandler, Controller {
    final VGraphExplorer parent;
    final HTML view;
    int dragStartX;
    int dragStartY;
    protected boolean mouseDown;
    private final NodeProxy node;
    private GraphProxy graph;
    private NodeAnimation animation = new NodeAnimation();

    NodeController(VGraphExplorer parent, GraphProxy graph, NodeProxy node,
            HTML view) {
        this.parent = parent;
        this.node = node;
        this.view = view;
        this.graph = graph;

        view.setTitle(node.getId());
        Style style = view.getElement().getStyle();
        style.setLeft(node.getX(), Unit.PX);
        style.setTop(node.getY(), Unit.PX);
        view.addDomHandler(this, MouseDownEvent.getType());
        view.addDomHandler(this, MouseMoveEvent.getType());
        view.addDomHandler(this, MouseUpEvent.getType());
    }

    public void onMouseDown(MouseDownEvent event) {
        mouseDown = true;
        updateCSS();
        DOM.setCapture(view.getElement());
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
            onUpdateInModel();
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
        Element element = view.getElement();
        if (!node.isDragging()) {
            updateCSS();
            reposition();
            if (NodeProxy.EXPANDED.equals(node.getState())) {
                node.setState(NodeProxy.COLLAPSED);
                for (NodeProxy neighbor : graph.getNeighbors(node)) {
                    if (NodeProxy.COLLAPSED.equals(neighbor.getState())
                            && graph.degree(neighbor) == 1) {
                        graph.removeNode(neighbor);
                    }
                }
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

    public void onRemoveFromModel() {

        VConsole.log("NodeController.onRemoveFromModel()");

        node.setController(null);
        view.removeFromParent();
    }

    private void reposition() {
        Element element = view.getElement();
        Style style = view.getElement().getStyle();

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

    public void onUpdateInModel() {
        view.setHTML("<div class='label'>" + node.getContent() + "</div>");
        reposition();
        updateCSS();
        updateArcs();
    }

    private void updateCSS() {
        view.getElement().setClassName(
                "node " + node.getState() + ' ' + node.getKind()
                        + (mouseDown ? ' ' + "down" : ""));
    }

    void updateArcs() {
        update(graph.getInArcs(node));
        update(graph.getOutArcs(node));
    }

    /** Limits value to [min, max], so that min <= value <= max. */
    private static int limit(int min, int value, int max) {
        return Math.min(Math.max(min, value), max);
    }

    private static void update(Collection<ArcProxy> arcs) {
        if (arcs != null) {
            for (ArcProxy arc : arcs) {
                arc.notifyUpdate();
            }
        }
    }

    public void move(int x, int y) {
        animation.targetX = x;
        animation.targetY = y;
        animation.run(500);
    }

    private class NodeAnimation extends Animation {
        int targetX = 0;
        int targetY = 0;

        @Override
        protected void onUpdate(double progress) {
            if (progress > 1) {
                progress = 1;
            }
            node.setX((int) Math.round(progress * targetX + (1 - progress)
                    * node.getX()));
            node.setY((int) Math.round(progress * targetY + (1 - progress)
                    * node.getY()));
            node.notifyUpdate();
        }

        @Override
        protected void onCancel() {
            // do nothing
        }
    }
}
