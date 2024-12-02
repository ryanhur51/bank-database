import java.sql.*;
import java.util.*;
import java.io.*;

public class CustomerUtil {
    public static void customerInterface(Scanner in, Connection conn) {
        boolean flag = true;
        String customerId = null;
        String customerName = null;

        // Validate Customer ID before entering the main menu loop
        while (customerId == null) {
            System.out.print("\nEnter your Customer ID: ");
            customerId = in.nextLine().trim();

            // Validate the Customer ID input
            if (!customerId.matches("\\d+")) {
                System.out.println("Invalid Customer ID. Please enter a valid ID (integers only).");
                customerId = null; // Reset for the next attempt
                continue;
            }

            // Validate if the Customer ID exists in the database
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT first_name, last_name FROM customer WHERE ID = ?")) {
                pstmt.setString(1, customerId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    customerName = rs.getString("first_name") + " " + rs.getString("last_name");
                } else {
                    System.out.println("Customer ID not found. Please enter a valid ID.");
                    customerId = null; // Reset for the next attempt
                }
            } catch (SQLException e) {
                System.out.println("An error occurred while retrieving customer data: " + e.getMessage());
                customerId = null; // Reset for the next attempt
            }
        }

        // Main menu loop
        while (flag) {
            System.out.println("\nWelcome, " + customerName + " to the Customer Interface\n-----------------------------------------");
            System.out.println("[1] View Account(s)");
            System.out.println("[2] Open New Account");
            System.out.println("[3] Deposit");
            System.out.println("[4] Withdraw");
            System.out.println("[5] Apply for debit/credit card");
            System.out.println("[6] Pay loans/credit card");
            System.out.println("[7] Buy assets");
            System.out.println("[8] Exit");

            // Validate user choice
            if (in.hasNextInt()) {
                int choice = in.nextInt();
                in.nextLine(); // Clear the newline character

                switch (choice) {
                    case 1:
                        viewAccount(in, conn, customerId);
                        break;
                    case 2:
                        openNewAccount(in, conn, customerId);
                        break;
                    case 3:
                        customerDeposit(in, conn, customerId);
                        break;
                    case 4:
                        customerWithdrawal(in, conn, customerId);
                        break;
                    case 5:
                        CardUtil.applyCard(in, conn, customerId);
                        break;
                    case 6:
                        System.out.println("Paying loans/credit card functionality is under construction.");
                        break;
                    case 7:
                        InvestmentUtil.buyAsset(in, conn, customerId);
                        break;
                    case 8:
                        System.out.println("Exiting Customer Interface...");
                        flag = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please select a valid option.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                in.next(); // Clear invalid input
            }
        }
    }


    public static void viewAccount(Scanner in, Connection conn, String customerId) {
        boolean flag = true;
        do {
            System.out.println("\nWhat account type do you want to view?\n-----------------------------------------");
            System.out.println("[1] Checkings");
            System.out.println("[2] Savings");
            System.out.println("[3] Investment");
            System.out.println("[4] Exit");

            int choice = in.nextInt();
            in.nextLine(); // Consume the newline character

            try {
                String query = null;
                String accountType = null;

                switch (choice) {
                    case 1: // View Checkings
                        accountType = "Checkings";
                        query = "SELECT c.first_name, c.last_name, c.address, " +
                                "a.account_id, a.balance, chk.routing_number " +
                                "FROM customer c " +
                                "INNER JOIN account a ON c.id = a.id " +
                                "INNER JOIN checking chk ON a.account_id = chk.account_id " +
                                "WHERE c.id = ? AND a.account_id = ?";
                        break;
                    case 2: // View Savings
                        accountType = "Savings";
                        query = "SELECT c.first_name, c.last_name, c.address, " +
                                "a.account_id, a.balance, s.interest_rate " +
                                "FROM customer c " +
                                "INNER JOIN account a ON c.id = a.id " +
                                "INNER JOIN saving s ON a.account_id = s.account_id " +
                                "WHERE c.id = ? AND a.account_id = ?";
                        break;
                    case 3: // View Investment
                        accountType = "Investment";
                        query = "SELECT c.first_name, c.last_name, c.address, " +
                                "a.account_id, a.balance " +
                                "FROM customer c " +
                                "INNER JOIN account a ON c.id = a.id " +
                                "INNER JOIN investment i ON a.account_id = i.account_id " +
                                "WHERE c.id = ? AND a.account_id = ?";
                        break;
                    case 4: // Exit
                        flag = false;
                        System.out.println("Exiting...");
                        continue;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        continue;
                }

                if (query != null) {
                    boolean checkInput = true;
                    String accountIdInput = null;

                    while (checkInput) { // Input validation check for account ID
                        System.out.print("Enter the Account ID (must be integers): ");
                        accountIdInput = in.nextLine(); // Using nextLine to make sure scanner is clear

                        if (accountIdInput.matches("\\d+")) {
                            checkInput = false;
                        } else {
                            System.out.println("Invalid input. Please enter a valid Account ID (integers only).");
                        }
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, customerId); // Set the customer ID for the query
                        stmt.setString(2, accountIdInput); // Set the account ID for the query

                        try (ResultSet rs = stmt.executeQuery()) {
                            // Check if results exist
                            if (!rs.next()) {
                                System.out.println("Cannot find an account with the given Customer ID and Account ID.");
                            } else {
                                System.out.println("\n" + accountType + " Account Details:");
                                System.out.println("------------------------------------------------------------------------------------------------------");
                                System.out.printf("%-15s %-15s %-30s %-15s %-15s\n", 
                                                "First Name", "Last Name", "Address", "Account ID", "Balance");

                                do {
                                    String firstName = rs.getString("first_name");
                                    String lastName = rs.getString("last_name");
                                    String address = rs.getString("address");
                                    String accountId = rs.getString("account_id");
                                    double balance = rs.getDouble("balance");

                                    System.out.printf("%-15s %-15s %-30s %-15s $%-14.2f\n", 
                                                    firstName, lastName, address, accountId, balance);

                                    if (choice == 3) { // Fetch asset details for investment accounts
                                        System.out.println("------------------------------------------------------------------------------------------------------");
                                        String assetQuery = "SELECT asset_name, amount FROM asset WHERE account_id = ?";
                                        try (PreparedStatement assetStmt = conn.prepareStatement(assetQuery)) {
                                            assetStmt.setString(1, accountId);
                                            try (ResultSet assetRs = assetStmt.executeQuery()) {
                                                System.out.printf("%-20s %-14s\n", "Asset Name", "Asset Amount");
                                                while (assetRs.next()) {
                                                    String assetName = assetRs.getString("asset_name");
                                                    double assetAmount = assetRs.getDouble("amount");
                                                    
                                                    System.out.printf("%s %-20s $%-14.2f\n", "", assetName, assetAmount);
                                                }
                                            }
                                        } catch (SQLException e) {
                                            System.err.println("Error retrieving asset details: " + e.getMessage());
                                        }
                                    }
                                } while (rs.next());
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving account details: " + e.getMessage());
            }
        } while (flag);
    }


    public static void openNewAccount(Scanner in, Connection conn, String customerId) {
        boolean flag = true;
        do {
            System.out.println("\nWhat type of account would you like to open?");
            System.out.println("[1] Checkings");
            System.out.println("[2] Savings");
            System.out.println("[3] Investments");
            System.out.println("[4] Exit");
            System.out.print("Enter your choice: ");

            int choice = in.nextInt();
            in.nextLine(); // Consume the newline character

            String accountType = "";
            String tableName = "";
            String additionalField = "";
            String additionalColumn = "";

            double interestRate = 0.0; // For Savings account types
            double initialDeposit = 0.0;

            switch (choice) {
                case 1: // Checkings
                    accountType = "Checkings";
                    tableName = "checking";
                    additionalField = "routing_number";
                    additionalColumn = "routing_number";
                    break;
                case 2: // Savings
                    accountType = "Savings";
                    tableName = "saving";
                    additionalField = "interest_rate";
                    additionalColumn = "interest_rate";
                    System.out.print("Enter the interest rate for the savings account: ");
                    interestRate = in.nextDouble();
                    in.nextLine(); // Consume newline
                    break;
                case 3: // Investments
                    accountType = "Investment";
                    tableName = "investment";
                    break;
                case 4: // Exit
                    flag = false;
                    System.out.println("Exiting...");
                    continue;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    continue;
            }

            if (!accountType.equals("")) {
                boolean validInput = false;
                String accountIdInput = "";

                while (!validInput) {
                    System.out.print("Enter the initial deposit amount for the new account: ");
                    accountIdInput = in.nextLine().trim();
                    try {
                        initialDeposit = Double.parseDouble(accountIdInput);
                        if (initialDeposit >= 0) {
                            validInput = true;
                        } else {
                            System.out.println("Deposit must be a positive number. Please try again.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a valid deposit amount.");
                    }
                }

                // Generate a new account ID
                String newAccountId = generateNewAccountId(conn);

                // Insert the account into the account table first
                try {
                    String accountInsertQuery = "INSERT INTO account (account_id, id, balance) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(accountInsertQuery)) {
                        stmt.setString(1, newAccountId);
                        stmt.setString(2, customerId);
                        stmt.setDouble(3, initialDeposit);
                        int affectedRows = stmt.executeUpdate();

                        if (affectedRows > 0) {
                            System.out.println("New " + accountType + " account created successfully! Account ID: " + newAccountId);

                            // Insert into the specific account table (e.g., checking, savings, or investment)
                            if (!tableName.equals("")) {
                                String accountDetailsInsertQuery = "INSERT INTO " + tableName + " (account_id) VALUES (?)";
                                try (PreparedStatement detailsStmt = conn.prepareStatement(accountDetailsInsertQuery)) {
                                    detailsStmt.setString(1, newAccountId);
                                    detailsStmt.executeUpdate();
                                }

                                System.out.println(accountType + " account details added successfully!");
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error creating new account: " + e.getMessage());
                }
            }
        } while (flag);
    }

    public static String generateNewAccountId(Connection conn) {
        String newAccountId = null;
        String query = "SELECT MAX(CAST(account_id AS INT)) AS max_id FROM account";

        try (PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int maxAccountId = rs.getInt("max_id");
                newAccountId = String.valueOf(maxAccountId + 1);
            } 
        } catch (SQLException e) {
            System.err.println("Error generating new account ID: " + e.getMessage());
        }

        return newAccountId;
    }
    public static void customerDeposit(Scanner in, Connection conn, String customerId) {
        try {
            System.out.print("Enter the Account ID for the deposit: ");
            String accountId = in.nextLine().trim();

            System.out.print("Enter the amount to deposit: ");
            double amount = in.nextDouble();
            in.nextLine(); // Consume newline

            // Check if the amount is valid
            if (amount <= 0) {
                System.out.println("Invalid amount. Please enter a positive value.");
                return;
            }

            // SQL query to check if the account belongs to the customer
            String validateAccountQuery = "SELECT balance FROM account WHERE account_id = ? AND id = ?";
            PreparedStatement validateStmt = conn.prepareStatement(validateAccountQuery);
            validateStmt.setString(1, accountId);
            validateStmt.setString(2, customerId);
            ResultSet rs = validateStmt.executeQuery();

            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");

                // Update the balance
                String updateBalanceQuery = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateBalanceQuery);
                updateStmt.setDouble(1, amount);
                updateStmt.setString(2, accountId);

                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Deposit successful. Your new balance is: $" + (currentBalance + amount));
                } else {
                    System.out.println("Error during deposit. Please try again.");
                }

                updateStmt.close();
            } else {
                System.out.println("Account not found or does not belong to you.");
            }

            validateStmt.close();
            rs.close();
        } catch (Exception e) {
            System.out.println("An error occurred during the deposit process: " + e.getMessage());
        }
    }
    public static void customerWithdrawal(Scanner in, Connection conn, String customerId) {
        try {
            System.out.print("Enter the Account ID for the withdrawal: ");
            String accountId = in.nextLine().trim();

            System.out.print("Enter the amount to withdraw: ");
            double amount = in.nextDouble();
            in.nextLine(); // Consume newline

            // Check if the amount is valid
            if (amount <= 0) {
                System.out.println("Invalid amount. Please enter a positive value.");
                return;
            }

            // SQL query to check the account and its balance
            String validateAccountQuery = "SELECT balance FROM account WHERE account_id = ? AND id = ?";
            PreparedStatement validateStmt = conn.prepareStatement(validateAccountQuery);
            validateStmt.setString(1, accountId);
            validateStmt.setString(2, customerId);
            ResultSet rs = validateStmt.executeQuery();

            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");

                // Check if there are sufficient funds
                if (currentBalance >= amount) {
                    // Update the balance
                    String updateBalanceQuery = "UPDATE account SET balance = balance - ? WHERE account_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateBalanceQuery);
                    updateStmt.setDouble(1, amount);
                    updateStmt.setString(2, accountId);

                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Withdrawal successful. Your new balance is $" + (currentBalance - amount));
                    } else {
                        System.out.println("Error during withdrawal. Please try again.");
                    }

                    updateStmt.close();
                } else {
                    System.out.println("Insufficient funds. Your current balance is: $" + currentBalance);
                }
            } else {
                System.out.println("Account not found or does not belong to you.");
            }

            validateStmt.close();
            rs.close();
        } catch (Exception e) {
            System.out.println("An error occurred during the withdrawal process: " + e.getMessage());
        }
    }
}