package com.vaadin.graph;

import java.util.Collection;

import com.vaadin.graph.shared.ArcProxy;
import com.vaadin.graph.shared.NodeProxy;

public interface LayoutEngineModel {

	public abstract boolean removeNode(NodeProxy v);

	public abstract Collection<NodeProxy> getNodes();

	public abstract NodeProxy getNode(String id);

	public abstract Collection<NodeProxy> getNeighbors(NodeProxy node);

	public abstract Collection<ArcProxy> getArcs();

	public abstract ArcProxy getArc(String id);

	public abstract int degree(NodeProxy v);

	public abstract boolean addNode(NodeProxy v);

	public abstract void addArc(ArcProxy arc);

}
