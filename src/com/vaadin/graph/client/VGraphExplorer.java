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

import org.vaadin.gwtgraphics.client.*;

import com.google.gwt.dom.client.*;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.*;
import com.vaadin.terminal.gwt.client.*;

public class VGraphExplorer extends Composite implements Paintable {

    public static final String HEIGHT = "height";
    public static final String WIDTH = "width";
    public static final String TOGGLE = "toggle";
    public static final String NODES = "nodes";
    public static final String ARCS = "arcs";
    public static final String HIDE = "hide";

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-mycomponent";

    /** The client side widget identifier */
    protected String paintableId;

    /** Reference to the server connection object. */
    ApplicationConnection connection;

    private final Panel root = new AbsolutePanel();
    private final DrawingArea canvas = new DrawingArea(0, 0);
    private final GraphProxy graph = new GraphProxy();
    private int oldHeight;
    private int oldWidth;
    private NodeProxy current;
    private boolean initialized;

    /**
     * The constructor should first call super() to initialize the component and
     * then handle any initialization relevant to Vaadin.
     */
    public VGraphExplorer() {
        initWidget(root);
        RootPanel.getBodyElement().getStyle().setBackgroundColor("green");
        Style canvasStyle = canvas.getElement().getStyle();
        canvasStyle.setPosition(Position.ABSOLUTE);
        canvasStyle.setBackgroundColor("white");
        root.add(canvas);
        root.getElement().getStyle().setPosition(Position.ABSOLUTE);

        /*
         * This method call of the Paintable interface sets the component style
         * name in DOM tree
         */
        setStyleName(CLASSNAME);
    }

    void add(HTML widget) {
        root.add(widget);
    }

    void add(VectorObject widget) {
        canvas.add(widget);
    }

    public GraphProxy getGraph() {
        return graph;
    }

    private void init() {
        Collection<NodeProxy> nodes = graph.getNodes();
        int newWidth = getOffsetWidth();
        int newHeight = getOffsetHeight();
        if (newWidth > 0 && newHeight > 0) {
            if (!initialized && nodes.size() == 1) {
                toggle(nodes.iterator().next());
                initialized = true;
            } else if (newWidth != oldWidth || newHeight != oldHeight) {
                toggle(null);
            }
        }
    }

    private void parseNodes(String[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return;
        }
        for (String json : nodes) {
            JSONObject object = JSONParser.parseLenient(json).isObject();
            String id = getString(object, NodeProxy.ID);
            NodeProxy node = new NodeProxy(id);
            if (graph.addNode(node)) {
                if (current == null) {
                    node.setX(Random.nextInt(getOffsetWidth()));
                    node.setY(Random.nextInt(getOffsetHeight()));
                } else {
                    node.setX(current.getX());
                    node.setY(current.getY());
                }
            } else {
                node = graph.getNode(id);
            }
            node.setContent(getString(object, NodeProxy.LABEL));
            node.setState(getString(object, NodeProxy.STATE));
            node.setKind(getString(object, NodeProxy.KIND));
            if (!node.hasController()) {
                node.setController(new NodePresenter(this, node));
            }
            int x = getInt(object, NodeProxy.X);
            int y = getInt(object, NodeProxy.Y);
            node.getController().move(x, y);
        }
    }

    private static int getInt(JSONObject object, String key) {
        return (int) object.get(key).isNumber().doubleValue();
    }

    private void parseArcs(String[] arcs) {
        if (arcs == null || arcs.length == 0) {
            return;
        }
        for (String json : arcs) {
            JSONObject object = JSONParser.parseLenient(json).isObject();
            String id = getString(object, ArcProxy.ID);
            if (!graph.containsArc(id)) {
                ArcProxy arc = new ArcProxy(id,
                                            getString(object, ArcProxy.TYPE));
                if (!graph.addArc(arc,
                                  graph.getNode(getString(object,
                                                          ArcProxy.FROM_ID)),
                                  graph.getNode(getString(object,
                                                          ArcProxy.TO_ID)))) {
                    arc = graph.getArc(id);
                }
                arc.setLabel(getString(object, ArcProxy.LABEL));
                arc.setGroup(object.get(ArcProxy.GROUP).isBoolean().booleanValue());
                new ArcPresenter(this, arc);
            }
        }
    }

    private static String getString(JSONObject object, String key) {
        return object.get(key).isString().stringValue();
    }

    public void remove(HTML widget) {
        widget.removeFromParent();
    }

    public void remove(VectorObject widget) {
        canvas.remove(widget);
    }

    void save(NodeProxy node, boolean immediate) {
        connection.updateVariable(paintableId, "" + node.getId(),
                                  node.toString(), immediate);
    }

    @Override
    public void setHeight(String height) {
        oldHeight = getOffsetHeight();
        Util.setHeightExcludingPaddingAndBorder(this, height, 0);
        int offsetHeight = getOffsetHeight();
        canvas.setHeight(offsetHeight);
        init();
    }

    @Override
    public void setWidth(String width) {
        oldWidth = getOffsetWidth();
        Util.setWidthExcludingPaddingAndBorder(this, width, 0);
        int offsetWidth = getOffsetWidth();
        canvas.setWidth(offsetWidth);
        init();
    }

    void toggle(NodeProxy node) {
        current = node;
        if (connection == null) {
            return;
        }
        connection.updateVariable(paintableId, WIDTH, getOffsetWidth(), false);
        connection.updateVariable(paintableId, HEIGHT, getOffsetHeight(), false);
        if (node == null) {
            connection.updateVariable(paintableId, TOGGLE, "", true);
        } else {
            connection.updateVariable(paintableId, TOGGLE, node.getId(), true);
        }
    }

    /**
     * Called whenever an update is received from the server
     */
    public void updateFromUIDL(UIDL uidl, ApplicationConnection app) {
        /*
         * This call should be made first. It handles sizes, captions, tooltips,
         * etc. automatically.
         */
        if (app.updateComponent(this, uidl, true)) {
            /*
             * If client.updateComponent returns true there has been no changes
             * and we do not need to update anything.
             */
            return;
        }

        /*
         * Save reference to server connection object to be able to send user
         * interaction later
         */
        connection = app;

        /* Save the client side identifier (paintable id) for the widget */
        paintableId = uidl.getId();

        parseNodes(uidl.getStringArrayVariable(NODES));
        parseArcs(uidl.getStringArrayVariable(ARCS));
        if (uidl.hasVariable(HIDE)) {
            graph.removeNode(uidl.getStringVariable(HIDE));
        }

        for (NodeProxy node : graph.getNodes()) {
            node.notifyUpdate();
        }
    }
}
