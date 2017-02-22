/*
 * Copyright 2011-2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License. 
 */
package com.vaadin.graph.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.vaadin.graph.shared.NodeProxy;
import com.vaadin.graph.shared.NodeProxy.NodeState;

/**
 * Presenter/controller for a node in a graph.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
class NodePresenter implements Controller, MouseDownHandler, MouseMoveHandler, MouseUpHandler {

	/** Set the CSS class name to allow styling. */
	public static final String CSS_CLASSNAME = "node";

	private final GraphExplorerConnector connector;
	private final Set<String> inArcSets = new HashSet<String>();
	private final Set<String> outArcSets = new HashSet<String>();

	private final HTML view = new HTML();
	private final NodeAnimation animation = new NodeAnimation();

	private NodeProxy model;
	protected int x;
	protected int y;
    private int width;
    private int height;

	private int dragStartX;
	private int dragStartY;
	private boolean mouseDown;
	private boolean dragging;

	NodePresenter(GraphExplorerConnector connector, NodeProxy model) {
		this.connector = connector;
		this.model = model;
		this.x = model.getX();
		this.y = model.getY();

		view.setTitle(model.getId());
		Style style = view.getElement().getStyle();
		style.setLeft(x, Unit.PX);
		style.setTop(y, Unit.PX);

		view.addDomHandler(this, MouseDownEvent.getType());
		view.addDomHandler(this, MouseMoveEvent.getType());
		view.addDomHandler(this, MouseUpEvent.getType());

		connector.getWidget().add(view);
	}

	public void onMouseDown(MouseDownEvent event) {
		setMouseDown(true);
		updateCSS();
		DOM.setCapture(view.getElement());
		dragStartX = event.getX();
		dragStartY = event.getY();
		event.preventDefault();
	}

	public void onMouseMove(MouseMoveEvent event) {
		if (isMouseDown()) {
			setDragging(true);
			updateCSS();
			setX(event.getX() + getX() - dragStartX);
			setY(event.getY() + getY() - dragStartY);
			onUpdateInModel();
			int clientX = event.getClientX();
			int clientY = event.getClientY();
			boolean outsideWindow = clientX < 0 || clientY < 0
					|| clientX > Window.getClientWidth()
					|| clientY > Window.getClientHeight();
			if (outsideWindow) {
				connector.updateNode(model, getX(), getY());
				setDragging(false);
			}
		}
		event.preventDefault();
	}

	public void onMouseUp(MouseUpEvent event) {
		Element element = view.getElement();
		if (!isDragging()) {
			updateCSS();
			limitToBoundingBox();
			if (NodeState.EXPANDED.equals(model.getState())) {
				model.setState(NodeState.COLLAPSED);
				for (NodePresenter neighbor : getNeighbors()) {
					boolean collapsed = NodeState.COLLAPSED.equals(neighbor.getModel().getState());
					boolean leafNode = neighbor.degree() == 1;
					if (collapsed && leafNode) {
						connector.getGraph().removeNode(neighbor.getModel().getId());
					}
				}
			}
			connector.toggle(this);
		} else {
			connector.updateNode(model, getX(), getY());
			setDragging(false);
		}
		setMouseDown(false);
		DOM.releaseCapture(element);
		event.preventDefault();
	}

	public void onRemoveFromModel() {
		for (String each : inArcSets) {
			connector.getGraph().removeArc(each);
		}
		for (String each : outArcSets) {
			connector.getGraph().removeArc(each);
		}
		view.removeFromParent();
	}

	private void limitToBoundingBox() {
		Element element = view.getElement();
		Style style = element.getStyle();

		width = element.getOffsetWidth();
		int xRadius = width / 2;
		int leftEdge = getX() - xRadius;
		leftEdge = limit(0, leftEdge, connector.getWidget().getOffsetWidth() - width);
		setX(leftEdge + xRadius);
		style.setLeft(leftEdge, Unit.PX);

		height = element.getOffsetHeight();
		int yRadius = height / 2;
		int topEdge = getY() - yRadius;
		topEdge = limit(0, topEdge, connector.getWidget().getOffsetHeight() - height);
		setY(topEdge + yRadius);
		style.setTop(topEdge, Unit.PX);
	}

	public void onUpdateInModel() {
		StringBuilder html = new StringBuilder();
		if ((model.getIconUrl() != null) && !model.getIconUrl().isEmpty()) {
			html.append("<div class='icon'>");
			html.append(connector.getConnection().getIcon(model.getIconUrl()).getElement().getString());
			html.append("<div class='label'>").append(model.getContent()).append("</div>");
			html.append("</div>");
		} else {
			html.append("<div class='label'>").append(model.getContent()).append("</div>");
		}
		view.setHTML(html.toString());
		limitToBoundingBox();
		updateCSS();
		updateArcs();
	}

	NodeProxy getModel() {
		return model;
	}

	void setModel(NodeProxy model) {
		this.model = model;
	}

	int getX() {
		return x;
	}

	void setX(int x) {
		this.x = x;
	}

	int getY() {
		return y;
	}

	void setY(int y) {
		this.y = y;
	}

	int getWidth() {
		return width;
	}

	int getHeight() {
		return height;
	}

	void addInArc(String arc) {
		inArcSets.add(arc);
	}

	void addOutArc(String arc) {
		outArcSets.add(arc);
	}

	private void updateCSS() {
		Element element = view.getElement();
		element.setClassName(CSS_CLASSNAME);
		element.addClassName(model.getState().name().toLowerCase());
		element.addClassName(model.getKind().name().toLowerCase());
		if (model.getStyle() != null) {
			element.addClassName(model.getStyle());
		}
		if (isMouseDown()) {
			element.addClassName("down");
		}
	}

	private void updateArcs() {
		for (String each : inArcSets) {
			ArcPresenter arc = connector.getGraph().getArc(each);
			if (arc != null) {
				arc.onUpdateInModel();
			}
		}
		for (String each : outArcSets) {
			ArcPresenter arc = connector.getGraph().getArc(each);
			if (arc != null) {
				arc.onUpdateInModel();
			}
		}
	}

	private Collection<NodePresenter> getNeighbors() {
		Set<NodePresenter> neighbors = new HashSet<NodePresenter>();
		for (String each : inArcSets) {
			ArcPresenter arc = connector.getGraph().getArc(each);
			if (arc != null) {
				NodePresenter node = arc.getFromNode();
				if (node != null) {
					neighbors.add(node);
				}
			}
		}
		for (String each : outArcSets) {
			ArcPresenter arc = connector.getGraph().getArc(each);
			if (arc != null) {
				NodePresenter node = arc.getToNode();
				if (node != null) {
					neighbors.add(node);
				}
			}
		}
		return neighbors;
	}

	private int degree() {
		int degree = 0;
		degree += inArcSets.size();
		degree += outArcSets.size();
		return degree;
	}

	void removeArc(String arc) {
		inArcSets.remove(arc);
		outArcSets.remove(arc);
	}

	/** Limits value to [min, max], so that min <= value <= max. */
	private static int limit(int min, int value, int max) {
		return Math.min(Math.max(min, value), max);
	}

	void move(int x, int y) {
		animation.targetX = x;
		animation.targetY = y;
		animation.run(500);
	}

	private boolean isDragging() {
		return dragging;
	}

	private void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	private boolean isMouseDown() {
		return mouseDown;
	}

	private void setMouseDown(boolean mouseDown) {
		this.mouseDown = mouseDown;
	}

	private class NodeAnimation extends Animation {
		private int targetX = 0;
		private int targetY = 0;

		@Override
		protected void onUpdate(double progress) {
			if (progress > 1) {
				progress = 1;
			}
			setX((int) Math.round(progress * targetX + (1 - progress) * getX()));
			setY((int) Math.round(progress * targetY + (1 - progress) * getY()));
			onUpdateInModel();
		}

		@Override
		protected void onCancel() {
			// do nothing
		}
	}
}
