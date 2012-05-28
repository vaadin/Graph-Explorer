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
import java.util.Map.Entry;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.graph.client.*;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.*;

/**
 * For loading a graph from a graph database.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class DefaultGraphController<N extends Node, A extends Arc> implements
        GraphController {
    private static final String GROUP_LABEL = "<br/>nodes";

    private final GraphRepository<N, A> graphRepository;
    private final Map<String, Map<String, A>> groups = new HashMap<String, Map<String, A>>();

    public DefaultGraphController(GraphRepository<N, A> graphDb) {
        graphRepository = graphDb;
    }

    private void addGroupRel(GraphModel graph, String relId, String relType,
            String fromId, String toId) {
        ArcProxy arc = new ArcProxy(relId, relType);
        arc.setGroup(true);
        arc.setLabel(getGroupLabel(relType));
        graph.addArc(arc, graph.getNode(fromId), graph.getNode(toId));
    }

    private void addRel(GraphModel graph, A rel) {
        ArcProxy arc = new ArcProxy("" + rel.getId(), rel.getLabel());
        arc.setLabel(getLabel(rel));
        graph.addArc(arc,
                graph.getNode("" + graphRepository.getSource(rel).getId()),
                graph.getNode("" + graphRepository.getDestination(rel).getId()));
    }

    private String getLabel(Node node, boolean html) {
        StringBuilder builder = new StringBuilder(node.getLabel() + "; ");
        String delim = ", ";
        String before = "";
        String after = ": ";
        if (html) {
            builder = new StringBuilder("<b>" + node.getLabel() + "</b><br>");
            delim = "<br>";
            before = "<i>";
            after = ":</i> ";
        }
        for (Entry<String, Object> property : node.getProperties().entrySet()) {
            builder.append(before).append(property.getKey()).append(after)
                    .append(property.getValue()).append(delim);
        }
        String label = builder.toString();
        return label;
    }

    private String getGroupLabel(String relType) {
        return "<b>" + relType.toLowerCase().replace('_', ' ') + "</b>";
    }

    private String getLabel(Arc rel) {
        StringBuilder builder = new StringBuilder();
        String delim = "<br>";
        String before = "<i>";
        String after = ":</i> ";
        builder.append(getGroupLabel(rel.getLabel()));
        for (Map.Entry<?, ?> property : rel.getProperties().entrySet()) {
            builder.append(delim).append(before).append(property.getKey())
                    .append(after).append(property.getValue());
        }
        String label = builder.toString();
        return label;
    }

    public NodeSelector getMemberSelector(final GraphModel graph,
            final String groupId) {

        @SuppressWarnings("serial")
        class SelectorUI extends CustomComponent implements NodeSelector {

            private static final String NODENAME = "nodename";
            Table matchList = new Table();
            private final Map<String, String> members = new HashMap<String, String>();

            public SelectorUI() {
                VerticalLayout layout = new VerticalLayout();

                layout.setSizeFull();

                setCompositionRoot(layout);
                setSizeFull();

                TextField stringMatcher = new TextField();
                layout.addComponent(stringMatcher);
                stringMatcher.setWidth("100%");

                layout.addComponent(matchList);
                layout.setExpandRatio(matchList, 1.0f);
                matchList.setSizeFull();
                matchList.setPageLength(40);

                matchList.setSelectable(true);
                matchList.setMultiSelect(true);
                matchList.addContainerProperty(NODENAME, String.class, "");
                matchList.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);

                stringMatcher.setTextChangeEventMode(TextChangeEventMode.LAZY);

                StringTokenizer tokenizer = new StringTokenizer(groupId);
                String parentId = tokenizer.nextToken();
                N parent = graphRepository.getNodeById(parentId);
                for (A rel : groups.get(groupId).values()) {
                    N child = graphRepository.getOpposite(parent, rel);
                    String id = "" + child.getId();
                    String label = getLabel(child, false);
                    members.put(id, label);
                    matchList.addItem(id);
                    matchList.getContainerProperty(id, NODENAME)
                            .setValue(label);
                }

                stringMatcher.addListener(new TextChangeListener() {
                    public void textChange(TextChangeEvent event) {
                        String searchString = event.getText().toLowerCase()
                                .replaceAll("\\s", "");

                        matchList.removeAllItems();

                        for (Map.Entry<String, String> entry : members
                                .entrySet()) {
                            String nodeId = entry.getKey();
                            String value = entry.getValue();
                            if (value.toLowerCase().replaceAll("\\s", "")
                                    .contains(searchString)) {
                                matchList.addItem(nodeId);
                                matchList
                                        .getContainerProperty(nodeId, NODENAME)
                                        .setValue(value);
                            }
                        }
                    }
                });

            }

            @SuppressWarnings("unchecked")
            public Collection<String> getSelectedNodeIds() {
                Collection<String> match = (Collection<String>) matchList
                        .getValue();
                if (match.size() == 0) {
                    return (Collection<String>) matchList.getItemIds();
                }
                return match;
            }

        }

        return new SelectorUI();
    }

    public void init(GraphModel graph) {
        load(graph, graphRepository.getHomeNode());
    }

    private NodeProxy load(GraphModel graph, Node node) {
        String label = getLabel(node, true);
        String id = "" + node.getId();
        NodeProxy v = new NodeProxy(id);
        if (!graph.addNode(v)) {
            v = graph.getNode(id);
        }
        v.setContent(label);
        if (label.isEmpty()) {
            v.setKind(NodeProxy.EMPTY);
        }
        return v;
    }

    public Collection<NodeProxy> loadMembers(GraphModel graph, String groupId,
            Collection<String> memberIds) {
        StringTokenizer tokenizer = new StringTokenizer(groupId);
        final String parentId = tokenizer.nextToken();
        final N parent = graphRepository.getNodeById(parentId);
        Map<String, A> rels = groups.get(groupId);
        Collection<NodeProxy> loaded = new HashSet<NodeProxy>();
        for (String id : memberIds) {
            A rel = rels.remove(id);
            loaded.add(load(graph, graphRepository.getOpposite(parent, rel)));
            addRel(graph, rel);
        }
        NodeProxy group = graph.getNode(groupId);
        if (rels.size() > 0) {
            group.setContent(rels.size() + GROUP_LABEL);
        } else {
            graph.removeNode(group);
            groups.remove(groupId);
        }
        return loaded;
    }

    public Collection<NodeProxy> loadNeighbors(GraphModel graph, String nodeId) {
        NodeProxy v = graph.getNode(nodeId);
        Set<NodeProxy> neighbors = new HashSet<NodeProxy>();
        if (NodeProxy.EXPANDED.equals(v.getState())) {
            return neighbors;
        }
        v.setState(NodeProxy.EXPANDED);
        N node = graphRepository.getNodeById(nodeId);
        for (ArcDirection dir : new ArcDirection[] { ArcDirection.INCOMING,
                ArcDirection.OUTGOING }) {
            for (String label : graphRepository.getArcLabels()) {
                Map<String, A> rels = new HashMap<String, A>();
                for (A rel : graphRepository.getArcs(node, label, dir)) {
                    rels.put(""
                            + graphRepository.getOpposite(node, rel).getId(),
                            rel);
                }
                int nrRels = rels.size();
                if (nrRels > 10) {
                    String groupId = nodeId + ' ' + dir + ' ' + label;
                    NodeProxy groupNode = new NodeProxy(groupId);
                    if (!graph.addNode(groupNode)) {
                        groupNode = graph.getNode(groupId);
                    }
                    groupNode.setKind(NodeProxy.GROUP);
                    groupNode.setContent(nrRels + GROUP_LABEL);
                    switch (dir) {
                    case INCOMING:
                        addGroupRel(graph, groupId, label, groupId, nodeId);
                        break;
                    case OUTGOING:
                        addGroupRel(graph, groupId, label, nodeId, groupId);
                        break;
                    default:
                        throw new AssertionError("unexpected direction " + dir);
                    }
                    neighbors.add(groupNode);
                    groups.put(groupId, rels);
                } else {
                    for (A rel : rels.values()) {
                        String id = "" + rel.getId();
                        if (!graph.containsArc(id)) {
                            Node other = graphRepository.getOpposite(node, rel);
                            NodeProxy vOther = load(graph, other);
                            addRel(graph, rel);
                            neighbors.add(vOther);
                        }
                    }
                }
            }
        }
        return neighbors;
    }
}
