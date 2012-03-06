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
package com.vaadin.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.graph.GraphLoader.NodeSelector;
import com.vaadin.graph.client.ClientEdge;
import com.vaadin.graph.client.ClientVertex;
import com.vaadin.graph.client.VGraphExplorer;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

/**
 * Server side component for the VGraphExplorer widget.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
@ClientWidget(VGraphExplorer.class)
public class GraphExplorer extends AbstractComponent {
    private static final long serialVersionUID = 1L;

    protected String removedId = null;

    // FIXME: convert into a wrapper (no longer inherit from Jung.Graph)
    private final IndexedGraph graph = new IndexedGraph();

    private int clientHeight = 0;
    private int clientWidth = 0;
    private transient final GraphLoader graphLoader;

    public GraphExplorer(GraphLoader graphLoader) {
        this.graphLoader = graphLoader;
        setWidth("100%");
        setHeight("100%");
        graphLoader.init(graph);
    }

    @SuppressWarnings("boxing")
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        Set<String> keys = new HashSet<String>(variables.keySet());
        ClientVertex toggledNode = null;
        Set<ClientVertex> lockedVertices = new HashSet<ClientVertex>();
        boolean lockExpanded = true;
        if (variables.containsKey(VGraphExplorer.WIDTH)) {
            keys.remove(VGraphExplorer.WIDTH);
            clientWidth = (Integer) variables.get(VGraphExplorer.WIDTH);
        }
        if (variables.containsKey(VGraphExplorer.HEIGHT)) {
            keys.remove(VGraphExplorer.HEIGHT);
            clientHeight = (Integer) variables.get(VGraphExplorer.HEIGHT);
        }
        if (keys.contains(VGraphExplorer.TOGGLE)) {
            keys.remove(VGraphExplorer.TOGGLE);
            String toggledId = (String) variables.get(VGraphExplorer.TOGGLE);
            toggledNode = graph.getVertex(toggledId);
            if (toggledNode != null) {
                if (ClientVertex.GROUP.equals(toggledNode.getKind())) {
                    openMemberSelector(toggledId);
                } else {
                    if (ClientVertex.COLLAPSED.equals(toggledNode.getState())) {
                        graphLoader.loadNeighbors(graph, toggledId);
                        lockExpanded = false;
                        lockedVertices.add(toggledNode);
                        toggledNode.setX(clientWidth / 2);
                        toggledNode.setY(clientHeight / 2);
                    } else {
                        collapse(toggledNode);
                    }
                }
            }
        }
        for (String key : keys) {
            Object variable = variables.get(key);
            ClientVertex node = graph.getVertex(key);
            if (variable != null) {
                VertexReader.readFromJSON(node, variable);
                lockedVertices.add(node);
            }
        }
        if (clientWidth > 0 && clientHeight > 0) {
            if (lockExpanded) {
                for (ClientVertex v : graph.getVertices()) {
                    if (ClientVertex.EXPANDED.equals(v.getState())) {
                        lockedVertices.add(v);
                    }
                }
            }
            LayoutEngine.layout(graph, clientWidth, clientHeight,
                    lockedVertices);
        }
        requestRepaint();
    }

    private void collapse(ClientVertex node) {
        node.setState(ClientVertex.COLLAPSED);
        for (ClientVertex v : graph.getNeighbors(node)) {
            if (ClientVertex.COLLAPSED.equals(v.getState())
                    && graph.degree(v) == 1) {
                graph.removeVertex(v);
            }
        }
    }

    public String[] nodesToJSON() {
        List<String> list = new ArrayList<String>();
        for (ClientVertex v : graph.getVertices()) {
            list.add(v.toString());
        }
        return list.toArray(new String[list.size()]);
    }

    private void openMemberSelector(String groupId) {
        VerticalLayout layout = new VerticalLayout();
        Window dialog = new Window("Select nodes to show", layout);
        dialog.setModal(true);
        dialog.setStyleName(Reindeer.WINDOW_BLACK);
        dialog.setWidth("300px");
        dialog.setHeight("400px");

        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();

        GraphLoader.NodeSelector selector = graphLoader.getMemberSelector(
                graph, groupId);
        layout.addComponent(selector);
        layout.setExpandRatio(selector, 1.0f);

        HorizontalLayout buttons = new HorizontalLayout();
        Button showButton = new Button("Show");
        Button cancelButton = new Button("Cancel");
        buttons.addComponent(cancelButton);
        buttons.addComponent(showButton);
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);

        Window browserWindow = getWindow();
        while (browserWindow.getParent() != null) {
            browserWindow = browserWindow.getParent();
        }
        browserWindow.addWindow(dialog);

        cancelButton.addListener(new CancelHandler(dialog));

        showButton
                .addListener(new ShowHandler(this, selector, groupId, dialog));
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addVariable(this, VGraphExplorer.NODES, nodesToJSON());
        target.addVariable(this, VGraphExplorer.RELATIONSHIPS,
                relationshipsToJSON());
        if (removedId != null) {
            target.addVariable(this, VGraphExplorer.HIDE, removedId);
            removedId = null;
        }
    }

    public String[] relationshipsToJSON() {
        List<String> list = new ArrayList<String>();
        for (ClientEdge e : graph.getEdges()) {
            list.add('{' + key(ClientEdge.ID) + q(e.getId()) + ','
                    + key(ClientEdge.TYPE) + q(e.getType()) + ','
                    + key(ClientEdge.LABEL) + q(e.getLabel()) + ','
                    + key(ClientEdge.GROUP) + e.isGroup() + ','
                    + key(ClientEdge.FROM_ID) + q(graph.getSource(e).getId())
                    + ',' + key(ClientEdge.TO_ID) + q(graph.getDest(e).getId())
                    + '}');
        }
        return list.toArray(new String[list.size()]);
    }

    /** Formats the given string for use as a key in a JSON object. */
    private static String key(String s) {
        return q(s) + ':';
    }

    /** Quotes the given string in double quotes. */
    private static String q(String s) {
        return '"' + s + '"';
    }

    private final static class CancelHandler implements ClickListener {
        private static final long serialVersionUID = 1L;

        private final Window dialog;

        private CancelHandler(Window dialog) {
            this.dialog = dialog;
        }

        public void buttonClick(ClickEvent event) {
            dialog.getParent().removeWindow(dialog);
        }
    }

    private static final class ShowHandler implements ClickListener {
        private static final long serialVersionUID = 1L;

        private final Window dialog;
        private final String groupId;
        private final NodeSelector selector;
        private final GraphExplorer parent;

        private ShowHandler(GraphExplorer parent, NodeSelector selector,
                String groupId, Window dialog) {
            this.parent = parent;
            this.selector = selector;
            this.groupId = groupId;
            this.dialog = dialog;
        }

        public void buttonClick(ClickEvent event) {
            dialog.getParent().removeWindow(dialog);
            parent.graphLoader.loadMembers(parent.graph, groupId,
                    selector.getSelectedNodeIds());
            Set<ClientVertex> lockedVertices = new HashSet<ClientVertex>();
            if (!parent.graph.containsVertex(groupId)) {
                parent.removedId = groupId;
            } else {
                lockedVertices.add(parent.graph.getVertex(groupId));
            }
            for (ClientVertex v : parent.graph.getVertices()) {
                if (ClientVertex.EXPANDED.equals(v.getState())) {
                    lockedVertices.add(v);
                }
            }
            LayoutEngine.layout(parent.graph, parent.clientWidth,
                    parent.clientHeight, lockedVertices);
            parent.requestRepaint();
        }
    }
}
