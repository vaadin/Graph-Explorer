package com.vaadin.graph;

import java.io.Serializable;
import java.util.Collection;

import com.vaadin.graph.shared.NodeProxy;

public interface LayoutEngine extends Serializable {

	public LayoutEngineModel getModel();
	
	public void layout(final int width, final int height, Collection<NodeProxy> lockedNodes);

}
