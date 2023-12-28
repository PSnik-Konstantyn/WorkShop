package com.example.workshop;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class WorkController {

    public int craftNumber;
    public String craftLocation;
    public String specialization;
    public int amountOfWorkers;

    @FXML
    public Label locationField;
    @FXML
    public Label areWeTheBestField;
    @FXML
    public Button update;
    @FXML
    public TableColumn nameProdColumn;
    @FXML
    public TableColumn quantityProdColumn;
    @FXML
    public Button show;
    @FXML
    public TableColumn nameColumn;
    @FXML
    public TableColumn quantityColumn;
    @FXML
    public TextArea componentArea;
    @FXML
    public TextArea productionArea;
    @FXML
    public DatePicker startDatePicker;
    @FXML
    public DatePicker endDatePicker;
    public TextField newProductNameField;
    public TextArea newProductDescriptionArea;
    public ComboBox productComboBox;
    public TextField newComponentNameField;
    public TextField newComponentQuantityField;
    @FXML
    private Label specializationField;
    @FXML
    private Label workersCountField;
    @FXML
    private Label productsCountField;
    @FXML
    private RadioButton ourCraftRadioButton;
    @FXML
    private RadioButton allCraftsRadioButton;
    @FXML
    private TableView<Production> productTableView;
    @FXML
    private TableColumn<String, String> productNameColumn;
    @FXML
    private TableColumn<String, Integer> productionAmountColumn;

    @FXML
    private TableView<Component> componentTableView;
    @FXML
    private TableColumn<String, String> componentNameColumn;
    @FXML
    private TableColumn<String, Integer> componentQuantityColumn;

    @FXML
    private TextField editSpecializationField;
    @FXML
    private TextField editWorkersCountField;
    @FXML
    private TextField editLocationField;
    @FXML
    private TextField productNameField;
    @FXML
    private TextField quantityField;

    @FXML
    private ToggleGroup radioButtonGroup;

    public WorkController() {

    }

    @FXML
    public void initial(Stage stage, int craftNumber, String location, String specialization, int amountOfWorkers) throws IOException {
        this.specialization = specialization;
        this.amountOfWorkers = amountOfWorkers;
        this.craftLocation = location;
        this.craftNumber = craftNumber;
        radioButtonGroup = new ToggleGroup();
        ourCraftRadioButton.setToggleGroup(radioButtonGroup);
        allCraftsRadioButton.setToggleGroup(radioButtonGroup);
        int producedQuantity = getProducedQuantityForCraft(craftNumber);
        locationField.setText("Доброго вечора ми з " + location);
        specializationField.setText("Наша спеціалізація: " + specialization);
        workersCountField.setText("Всього працівників:" + amountOfWorkers);
        productsCountField.setText("Цех номер " + craftNumber + " виготовив " + producedQuantity + " одиниць продукції.");
        int craftBest = getCraftWithMaxProduction();
        if (craftBest == craftNumber) {
            areWeTheBestField.setText("Ми найкращий цех!");
        } else {
            areWeTheBestField.setText("Треба працювати краще!");
        }
        updateProductComboBox();
    }

    private int getProducedQuantityForCraft(int craftNumber) {
        int producedQuantity = 0;

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");

            String sql = "SELECT SUM(AmountOfProduction) FROM Task WHERE idCraft = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, craftNumber);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    producedQuantity = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return producedQuantity;
    }

    public int getCraftWithMaxProduction() {
        int craftWithMaxProduction = -1;

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");

            String sql = "SELECT idCraft, SUM(AmountOfProduction) AS TotalProduction FROM Task GROUP BY idCraft ORDER BY TotalProduction DESC LIMIT 1";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    craftWithMaxProduction = resultSet.getInt("idCraft");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return craftWithMaxProduction;
    }

    @FXML
    private void deliver(ActionEvent event) {
        String productName = productNameField.getText();
        String quantityStr = quantityField.getText();

        if (productName.isEmpty() || quantityStr.isEmpty()) {
            productNameField.setText("Будь ласка, заповніть всі поля");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);

            try {
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");

                String sql = "INSERT INTO Task (data, idCraft, idProduction, AmountOfProduction) " +
                        "VALUES (?, ?, (SELECT idProduction FROM Production WHERE Name = ?), ?)";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                    statement.setInt(2, this.craftNumber);
                    statement.setString(3, productName);
                    statement.setInt(4, quantity);

                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                productNameField.setText("Щось пішло не так! Введіть корректну назву та спробуйте ще.");
                e.printStackTrace();
            }
            productNameField.clear();
            quantityField.clear();

            productNameField.setText("Замовлення успішно виконано!");
        } catch (NumberFormatException e) {
            productNameField.setText("Будь ласка, введіть коректну кількість товару");
        }
    }


    public void updateB(ActionEvent actionEvent) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");
            try {
                String query = "SELECT Location, AmountOfWorkers, Specialization FROM Craft WHERE idCraft = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, String.valueOf(craftNumber));
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String location = resultSet.getString("Location");
                    int workersCount = resultSet.getInt("AmountOfWorkers");
                    String specialization = resultSet.getString("Specialization");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("work.fxml"));
                    Parent root = loader.load();
                    Stage currentStage = (Stage) update.getScene().getWindow();
                    currentStage.close();
                    WorkController workController = loader.getController();
                    workController.initial(new Stage(), craftNumber, location, specialization, workersCount);

                    Stage workStage = new Stage();
                    workStage.setTitle("Work Window");
                    workStage.setScene(new Scene(root, 800, 700));
                    workStage.show();
                }


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateInfo(ActionEvent event) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");

            String updateSql = "UPDATE Craft SET " +
                    "Specialization = COALESCE(?, Specialization), " +
                    "AmountOfWorkers = COALESCE(?, AmountOfWorkers), " +
                    "Location = COALESCE(?, Location) " +
                    "WHERE idCraft = ?";

            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                setStringOrNull(statement, 1, editSpecializationField.getText());
                setIntOrNull(statement, 2, parseIntegerField(editWorkersCountField.getText()));
                setStringOrNull(statement, 3, editLocationField.getText());
                statement.setInt(4, craftNumber);
                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    editWorkersCountField.clear();
                    editLocationField.clear();
                    editSpecializationField.setText("Інформацію успішно оновлено!");
                } else {
                    editWorkersCountField.clear();
                    editLocationField.clear();
                    editSpecializationField.setText("Інформацію не вдалося оновити.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setStringOrNull(PreparedStatement statement, int parameterIndex, String value) throws SQLException {
        if (value != null && !value.isEmpty()) {
            statement.setString(parameterIndex, value);
        } else {
            statement.setNull(parameterIndex, Types.VARCHAR);
        }
    }

    private void setIntOrNull(PreparedStatement statement, int parameterIndex, Integer value) throws SQLException {
        if (value != null) {
            statement.setInt(parameterIndex, value);
        } else {
            statement.setNull(parameterIndex, Types.INTEGER);
        }
    }

    private Integer parseIntegerField(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML
    private void showZvit() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");

        String query;
        Date startDate = Date.valueOf(startDatePicker.getValue());
        Date endDate = Date.valueOf(endDatePicker.getValue());

        if (ourCraftRadioButton.isSelected()) {
            query = "SELECT p.Name, SUM(mt.AmountOfProduction) as Total " +
                    "FROM Production p " +
                    "JOIN Task mt ON p.idProduction = mt.idProduction " +
                    "WHERE mt.idCraft = ? AND mt.data BETWEEN ? AND ? " +
                    "GROUP BY p.Name ORDER BY Total DESC";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, craftNumber);
            preparedStatement.setDate(2, startDate);
            preparedStatement.setDate(3, endDate);

            ResultSet resultSet = preparedStatement.executeQuery();

            ObservableList<Production> productionData = FXCollections.observableArrayList();
            while (resultSet.next()) {
                productionData.add(new Production(resultSet.getString("Name"), resultSet.getInt("Total")));
            }
            nameProdColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            quantityProdColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            productTableView.setItems(productionData);

            resultSet.close();
            preparedStatement.close();

            query = "SELECT c.Name, SUM(c.Quantity * mt.AmountOfProduction) as Total " +
                    "FROM Components c " +
                    "JOIN Task mt ON c.idProduct = mt.idProduction " +
                    "WHERE mt.idCraft = ? AND mt.data BETWEEN ? AND ? " +
                    "GROUP BY c.Name ORDER BY Total DESC";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, craftNumber);
            preparedStatement.setDate(2, startDate);
            preparedStatement.setDate(3, endDate);

            resultSet = preparedStatement.executeQuery();

            ObservableList<Component> componentData = FXCollections.observableArrayList();
            while (resultSet.next()) {
                componentData.add(new Component(resultSet.getString("Name"), resultSet.getInt("Total")));
            }
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            componentTableView.setItems(componentData);

            resultSet.close();
            preparedStatement.close();
        } else {

            query = "SELECT p.Name, SUM(mt.AmountOfProduction) as Total " +
                    "FROM Production p " +
                    "JOIN Task mt ON p.idProduction = mt.idProduction " +
                    "WHERE mt.data BETWEEN ? AND ? " +
                    "GROUP BY p.Name ORDER BY Total DESC";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDate(1, startDate);
            preparedStatement.setDate(2, endDate);

            ResultSet resultSet = preparedStatement.executeQuery();

            ObservableList<Production> productionData = FXCollections.observableArrayList();
            while (resultSet.next()) {
                productionData.add(new Production(resultSet.getString("Name"), resultSet.getInt("Total")));
            }
            nameProdColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            quantityProdColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            productTableView.setItems(productionData);

            resultSet.close();
            preparedStatement.close();

            query = "SELECT c.Name, SUM(c.Quantity * mt.AmountOfProduction) as Total " +
                    "FROM Components c " +
                    "JOIN Task mt ON c.idProduct = mt.idProduction " +
                    "WHERE mt.data BETWEEN ? AND ? " +
                    "GROUP BY c.Name ORDER BY Total DESC";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDate(1, startDate);
            preparedStatement.setDate(2, endDate);

            resultSet = preparedStatement.executeQuery();

            ObservableList<Component> componentData = FXCollections.observableArrayList();
            while (resultSet.next()) {
                componentData.add(new Component(resultSet.getString("Name"), resultSet.getInt("Total")));
            }
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            componentTableView.setItems(componentData);

            resultSet.close();
            preparedStatement.close();
        }

        connection.close();
    }

    public void addNewProduct(ActionEvent actionEvent) {
        String productName = newProductNameField.getText();
        String productDescription = newProductDescriptionArea.getText();

        if (productName.isEmpty() || productDescription.isEmpty()) {
            return;
        }

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");

            int newProductId = getNewProductId(connection);
            System.out.println(newProductId);

            String sql = "INSERT INTO Production (idProduction, Name, Description) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, newProductId);
                statement.setString(2, productName);
                statement.setString(3, productDescription);

                statement.executeUpdate();
                newProductNameField.setText("Успішно додано!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        newProductNameField.clear();
        newProductDescriptionArea.clear();
        updateProductComboBox();
    }

    public void addNewComponent(ActionEvent actionEvent) {
        String selectedProduct = (String) productComboBox.getValue();
        String componentName = newComponentNameField.getText();
        String componentQuantityStr = newComponentQuantityField.getText();

        if (selectedProduct == null || componentName.isEmpty() || componentQuantityStr.isEmpty()) {
            return;
        }

        try {
            int componentQuantity = Integer.parseInt(componentQuantityStr);
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");

            int productId = getProductIdByName(selectedProduct);
            System.out.println(selectedProduct + productId);
            int componentId = getNewComponentId();

            String sql = "INSERT INTO Components (idComponent, idProduct, Name, Quantity) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, componentId);
                statement.setInt(2, productId);
                statement.setString(3, componentName);
                statement.setInt(4, componentQuantity);

                statement.executeUpdate();
                newProductDescriptionArea.setText("Успішно додано!");
            }

        } catch (SQLException | NumberFormatException e) {
            newProductDescriptionArea.setText("Щось пішло не так!");
            e.printStackTrace();
        }

        newComponentNameField.clear();
        newComponentQuantityField.clear();
    }

    private int getNewComponentId() throws SQLException {
        int newComponentId = 0;
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");
        try {
            String sql = "SELECT MAX(idComponent) + 1 AS newId FROM Components";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    newComponentId = resultSet.getInt("newId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newComponentId;
    }


    public class Production {
        private String name;
        private int quantity;

        public Production(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public class Component {
        private String name;
        private int quantity;

        public Component(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }



    private int getNewProductId(Connection connection) {
        int newProductId = 0;

        try {
            String sql = "SELECT MAX(idProduction) + 1 AS newId FROM Production";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    newProductId = resultSet.getInt("newId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newProductId;
    }

    private void updateProductComboBox() {
        productComboBox.getItems().clear();

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");

            String sql = "SELECT Name FROM Production";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    productComboBox.getItems().add(resultSet.getString("Name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private int getProductIdByName(String productName) {
        int productId = -1;

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");

            String sql = "SELECT idProduction FROM Production WHERE Name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, productName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    productId = resultSet.getInt("idProduction");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productId;
    }

}
