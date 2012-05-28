package com.vaadin.graph;

import java.awt.geom.*;
import java.util.*;

import org.apache.commons.collections15.*;

import com.vaadin.graph.client.*;

final class LayoutInitializer implements Transformer<NodeProxy, Point2D> {
    private final int height;
    private final int width;

    public LayoutInitializer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Point2D transform(NodeProxy input) {
        int x = input.getX();
        int y = input.getY();
        return new Point2D.Double(x == -1 ? new Random().nextInt(width) : x,
                y == -1 ? new Random().nextInt(height) : y);
    }
}