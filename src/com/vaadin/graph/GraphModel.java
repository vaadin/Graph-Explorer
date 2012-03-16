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

import com.vaadin.graph.client.ClientEdge;
import com.vaadin.graph.client.ClientVertex;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Data structure consisting of nodes with relationships between them.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class GraphModel {
    private final Map<String, ClientVertex> vertices = new HashMap<String, ClientVertex>();
    private final Map<String, ClientEdge> edges = new HashMap<String, ClientEdge>();
    private DirectedSparseMultigraph<ClientVertex, ClientEdge> graph = new DirectedSparseMultigraph<ClientVertex, ClientEdge>() {

        /**
         * Returns the relationship with the given ID, creating it if it doesn't
         * exist.
         */
        @Override
        public boolean addEdge(ClientEdge edge,
                Pair<? extends ClientVertex> endpoints, EdgeType edgeType) {
            boolean success = super.addEdge(edge, endpoints, edgeType);
            if (success) {
                GraphModel.this.edges.put(edge.getId(), edge);
            }
            return success;
        }

        @Override
        public boolean addVertex(ClientVertex vertex) {
            boolean success = super.addVertex(vertex);
            if (success) {
                GraphModel.this.vertices.put(vertex.getId(), vertex);
            }
            return success;
        }

        @Override
        public boolean removeEdge(ClientEdge edge) {
            boolean success = super.removeEdge(edge);
            if (success) {
                GraphModel.this.edges.remove(edge.getId());
            }
            return success;
        }

        @Override
        public boolean removeVertex(ClientVertex vertex) {
            boolean success = super.removeVertex(vertex);
            if (success) {
                GraphModel.this.vertices.remove(vertex.getId());
            }
            return success;
        }
    };

    public void addEdge(ClientEdge edge, ClientVertex vertex,
            ClientVertex vertex2) {
        graph.addEdge(edge, new Pair<ClientVertex>(vertex, vertex2),
                EdgeType.DIRECTED);
    }

    public boolean addVertex(ClientVertex v) {
        return graph.addVertex(v);
    }

    public boolean containsEdge(String id) {
        return edges.containsKey(id);
    }

    public boolean containsVertex(String id) {
        return vertices.containsKey(id);
    }

    public int degree(ClientVertex v) {
        return graph.degree(v);
    }

    public ClientVertex getDest(ClientEdge e) {
        return graph.getDest(e);
    }

    public ClientEdge getEdge(String id) {
        return edges.get(id);
    }

    public Collection<ClientEdge> getEdges() {
        return edges.values();
    }

    public Collection<ClientVertex> getNeighbors(ClientVertex node) {
        return graph.getNeighbors(node);
    }

    public ClientVertex getSource(ClientEdge e) {
        return graph.getSource(e);
    }

    public ClientVertex getVertex(String id) {
        return vertices.get(id);
    }

    public Collection<ClientVertex> getVertices() {
        return vertices.values();
    }

    public void layout(int clientWidth, int clientHeight,
            Set<ClientVertex> lockedVertices) {
        LayoutEngine.layout(graph, clientWidth, clientHeight, lockedVertices);
    }

    public boolean removeVertex(ClientVertex v) {
        return graph.removeVertex(v);
    }
}
