package com.vaadin.graph.layout;

import java.awt.Dimension;

import com.vaadin.graph.shared.ArcProxy;
import com.vaadin.graph.shared.NodeProxy;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.Graph;

public class JungCircleLayoutEngine extends JungLayoutEngine {
	private static final long serialVersionUID = 1L;
	
	public JungCircleLayoutEngine() {
		this(new JungLayoutEngineModel());
	}

	public JungCircleLayoutEngine(JungLayoutEngineModel model) {
		super(model);
	}

	@Override
	protected AbstractLayout<NodeProxy, ArcProxy> createLayout(Graph<NodeProxy, ArcProxy> graph, Dimension size) {
		CircleLayout<NodeProxy, ArcProxy> layout = new CircleLayout<NodeProxy, ArcProxy>(graph);
		layout.setSize(size);
		return layout;
	}

}
