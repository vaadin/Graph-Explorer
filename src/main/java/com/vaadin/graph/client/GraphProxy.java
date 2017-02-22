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
package com.vaadin.graph.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client-side proxy of the server-side graph model.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class GraphProxy {
    private final Map<String, NodePresenter> nodes = new HashMap<String, NodePresenter>();
    private final Map<String, ArcPresenter> arcs = new HashMap<String, ArcPresenter>();

    /**
     * Adds a new arc from the given tail to the given head.
     * 
     * @param arc arc presenter
     * @return true, if successful; false, otherwise
     */
    public boolean addArc(ArcPresenter arc) {
    	String id = arc.getModel().getId();
        if (arcs.containsKey(id)) {
            return false;
        }
        arcs.put(id, arc);
        return true;
    }

    public boolean addNode(NodePresenter node) {
    	String id = node.getModel().getId();
        if (nodes.containsKey(id)) {
            return false;
        }
        nodes.put(id, node);
        return true;
    }

    public void removeNode(String id) {
    	NodePresenter node = nodes.remove(id);
        if (node != null) {
            node.onRemoveFromModel();
        }
    }

    public void removeArc(String id) {
    	ArcPresenter arc = arcs.remove(id);
        if (arc != null) {
            arc.onRemoveFromModel();
        }
    }

    public ArcPresenter getArc(String id) {
        return arcs.get(id);
    }

    public NodePresenter getNode(String id) {
        return nodes.get(id);
    }

    public Collection<NodePresenter> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }
}
