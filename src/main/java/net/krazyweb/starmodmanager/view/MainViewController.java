package net.krazyweb.starmodmanager.view;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javafx.concurrent.Task;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.starmodmanager.data.DatabaseFactory;
import net.krazyweb.starmodmanager.data.DatabaseModelInterface;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.ModList;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainViewController extends Observable {
	
	private static final Logger log = LogManager.getLogger(MainViewController.class);

	private MainView view;
	private ModListView modListView;
	private SettingsView settingsView;
	private AboutView aboutView;
	
	private ModList modList;
	
	private boolean dragOver = false;

	private SettingsModelInterface settings;
	private DatabaseModelInterface database;
	private LocalizerModelInterface localizer;
	
	protected MainViewController(final ModList modList) {
		
		view = new MainView(this);
		view.build();
		
		settings = new SettingsFactory().getInstance();
		database = new DatabaseFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		
		modListView = new ModListView(modList);
		settingsView = new SettingsView();
		aboutView = new AboutView();
		
		view.setContent(modListView.getContent());
		
		view.show();
		
		this.modList = modList;
		
	}
	
	protected void modTabClicked() {
		view.setContent(modListView.getContent());
	}
	
	protected void backupsTabClicked() {
		MessageDialogue m = new MessageDialogue("This feature will be available in Version 2.1.0.", "Coming Soon!", MessageType.INFO, new LocalizerFactory());
		log.debug(m.getResult());
	}
	
	protected void settingsTabClicked() {
		view.setContent(settingsView.getContent());
	}
	
	protected void aboutTabClicked() {
		view.setContent(aboutView.getContent());
		((VBox) aboutView.getContent()).prefHeightProperty().bind(view.getContent().heightProperty());
		log.debug(view.getContent().heightProperty());
		log.debug(((VBox) aboutView.getContent()).heightProperty());
	}
	
	protected void backupButtonClicked() {
		
	}
	
	protected void lockButtonClicked() {
		modListView.toggleLock();
		//TODO Update lock image
	}
	
	protected void refreshButtonClicked() {
		modListView.getNewMods();
	}
	
	protected void expandButtonClicked() {
		modListView.toggleExpansion();
		//TODO Update expand image
	}
	
	protected void filesDraggedOver(final DragEvent event) {
		
		 Dragboard db = event.getDragboard();
         
         if (db.hasFiles()) {

         	boolean filesAccepted = false;
         	String fileName = "";
         	
				for (File file : db.getFiles()) {
					if (FileHelper.isSupported(Paths.get(file.getPath()), dragOver)) {
						filesAccepted = true;
						fileName += localizer.formatMessage(dragOver, "inquotes", file.getName()) + "\n";
					}
				}
         	
				if (filesAccepted) {
					event.acceptTransferModes(TransferMode.COPY);
					if (!dragOver) {
						view.showOverlay(localizer.formatMessage("mainview.addmods", db.getFiles().size()), fileName);
						dragOver = true;
					}
				} else {
					event.consume();
				}
             
         } else {
             event.consume();
         }
         
	}
	
	protected void filesDropped(final DragEvent event) {

		Dragboard db = event.getDragboard();
	   
		boolean success = false;
		
		if (db.hasFiles()) {
			success = true;
			List<Path> toAdd = new ArrayList<>();
			for (File file : db.getFiles()) {
				toAdd.add(file.toPath());
				log.debug("File '{}' dropped on Mod Manager.", file.toPath());
			}
			modList.addMods(toAdd);
		}
		
		event.setDropCompleted(success);
		event.consume();
		
		view.hideOverlay();
		
	}
	
	protected void dragExited() {
		dragOver = false;
		view.hideOverlay();
	}
	
	protected void closeRequested(final WindowEvent event) {
		
		settings.setProperty("windowwidth", view.getScene().getWidth());
		settings.setProperty("windowheight", view.getScene().getHeight());
		
		Task<Void> task = database.getCloseTask();
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.setName("Database Closing Thread");
		thread.start();
		
	}
	
}