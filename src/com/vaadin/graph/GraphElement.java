package com.vaadin.graph;

import java.util.Map;

public interface GraphElement {

    String getId();

    String getLabel();

    Map<String, Object> getProperties();

}