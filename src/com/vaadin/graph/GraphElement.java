package com.vaadin.graph;

import java.util.*;

public interface GraphElement {

    String getId();

    String getLabel();

    Map<String, Object> getProperties();

}