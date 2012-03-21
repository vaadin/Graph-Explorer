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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.vaadin.graph.client.ArcProxy;
import com.vaadin.graph.client.NodeProxy;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Data structure consisting of nodes with relationships between them.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class GraphModel {
    private final Map<String, NodeProxy> vertices = new HashMap<String, NodeProxy>();
    private final Map<String, ArcProxy> edges = new HashMap<String, ArcProxy>();
    private DirectedSparseMultigraph<NodeProxy, ArcProxy> graph = new DirectedSparseMultigraph<NodeProxy, ArcProxy>() {

        /**
         * Returns the relationship with the given ID, creating it if it doesn't
         * exist.
         */
        @Override
        public boolean addEdge(ArcProxy edge,
                Pair<? extends NodeProxy> endpoints, EdgeType edgeType) {
            boolean success = super.addEdge(edge, endpoints, edgeType);
            if (success) {
                GraphModel.this.edges.put(edge.getId(), edge);
            }
            return success;
        }

        @Override
        public boolean addVertex(NodeProxy vertex) {
            boolean success = super.addVertex(vertex);
            if (success) {
                GraphModel.this.vertices.put(vertex.getId(), vertex);
            }
            return success;
        }

        @Override
        public boolean removeEdge(ArcProxy edge) {
            boolean success = super.removeEdge(edge);
            if (success) {
                GraphModel.this.edges.remove(edge.getId());
            }
            return success;
        }

        @Override
        public boolean removeVertex(NodeProxy vertex) {
            boolean success = super.removeVertex(vertex);
            if (success) {
                GraphModel.this.vertices.remove(vertex.getId());
            }
            return success;
        }
    };

    public void addEdge(ArcProxy edge, NodeProxy vertex,
            NodeProxy vertex2) {
        graph.addEdge(edge, new Pair<NodeProxy>(vertex, vertex2),
                EdgeType.DIRECTED);
    }

    public boolean addVertex(NodeProxy v) {
        return graph.addVertex(v);
    }

    public boolean containsEdge(String id) {
        return edges.containsKey(id);
    }

    public boolean containsVertex(String id) {
        return vertices.containsKey(id);
    }

    public int degree(NodeProxy v) {
        return graph.degree(v);
    }

    public NodeProxy getDest(ArcProxy e) {
        return graph.getDest(e);
    }

    public ArcProxy getEdge(String id) {
        return edges.get(id);
    }

    public Collection<ArcProxy> getEdges() {
        return edges.values();
    }

    public Collection<NodeProxy> getNeighbors(NodeProxy node) {
        return graph.getNeighbors(node);
    }

    public NodeProxy getSource(ArcProxy e) {
        return graph.getSource(e);
    }

    public NodeProxy getVertex(String id) {
        return vertices.get(id);
    }

    public Collection<NodeProxy> getVertices() {
        return vertices.values();
    }

    public void layout(int clientWidth, int clientHeight,
            Set<NodeProxy> lockedVertices) {
        LayoutEngine.layout(graph, clientWidth, clientHeight, lockedVertices);
    }

    public boolean removeVertex(NodeProxy v) {
        return graph.removeVertex(v);
    }
}
