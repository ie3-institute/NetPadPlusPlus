/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map;

import edu.ie3.datamodel.models.UniqueEntity;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.system.LoadInput;
import edu.ie3.datamodel.models.input.system.PvInput;
import edu.ie3.datamodel.models.input.system.StorageInput;
import edu.ie3.datamodel.models.input.system.WecInput;
import javafx.scene.paint.Color;

/**
 * Contains attributes for map grid graphic elements incl. e.g. their color definition or their name
 *
 * @version 0.1
 * @since 01.06.20
 */
public enum MapGridElementAttribute {
  SLACK(NodeInput.class, "Slack", Color.BLACK),
  PV(PvInput.class, "Photovoltaic", Color.GREEN),
  LOAD(LoadInput.class, "Load", Color.DARKMAGENTA),
  WEC(WecInput.class, "Wec", Color.CADETBLUE),
  STORAGE(StorageInput.class, "Storage", Color.CHOCOLATE);

  private final Class<? extends UniqueEntity> clz;
  private final String id;
  private final Color color;

  MapGridElementAttribute(Class<? extends UniqueEntity> clz, String id, Color color) {
    this.clz = clz;
    this.id = id;
    this.color = color;
  }

  public String getId() {
    return id;
  }

  public Color getColor() {
    return color;
  }

  public Class<? extends UniqueEntity> getClz() {
    return clz;
  }
}
