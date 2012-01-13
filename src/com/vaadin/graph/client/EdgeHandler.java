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

import org.vaadin.gwtgraphics.client.Line;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTML;

/**
 * Presenter/controller for an edge in a graph.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
class EdgeHandler implements Handler {

    private static final int ARROWHEAD_LENGTH = 10;
    private static final int ARROWHEAD_WIDTH = ARROWHEAD_LENGTH / 2;
    private Line edge;
    private HTML label;
    private Line arrowheadLeft;
    private Line arrowheadRight;
    private final ClientEdge relationship;
    private double terminusX;
    private double terminusY;
    private final VGraphExplorer parent;

    EdgeHandler(VGraphExplorer parent, ClientEdge relationship) {
        this.parent = parent;
        this.relationship = relationship;
        addEdge();
        addArrowhead();
        addLabel();
        relationship.setObserver(this);
        update();
    }

    private void addArrowhead() {
        arrowheadLeft = new Line(0, 0, 0, 0);
        parent.add(arrowheadLeft);
        arrowheadRight = new Line(0, 0, 0, 0);
        parent.add(arrowheadRight);
    }

    private void addEdge() {
        edge = new Line(0, 0, 0, 0);
        parent.add(edge);
    }

    private void addLabel() {
        label = new HTML(relationship.getLabel());
        label.getElement().setClassName("relationship");
        if (!relationship.isGroup()) {
            label.setTitle(relationship.getId());
        }
        parent.add(label);
    }

    private double distance(double fromX, double fromY, double toX, double toY) {
        return Math.abs(toX - fromX) + Math.abs(toY - fromY);
    }

    public void remove() {
        relationship.setObserver(null);
        parent.remove(edge);
        parent.remove(edge);
        parent.remove(label);
        parent.remove(arrowheadLeft);
        parent.remove(arrowheadRight);
        parent.getGraph().removeEdge(relationship.getId());
    }

    public void update() {
        updateEdge();
        updateLabel();
        updateArrowhead();
    }

    private void updateArrowhead() {
        VIndexedGraph graph = parent.getGraph();
        ClientVertex from = graph.getSource(relationship);
        double fromX = from.getX();
        double fromY = from.getY();
        ClientVertex to = graph.getDest(relationship);
        double toX = to.getX();
        double toY = to.getY();
        double dX = toX - fromX;
        double dY = toY - fromY;
        terminusX = toX;
        terminusY = toY;
        double distance = distance(fromX, fromY, toX, toY);
        double newX;
        double newY;

        double halfWidth = to.getWidth() / 2.0;
        double left = toX - halfWidth;
        double right = toX + halfWidth;
        newX = fromX < left ? left : fromX > right ? right : fromX;
        newY = fromY + dY * (newX - fromX) / dX;
        double newDistance = distance(newX, newY, toX, toY);
        if (newDistance < distance) {
            distance = newDistance;
            terminusX = newX;
            terminusY = newY;
        }

        double halfHeight = to.getHeight() / 2.0;
        double top = toY - halfHeight;
        double bottom = toY + halfHeight;
        newY = fromY < top ? top : fromY > bottom ? bottom : fromY;
        newX = fromX + dX * (newY - fromY) / dY;
        if (distance(newX, newY, toX, toY) < distance) {
            terminusX = newX;
            terminusY = newY;
        }

        double angle = Math.atan2(dY, dX);
        double leftX = terminusX
                + rotateX(-ARROWHEAD_LENGTH, -ARROWHEAD_WIDTH, angle);
        double leftY = terminusY
                + rotateY(-ARROWHEAD_LENGTH, -ARROWHEAD_WIDTH, angle);
        updateLine(arrowheadLeft, terminusX, terminusY, leftX, leftY);

        double rightX = terminusX
                + rotateX(-ARROWHEAD_LENGTH, ARROWHEAD_WIDTH, angle);
        double rightY = terminusY
                + rotateY(-ARROWHEAD_LENGTH, ARROWHEAD_WIDTH, angle);
        updateLine(arrowheadRight, terminusX, terminusY, rightX, rightY);
    }

    private void updateEdge() {
        VIndexedGraph graph = parent.getGraph();
        ClientVertex from = graph.getSource(relationship);
        ClientVertex to = graph.getDest(relationship);
        updateLine(edge, from.getX(), from.getY(), to.getX(), to.getY());
    }

    private Style updateLabel() {
        Style style = label.getElement().getStyle();
        VIndexedGraph graph = parent.getGraph();
        ClientVertex from = graph.getSource(relationship);

        double x = getLabelCenter(from.getX(), terminusX)
                - label.getOffsetWidth() / 2.0;
        style.setLeft(x, Unit.PX);
        double y = getLabelCenter(from.getY(), terminusY)
                - label.getOffsetHeight() / 2.0;
        style.setTop(y, Unit.PX);

        return style;
    }

    private void updateLine(Line line, double x1, double y1, double x2,
            double y2) {
        updateLine(line, (int) Math.round(x1), (int) Math.round(y1),
                (int) Math.round(x2), (int) Math.round(y2));
    }

    private void updateLine(Line line, int x1, int y1, int x2, int y2) {
        line.setX1(x1);
        line.setY1(y1);
        line.setX2(x2);
        line.setY2(y2);
    }

    private static double getLabelCenter(double from, double to) {
        return .2 * from + .8 * to;
    }

    private static double rotateX(double x, double y, double angle) {
        return x * Math.cos(angle) - y * Math.sin(angle);
    }

    private static double rotateY(double x, double y, double angle) {
        return x * Math.sin(angle) + y * Math.cos(angle);
    }
}
