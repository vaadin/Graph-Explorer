package com.vaadin.graph;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.collections15.Transformer;

import com.vaadin.graph.client.ClientEdge;
import com.vaadin.graph.client.ClientVertex;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;

class LayoutEngine {
    static void layout(Graph<ClientVertex, ClientEdge> graph, int width,
            int height, Collection<ClientVertex> lockedVertices) {
        FRLayout<ClientVertex, ClientEdge> layout = new FRLayout<ClientVertex, ClientEdge>(
                graph, new Dimension(width, height));
        layout.lock(false);
        for (ClientVertex v : lockedVertices) {
            layout.lock(v, true);
        }
        layout.setInitializer(new LayoutInitializer(width, height));
        layout.initialize();
        while (!layout.done()) {
            layout.step();
        }
        for (ClientVertex v : graph.getVertices()) {
            Point2D location = layout.transform(v);
            v.setX((int) location.getX());
            v.setY((int) location.getY());
        }
    }

    static final class LayoutInitializer implements
            Transformer<ClientVertex, Point2D> {
        private final int height;
        private final int width;

        public LayoutInitializer(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Point2D transform(ClientVertex input) {
            int x = input.getX();
            int y = input.getY();
            return new Point2D.Double(
                    x == -1 ? new Random().nextInt(width) : x,
                    y == -1 ? new Random().nextInt(height) : y);
        }
    }
}
