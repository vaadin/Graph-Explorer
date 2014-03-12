package com.vaadin.graph;

import java.io.Serializable;
import java.util.Collection;

import com.vaadin.graph.client.NodeProxy;

public interface LayoutEngine<T extends GraphModel> extends Serializable {

	public void layout(T graphModel, final int width, final int height, Collection<NodeProxy> lockedNodes);

}
