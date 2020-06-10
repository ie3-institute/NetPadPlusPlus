/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map;

import com.gluonhq.maps.MapLayer;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 02.06.20
 */
public class CaptionLayer extends MapLayer {

  public CaptionLayer() {
    super();

    this.getChildren().add(createCaption());

    this.markDirty();
  }

  private Node createCaption() {

    final double captionHeight = MapGridElementAttribute.values().length * 20 + 5;
    final double captionWidth = 100.0;
    final double lblPadding = 20.0;

    // caption field
    AnchorPane pane = new AnchorPane();
    Rectangle caption = new Rectangle(captionWidth, captionHeight);
    caption.setFill(new Color(1, 1, 1, 0.75));
    caption.setLayoutX(10);
    caption.setLayoutY(10);
    pane.getChildren().add(caption);

    // caption content
    for (int i = 0; i < MapGridElementAttribute.values().length; i++) {
      MapGridElementAttribute mapGridElementAttribute = MapGridElementAttribute.values()[i];
      Circle c = new Circle(10, 10, 5.0);
      c.setFill(mapGridElementAttribute.getColor());
      Label lbl = new Label(mapGridElementAttribute.getId(), c);
      lbl.setLayoutY(15 + i * lblPadding);
      lbl.setLayoutX(20);
      pane.getChildren().add(lbl);
    }

    return pane;
  }
}
