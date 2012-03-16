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
package com.vaadin.graph;

import java.util.Collection;

import com.vaadin.graph.client.ClientVertex;
import com.vaadin.ui.Component;

public interface GraphLoader {
    NodeSelector getMemberSelector(GraphModel graph, String groupId);

    void init(GraphModel graph);

    Collection<ClientVertex> loadMembers(GraphModel graph, String groupId,
            Collection<String> memberIds);

    Collection<ClientVertex> loadNeighbors(GraphModel graph, String nodeId);

    public interface NodeSelector extends Component {
        Collection<String> getSelectedNodeIds();
    }
}
