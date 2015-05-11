package com.vaadin.graph.shared;

import com.vaadin.graph.shared.NodeProxy.NodeState;
import com.vaadin.shared.communication.ServerRpc;

public interface GraphExplorerServerRpc extends ServerRpc {

	void toggleNode(String nodeId);
	
	void updateNode(String nodeId, NodeState state, int x, int y);
	
	void clientResized(int clientWidth, int clientHeight);

}
