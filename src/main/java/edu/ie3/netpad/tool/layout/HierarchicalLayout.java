/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.tool.layout;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.view.mxGraph;

/** Wrapper class to access the results from the {@link mxHierarchicalLayout} algorithm. */
public class HierarchicalLayout extends mxHierarchicalLayout {

  public HierarchicalLayout(mxGraph graph) {
    super(graph);
  }

  public mxGraph getGraph() {
    return graph;
  }
}
