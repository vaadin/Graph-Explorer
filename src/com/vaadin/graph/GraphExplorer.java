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
import java.util.Set;

import com.vaadin.graph.shared.ArcProxy;
import com.vaadin.graph.shared.GraphExplorerServerRpc;
import com.vaadin.graph.shared.GraphExplorerState;
import com.vaadin.graph.shared.NodeProxy;
import com.vaadin.graph.shared.NodeProxy.NodeKind;
import com.vaadin.graph.shared.NodeProxy.NodeState;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class GraphExplorer<N extends Node, A extends Arc> extends AbstractComponent implements GraphExplorerServerRpc {
    private static final long serialVersionUID = 1L;

    private static final String STYLE_MEMBER_SELECTOR = "member-selector"; 

    private int clientHeight = 0;
    private int clientWidth = 0;
    private transient final GraphController<N, A> controller;

    private final GraphRepository<N, A> repository;
    private LayoutEngine layoutEngine;

    public GraphExplorer(GraphRepository<N, A> repository) {
    	this(repository, new GraphController<N, A>(), new JungFRLayoutEngine());
    }

    public GraphExplorer(GraphRepository<N, A> repository, GraphController<N, A> controller, LayoutEngine layoutEngine) {
    	super();
    	registerRpc(this, GraphExplorerServerRpc.class);
    	
    	this.repository = repository;
        this.controller = controller;
        this.layoutEngine = layoutEngine;

        NodeProxy homeNode = controller.load(repository.getHomeNode(), layoutEngine.getModel());
        expand(homeNode);
        
       	getState().nodes = new ArrayList<NodeProxy>(layoutEngine.getModel().getNodes());
       	getState().arcs = new ArrayList<ArcProxy>(layoutEngine.getModel().getArcs());

        setSizeFull();
    }

	public LayoutEngine getLayoutEngine() {
		return layoutEngine;
	}

	public void setLayoutEngine(LayoutEngine layoutEngine) {
		this.layoutEngine = layoutEngine;
        refreshLayout(new HashSet<NodeProxy>(), true, null);
	}

    protected GraphController<N, A> getController() {
		return controller;
	}

    public GraphRepository<N, A> getRepository() {
		return repository;
	}

	@Override
    protected GraphExplorerState getState() {
      return (GraphExplorerState) super.getState();
    }

	protected void refreshLayout(Set<NodeProxy> lockedNodes, boolean lockExpanded, String removedId) {
		if (clientWidth > 0 && clientHeight > 0) {
            if (lockExpanded) {
                for (NodeProxy v : layoutEngine.getModel().getNodes()) {
                    if (NodeState.EXPANDED.equals(v.getState())) {
                        lockedNodes.add(v);
                    }
                }
            }
            layoutEngine.layout(clientWidth, clientHeight, lockedNodes);
           	getState().nodes = new ArrayList<NodeProxy>(layoutEngine.getModel().getNodes());
           	getState().arcs = new ArrayList<ArcProxy>(layoutEngine.getModel().getArcs());
            getState().removedId = removedId;
        }
	}

	@Override
	public void updateNode(String nodeId, NodeState state, int x, int y) {
		NodeProxy node = layoutEngine.getModel().getNode(nodeId);
		if (node != null) {
			node.setState(state);
			node.setX(x);
			node.setY(y);
			Set<NodeProxy> lockedNodes = new HashSet<NodeProxy>();
			lockedNodes.add(node);
			refreshLayout(lockedNodes, true, null);
		}
	}

	@Override
	public void clientResized(int clientWidth, int clientHeight) {
		if ((this.clientWidth == 0) && (this.clientHeight == 0)) {
			//initial layout - center the home node
			NodeProxy homeNode = layoutEngine.getModel().getNode(repository.getHomeNode().getId());
			if (homeNode != null) {
				homeNode.setX(clientWidth / 2);
				homeNode.setY(clientHeight / 2);
			}
		}
    	this.clientWidth = clientWidth;
    	this.clientHeight = clientHeight;
        refreshLayout(new HashSet<NodeProxy>(), true, null);
	}
	
    @Override
    public void toggleNode(String nodeId) {
        Set<NodeProxy> lockedNodes = new HashSet<NodeProxy>();
        boolean lockExpanded = true;
    	NodeProxy toggledNode = layoutEngine.getModel().getNode(nodeId);
        if (toggledNode != null) {
            if (NodeKind.GROUP.equals(toggledNode.getKind())) {
                openMemberSelector(nodeId);
            } else {
                if (NodeState.COLLAPSED.equals(toggledNode.getState())) {
                    expand(toggledNode);
            		lockedNodes.add(toggledNode);
                    lockExpanded = false;
                } else {
                    collapse(toggledNode);
                }
            }
        }
        refreshLayout(lockedNodes, lockExpanded, null);
    }

    protected void expand(NodeProxy node) {
		controller.loadNeighbors(node, repository, layoutEngine.getModel());
        node.setState(NodeState.EXPANDED);
        if ((clientWidth > 0) && (clientHeight > 0)) {
        	node.setX(clientWidth / 2);
        	node.setY(clientHeight / 2);
        }
	}
    
    protected void collapse(NodeProxy node) {
        node.setState(NodeState.COLLAPSED);
        for (NodeProxy neighbor : layoutEngine.getModel().getNeighbors(node)) {
            boolean collapsed = NodeState.COLLAPSED.equals(neighbor.getState());
            boolean leafNode = layoutEngine.getModel().degree(neighbor) == 1;
            if (collapsed && leafNode) {
            	layoutEngine.getModel().removeNode(neighbor);
            }
        }
    }

    protected void openMemberSelector(final String groupId) {

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();

        final NodeSelector selector = controller.getMemberSelector(groupId, repository);
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
                controller.loadMembers(groupId, selector.getSelectedNodeIds(), repository, layoutEngine.getModel());
                Set<NodeProxy> lockedNodes = new HashSet<NodeProxy>();
                NodeProxy groupNode = layoutEngine.getModel().getNode(groupId);
                if (groupNode == null) {
                    refreshLayout(lockedNodes, true, groupId);
                } else {
                    lockedNodes.add(groupNode);
                    refreshLayout(lockedNodes, true, null);
                }
            }
        });
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
