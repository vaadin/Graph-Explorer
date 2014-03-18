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
 * Client-side proxy for a server-side graph arc between two nodes.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class ArcProxy extends IndexedElement {
    private static final long serialVersionUID = 1L;

	private String type;
	private String label;
	private boolean group = false;
	private String fromNode;
	private String toNode;
    private String style = null;

    public ArcProxy() {
    	super();
    }

	public ArcProxy(String id, String type) {
		super(id);
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}

	public boolean isGroup() {
		return group;
	}

	public String getFromNode() {
		return fromNode;
	}

	public void setFromNode(String fromNode) {
		this.fromNode = fromNode;
	}

	public String getToNode() {
		return toNode;
	}

	public void setToNode(String toNode) {
		this.toNode = toNode;
	}

    public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Arc[").append(getId()).append("] ");
    	sb.append('"').append(getLabel()).append('"');
    	sb.append(getFromNode()).append(" -> ").append(getToNode());
    	return sb.toString();
    }
}
