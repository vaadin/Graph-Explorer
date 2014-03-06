package com.vaadin.graph.client;

import java.util.Collection;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Random;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.SimpleManagedLayout;
import com.vaadin.graph.GraphExplorer;
import com.vaadin.graph.shared.GraphExplorerServerRpc;
import com.vaadin.graph.shared.GraphExplorerState;
import com.vaadin.shared.ui.Connect;

@Connect(GraphExplorer.class)
public class GraphExplorerConnector extends AbstractComponentConnector implements SimpleManagedLayout {

	private static final long serialVersionUID = 1L;

	private GraphExplorerServerRpc rpc = RpcProxy.create(GraphExplorerServerRpc.class, this);

    private boolean initialized;
    private int oldHeight;
    private int oldWidth;

    private NodeProxy current;

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

        oldHeight = getWidget().getOffsetHeight();
        Util.setHeightExcludingPaddingAndBorder(getWidget().getElement(), height, 0, true);
        int offsetHeight = getWidget().getOffsetHeight();
        getWidget().canvas.setHeight(offsetHeight);

        oldWidth = getWidget().getOffsetWidth();
        Util.setWidthExcludingPaddingAndBorder(getWidget().getElement(), width, 0, true);
        int offsetWidth = getWidget().getOffsetWidth();
        getWidget().canvas.setWidth(offsetWidth);
        
        initWidget();
	}

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

    	parseNodes(getState().nodes);
    	parseArcs(getState().arcs);
    	
    	if (getState().removedId != null) {
        	getWidget().getGraph().removeNode(getState().removedId);
    	}
    	
        for (NodeProxy node : getWidget().getGraph().getNodes()) {
            node.notifyUpdate();
        }
    }

    private void initWidget() {
        Collection<NodeProxy> nodes = getWidget().getGraph().getNodes();
        int newWidth = getWidget().getOffsetWidth();
        int newHeight = getWidget().getOffsetHeight();
        if (newWidth > 0 && newHeight > 0) {
            if (!initialized && nodes.size() == 1) {
            	toggle(nodes.iterator().next());
                initialized = true;
            } else if (newWidth != oldWidth || newHeight != oldHeight) {
            	toggle(null);
            }
        }
    }

	void updateNode(NodeProxy node) {
		rpc.updateNode(node.getId(), node.getState(), node.getX(), node.getY());
	}

    void toggle(NodeProxy node) {
    	current = node;
     	rpc.toggleNode((node != null) ? node.getId() : "", getWidget().getOffsetWidth(), getWidget().getOffsetHeight());
    }

    private void parseNodes(String[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return;
        }
        for (String json : nodes) {
            JSONObject object = parseJSON(json);
            String id = getString(object, NodeProxy.ID);
            NodeProxy node = new NodeProxy(id);
            if (getWidget().getGraph().addNode(node)) {
                if (current == null) {
                    node.setX(Random.nextInt(getWidget().getOffsetWidth()));
                    node.setY(Random.nextInt(getWidget().getOffsetHeight()));
                } else {
                    node.setX(current.getX());
                    node.setY(current.getY());
                }
                node.setController(new NodePresenter(this, node));
            } else {
                node = getWidget().getGraph().getNode(id);
            }
            node.setContent(getString(object, NodeProxy.LABEL));
            node.setState(getString(object, NodeProxy.STATE));
            node.setKind(getString(object, NodeProxy.KIND));
            node.getController().move(getInt(object, NodeProxy.X),
                                      getInt(object, NodeProxy.Y));
        }
    }

    private void parseArcs(String[] arcs) {
        if (arcs == null || arcs.length == 0) {
            return;
        }
        for (String json : arcs) {
            JSONObject object = parseJSON(json);
            String id = getString(object, ArcProxy.ID);
            if (!getWidget().getGraph().containsArc(id)) {
                ArcProxy arc = new ArcProxy(id,
                                            getString(object, ArcProxy.TYPE));
                if (!getWidget().getGraph().addArc(arc,
                		getWidget().getGraph().getNode(getString(object,
                                                          ArcProxy.FROM_ID)),
                                                          getWidget().getGraph().getNode(getString(object,
                                                          ArcProxy.TO_ID)))) {
                    arc = getWidget().getGraph().getArc(id);
                }
                arc.setLabel(getString(object, ArcProxy.LABEL));
                arc.setGroup(object.get(ArcProxy.GROUP).isBoolean().booleanValue());
                arc.setController(new ArcPresenter(this, arc));
            }
        }
    }


    private static JSONObject parseJSON(String json) {
        return JSONParser.parseLenient(json).isObject();
    }

    private static String getString(JSONObject object, String key) {
        return object.get(key).isString().stringValue();
    }

    private static int getInt(JSONObject object, String key) {
        return (int) object.get(key).isNumber().doubleValue();
    }

}