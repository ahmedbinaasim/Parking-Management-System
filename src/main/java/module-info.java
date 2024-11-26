module com.pms {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;



    opens com.pms.controllers to javafx.fxml; // Fixes the issue
    exports com.pms.controllers;

    opens com.pms to javafx.fxml; // Fixes the issue
    exports com.pms;
}
