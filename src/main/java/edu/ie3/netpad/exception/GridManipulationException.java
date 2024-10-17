/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.exception;

public class GridManipulationException extends Exception {
  public GridManipulationException(String message) {
    super(message);
  }

  public GridManipulationException(String message, Throwable cause) {
    super(message, cause);
  }
}
