package com.vaadin.graph;

import java.util.Collection;

import com.vaadin.ui.Component;

/**
 * Interface for UI component (popup) used to select visualized/expanded graph
 * nodes
 *
 */
public interface NodeSelector extends Component {

	/**
	 * Answer a collection of node IDs selected by user
	 * 
	 * @return collection of selected node IDs
	 */
	public Collection<String> getSelectedNodeIds();
}
