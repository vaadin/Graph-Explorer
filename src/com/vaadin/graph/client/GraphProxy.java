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

import com.vaadin.terminal.gwt.client.VConsole;

/**
 * Data structure consisting of nodes with relationships between them.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class GraphProxy {

    private final Map<String, NodeProxy> nodes = new HashMap<String, NodeProxy>();
    private final Map<String, ArcProxy> arcs = new HashMap<String, ArcProxy>();
    private final Map<NodeProxy, Set<ArcProxy>> inArcSets = new HashMap<NodeProxy, Set<ArcProxy>>();
    private final Map<NodeProxy, Set<ArcProxy>> outArcSets = new HashMap<NodeProxy, Set<ArcProxy>>();
    private final Map<ArcProxy, NodeProxy> sourceNodes = new HashMap<ArcProxy, NodeProxy>();
    private final Map<ArcProxy, NodeProxy> destNodes = new HashMap<ArcProxy, NodeProxy>();

    public boolean addArc(ArcProxy e, NodeProxy source, NodeProxy dest) {
        if (arcs.containsKey(e.id)) {
            return false;
        }
        arcs.put(e.id, e);
        inArcSets.get(dest).add(e);
        outArcSets.get(source).add(e);
        sourceNodes.put(e, source);
        destNodes.put(e, dest);
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
        return destNodes.get(e);
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
        return sourceNodes.get(e);
    }

    public NodeProxy getNode(String id) {
        return nodes.get(id);
    }

    public Collection<NodeProxy> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public void removeArc(ArcProxy e) {
        removeArc(e.getId());
    }

    public boolean removeArc(String id) {
        boolean success = arcs.containsKey(id);
        if (success) {
            ArcProxy e = arcs.remove(id);

            VConsole.log("remove " + getSource(e).id + " " + e.getType() + " "
                    + getDest(e).id);

            outArcSets.get(sourceNodes.remove(e)).remove(e);
            inArcSets.get(destNodes.remove(e)).remove(e);
        }
        return success;
    }

    public boolean removeNode(String id) {

        VConsole.log("removeNode(" + id + ")");

        boolean success = nodes.containsKey(id);
        if (success) {
            NodeProxy v = nodes.remove(id);
            for (ArcProxy e : inArcSets.remove(v)) {
                removeArc(e);
            }
            for (ArcProxy e : outArcSets.remove(v)) {
                removeArc(e);
            }
        }
        return success;
    }

    public boolean removeNode(NodeProxy v) {
        return removeNode(v.getId());
    }
}
