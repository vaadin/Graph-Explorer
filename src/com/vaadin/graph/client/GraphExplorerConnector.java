package com.vaadin.graph.client;

import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.graph.GraphExplorer;
import com.vaadin.shared.ui.Connect;

@Connect(GraphExplorer.class)
public class GraphExplorerConnector extends LegacyConnector {

	  @Override
	  public VGraphExplorer getWidget() {
	    return (VGraphExplorer)super.getWidget();
	  }

}
