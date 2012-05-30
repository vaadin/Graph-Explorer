package com.vaadin.graph;

import java.util.Collection;

import com.vaadin.ui.Component;

interface NodeSelector extends Component {
    Collection<String> getSelectedNodeIds();
}
