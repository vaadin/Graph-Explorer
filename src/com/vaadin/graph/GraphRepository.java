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

public interface GraphRepository<N extends Node, A extends Arc> {
    /** Gets the node that the given arc points away from. */
    N getSource(A arc);

    /** Gets the node that the given arc points to. */
    N getDestination(A arc);

    /** Returns a list of all possible arc labels in this graph. */
    Iterable<String> getArcLabels();

    /**
     * Gets all arcs connected to the given node, with the given label, in the
     * given direction.
     * 
     * @param node
     *            return only arcs connected to this node
     * @param label
     *            return only arcs with this label
     * @param dir
     *            INCOMING for arcs pointing towards the given node, OUTGOING
     *            for arcs pointing away from the given node
     */
    Collection<A> getArcs(N node, String label, ArcDirection dir);

    /** Gets the "origin" of the graph. */
    N getHomeNode();

    /** Gets the node at the other end of the given arc. */
    N getOpposite(N node, A arc);

    /**
     * Returns the node with the given ID.
     * 
     * @return null if there's no such node
     */
    N getNodeById(String id);
}
