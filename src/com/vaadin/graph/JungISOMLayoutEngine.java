package com.vaadin.graph;

import java.awt.Dimension;

import com.vaadin.graph.shared.ArcProxy;
import com.vaadin.graph.shared.NodeProxy;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.graph.Graph;

public class JungISOMLayoutEngine extends JungLayoutEngine {
	private static final long serialVersionUID = 1L;

	public JungISOMLayoutEngine() {
		this(new JungLayoutEngineModel());
	}

	public JungISOMLayoutEngine(JungLayoutEngineModel model) {
		super(model);
	}

	@Override
	protected AbstractLayout<NodeProxy, ArcProxy> createLayout(Graph<NodeProxy, ArcProxy> graph, Dimension size) {
		ISOMLayout<NodeProxy, ArcProxy> layout = new ISOMLayout<NodeProxy, ArcProxy>(graph);
		layout.setSize(size);
		return layout;
	}

}
