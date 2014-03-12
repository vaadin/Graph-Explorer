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
import com.vaadin.graph.shared.ArcProxy;
import com.vaadin.graph.shared.GraphExplorerServerRpc;
import com.vaadin.graph.shared.GraphExplorerState;
import com.vaadin.graph.shared.NodeProxy;
import com.vaadin.shared.ui.Connect;

@Connect(GraphExplorer.class)
public class GraphExplorerConnector extends AbstractComponentConnector implements SimpleManagedLayout {

	private static final long serialVersionUID = 1L;

	private GraphExplorerServerRpc rpc = RpcProxy.create(GraphExplorerServerRpc.class, this);

    private boolean initialized;
    private int oldHeight;
    private int oldWidth;

    private final GraphProxy graph = new GraphProxy();
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
    		graph.removeNode(getState().removedId);
    	}
    	
        for (NodePresenter node : graph.getNodes()) {
            node.onUpdateInModel();
        }
    }

    private void initWidget() {
        Collection<NodePresenter> nodes = graph.getNodes();
        int newWidth = getWidget().getOffsetWidth();
        int newHeight = getWidget().getOffsetHeight();
        if (newWidth > 0 && newHeight > 0) {
            if (!initialized && nodes.size() == 1) {
            	toggle(nodes.iterator().next().getModel());
                initialized = true;
            } else if (newWidth != oldWidth || newHeight != oldHeight) {
            	toggle(null);
            }
        }
    }

    GraphProxy getGraph() {
        return graph;
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
            NodePresenter node = graph.getNode(id);
            if (node == null) {
            	NodeProxy nodeModel = new NodeProxy(id);
                if (current == null) {
                	nodeModel.setX(Random.nextInt(getWidget().getOffsetWidth()));
                	nodeModel.setY(Random.nextInt(getWidget().getOffsetHeight()));
                } else {
                	nodeModel.setX(current.getX());
                	nodeModel.setY(current.getY());
                }
                node = new NodePresenter(this, nodeModel);
                graph.addNode(node);
            }

            node.getModel().setContent(getString(object, NodeProxy.LABEL));
            node.getModel().setIconUrl(getString(object, NodeProxy.ICONURL));
            node.getModel().setState(getString(object, NodeProxy.STATE));
            node.getModel().setKind(getString(object, NodeProxy.KIND));
            node.move(getInt(object, NodeProxy.X), getInt(object, NodeProxy.Y));
        }
    }

    private void parseArcs(String[] arcs) {
        if (arcs == null || arcs.length == 0) {
            return;
        }
        for (String json : arcs) {
            JSONObject object = parseJSON(json);
            String id = getString(object, ArcProxy.ID);
            ArcPresenter arc = graph.getArc(id);
            if (arc == null) {
                ArcProxy arcModel = new ArcProxy(id, getString(object, ArcProxy.TYPE));
				arcModel.setLabel(getString(object, ArcProxy.LABEL));
				arcModel.setGroup(object.get(ArcProxy.GROUP).isBoolean().booleanValue());
                String from = getString(object, ArcProxy.FROM_ID);
				String to = getString(object, ArcProxy.TO_ID);				
				graph.addArc(new ArcPresenter(this, arcModel, from, to));
				graph.getNode(from).addOutArc(id);
				graph.getNode(to).addInArc(id);
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
