package net.talaatharb.ragdoll.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class MainWindowController implements Initializable {

	@Setter(value = AccessLevel.PACKAGE)
	@FXML
	private ProgressBar progressBar;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		log.info("Main window loaded");

	}

	
	@FXML
	void newClicked() {
		log.info("New Clicked");
	}

	@FXML
	void openClicked() {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Open Project File");

		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Project Files", "*.ragdoll"));

		File selectedFile = fileChooser.showOpenDialog(null);

		if (selectedFile != null) {
			String absolutePath = selectedFile.getAbsolutePath();
			log.info("Selected file: " + absolutePath);

			new Thread(() -> {
				try {
					Platform.runLater(() -> progressBar.setVisible(true));
					loadProject(absolutePath);
					Platform.runLater(() -> progressBar.setVisible(false));
				} catch (IOException e) {
					log.error("Unable to load file");
				}
			}).start();

		} else {
			log.debug("File selection cancelled.");
		}
	}

	void loadProject(String absolutePath) throws IOException {
		// load project
	}

	@FXML
	void closeClicked() {
		log.info("Close Clicked");
	}

	@FXML
	void saveClicked() {
		log.info("Save Clicked");
	}

	@FXML
	void saveAsClicked() {
		log.info("Save As Clicked");
	}

	@FXML
	void resetClicked() {
		log.info("Reset Clicked");
	}

	@FXML
	void preferencesClicked() {
		log.info("Preferences Clicked");
	}

	@FXML
	void quitClicked() {
		log.info("Quit Clicked");
	}

	@FXML
	void aboutClicked() {
		log.info("About Clicked");
	}

	@FXML
	void rollbackClicked() {
		log.info("Rollback Clicked");
	}

	@FXML
	void cutClicked() {
		log.info("Cut Clicked");
	}

	@FXML
	void copyClicked() {
		log.info("Copy Clicked");
	}

	@FXML
	void pasteClicked() {
		log.info("Paste Clicked");
	}

	@FXML
	void deleteClicked() {
		log.info("Delete Clicked");
	}

}