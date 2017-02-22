package com.vaadin.graph.shared;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.AbstractComponentState;

public class GraphExplorerState extends AbstractComponentState {

	private static final long serialVersionUID = 1L;

    public List<NodeProxy> nodes = new ArrayList<NodeProxy>();
    public List<ArcProxy> arcs = new ArrayList<ArcProxy>();
    public String removedId;

}
