package com.vaadin.graph.client;

import com.google.gwt.user.client.Random;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.SimpleManagedLayout;
import com.vaadin.graph.GraphExplorer;
import com.vaadin.graph.shared.ArcProxy;
import com.vaadin.graph.shared.GraphExplorerServerRpc;
import com.vaadin.graph.shared.GraphExplorerState;
import com.vaadin.graph.shared.NodeProxy;
import com.vaadin.shared.ui.Connect;

@Connect(GraphExplorer.class)
public class GraphExplorerConnector extends AbstractComponentConnector implements SimpleManagedLayout {

	private static final long serialVersionUID = 1L;

	private GraphExplorerServerRpc rpc = RpcProxy.create(GraphExplorerServerRpc.class, this);

    private int oldHeight = 0;
    private int oldWidth = 0;

    private final GraphProxy graph = new GraphProxy();
    private NodePresenter current;

    @Override
    public GraphExplorerState getState() {
      return (GraphExplorerState) super.getState();
    }

	@Override
	public VGraphExplorer getWidget() {
		return (VGraphExplorer) super.getWidget();
	}

	@Override
	public void layout() {
        int height = getLayoutManager().getOuterHeight(getWidget().getElement());
        int width = getLayoutManager().getOuterWidth(getWidget().getElement());

        Util.setHeightExcludingPaddingAndBorder(getWidget().getElement(), height, 0, true);
        int offsetHeight = getWidget().getOffsetHeight();
        getWidget().canvas.setHeight(offsetHeight);

        Util.setWidthExcludingPaddingAndBorder(getWidget().getElement(), width, 0, true);
        int offsetWidth = getWidget().getOffsetWidth();
        getWidget().canvas.setWidth(offsetWidth);
        
        int newWidth = getWidget().getOffsetWidth();
        int newHeight = getWidget().getOffsetHeight();
        if ((newWidth > 0 && newHeight > 0) && (newWidth != oldWidth || newHeight != oldHeight)) {
           	rpc.clientResized(newWidth, newHeight);
        }
        oldHeight = getWidget().getOffsetHeight();
        oldWidth = getWidget().getOffsetWidth();
	}

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

    	reloadNodes();
    	reloadArcs();
    	
    	if (getState().removedId != null) {
    		graph.removeNode(getState().removedId);
    	}
    	
        for (NodePresenter node : graph.getNodes()) {
            node.onUpdateInModel();
        }
    }

    GraphProxy getGraph() {
        return graph;
    }

	void updateNode(NodeProxy node, int x, int y) {
		rpc.updateNode(node.getId(), node.getState(), x, y);
	}

    void toggle(NodePresenter node) {
    	current = node;
    	if (node != null) {
    		rpc.toggleNode(node.getModel().getId());
    	}
    }

    private void reloadNodes() {
        if (getState().nodes == null) {
            return;
        }
        for (NodeProxy node : getState().nodes) {
            NodePresenter presenter = graph.getNode(node.getId());
            if (presenter == null) {
                presenter = new NodePresenter(this, node);
                if (current == null) {
                	presenter.setX(Random.nextInt(getWidget().getOffsetWidth()));
                	presenter.setY(Random.nextInt(getWidget().getOffsetHeight()));
                } else {
                	presenter.setX(current.getX());
                	presenter.setY(current.getY());
                }
                graph.addNode(presenter);
            } else {
            	presenter.setModel(node);
            }
            presenter.move(node.getX(), node.getY());
        }
    }

    private void reloadArcs() {
        if (getState().arcs == null) {
            return;
        }
        for (ArcProxy arc : getState().arcs) {
            ArcPresenter presenter = graph.getArc(arc.getId());
            if (presenter == null) {
				graph.addArc(new ArcPresenter(this, arc));
				graph.getNode(arc.getFromNode()).addOutArc(arc.getId());
				graph.getNode(arc.getToNode()).addInArc(arc.getId());
            }
        }
    }
}
