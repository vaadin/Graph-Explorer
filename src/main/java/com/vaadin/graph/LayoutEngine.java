package com.vaadin.graph;

import java.io.Serializable;
import java.util.Collection;

import com.vaadin.graph.shared.NodeProxy;

/**
 * An interface representing graph layout engine
 *
 */
public interface LayoutEngine extends Serializable {

	public LayoutEngineModel getModel();

	/**
	 * Perform the graph layout calculation.
	 * Elements of the model are expected to contain proper positions/dimensions after layout is invoked.
	 * 
	 * @param width graph canvas width
	 * @param height graph canvas height
	 * @param lockedNodes collection of nodes which should not be moved by layout calculation
	 */
	public void layout(final int width, final int height, Collection<NodeProxy> lockedNodes);

}
