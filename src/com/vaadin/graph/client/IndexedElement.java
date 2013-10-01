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
 * A graph element with a unique ID.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
abstract class IndexedElement<C extends Controller> {
    private C controller;
    protected final String id;

    IndexedElement(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean hasController() {
        return controller != null;
    }

    public void notifyRemove() {
        if (hasController()) {
            controller.onRemoveFromModel();
        }
    }

    public void notifyUpdate() {
        if (hasController()) {
            controller.onUpdateInModel();
        }
    }

    void setController(C controller) {
        this.controller = controller;
    }

    C getController() {
        return controller;
    }

    /** Formats the given string for use as a key in a JSON object. */
    static String key(String s) {
        return q(s) + ':';
    }

    /** Quotes the given string in double quotes. */
    static String q(String s) {
        return '"' + s + '"';
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IndexedElement)) {
            return false;
        }
        IndexedElement<?> other = (IndexedElement<?>) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        return result;
    }
}
