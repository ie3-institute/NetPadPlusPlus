/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.tool.event;

import edu.ie3.netpad.tool.LineLengthResolutionMode;
import java.util.Objects;
import java.util.Set;

/**
 * An event, that is sent from {@link edu.ie3.netpad.tool.controller.ToolController} to {@link
 * edu.ie3.netpad.grid.controller.GridController} to request a fixing of line length discrepancy
 */
public class FixLineLengthRequestEvent implements ToolEvent {
  private final LineLengthResolutionMode resolutionMode;
  private final Set<Integer> selectedSubnets;

  public FixLineLengthRequestEvent(
      LineLengthResolutionMode resolutionMode, Set<Integer> selectedSubnets) {
    this.resolutionMode = resolutionMode;
    this.selectedSubnets = selectedSubnets;
  }

  public LineLengthResolutionMode getResolutionMode() {
    return resolutionMode;
  }

  public Set<Integer> getSelectedSubnets() {
    return selectedSubnets;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FixLineLengthRequestEvent that = (FixLineLengthRequestEvent) o;
    return resolutionMode == that.resolutionMode && selectedSubnets.equals(that.selectedSubnets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resolutionMode, selectedSubnets);
  }

  @Override
  public String toString() {
    return "FixLineLengthRequestEvent{"
        + "mode="
        + resolutionMode
        + ", selectedSubnets="
        + selectedSubnets
        + '}';
  }
}
