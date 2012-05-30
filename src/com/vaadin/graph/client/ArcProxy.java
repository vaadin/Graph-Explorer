/*
 * Copyright 2011 Vaadin Ltd.
 *
 * Licensed under the GNU Affero General Public License, Version 3.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/agpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.graph.client;

/**
 * A relationship between nodes in a graph.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public final class ArcProxy extends IndexedElement<ArcController> {
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
