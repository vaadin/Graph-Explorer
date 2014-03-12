package com.vaadin.graph;

import java.util.Collection;

import com.vaadin.ui.Component;

public interface NodeSelector extends Component {
    Collection<String> getSelectedNodeIds();
}
