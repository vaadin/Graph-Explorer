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
package com.vaadin.graph.client;

/**
 * A node in a graph.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public final class NodeProxy extends IndexedElement<NodePresenter> {

    public static final String ID = "id";
    public static final String LABEL = "label";
    public static final String ICONURL = "iconUrl";
    public static final String X = "x";
    public static final String Y = "y";

    public static final String KIND = "kind";
    public static final String NORMAL = "normal";
    public static final String GROUP = "group";
    public static final String EMPTY = "empty";

    public static final String STATE = "state";
    public static final String COLLAPSED = "collapsed";
    public static final String EXPANDED = "expanded";

    private String content = "";
    private String iconUrl = "";
    private int x = -1;
    private int y = -1;
    private String kind = NORMAL;
    private String state = COLLAPSED;
    private int width;
    private int height;

    public NodeProxy(String id) {
        super(id);
    }

    public int getHeight() {
        return height;
    }

    public String getKind() {
        return kind;
    }

    public String getContent() {
        return content;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getState() {
        return state;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public void setState(String state) {
        this.state = state;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return '{' + key(ID) + q(id) + ',' + key(LABEL) + q(content) + ','
        	   + key(ICONURL) + q(iconUrl) + ','
               + key(X) + x + ',' + key(Y) + y + ',' + key(STATE)
               + q(state) + ',' + key(KIND) + q(kind) + '}';
    }
}
