package net.krazyweb.starmodmanager.view;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import net.krazyweb.helpers.CSSHelper;
import net.krazyweb.helpers.FXHelper;
import net.krazyweb.starmodmanager.ModManager;
import net.krazyweb.starmodmanager.data.Localizer;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(MainView.class);
	
	private MainViewController controller;

	private Scene scene;
	private VBox root;
	private StackPane stackPane;
	private ScrollPane mainContentPane;
	
	private Text appName;
	private Text versionName;

	private GridPane pageTabs;
	private Button modListButton;
	private Button backupListButton;
	private Button settingsButton;
	private Button aboutButton;
	
	private Button quickBackupButton;
	private Button addModButton;
	private Button lockButton;
	private Button refreshButton;
	private Button expandButton;
	
	private boolean firstRunComplete = false;

	private SettingsModelInterface settings;
	private LocalizerModelInterface localizer;
	
	protected MainView(final MainViewController c) {
		this.controller = c;
		settings = new SettingsFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
	}
	
	protected void build() {
		
		appName = new Text();
		appName.setId("topbar-title");
		versionName = new Text();
		versionName.setId("topbar-version");
		
		AnchorPane.setTopAnchor(appName, 15.0);
		AnchorPane.setBottomAnchor(appName, 15.0);
		AnchorPane.setLeftAnchor(appName, 19.0);
		AnchorPane.setRightAnchor(versionName, 19.0);
		AnchorPane.setTopAnchor(versionName, 15.0);

		AnchorPane topBar = new AnchorPane();
		topBar.getChildren().addAll(appName, versionName);
		topBar.setId("topbar");
		
		root = new VBox();
		root.getChildren().add(topBar);
		
		pageTabs = new GridPane();
		HBox buttons = new HBox();
		
		buildTabs();
		buildActionButtons();
		
		pageTabs.add(modListButton, 0, 0);
		pageTabs.add(backupListButton, 1, 0);
		pageTabs.add(settingsButton, 2, 0);
		pageTabs.add(aboutButton, 3, 0);
		pageTabs.setHgap(36);
		GridPane.setValignment(modListButton, VPos.CENTER);
		GridPane.setHalignment(modListButton, HPos.CENTER);
		GridPane.setValignment(backupListButton, VPos.CENTER);
		GridPane.setHalignment(backupListButton, HPos.CENTER);
		GridPane.setValignment(settingsButton, VPos.CENTER);
		GridPane.setHalignment(settingsButton, HPos.CENTER);
		GridPane.setValignment(aboutButton, VPos.CENTER);
		GridPane.setHalignment(aboutButton, HPos.CENTER);
		
		buttons.setSpacing(25);
		buttons.getChildren().addAll(
			//quickBackupButton, TODO Implement later
			addModButton,
			lockButton,
			refreshButton,
			expandButton
		);
		
		AnchorPane.setLeftAnchor(pageTabs, 35.0);
		AnchorPane.setTopAnchor(pageTabs, 29.0);
		AnchorPane.setBottomAnchor(pageTabs, 25.0);

		AnchorPane.setTopAnchor(buttons, 29.0);
		AnchorPane.setRightAnchor(buttons, 24.0);

		AnchorPane tabsBar = new AnchorPane();
		tabsBar.setId("tabsbar");
		tabsBar.getChildren().addAll(pageTabs, buttons);
		
		mainContentPane = new ScrollPane();
		mainContentPane.setFitToHeight(true);
		mainContentPane.setFitToWidth(true);
		mainContentPane.setFocusTraversable(false);
		mainContentPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		mainContentPane.setHbarPolicy(ScrollBarPolicy.NEVER);

		VBox v = new VBox();
		v.getChildren().add(mainContentPane);
		v.setPadding(new Insets(20, 20, 20, 1));
		
		VBox.setVgrow(mainContentPane, Priority.ALWAYS);
		VBox.setVgrow(v, Priority.ALWAYS);
		
		root.getChildren().add(tabsBar);
		root.getChildren().add(v);
		
		stackPane = new StackPane();
		stackPane.getChildren().add(root);
		
		scene = new Scene(stackPane,
				Math.max(settings.getPropertyDouble("windowwidth"), settings.getPropertyDouble("enforcedminwidth")),
				Math.max(settings.getPropertyDouble("windowheight"), settings.getPropertyDouble("enforcedminheight")));
		scene.getStylesheets().add(MainView.class.getClassLoader().getResource("theme_base.css").toString());
		scene.getStylesheets().add(MainView.class.getClassLoader().getResource(settings.getPropertyString("theme")).toString());
		
		Stage stage = ModManager.getPrimaryStage();
		
		/*
		 * Similar to pre-rendering the views of each tab, to set the
		 * correct window size (including borders, making it an unknown value)
		 * to maintain a minimum canvas size (known), an empty scene with the
		 * wanted values must be rendered in a fully transparent window.
		 * The stage width/height is then the real minimum size that is
		 * desired, so that value is plugged in for future use.
		 * Once all that is done, hide the stage, change the opacity back to 1.0,
		 * then add in the real scene.
		 */
		stage.setOpacity(0.0);
		stage.setScene(new Scene(new VBox(), settings.getPropertyDouble("enforcedminwidth"), settings.getPropertyDouble("enforcedminheight")));
		stage.show();
		
		stage.setMinWidth(stage.getWidth());
		stage.setMinHeight(stage.getHeight());
		
		stage.hide();
		stage.setOpacity(1.0);
		
		stage.setScene(scene);
		
		setSceneEvents(scene, stackPane, root);
		setStageEvents();
		
		updateStrings();
		updateRefreshButton();
		updateAddModButton();
		
	}
	
	private void buildTabs() {

		modListButton = new Button();
		modListButton.setId("pagetab-selected");
		modListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				modListButton.setId("pagetab-selected");
				backupListButton.setId("pagetab");
				settingsButton.setId("pagetab");
				aboutButton.setId("pagetab");
				controller.modTabClicked();
			}
		});
		
		backupListButton = new Button();
		backupListButton.setId("pagetab");
		backupListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				modListButton.setId("pagetab");
				backupListButton.setId("pagetab-selected");
				settingsButton.setId("pagetab");
				aboutButton.setId("pagetab");
				controller.backupsTabClicked();
			}
		});
		
		settingsButton = new Button();
		settingsButton.setId("pagetab");
		settingsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				modListButton.setId("pagetab");
				backupListButton.setId("pagetab");
				settingsButton.setId("pagetab-selected");
				aboutButton.setId("pagetab");
				controller.settingsTabClicked();
			}
		});
		
		aboutButton = new Button();
		aboutButton.setId("pagetab");
		aboutButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				modListButton.setId("pagetab");
				backupListButton.setId("pagetab");
				settingsButton.setId("pagetab");
				aboutButton.setId("pagetab-selected");
				controller.aboutTabClicked();
			}
		});
		
	}
	
	private void buildActionButtons() {
		
		quickBackupButton = new Button();
		quickBackupButton.setGraphic(new ImageView(new Image(MainView.class.getClassLoader().getResourceAsStream("quick-backup-icon.png"))));
		quickBackupButton.setId("mainview-action-button");
		quickBackupButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.backupButtonClicked();
				e.consume();
			}
		});
		
		addModButton = new Button();
		addModButton.setGraphic(new ImageView(new Image(MainView.class.getClassLoader().getResourceAsStream("add-mods-icon.png"))));
		addModButton.setId("mainview-action-button");
		addModButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.addModsButtonClicked();
				e.consume();
			}
		});
		
		lockButton = new Button();
		lockButton.setGraphic(new ImageView(new Image(MainView.class.getClassLoader().getResourceAsStream("unlocked-list-icon.png"))));
		lockButton.setId("mainview-action-button");
		lockButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.lockButtonClicked();
				e.consume();
			}
		});
		
		refreshButton = new Button();
		refreshButton.setGraphic(new ImageView(new Image(MainView.class.getClassLoader().getResourceAsStream("refresh-list-icon.png"))));
		refreshButton.setId("mainview-action-button");
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.refreshButtonClicked();
				e.consume();
			}
		});
		
		expandButton = new Button();
		expandButton.setGraphic(new ImageView(new Image(MainView.class.getClassLoader().getResourceAsStream("expand-list-icon.png"))));
		expandButton.setId("mainview-action-button");
		expandButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.expandButtonClicked();
				e.consume();
			}
		});
		
	}
	
	protected void updateAddModButton() {
		
		Color color = CSSHelper.getColor("mod-list-button-color", settings.getPropertyString("theme"));
		FXHelper.setColor(addModButton.getGraphic(), color);
		
	}
	
	protected void updateLockButton(final boolean locked) {
		
		if (locked) {
			
			lockButton.setGraphic(new ImageView(new Image(MainView.class.getClassLoader().getResourceAsStream("locked-list-icon.png"))));
			
			Color color = CSSHelper.getColor("mod-list-locked-button-color", settings.getPropertyString("theme"));
			FXHelper.setColor(lockButton.getGraphic(), color);
			
		} else {
			
			lockButton.setGraphic(new ImageView(new Image(MainView.class.getClassLoader().getResourceAsStream("unlocked-list-icon.png"))));
			
			Color color = CSSHelper.getColor("mod-list-button-color", settings.getPropertyString("theme"));
			FXHelper.setColor(lockButton.getGraphic(), color);
			
		}
		
	}
	
	protected void updateRefreshButton() {
		
		Color color = CSSHelper.getColor("mod-list-button-color", settings.getPropertyString("theme"));
		FXHelper.setColor(refreshButton.getGraphic(), color);
		
		if (!firstRunComplete) {
			firstRunComplete = true;
			return;
		}
		
		Timeline refreshAnimation = new Timeline();
		refreshAnimation.setCycleCount(1);
		
		refreshAnimation.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				refreshButton.setRotate(0);
			}
		});
		
		final KeyValue kv1 = new KeyValue(refreshButton.rotateProperty(), 1440, Interpolator.EASE_BOTH);
		final KeyFrame kf1 = new KeyFrame(Duration.millis(4500), kv1);
		refreshAnimation.getKeyFrames().addAll(kf1);
		refreshAnimation.playFromStart();
		
	}
	
	protected void updateExpandButton(final boolean expanded) {
		
		if (expanded) {
			expandButton.setGraphic(new ImageView(new Image(MainView.class.getClassLoader().getResourceAsStream("collapse-list-icon.png"))));
		} else {
			expandButton.setGraphic(new ImageView(new Image(MainView.class.getClassLoader().getResourceAsStream("expand-list-icon.png"))));
		}
		
		Color color = CSSHelper.getColor("mod-list-button-color", settings.getPropertyString("theme"));
		FXHelper.setColor(expandButton.getGraphic(), color);
		
	}
	
	private void setSceneEvents(final Scene scene, final StackPane stackPane, final VBox root) {
		
		scene.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
               controller.filesDraggedOver(event);
			}
			
		});
		
		scene.setOnDragExited(new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
				controller.dragExited();
			}
			
		});
		
		scene.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
				controller.filesDropped(event);
			}
			
		});
		
	}
	
	private void setStageEvents() {
		
		ModManager.getPrimaryStage().setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(final WindowEvent event) {
				controller.closeRequested(event);
			}
			
		});
		
	}
	
	protected void showOverlay(final String messageTitle, final String message) {
		Text textTitle = new Text(messageTitle);
		Text textMessage = new Text(message);
		textTitle.setId("add-mods-overlay-text");
		textMessage.setId("add-mods-overlay-text");
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().addAll(textTitle, textMessage);
		stackPane.getChildren().addAll(new Rectangle(root.getWidth(), root.getHeight(), new Color(0.0, 0.0, 0.0, 0.8)), box);
	}
	
	protected void hideOverlay() {
		stackPane.getChildren().clear();
		stackPane.getChildren().add(root);
	}
	
	protected void show() {
		ModManager.getPrimaryStage().show();
	}
	
	protected void setContent(final Node content) {
		mainContentPane.setContent(content);
	}
	
	protected ScrollPane getContent() {
		return mainContentPane;
	}
	
	protected Scene getScene() {
		return scene;
	}

	private void updateStrings() {
		
		ModManager.getPrimaryStage().setTitle(localizer.formatMessage("windowtitle", settings.getVersion()));
		
		appName.setText(localizer.getMessage("appname").toUpperCase());
		versionName.setText(settings.getVersion());

		modListButton.setText(localizer.getMessage("navbartabs.mods"));
		backupListButton.setText(localizer.getMessage("navbartabs.backups"));
		settingsButton.setText(localizer.getMessage("navbartabs.settings"));
		aboutButton.setText(localizer.getMessage("navbartabs.about"));

		modListButton.setEllipsisString(localizer.getMessage("navbartabs.mods"));
		backupListButton.setEllipsisString(localizer.getMessage("navbartabs.backups"));
		settingsButton.setEllipsisString(localizer.getMessage("navbartabs.settings"));
		aboutButton.setEllipsisString(localizer.getMessage("navbartabs.about"));
		
		/*
		 * JavaFX's GridPane pushes around elements when the size of a column changes.
		 * When changing the styling of the text for highlighted buttons, this becomes
		 * a problem, as the whole layout shifts every click. To get around this, it's
		 * necessary to compute the pixel width of the text in each button, then
		 * constrain the columns to be the maximum size of the text-no more, no less.
		 * Accurate HGaps can then be applied and the elements will not move around.
		 * 
		 * To compute the actual size of the text in the scene, it's necessary to
		 * create an invisible stage, add a Text node with the font and size
		 * equivalent to the stylesheet's, then get the node's layout width.
		 */
		ColumnConstraints col1 = new ColumnConstraints();
		ColumnConstraints col2 = new ColumnConstraints();
		ColumnConstraints col3 = new ColumnConstraints();
		ColumnConstraints col4 = new ColumnConstraints();

		Text test = new Text();
		test.setFont(Font.loadFont(MainView.class.getClassLoader().getResourceAsStream("Lato-Medium.ttf"), 18));
		test.setId("pagetab-selected");

		VBox t = new VBox();
		t.getChildren().add(test);
		
		Stage s = new Stage();
		s.setOpacity(0);
		s.setScene(new Scene(t, 500, 500));
		s.show();

		test.setText(localizer.getMessage("navbartabs.mods"));
		col1.setPrefWidth(test.getLayoutBounds().getWidth());
		test.setText(localizer.getMessage("navbartabs.backups"));
		col2.setPrefWidth(test.getLayoutBounds().getWidth());
		test.setText(localizer.getMessage("navbartabs.settings"));
		col3.setPrefWidth(test.getLayoutBounds().getWidth());
		test.setText(localizer.getMessage("navbartabs.about"));
		col4.setPrefWidth(test.getLayoutBounds().getWidth());
		
		s.close();
		
		pageTabs.getColumnConstraints().clear();
		pageTabs.getColumnConstraints().addAll(col1, col2, col3, col4);
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		}
		
	}
	
}
