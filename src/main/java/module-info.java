module com.example.workshop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens com.example.workshop to javafx.fxml;
    exports com.example.workshop;
}