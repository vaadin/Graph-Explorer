/*
 * Copyright 2011-2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License. 
 */
package com.vaadin.graph.layout;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.graph.LayoutEngineModel;
import com.vaadin.graph.shared.ArcProxy;
import com.vaadin.graph.shared.NodeProxy;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Data structure consisting of nodes with arcs between them.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class JungLayoutEngineModel implements LayoutEngineModel {
	
    private final Map<String, NodeProxy> nodes = new HashMap<String, NodeProxy>();
    private final Map<String, ArcProxy> arcs = new HashMap<String, ArcProxy>();

    private final DirectedSparseMultigraph<NodeProxy, ArcProxy> graph = new DirectedSparseMultigraph<NodeProxy, ArcProxy>() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean addEdge(ArcProxy arc,
                               Pair<? extends NodeProxy> endpoints,
                               EdgeType arcType) {
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
                nodes.put(node.getId(), node);
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
                nodes.remove(node.getId());
            }
            return success;
        }
    };

    @Override
	public void addArc(ArcProxy arc) {
        NodeProxy from = getNode(arc.getFromNode());
		NodeProxy to = getNode(arc.getToNode());
        graph.addEdge(arc, new Pair<NodeProxy>(from, to), EdgeType.DIRECTED);
    }

    @Override
	public boolean addNode(NodeProxy v) {
        return graph.addVertex(v);
    }

    @Override
	public int degree(NodeProxy v) {
        return graph.degree(v);
    }

    @Override
	public ArcProxy getArc(String id) {
        return arcs.get(id);
    }

    @Override
	public Collection<ArcProxy> getArcs() {
        return arcs.values();
    }

    @Override
	public Collection<NodeProxy> getNeighbors(NodeProxy node) {
        return graph.getNeighbors(node);
    }

    @Override
	public NodeProxy getNode(String id) {
        return nodes.get(id);
    }

    @Override
	public Collection<NodeProxy> getNodes() {
        return nodes.values();
    }

    @Override
	public boolean removeNode(NodeProxy v) {
        return graph.removeVertex(v);
    }
    
    public Graph<NodeProxy, ArcProxy> getGraph() {
    	return graph;
    }
}
