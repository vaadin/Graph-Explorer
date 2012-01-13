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

public interface GraphProvider {
    /** Gets the vertex that the given edge points to. */
    Vertex getDestination(Edge edge);

    /** Returns a list of all possible edge labels in this graph. */
    Iterable<String> getEdgeLabels();

    /**
     * Gets all edges connected to the given node, with the given label, in the
     * given direction.
     * 
     * @param vertex
     *            return only edges connected to this vertex
     * @param label
     *            return only edges with this label
     * @param dir
     *            INCOMING for edges pointing towards the given vertex, OUTGOING
     *            for edges pointing away from the given vertex
     */
    Collection<Edge> getEdges(Vertex vertex, String label, EdgeDirection dir);

    /** Gets the "origin" of the graph. */
    Vertex getHomeVertex();

    /** Gets the vertex at the other end of the given edge. */
    Vertex getOpposite(Vertex vertex, Edge edge);

    /** Gets the vertex that the given edge points away from. */
    Vertex getSource(Edge edge);

    /**
     * Returns the vertex with the given ID.
     * 
     * @return null if there's no such vertex
     */
    Vertex getVertexById(String id);

    /** Shuts down the graph provider to free up resources. Optional operation. */
    void shutdown();
}
