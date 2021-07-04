package ru.darvell.gb.cloud.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import ru.darvell.gb.cloud.client.api.FileSender;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ActionController implements Initializable {

    @FXML
    private AnchorPane ap;

    @FXML
    private TextField textFilePath;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label successLabel;

    private File selectedFile;
    private boolean sendProcessActive = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void choose() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        checkSelectedFile(fileChooser.showOpenDialog(ap.getScene().getWindow()));
    }

    private void checkSelectedFile(File file) {
        if (file != null && file.exists()) {
            setFileToSend(file);
        }
    }

    private void setFileToSend(File file) {
        selectedFile = file;
        textFilePath.setText(file.getAbsolutePath());
    }

    public void sendFileAction() {
        if (selectedFile != null && !sendProcessActive) {
            prepareSendingFile();
            new Thread(new FileSender(
                    p -> progressBar.setProgress(p),
                    this::processFinishSendingFile,
                    selectedFile)
            ).start();
        }
    }

    private void prepareSendingFile() {
        sendProcessActive = true;
        progressBar.setProgress(0);
        successLabel.setVisible(false);
    }

    private void processFinishSendingFile() {
        successLabel.setVisible(true);
        sendProcessActive = false;
    }
}
