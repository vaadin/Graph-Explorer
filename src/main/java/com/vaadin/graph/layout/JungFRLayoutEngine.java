package com.vaadin.graph.layout;

import java.awt.Dimension;

import com.vaadin.graph.shared.ArcProxy;
import com.vaadin.graph.shared.NodeProxy;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;

public class JungFRLayoutEngine extends JungLayoutEngine {
	private static final long serialVersionUID = 1L;

	public JungFRLayoutEngine() {
		this(new JungLayoutEngineModel());
	}

	public JungFRLayoutEngine(JungLayoutEngineModel model) {
		super(model);
	}

	protected AbstractLayout<NodeProxy, ArcProxy> createLayout(Graph<NodeProxy, ArcProxy> graph, Dimension size) {
		return new FRLayout<NodeProxy, ArcProxy>(graph, size);
	}
}
