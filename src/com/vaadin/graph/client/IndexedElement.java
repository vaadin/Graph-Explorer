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
 * A graph element with a unique ID.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public abstract class IndexedElement {
    private Controller controller;
    protected final String id;

    IndexedElement(String id) {
        this.id = id;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IndexedElement other = (IndexedElement) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public boolean hasHandler() {
        return controller != null;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        return result;
    }

    public void notifyRemove() {
        if (controller != null) {
            controller.onRemoveFromModel();
        }
    }

    public void notifyUpdate() {
        if (controller != null) {
            controller.onUpdateInModel();
        }
    }

    void setController(Controller controller) {
        this.controller = controller;
    }

    /** Formats the given string for use as a key in a JSON object. */
    static String key(String s) {
        return q(s) + ':';
    }

    /** Quotes the given string in double quotes. */
    static String q(String s) {
        return '"' + s + '"';
    }
}
