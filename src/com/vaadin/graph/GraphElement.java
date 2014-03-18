package com.vaadin.graph;

import java.util.Map;

public interface GraphElement {

	/** Constant for element property name responsible for CSS style value */
	String PROPERTY_NAME_STYLE = "style";
	
    String getId();

    String getLabel();

    Map<String, Object> getProperties();
}
