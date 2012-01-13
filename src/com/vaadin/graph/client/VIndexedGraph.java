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
package com.vaadin.graph.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.terminal.gwt.client.VConsole;

/**
 * Data structure consisting of nodes with relationships between them.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class VIndexedGraph {

    private final Map<String, ClientVertex> vertices = new HashMap<String, ClientVertex>();
    private final Map<String, ClientEdge> edges = new HashMap<String, ClientEdge>();
    private final Map<ClientVertex, Set<ClientEdge>> inEdgeSets = new HashMap<ClientVertex, Set<ClientEdge>>();
    private final Map<ClientVertex, Set<ClientEdge>> outEdgeSets = new HashMap<ClientVertex, Set<ClientEdge>>();
    private final Map<ClientEdge, ClientVertex> sourceVertices = new HashMap<ClientEdge, ClientVertex>();
    private final Map<ClientEdge, ClientVertex> destVertices = new HashMap<ClientEdge, ClientVertex>();

    public boolean addEdge(ClientEdge e, ClientVertex source, ClientVertex dest) {
        if (edges.containsKey(e.id)) {
            return false;
        }
        edges.put(e.id, e);
        inEdgeSets.get(dest).add(e);
        outEdgeSets.get(source).add(e);
        sourceVertices.put(e, source);
        destVertices.put(e, dest);
        return true;
    }

    public boolean addVertex(ClientVertex v) {
        if (vertices.containsKey(v.id)) {
            return false;
        }
        vertices.put(v.id, v);
        inEdgeSets.put(v, new HashSet<ClientEdge>());
        outEdgeSets.put(v, new HashSet<ClientEdge>());
        return true;
    }

    public boolean containsEdge(String id) {
        return edges.containsKey(id);
    }

    public boolean containsVertex(String id) {
        return vertices.containsKey(id);
    }

    public int degree(ClientVertex v) {
        int degree = 0;
        if (inEdgeSets.containsKey(v)) {
            degree += inEdgeSets.get(v).size();
        }
        if (outEdgeSets.containsKey(v)) {
            degree += outEdgeSets.get(v).size();
        }
        return degree;
    }

    public ClientVertex getDest(ClientEdge e) {
        return destVertices.get(e);
    }

    public ClientEdge getEdge(String id) {
        return edges.get(id);
    }

    public Collection<ClientEdge> getInEdges(ClientVertex v) {
        Set<ClientEdge> set = inEdgeSets.get(v);
        if (set == null) {
            set = new HashSet<ClientEdge>();
        }
        return Collections.unmodifiableCollection(set);
    }

    public Collection<ClientVertex> getNeighbors(ClientVertex node) {
        Set<ClientVertex> neighbors = new HashSet<ClientVertex>();
        if (inEdgeSets.containsKey(node)) {
            for (ClientEdge e : inEdgeSets.get(node)) {
                neighbors.add(getSource(e));
            }
        }
        if (outEdgeSets.containsKey(node)) {
            for (ClientEdge e : outEdgeSets.get(node)) {
                neighbors.add(getDest(e));
            }
        }
        return neighbors;
    }

    public Collection<ClientEdge> getOutEdges(ClientVertex v) {
        Set<ClientEdge> set = outEdgeSets.get(v);
        if (set == null) {
            set = new HashSet<ClientEdge>();
        }
        return Collections.unmodifiableCollection(set);
    }

    public ClientVertex getSource(ClientEdge e) {
        return sourceVertices.get(e);
    }

    public ClientVertex getVertex(String id) {
        return vertices.get(id);
    }

    public Collection<ClientVertex> getVertices() {
        return Collections.unmodifiableCollection(vertices.values());
    }

    public void removeEdge(ClientEdge e) {
        removeEdge(e.getId());
    }

    public boolean removeEdge(String id) {
        boolean success = edges.containsKey(id);
        if (success) {
            ClientEdge e = edges.remove(id);

            VConsole.log("remove " + getSource(e).id + " " + e.getType() + " "
                    + getDest(e).id);

            outEdgeSets.get(sourceVertices.remove(e)).remove(e);
            inEdgeSets.get(destVertices.remove(e)).remove(e);
        }
        return success;
    }

    public boolean removeVertex(String id) {

        VConsole.log("removeVertex(" + id + ")");

        boolean success = vertices.containsKey(id);
        if (success) {
            ClientVertex v = vertices.remove(id);
            for (ClientEdge e : inEdgeSets.remove(v)) {
                removeEdge(e);
            }
            for (ClientEdge e : outEdgeSets.remove(v)) {
                removeEdge(e);
            }
        }
        return success;
    }

    public boolean removeVertex(ClientVertex v) {
        return removeVertex(v.getId());
    }
}
