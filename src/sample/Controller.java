package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.awt.*;
import javafx.scene.layout.VBox;

public class Controller {
    @FXML private TextField UserInput;
    @FXML private TabPane tab1;
    @FXML private TableView tableView;
    @FXML private TableColumn file;
    @FXML private TableColumn actualClass;
    @FXML private TableColumn detectedClass;
    @FXML private TableColumn wordCount;


    @FXML private Text actiontarget;
    private String dirName;
    Scene scene2;
    Stage window;

    public void initialize(){
        file.setCellValueFactory(new PropertyValueFactory<User, String>("file"));
        actualClass.setCellValueFactory(new PropertyValueFactory<User, String>("actualClass"));
        detectedClass.setCellValueFactory(new PropertyValueFactory<User, String>("detectedClass"));
        wordCount.setCellValueFactory(new PropertyValueFactory<User, String>("wordCount"));

    }

    //after the submit button is clicked, saves the text into dirName and closes the tab
    public void handleSubmitButtonAction(ActionEvent actionEvent) {
        System.out.println("Click");
        setDirName(actiontarget.getText());
        tab1.getTabs().remove(0);
    }

    private void setDirName(String str){
        dirName=UserInput.getText();
    }

    private String getDirName(){
        return dirName;
    }

    private class User {

        private String file;

        public User() {}

        public User(String file) {
            this.file = file;
        }

        public String getfile() {
            return file;
        }

        public void setfile(String file) {
            this.file = file;
        }
    }


}



