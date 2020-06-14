/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.main;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import com.gluonhq.attach.util.impl.ServiceFactory;
import edu.ie3.netpad.exception.NetPadPlusPlusException;
import edu.ie3.netpad.main.controller.MainController;
import java.io.*;
import java.util.Optional;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the application.
 *
 * @author mahr
 */
public class NetPadPlusPlus extends Application {

  private static final Logger logger = LoggerFactory.getLogger(NetPadPlusPlus.class);

  private static final String MAPS_BASE_FOLDER = System.getProperty("user.home");
  private static final String MAPS_PRIVATE_FOLDER = "gluon";

  // This is a workaround recommended in the official GitHub repository gluonhq/maps to solve
  // HTTP response 403 when downloading map tiles.
  // TODO: Please check the following links for updates: https://github.com/gluonhq/maps/issues/33
  static {
    String httpAgent = System.getProperty("http.agent");
    if (httpAgent == null) {
      httpAgent =
          "("
              + System.getProperty("os.name")
              + " / "
              + System.getProperty("os.version")
              + " / "
              + System.getProperty("os.arch")
              + ")";
    }
    System.setProperty("http.agent", "Gluon Desktop/1.0.3 " + httpAgent);

    // GluonHQ maps settings
    // define service for desktop
    StorageService storageService =
        new StorageService() {
          @Override
          public Optional<File> getPrivateStorage() {
            try {
              File f = new File(MAPS_BASE_FOLDER, MAPS_PRIVATE_FOLDER);
              if (!f.isDirectory()) {
                f.mkdirs();
              }
              return Optional.of(f);
            } catch (Exception e) {
              return Optional.empty();
            }
          }

          @Override
          public Optional<File> getPublicStorage(String subdirectory) {
            try {
              String home = MAPS_PRIVATE_FOLDER;
              File f;
              if (null == subdirectory) {
                f = new File(home);
              } else {
                f = new File(home, subdirectory);
              }
              return Optional.of(f);
            } catch (Exception e) {
              return Optional.empty();
            }
          }

          @Override
          public boolean isExternalStorageWritable() {
            return true;
          }

          @Override
          public boolean isExternalStorageReadable() {
            return true;
          }
        };

    // define service factory for desktop
    ServiceFactory<StorageService> storageServiceFactory =
        new ServiceFactory<StorageService>() {

          @Override
          public Class<StorageService> getServiceType() {
            return StorageService.class;
          }

          @Override
          public Optional<StorageService> getInstance() {
            return Optional.of(storageService);
          }
        };
    // register service
    Services.registerServiceFactory(storageServiceFactory);
  }

  public static void main(String[] args) {
    logger.trace("begin main");
    if (args.length > 0)
      throw new NetPadPlusPlusException("Providing arguments is currently not supported!");

    launch(args);
    logger.trace("end main");
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    logger.info("Starting NetPad++ ...");

    //   setupGlobalExceptionHandler();

    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
    Parent rootNode = loader.load();
    logger.trace("MainView.fxml as stage loaded!");

    final MainController mainController = loader.getController();

    // Call the mainController initialization method before the window is shown
    primaryStage.addEventHandler(
        WindowEvent.WINDOW_SHOWING, windowEvent -> mainController.postInitialization());

    primaryStage.setTitle("NetPad++");
    primaryStage.setMaximized(true);

    // Confirm dialog before closing the application
    primaryStage.setOnCloseRequest(
        event ->
            System.exit(0) // todo JH enable again and replace with question to clear maps cache
        //            mainController
        //                .getDialogController()
        //                .closeRequestDialog()
        //                .showAndWait()
        //                .ifPresent(
        //                    result -> {
        //                      if (result) {
        //                        System.exit(0);
        //                      } else {
        //                        event.consume();
        //                      }
        //                    })
        );

    Scene scene = new Scene(rootNode);
    primaryStage.setScene(scene);
    logger.trace("showing scene");
    primaryStage.show();

    logger.debug("application start method finished.");
  }

  private void setupGlobalExceptionHandler() {
    // todo JH make this nice

    // todo JH global exception handler needs to reset (everything but grid should be enough due to
    // cascade)
    //  grid in grid controller to clean everything

    // start is called on the FX Application Thread,
    // so Thread.currentThread() is the FX application thread.
    // Setup an exception handling for this thread:
    Thread.currentThread()
        .setUncaughtExceptionHandler(
            (thread, throwable) -> {
              StringBuilder sb = new StringBuilder(throwable.toString());
              for (StackTraceElement ste : throwable.getStackTrace()) {
                sb.append("\n\tat ");
                sb.append(ste);
              }
              String trace = sb.toString();
              Alert alert =
                  new Alert(
                      Alert.AlertType.ERROR,
                      "An unexpected exception occurred: \n" + trace,
                      ButtonType.OK);
              alert.showAndWait();

              if (alert.getResult() == ButtonType.OK) {
                alert.close();
              }
            });
  }

  //  private void enableDebugWindows() {
  //
  //    // console
  //    TextArea consoleTxtArea = new TextArea();
  //    PrintStream ps = new PrintStream(new Console(consoleTxtArea));
  //    System.setOut(ps);
  //    System.setErr(ps);
  //
  ////    Label secondLabel = new Label("I'm a Label on new Window");
  //
  //    StackPane secondaryLayout = new StackPane();
  //    secondaryLayout.getChildren().add(consoleTxtArea);
  //
  //    Scene secondScene = new Scene(secondaryLayout, 230, 100);
  //
  //    // New window (Stage)
  //    Stage newWindow = new Stage();
  //    newWindow.setTitle("Second Stage");
  //    newWindow.setScene(secondScene);
  //
  //    newWindow.show();
  //
  //  }

  //  private class Console extends OutputStream {
  //    private TextArea console;
  //
  //    public Console(TextArea console) {
  //      this.console = console;
  //    }
  //
  //    public void appendText(String valueOf) {
  //      Platform.runLater(() -> console.appendText(valueOf));
  //    }
  //
  //    public void write(int b) throws IOException {
  //      appendText(String.valueOf((char)b));
  //    }
  //  }

  // MOUSE DEBUG
  //        Label secondLabel = new Label("I'm a Label on new Window");
  //        StackPane secondaryLayout = new StackPane();
  //        secondaryLayout.getChildren().add(secondLabel);
  //
  //        Scene secondScene = new Scene(secondaryLayout, 230, 100);
  //
  //        // New window (Stage)
  //        Stage newWindow = new Stage();
  //        newWindow.setTitle("Second Stage");
  //        newWindow.setScene(secondScene);
  //
  //        newWindow.show();
  //
  //        map.setOnMouseMoved(new EventHandler<MouseEvent>() {
  //            @Override public void handle(MouseEvent event) {
  //                String msg =
  //                                "(x: "       + event.getX()      + ", y: "       + event.getY()
  //      + ") -- " +
  //                                "(sceneX: "  + event.getSceneX() + ", sceneY: "  +
  // event.getSceneY()  + ") -- " +
  //                                "(screenX: " + event.getScreenX()+ ", screenY: " +
  // event.getScreenY() + ")";
  //
  //               secondLabel.setText(msg);
  //            }
  //        });

}
