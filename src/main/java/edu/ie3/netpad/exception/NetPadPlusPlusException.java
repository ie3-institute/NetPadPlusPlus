/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.exception;

/**
 * Global exception that is used whenever some non-specific runtime errors are occurring
 *
 * @version 0.1
 * @since 10.06.20
 */
public class NetPadPlusPlusException extends RuntimeException {

  public NetPadPlusPlusException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public NetPadPlusPlusException(final Throwable cause) {
    super(cause);
  }

  public NetPadPlusPlusException(final String message) {
    super(message);
  }
}
