package com.vaadin.graph;

import net.sf.json.JSONObject;

import com.vaadin.graph.client.ClientVertex;

class VertexReader {
    static void readFromJSON(ClientVertex node, Object json) {
        JSONObject object = JSONObject.fromObject(json);
        String state = object.getString(ClientVertex.STATE);
        node.setState(state);
        node.setX(object.getInt(ClientVertex.X));
        node.setY(object.getInt(ClientVertex.Y));
    }
}
