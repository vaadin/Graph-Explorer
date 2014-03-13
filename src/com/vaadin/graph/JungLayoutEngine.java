package com.vaadin.graph;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.collections15.Transformer;

import com.vaadin.graph.shared.ArcProxy;
import com.vaadin.graph.shared.NodeProxy;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;

public abstract class JungLayoutEngine implements LayoutEngine<JungLayoutEngineModel> {

	private static final long serialVersionUID = 1L;

	public void layout(JungLayoutEngineModel graphModel, final int width, final int height, Collection<NodeProxy> lockedNodes) {
        AbstractLayout<NodeProxy, ArcProxy> layout = createLayout(graphModel.getGraph(), new Dimension(width, height));
        layout.lock(false);
        for (NodeProxy v : lockedNodes) {
            layout.lock(v, true);
        }

        layout.setInitializer(new Transformer<NodeProxy, Point2D>() {
            public Point2D transform(NodeProxy input) {
                int x = input.getX();
                int y = input.getY();
                return new Point2D.Double(x == -1 ? new Random().nextInt(width) : x,
                                          y == -1 ? new Random().nextInt(height) : y);
            }
        });

        layout.initialize();
        if (layout instanceof IterativeContext) {
        	while (!((IterativeContext)layout).done()) {
        		((IterativeContext)layout).step();
        	}
        }
        for (NodeProxy v : graphModel.getGraph().getVertices()) {
            Point2D location = layout.transform(v);
            v.setX((int) location.getX());
            v.setY((int) location.getY());
        }
    }
	
	protected abstract AbstractLayout<NodeProxy, ArcProxy> createLayout(Graph<NodeProxy, ArcProxy> graph, Dimension size);
}
