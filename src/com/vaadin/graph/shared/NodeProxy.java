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
package com.vaadin.graph.shared;


/**
 * A node in a graph.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class NodeProxy extends IndexedElement {
    private static final long serialVersionUID = 1L;

    private String content = "";
    private String iconUrl = "";
    private int x = -1;
    private int y = -1;
    private NodeKind kind = NodeKind.NORMAL;
    private NodeState state = NodeState.COLLAPSED;
    private String style = null;

    public NodeProxy() {
    	super();
    }

    public NodeProxy(String id) {
        super(id);
    }

    public NodeKind getKind() {
        return kind;
    }

    public String getContent() {
        return content;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public NodeState getState() {
        return state;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getStyle() {
		return style;
	}

    public void setKind(NodeKind kind) {
        this.kind = kind;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public void setState(NodeState state) {
        this.state = state;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
        
	public void setStyle(String style) {
		this.style = style;
	}

	@Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Node[").append(getId()).append("] ");
    	sb.append('"').append(getContent()).append('"');
    	return sb.toString();
    }

    public enum NodeKind {
        NORMAL,
        GROUP,
        EMPTY;
    }

    public enum NodeState {
        COLLAPSED,
        EXPANDED;    	
    }
}
