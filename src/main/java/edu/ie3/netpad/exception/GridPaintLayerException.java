/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.exception;

public class GridPaintLayerException extends RuntimeException {
  public GridPaintLayerException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public GridPaintLayerException(final Throwable cause) {
    super(cause);
  }

  public GridPaintLayerException(final String message) {
    super(message);
  }
}
