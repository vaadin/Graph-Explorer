package com.vaadin.graph;

import org.json.JSONException;
import org.json.JSONObject;

import com.vaadin.graph.client.NodeProxy;

class NodeLoader {
	static void loadFromJSON(NodeProxy node, String json) {
		try {
			JSONObject object = new JSONObject(json);
			String state = object.getString(NodeProxy.STATE);
			node.setState(state);
			node.setX(object.getInt(NodeProxy.X));
			node.setY(object.getInt(NodeProxy.Y));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
