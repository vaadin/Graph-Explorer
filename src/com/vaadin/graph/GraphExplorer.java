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

import java.util.*;

import com.vaadin.graph.client.*;
import com.vaadin.terminal.*;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;

@ClientWidget(VGraphExplorer.class)
public class GraphExplorer extends AbstractComponent {
    private static final long serialVersionUID = 1L;

    protected String removedId = null;

    final GraphModel graph = new GraphModel();

    int clientHeight = 0;
    int clientWidth = 0;
    transient final GraphController graphController;

    public GraphExplorer(GraphController graphController) {
        this.graphController = graphController;
        setWidth("100%");
        setHeight("100%");
        graphController.init(graph);
    }

    @SuppressWarnings("boxing")
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        Set<String> keys = new HashSet<String>(variables.keySet());
        NodeProxy toggledNode = null;
        Set<NodeProxy> lockedNodes = new HashSet<NodeProxy>();
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
            toggledNode = graph.getNode(toggledId);
            if (toggledNode != null) {
                if (NodeProxy.GROUP.equals(toggledNode.getKind())) {
                    openMemberSelector(toggledId);
                } else {
                    if (NodeProxy.COLLAPSED.equals(toggledNode.getState())) {
                        graphController.loadNeighbors(graph, toggledId);
                        lockExpanded = false;
                        lockedNodes.add(toggledNode);
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
            NodeProxy node = graph.getNode(key);
            if (variable != null) {
                NodeLoader.loadFromJSON(node, variable);
                lockedNodes.add(node);
            }
        }
        if (clientWidth > 0 && clientHeight > 0) {
            if (lockExpanded) {
                for (NodeProxy v : graph.getNodes()) {
                    if (NodeProxy.EXPANDED.equals(v.getState())) {
                        lockedNodes.add(v);
                    }
                }
            }
            graph.layout(clientWidth, clientHeight, lockedNodes);
        }
        requestRepaint();
    }

    private void collapse(NodeProxy node) {
        node.setState(NodeProxy.COLLAPSED);
        for (NodeProxy v : graph.getNeighbors(node)) {
            if (NodeProxy.COLLAPSED.equals(v.getState())
                    && graph.degree(v) == 1) {
                graph.removeNode(v);
            }
        }
    }

    public String[] nodesToJSON() {
        List<String> list = new ArrayList<String>();
        for (NodeProxy v : graph.getNodes()) {
            list.add(v.toString());
        }
        return list.toArray(new String[list.size()]);
    }

    private void openMemberSelector(String groupId) {
        VerticalLayout layout = new VerticalLayout();
        final Window dialog = new Window("Select nodes to show", layout);
        dialog.setModal(true);
        dialog.setStyleName(Reindeer.WINDOW_BLACK);
        dialog.setWidth("300px");
        dialog.setHeight("400px");

        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();

        GraphController.NodeSelector selector = graphController
                .getMemberSelector(graph, groupId);
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

        cancelButton.addListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {
                dialog.getParent().removeWindow(dialog);
            }
        });

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
        for (ArcProxy e : graph.getArcs()) {
            list.add('{' + key(ArcProxy.ID) + q(e.getId()) + ','
                    + key(ArcProxy.TYPE) + q(e.getType()) + ','
                    + key(ArcProxy.LABEL) + q(e.getLabel()) + ','
                    + key(ArcProxy.GROUP) + e.isGroup() + ','
                    + key(ArcProxy.FROM_ID) + q(graph.getSource(e).getId())
                    + ',' + key(ArcProxy.TO_ID) + q(graph.getDest(e).getId())
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
}
