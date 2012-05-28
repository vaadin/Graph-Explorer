package com.vaadin.graph;

import java.util.*;

import com.vaadin.graph.GraphController.NodeSelector;
import com.vaadin.graph.client.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.*;

final class ShowHandler implements ClickListener {
    private static final long serialVersionUID = 1L;

    private final Window dialog;
    private final String groupId;
    private final NodeSelector selector;
    private final GraphExplorer parent;

    ShowHandler(GraphExplorer parent, NodeSelector selector, String groupId,
            Window dialog) {
        this.parent = parent;
        this.selector = selector;
        this.groupId = groupId;
        this.dialog = dialog;
    }

    public void buttonClick(ClickEvent event) {
        dialog.getParent().removeWindow(dialog);
        parent.graphController.loadMembers(parent.graph, groupId,
                selector.getSelectedNodeIds());
        Set<NodeProxy> lockedNodes = new HashSet<NodeProxy>();
        if (!parent.graph.containsNode(groupId)) {
            parent.removedId = groupId;
        } else {
            lockedNodes.add(parent.graph.getNode(groupId));
        }
        for (NodeProxy v : parent.graph.getNodes()) {
            if (NodeProxy.EXPANDED.equals(v.getState())) {
                lockedNodes.add(v);
            }
        }
        parent.graph.layout(parent.clientWidth, parent.clientHeight,
                lockedNodes);
        parent.requestRepaint();
    }
}