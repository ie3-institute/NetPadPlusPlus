/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * //ToDo: Class Description
 *
 * @author hiry
 * @version 0.1
 * @since 2019-07-10
 */
public class LangUtil {

  // TODO: implement (see https://simona.ie3.e-technik.tu-dortmund.de/jira/browse/NET-17)

  private static Locale currentLocale;
  private static ResourceBundle messages;

  static {
    LangUtil.initialize();
  }

  public static String SAVE = messages.getString("save");

  private static void initialize() {
    String language = "de";
    String country = "DE";

    currentLocale = new Locale(language, country);
  }
}
