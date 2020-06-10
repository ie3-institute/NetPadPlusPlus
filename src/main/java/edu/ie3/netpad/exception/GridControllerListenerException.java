/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.exception;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 10.06.20
 */
public class GridControllerListenerException extends RuntimeException {

  public GridControllerListenerException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public GridControllerListenerException(final Throwable cause) {
    super(cause);
  }

  public GridControllerListenerException(final String message) {
    super(message);
  }
}
