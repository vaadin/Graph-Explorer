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
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String FROM_ID = "fromId";
    public static final String TO_ID = "toId";
    public static final String LABEL = "label";
    public static final String GROUP = "group";
    private final String type;
    private String label;
    private boolean group = false;

    public ArcProxy(String id, String type) {
        super(id);
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
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
}
