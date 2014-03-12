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
package com.vaadin.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.graph.client.ArcProxy;
import com.vaadin.graph.client.NodeProxy;
import com.vaadin.graph.shared.GraphExplorerServerRpc;
import com.vaadin.graph.shared.GraphExplorerState;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class GraphExplorer<N extends Node, A extends Arc> extends
        AbstractComponent implements GraphExplorerServerRpc {
    private static final long serialVersionUID = 1L;

    private static final String STYLE_MEMBER_SELECTOR = "member-selector"; 

    private String removedId = null;

    private int clientHeight = 0;
    private int clientWidth = 0;
    private transient final GraphController<N, A> controller;

    private final JungGraphModel model;
    private LayoutEngine<JungGraphModel> layoutEngine;

    public GraphExplorer(GraphRepository<N, A> repository) {
    	this(new GraphController<N, A>(repository), new JungFRLayoutEngine());
    }

    public GraphExplorer(GraphController<N, A> controller, LayoutEngine<JungGraphModel> layoutEngine) {
    	super();
    	registerRpc(this, GraphExplorerServerRpc.class);
    	
        this.controller = controller;
        this.layoutEngine = layoutEngine;
        this.model = controller.getModel();

        getState().nodes = nodesToJSON();
        getState().arcs = arcsToJSON();
        getState().removedId = removedId;

        setSizeFull();
    }

	public LayoutEngine<JungGraphModel> getLayoutEngine() {
		return layoutEngine;
	}

	public void setLayoutEngine(LayoutEngine<JungGraphModel> layoutEngine) {
		this.layoutEngine = layoutEngine;
	}

    protected GraphController<N, A> getController() {
		return controller;
	}

    public GraphRepository<N, A> getRepository() {
		return controller.getRepository();
	}

	@Override
    protected GraphExplorerState getState() {
      return (GraphExplorerState) super.getState();
    }

	private void refreshLayout(Set<NodeProxy> lockedNodes, boolean lockExpanded) {
		if (clientWidth > 0 && clientHeight > 0) {
            if (lockExpanded) {
                for (NodeProxy v : model.getNodes()) {
                    if (NodeProxy.EXPANDED.equals(v.getState())) {
                        lockedNodes.add(v);
                    }
                }
            }
            layoutEngine.layout(model, clientWidth, clientHeight, lockedNodes);
            getState().nodes = nodesToJSON();
            getState().arcs = arcsToJSON();
            getState().removedId = removedId;
            removedId = null;
        }
	}

	@Override
	public void updateNode(String nodeId, String state, int x, int y) {
		NodeProxy node = model.getNode(nodeId);
		node.setState(state);
		node.setX(x);
		node.setY(y);
        Set<NodeProxy> lockedNodes = new HashSet<NodeProxy>();
        lockedNodes.add(node);
        refreshLayout(lockedNodes, true);
	}

    @Override
    public void toggleNode(String nodeId, int clientWidth, int clientHeight) {
    	this.clientWidth = clientWidth;
    	this.clientHeight = clientHeight;
        Set<NodeProxy> lockedNodes = new HashSet<NodeProxy>();
        boolean lockExpanded = true;
    	NodeProxy toggledNode = model.getNode(nodeId);
        if (toggledNode != null) {
            if (NodeProxy.GROUP.equals(toggledNode.getKind())) {
                openMemberSelector(nodeId);
            } else {
                if (NodeProxy.COLLAPSED.equals(toggledNode.getState())) {
                    controller.loadNeighbors(nodeId);
                    lockExpanded = false;
                    lockedNodes.add(toggledNode);
                    toggledNode.setX(clientWidth / 2);
                    toggledNode.setY(clientHeight / 2);
                } else {
                    collapse(toggledNode);
                }
            }
        }
        refreshLayout(lockedNodes, lockExpanded);
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

    protected void openMemberSelector(final String groupId) {

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();

        final NodeSelector selector = controller.getMemberSelector(groupId);
        layout.addComponent(selector);
        layout.setExpandRatio(selector, 1.0f);

        HorizontalLayout buttons = new HorizontalLayout();
        Button showButton = new Button(getShowButtonCaption());
        Button cancelButton = new Button(getCancelButtonCaption());
        buttons.addComponent(cancelButton);
        buttons.addComponent(showButton);
        buttons.setWidth(100, Unit.PERCENTAGE);
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);

        final Window dialog = createMemberSelectorWindow();
        dialog.setContent(layout);
        getUI().addWindow(dialog);

        cancelButton.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {
            	getUI().removeWindow(dialog);
            }
        });

        showButton.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {
            	getUI().removeWindow(dialog);
                controller.loadMembers(groupId, selector.getSelectedNodeIds());
                Set<NodeProxy> lockedNodes = new HashSet<NodeProxy>();
                if (!model.containsNode(groupId)) {
                    removedId = groupId;
                } else {
                    lockedNodes.add(model.getNode(groupId));
                }
                refreshLayout(lockedNodes, true);
            }
        });
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
    
	protected Window createMemberSelectorWindow() {
		final Window dialog = new Window(getMemberSelectorTitle());
        dialog.setModal(true);
        dialog.setStyleName(STYLE_MEMBER_SELECTOR);
        dialog.setWidth(300, Unit.PIXELS);
        dialog.setHeight(400, Unit.PIXELS);
		return dialog;
	}

    protected String getMemberSelectorTitle() {
    	return "Select nodes to show";
    }
    
    protected String getShowButtonCaption() {
    	return "Show";
    }

    protected String getCancelButtonCaption() {
    	return "Cancel";
    }

}
