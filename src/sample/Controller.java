package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import sample.datamodel.Contact;
import sample.datamodel.ContactData;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public class Controller {

    @FXML
    private BorderPane mainPanel;

    @FXML
    private TableView<Contact> contactsTable;

    private ContactData data;

    public void initialize() {
        data = new ContactData();
        data.loadContacts();
        contactsTable.setItems(data.getContacts());
    }

    @FXML
    public void showAddContactDialog() {
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Add New Contact");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("contactDialog.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't Load Border Pane");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        newContactDialogAndValidation(dialog, fxmlLoader);

    }

    public void newContactDialogAndValidation(Dialog<ButtonType> dialog, FXMLLoader fxmlLoader) {

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            ContactController contactController = fxmlLoader.getController();
            Contact newContact = contactController.getNewContact();



            if(newContact.getFirstName().equals("") || newContact.getLastName().equals("")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Name Hasn't Been Filled");
                alert.setHeaderText(null);
                alert.setContentText("Please fill out the First name and Last name fields");
                alert.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                alert.showAndWait();

                newContactDialogAndValidation(dialog, fxmlLoader);

            } else if (!newContact.getPhoneNumber().matches("^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$") && newContact.getPhoneNumber().equals("")) {
                // I didn't make the regex expression myself I just searched for one.
                // Formats the regex covers: 123 456 7890, 123-456-7890, 123.456.7890, (123) 456-7890
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Incorrect Number Formatting");
                alert.setHeaderText(null);
                alert.setContentText("The phone number isn't formatted properly \n Please format the contacts phone number In the normal US format or similar");
                alert.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                alert.showAndWait();

                newContactDialogAndValidation(dialog, fxmlLoader);
            }  else if (newContact.getNotes().equals("")) {
                newContact.setNotes("None");
                data.addContact(newContact);
                data.saveContacts();
            } else {
                    data.addContact(newContact);
                    data.saveContacts();
            }
        }
    }

    @FXML
    public void showEditContactDialog() {
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if(selectedContact == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Contact Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please Select The Contact You Want To Edit");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Edit Contact");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("contactdialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        ContactController contactController = fxmlLoader.getController();
        contactController.editContact(selectedContact);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            contactController.updateContact(selectedContact);
            data.saveContacts();
        }
    }

    public void deleteContact() {
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if(selectedContact == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Contact Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select the contact you want to delete");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Contact");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete " + selectedContact.getFirstName() + " " + selectedContact.getLastName() + " contact?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            data.deleteContact(selectedContact);
            data.saveContacts();
        }
    }


}
