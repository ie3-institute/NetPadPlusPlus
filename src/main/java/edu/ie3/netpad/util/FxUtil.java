/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 26.05.20
 */
public class FxUtil {

  public static void addTextLimiter(final TextField tf, final int maxLength) {
    tf.textProperty()
        .addListener(
            (ov, oldValue, newValue) -> {
              if (tf.getText().length() > maxLength) {
                String s = tf.getText().substring(0, maxLength);
                tf.setText(s);
              }
            });
  }

  public static void alert(String alertMsg) {
    Alert alert = new Alert(Alert.AlertType.ERROR, alertMsg, ButtonType.OK);
    alert.showAndWait();

    if (alert.getResult() == ButtonType.OK) {
      alert.close();
    }
  }
}
