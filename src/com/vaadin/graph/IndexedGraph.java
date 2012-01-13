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

import java.util.HashMap;
import java.util.Map;

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
public class IndexedGraph extends
        DirectedSparseMultigraph<ClientVertex, ClientEdge> {
    private static final long serialVersionUID = 1L;

    private final Map<String, ClientVertex> vertices = new HashMap<String, ClientVertex>();
    private final Map<String, ClientEdge> edges = new HashMap<String, ClientEdge>();

    /**
     * Returns the relationship with the given ID, creating it if it doesn't
     * exist.
     */
    @Override
    public boolean addEdge(ClientEdge edge,
            Pair<? extends ClientVertex> endpoints, EdgeType edgeType) {
        boolean success = super.addEdge(edge, endpoints, edgeType);
        if (success) {
            edges.put(edge.getId(), edge);
        }
        return success;
    }

    @Override
    public boolean addVertex(ClientVertex vertex) {
        boolean success = super.addVertex(vertex);
        if (success) {
            vertices.put(vertex.getId(), vertex);
        }
        return success;
    }

    public boolean containsEdge(String id) {
        return edges.containsKey(id);
    }

    public boolean containsVertex(String id) {
        return vertices.containsKey(id);
    }

    public ClientEdge getEdge(String id) {
        return edges.get(id);
    }

    public ClientVertex getVertex(String id) {
        return vertices.get(id);
    }

    @Override
    public boolean removeEdge(ClientEdge edge) {
        boolean success = super.removeEdge(edge);
        if (success) {
            edges.remove(edge.getId());
        }
        return success;
    }

    @Override
    public boolean removeVertex(ClientVertex vertex) {
        boolean success = super.removeVertex(vertex);
        if (success) {
            vertices.remove(vertex.getId());
        }
        return success;
    }
}
