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
public class GraphExplorer<N extends Node, A extends Arc> extends
        AbstractComponent {
    private static final long serialVersionUID = 1L;

    private String removedId = null;

    int clientHeight = 0;
    int clientWidth = 0;
    transient final GraphController<N, A> controller;

    private GraphModel model;

    public GraphExplorer(GraphRepository<N, A> repository) {
        this.controller = new GraphController<N, A>(repository);
        setWidth("100%");
        setHeight("100%");
        model = controller.getModel();
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
            toggledNode = model.getNode(toggledId);
            if (toggledNode != null) {
                if (NodeProxy.GROUP.equals(toggledNode.getKind())) {
                    openMemberSelector(toggledId);
                } else {
                    if (NodeProxy.COLLAPSED.equals(toggledNode.getState())) {
                        controller.loadNeighbors(toggledId);
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
            NodeProxy node = model.getNode(key);
            if (variable != null) {
                NodeLoader.loadFromJSON(node, variable);
                lockedNodes.add(node);
            }
        }
        if (clientWidth > 0 && clientHeight > 0) {
            if (lockExpanded) {
                for (NodeProxy v : model.getNodes()) {
                    if (NodeProxy.EXPANDED.equals(v.getState())) {
                        lockedNodes.add(v);
                    }
                }
            }
            model.layout(clientWidth, clientHeight, lockedNodes);
        }
        requestRepaint();
    }

    private void collapse(NodeProxy node) {
        node.setState(NodeProxy.COLLAPSED);
        for (NodeProxy neighbor : model.getNeighbors(node)) {
            boolean collapsed = NodeProxy.COLLAPSED.equals(neighbor.getState());
            boolean leafNode = model.degree(neighbor) == 1;
            if (collapsed && leafNode) {
                model.removeNode(neighbor);
            }
        }
    }

    public String[] nodesToJSON() {
        List<String> list = new ArrayList<String>();
        for (NodeProxy v : model.getNodes()) {
            list.add(v.toString());
        }
        return list.toArray(new String[list.size()]);
    }

    private void openMemberSelector(final String groupId) {
        VerticalLayout layout = new VerticalLayout();
        final Window dialog = new Window("Select nodes to show", layout);
        dialog.setModal(true);
        dialog.setStyleName(Reindeer.WINDOW_BLACK);
        dialog.setWidth("300px");
        dialog.setHeight("400px");

        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();

        final NodeSelector selector = controller.getMemberSelector(groupId);
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

        showButton.addListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                dialog.getParent().removeWindow(dialog);
                controller.loadMembers(groupId, selector.getSelectedNodeIds());
                Set<NodeProxy> lockedNodes = new HashSet<NodeProxy>();
                if (!model.containsNode(groupId)) {
                    removedId = groupId;
                } else {
                    lockedNodes.add(model.getNode(groupId));
                }
                for (NodeProxy v : model.getNodes()) {
                    if (NodeProxy.EXPANDED.equals(v.getState())) {
                        lockedNodes.add(v);
                    }
                }
                model.layout(clientWidth, clientHeight, lockedNodes);
                requestRepaint();
            }
        });
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addVariable(this, VGraphExplorer.NODES, nodesToJSON());
        target.addVariable(this, VGraphExplorer.ARCS, arcsToJSON());
        if (removedId != null) {
            target.addVariable(this, VGraphExplorer.HIDE, removedId);
            removedId = null;
        }
    }

    private String[] arcsToJSON() {
        List<String> list = new ArrayList<String>();
        for (ArcProxy e : model.getArcs()) {
            list.add('{' + key(ArcProxy.ID) + q(e.getId()) + ','
                     + key(ArcProxy.TYPE) + q(e.getType()) + ','
                     + key(ArcProxy.LABEL) + q(e.getLabel()) + ','
                     + key(ArcProxy.GROUP) + e.isGroup() + ','
                     + key(ArcProxy.FROM_ID) + q(model.getSource(e).getId())
                     + ',' + key(ArcProxy.TO_ID) + q(model.getDest(e).getId())
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
