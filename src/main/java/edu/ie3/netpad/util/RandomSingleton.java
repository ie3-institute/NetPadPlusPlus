/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.util;

import java.util.Random;

/** Random singleton to have a unique random instance for the whole project */
public class RandomSingleton {

  private final Random random = new Random(1337L);

  private static final class InstanceHolder {
    static final RandomSingleton INSTANCE = new RandomSingleton();
  }

  private RandomSingleton() {}

  public static RandomSingleton getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public static float nextFloat() {
    return getInstance().random.nextFloat();
  }

  public static double nextDouble() {
    return getInstance().random.nextDouble();
  }
}
