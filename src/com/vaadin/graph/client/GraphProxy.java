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

import java.util.*;

/**
 * Client-side proxy of the server-side graph model.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class GraphProxy {

    private final Map<String, NodeProxy> nodes = new HashMap<String, NodeProxy>();
    private final Map<String, ArcProxy> arcs = new HashMap<String, ArcProxy>();
    private final Map<NodeProxy, Set<ArcProxy>> inArcSets = new HashMap<NodeProxy, Set<ArcProxy>>();
    private final Map<NodeProxy, Set<ArcProxy>> outArcSets = new HashMap<NodeProxy, Set<ArcProxy>>();
    private final Map<ArcProxy, NodeProxy> tails = new HashMap<ArcProxy, NodeProxy>();
    private final Map<ArcProxy, NodeProxy> heads = new HashMap<ArcProxy, NodeProxy>();

    /**
     * Adds a new arc from the given tail to the given head.
     * 
     * @return true, if successful; false, otherwise
     */
    public boolean addArc(ArcProxy arc, NodeProxy tail, NodeProxy head) {
        if (arcs.containsKey(arc.id)) {
            return false;
        }

        arcs.put(arc.id, arc);

        heads.put(arc, head);
        inArcSets.get(head).add(arc);

        tails.put(arc, tail);
        outArcSets.get(tail).add(arc);

        return true;
    }

    public boolean addNode(NodeProxy v) {
        if (nodes.containsKey(v.id)) {
            return false;
        }
        nodes.put(v.id, v);
        inArcSets.put(v, new HashSet<ArcProxy>());
        outArcSets.put(v, new HashSet<ArcProxy>());
        return true;
    }

    public void removeNode(String id) {
        if (nodes.containsKey(id)) {
            NodeProxy node = nodes.get(id);
            for (ArcProxy arc : getIncidentArcs(node)) {
                removeArc(arc);
            }
            nodes.remove(id);
            node.notifyRemove();
        }
    }

    private Set<ArcProxy> getIncidentArcs(NodeProxy node) {
        Set<ArcProxy> incidentArcs = new HashSet<ArcProxy>(inArcSets.get(node));
        incidentArcs.addAll(outArcSets.get(node));
        return incidentArcs;
    }

    private void removeArc(ArcProxy e) {
        String id = e.getId();
        boolean success = arcs.containsKey(id);
        if (success) {

            ArcProxy arc = arcs.get(id);

            NodeProxy head = heads.remove(arc);
            inArcSets.get(head).remove(arc);

            NodeProxy tail = tails.remove(arc);
            outArcSets.get(tail).remove(arc);

            arcs.remove(id);
            arc.notifyRemove();
        }
    }

    public boolean containsArc(String id) {
        return arcs.containsKey(id);
    }

    public boolean containsNode(String id) {
        return nodes.containsKey(id);
    }

    public int degree(NodeProxy v) {
        int degree = 0;
        if (inArcSets.containsKey(v)) {
            degree += inArcSets.get(v).size();
        }
        if (outArcSets.containsKey(v)) {
            degree += outArcSets.get(v).size();
        }
        return degree;
    }

    public NodeProxy getDest(ArcProxy e) {
        return heads.get(e);
    }

    public ArcProxy getArc(String id) {
        return arcs.get(id);
    }

    public Collection<ArcProxy> getInArcs(NodeProxy v) {
        Set<ArcProxy> set = inArcSets.get(v);
        if (set == null) {
            set = new HashSet<ArcProxy>();
        }
        return Collections.unmodifiableCollection(set);
    }

    public Collection<NodeProxy> getNeighbors(NodeProxy node) {
        Set<NodeProxy> neighbors = new HashSet<NodeProxy>();
        if (inArcSets.containsKey(node)) {
            for (ArcProxy e : inArcSets.get(node)) {
                neighbors.add(getSource(e));
            }
        }
        if (outArcSets.containsKey(node)) {
            for (ArcProxy e : outArcSets.get(node)) {
                neighbors.add(getDest(e));
            }
        }
        return neighbors;
    }

    public Collection<ArcProxy> getOutArcs(NodeProxy v) {
        Set<ArcProxy> set = outArcSets.get(v);
        if (set == null) {
            set = new HashSet<ArcProxy>();
        }
        return Collections.unmodifiableCollection(set);
    }

    public NodeProxy getSource(ArcProxy e) {
        return tails.get(e);
    }

    public NodeProxy getNode(String id) {
        return nodes.get(id);
    }

    public Collection<NodeProxy> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public void removeNode(NodeProxy node) {
        removeNode(node.getId());
    }
}
