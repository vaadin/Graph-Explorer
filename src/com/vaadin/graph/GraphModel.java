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

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;

/**
 * Data structure consisting of nodes with relationships between them.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class GraphModel {
    private final Map<String, NodeProxy> vertices = new HashMap<String, NodeProxy>();
    private final Map<String, ArcProxy> arcs = new HashMap<String, ArcProxy>();

    private DirectedSparseMultigraph<NodeProxy, ArcProxy> graph = new DirectedSparseMultigraph<NodeProxy, ArcProxy>() {

        /**
         * Returns the relationship with the given ID, creating it if it doesn't
         * exist.
         */
        @Override
        public boolean addEdge(ArcProxy arc,
                Pair<? extends NodeProxy> endpoints, EdgeType arcType) {
            boolean success = super.addEdge(arc, endpoints, arcType);
            if (success) {
                arcs.put(arc.getId(), arc);
            }
            return success;
        }

        @Override
        public boolean addVertex(NodeProxy node) {
            boolean success = super.addVertex(node);
            if (success) {
                GraphModel.this.vertices.put(node.getId(), node);
            }
            return success;
        }

        @Override
        public boolean removeEdge(ArcProxy arc) {
            boolean success = super.removeEdge(arc);
            if (success) {
                arcs.remove(arc.getId());
            }
            return success;
        }

        @Override
        public boolean removeVertex(NodeProxy node) {
            boolean success = super.removeVertex(node);
            if (success) {
                GraphModel.this.vertices.remove(node.getId());
            }
            return success;
        }
    };

    public void addArc(ArcProxy arc, NodeProxy node, NodeProxy node2) {
        graph.addEdge(arc, new Pair<NodeProxy>(node, node2), EdgeType.DIRECTED);
    }

    public boolean addNode(NodeProxy v) {
        return graph.addVertex(v);
    }

    public boolean containsArc(String id) {
        return arcs.containsKey(id);
    }

    public boolean containsNode(String id) {
        return vertices.containsKey(id);
    }

    public int degree(NodeProxy v) {
        return graph.degree(v);
    }

    public NodeProxy getDest(ArcProxy e) {
        return graph.getDest(e);
    }

    public ArcProxy getArc(String id) {
        return arcs.get(id);
    }

    public Collection<ArcProxy> getArcs() {
        return arcs.values();
    }

    public Collection<NodeProxy> getNeighbors(NodeProxy node) {
        return graph.getNeighbors(node);
    }

    public NodeProxy getSource(ArcProxy e) {
        return graph.getSource(e);
    }

    public NodeProxy getNode(String id) {
        return vertices.get(id);
    }

    public Collection<NodeProxy> getVertices() {
        return vertices.values();
    }

    public void layout(int clientWidth, int clientHeight,
            Set<NodeProxy> lockedVertices) {
        LayoutEngine.layout(graph, clientWidth, clientHeight, lockedVertices);
    }

    public boolean removeNode(NodeProxy v) {
        return graph.removeVertex(v);
    }
}
