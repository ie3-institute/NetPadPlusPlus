/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.context.event;

import edu.ie3.datamodel.models.UniqueEntity;
import edu.ie3.datamodel.models.input.AssetInput;
import java.util.UUID;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 25.05.20
 */
public abstract class GridContextEventImpl<T extends AssetInput> implements GridContextEvent {

  private final UUID subGridUuid;
  protected final T oldAssetEntity;

  public GridContextEventImpl(UUID subGridUuid, T oldAssetEntity) {
    this.subGridUuid = subGridUuid;
    this.oldAssetEntity = oldAssetEntity;
  }

  @Override
  public UUID getSubGridUuid() {
    return subGridUuid;
  }

  @Override
  public UniqueEntity getOldValue() {
    return oldAssetEntity;
  }
}
