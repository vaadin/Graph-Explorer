package com.vaadin.graph;

import net.sf.json.*;

import com.vaadin.graph.client.*;

class NodeLoader {
    static void loadFromJSON(NodeProxy node, Object json) {
        JSONObject object = JSONObject.fromObject(json);
        String state = object.getString(NodeProxy.STATE);
        node.setState(state);
        node.setX(object.getInt(NodeProxy.X));
        node.setY(object.getInt(NodeProxy.Y));
    }
}
