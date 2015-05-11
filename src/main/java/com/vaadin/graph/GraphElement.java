package com.vaadin.graph;

import java.util.Map;

/**
 * An interface representing a graph element (Node/Vertex ot Arc/Edge)
 *
 */
public interface GraphElement {

	/** Constant for element property name specifying element's CSS style value */
	public static final String PROPERTY_NAME_STYLE = "_css_style_";

	/**
	 * Answer an element ID
	 * 
	 * @return element ID
	 */
	public String getId();

	/**
	 * Answer an element label (type)
	 * 
	 * @return element label
	 */
	public String getLabel();

	/**
	 * Answer element additional properties (May be used e.g. by UI rendering)
	 * 
	 * @return element additional properties
	 */
	public Map<String, Object> getProperties();
}
