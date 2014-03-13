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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.graph.shared.ArcProxy;
import com.vaadin.graph.shared.NodeProxy;
import com.vaadin.server.ResourceReference;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * For loading a graph from a graph database.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class GraphController<N extends Node, A extends Arc> {

    private final GraphRepository<N, A> repository;
    private final LayoutEngineModel model = new JungLayoutEngineModel();
    private final Map<String, Map<String, A>> groups = new HashMap<String, Map<String, A>>();

    public GraphController(GraphRepository<N, A> repository) {
        this.repository = repository;
    }

    public GraphRepository<N, A> getRepository() {
		return repository;
	}

	private void addGroupRel(String arcId, String arcType, String fromId, String toId) {
        ArcProxy arc = new ArcProxy(arcId, arcType);
        arc.setGroup(true);
        arc.setLabel(getGroupArcLabel(arcType));
        arc.setFromNode(fromId);
        arc.setToNode(toId);
        model.addArc(arc);
    }

    private void addArc(A arc) {
        ArcProxy p = new ArcProxy("" + arc.getId(), arc.getLabel());
        p.setLabel(getArcLabel(arc));
        p.setFromNode("" + repository.getTail(arc).getId());
        p.setToNode("" + repository.getHead(arc).getId());
		model.addArc(p);
    }

    /**
     * @param node
     * @return content (html snippet) to be displayed in the node 
     */
    protected String getNodeContent(Node node) {
        StringBuilder builder = new StringBuilder("<b>" + node.getLabel() + "</b>");
        for (Map.Entry<String, Object> property : node.getProperties().entrySet()) {
            builder.append("<br>").append("<i>").append(property.getKey()).append(":</i> ").append(property.getValue());
        }
        return builder.toString();
    }

    /**
     * @param node
     * @return image url to be displayed in the node 
     */
    protected String getNodeIconUrl(Node node) {
    	if (node.getIcon() != null) {
    		return ResourceReference.create(node.getIcon(), null, null).getURL();
    	} else {
    		return "";
    	}
    }

    /**
     * @param nrArcs
     * @return content (html snippet) to be displayed in the "group" node 
     */
    protected String getGroupNodeContent(int nrArcs) {    	
    	return nrArcs + "<br/>nodes";
    }

    /**
     * @param nrArcs
     * @return image url to be displayed in the node 
     */
    protected String getGroupNodeIconUrl(int nrArcs) {
  		return "";
    }

    /**
     * @param node
     * @return label (text) to represent node in node selector
     */
    protected String getNodeLabel(Node node) {
        StringBuilder builder = new StringBuilder(node.getLabel() + "; ");
        for (Map.Entry<String, Object> property : node.getProperties().entrySet()) {
            builder.append(property.getKey()).append(": ").append(property.getValue()).append(", ");
        }
        return builder.toString();
    }

    /**
     * @param arcType
     * @return label (html snippet) to be displayed in the arc
     */
    protected String getArcLabel(Arc arc) {
        StringBuilder builder = new StringBuilder("<b>" + arc.getLabel() + "</b>");
        for (Map.Entry<?, ?> property : arc.getProperties().entrySet()) {
            builder.append("<br>").append("<i>").append(property.getKey()).append(":</i> ").append(property.getValue());
        }
        return builder.toString();
    }

    protected String getGroupArcLabel(String arcType) {
        return "<b>" + arcType.toLowerCase().replace('_', ' ') + "</b>";
    }

    public NodeSelector getMemberSelector(final String groupId) {

        @SuppressWarnings("serial")
        class SelectorUI extends CustomComponent implements NodeSelector {

            private static final String NODENAME = "nodename";
            Table matchList = new Table();
            private final Map<String, String> members = new HashMap<String, String>();

            public SelectorUI() {
                VerticalLayout layout = new VerticalLayout();

                TextField stringMatcher = new TextField();
                layout.addComponent(stringMatcher);
                stringMatcher.setWidth(100, Unit.PERCENTAGE);

                layout.addComponent(matchList);
                layout.setExpandRatio(matchList, 1.0f);
                matchList.setSizeFull();
                matchList.setPageLength(40);

                matchList.setSelectable(true);
                matchList.setMultiSelect(true);
                matchList.addContainerProperty(NODENAME, String.class, "");
                matchList.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);

                stringMatcher.setTextChangeEventMode(TextChangeEventMode.LAZY);

                StringTokenizer tokenizer = new StringTokenizer(groupId);
                String parentId = tokenizer.nextToken();
                N parent = repository.getNodeById(parentId);
                for (A arc : groups.get(groupId).values()) {
                    N child = repository.getOpposite(parent, arc);
                    String id = "" + child.getId();
                    String label = getNodeLabel(child);
                    members.put(id, label);
                    matchList.addItem(id);
                    matchList.getContainerProperty(id, NODENAME).setValue(label);
                }

                stringMatcher.addTextChangeListener(new TextChangeListener() {
                    public void textChange(TextChangeEvent event) {
                        String query = event.getText().toLowerCase().replaceAll("\\s", "");
                        matchList.removeAllItems();
                        for (Map.Entry<String, String> entry : members.entrySet()) {
                            String nodeId = entry.getKey();
                            String value = entry.getValue();
                            if (value.toLowerCase().replaceAll("\\s", "").contains(query)) {
                                matchList.addItem(nodeId);
                                matchList.getContainerProperty(nodeId, NODENAME).setValue(value);
                            }
                        }
                    }
                });
              layout.setSizeFull();
              setCompositionRoot(layout);
              setSizeFull();
            }

            public Collection<String> getSelectedNodeIds() {
                Collection<String> match = (Collection<String>) matchList.getValue();
                if (match.size() == 0) {
                    return (Collection<String>) matchList.getItemIds();
                }
                return match;
            }

        }

        return new SelectorUI();
    }

    protected NodeProxy load(Node node) {
        String id = "" + node.getId();
        NodeProxy p = new NodeProxy(id);
        if (!model.addNode(p)) {
            p = model.getNode(id);
        }
        p.setContent(getNodeContent(node));
        p.setIconUrl(getNodeIconUrl(node));
        if (p.getContent().isEmpty()) {
            p.setKind(NodeProxy.EMPTY);
        }
        return p;
    }

    public Collection<NodeProxy> loadMembers(String groupId, Collection<String> memberIds) {
        StringTokenizer tokenizer = new StringTokenizer(groupId);
        final String parentId = tokenizer.nextToken();
        final N parent = repository.getNodeById(parentId);
        Map<String, A> arcs = groups.get(groupId);
        Collection<NodeProxy> loaded = new HashSet<NodeProxy>();
        for (String id : memberIds) {
            A arc = arcs.remove(id);
            loaded.add(load(repository.getOpposite(parent, arc)));
            addArc(arc);
        }
        NodeProxy group = model.getNode(groupId);
        if (arcs.size() > 0) {
            group.setContent(getGroupNodeContent(arcs.size()));
            group.setIconUrl(getGroupNodeIconUrl(arcs.size()));
        } else {
            model.removeNode(group);
            groups.remove(groupId);
        }
        return loaded;
    }

    public Collection<NodeProxy> loadNeighbors(String nodeId) {
        NodeProxy n = model.getNode(nodeId);
        Set<NodeProxy> neighbors = new HashSet<NodeProxy>();
        if (NodeProxy.EXPANDED.equals(n.getState())) {
            return neighbors;
        }
        n.setState(NodeProxy.EXPANDED);
        N node = repository.getNodeById(nodeId);
        for (Arc.Direction dir : new Arc.Direction[] {Arc.Direction.INCOMING,
                                                      Arc.Direction.OUTGOING}) {
            for (String label : repository.getArcLabels()) {
                Map<String, A> arcs = new HashMap<String, A>();
                for (A arc : repository.getArcs(node, label, dir)) {
                    arcs.put("" + repository.getOpposite(node, arc).getId(), arc);
                }
                int nrArcs = arcs.size();
                if (nrArcs > getGroupThreshold()) {
                    String groupId = nodeId + ' ' + dir + ' ' + label;
                    NodeProxy groupNode = new NodeProxy(groupId);
                    if (!model.addNode(groupNode)) {
                        groupNode = model.getNode(groupId);
                    }
                    groupNode.setKind(NodeProxy.GROUP);
                    groupNode.setContent(getGroupNodeContent(nrArcs));
                    groupNode.setIconUrl(getGroupNodeIconUrl(nrArcs));
                    switch (dir) {
                    case INCOMING:
                        addGroupRel(groupId, label, groupId, nodeId);
                        break;
                    case OUTGOING:
                        addGroupRel(groupId, label, nodeId, groupId);
                        break;
                    default:
                        throw new AssertionError("unexpected direction " + dir);
                    }
                    neighbors.add(groupNode);
                    groups.put(groupId, arcs);
                } else {
                    for (A arc : arcs.values()) {
                        String id = "" + arc.getId();
                        if (!model.containsArc(id)) {
                            Node other = repository.getOpposite(node, arc);
                            NodeProxy vOther = load(other);
                            addArc(arc);
                            neighbors.add(vOther);
                        }
                    }
                }
            }
        }
        return neighbors;
    }

    protected LayoutEngineModel getModel() {
        return model;
    }
    
    /**
     * @return number of arcs after which node will become a "group" node
     */
    protected int getGroupThreshold() {
    	return 10;
    }    
}
