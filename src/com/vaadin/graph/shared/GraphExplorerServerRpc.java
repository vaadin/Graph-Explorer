package com.vaadin.graph.shared;

import com.vaadin.shared.communication.ServerRpc;

public interface GraphExplorerServerRpc extends ServerRpc {

	void toggleNode(String nodeId,  int clientWidth, int clientHeight);
	
	void updateNode(String nodeId, String state, int x, int y);
}
