import java.sql.*;
import java.util.*;
import java.io.*;

public class InvestmentUtil{
    public static void buyAsset(Scanner in, Connection conn, String customerId) {
    boolean flag = true;
    do {
        try {
            System.out.println("\nBuy Asset Menu\n-----------------------------------------");
            System.out.println("[1] Select Investment Account and Buy Asset");
            System.out.println("[2] Exit");
            System.out.print("Enter your choice: ");
            int choice = in.nextInt();
            in.nextLine(); // Consume the newline character

            switch (choice) {
                case 1: // Select investment account and buy asset
                    System.out.print("Enter your Investment Account ID (must be integers): ");
                    String accountId = in.nextLine();

                    // Validate the account exists and belongs to the customer
                    String validateAccountQuery = 
                        "SELECT * FROM investment i " +
                        "INNER JOIN account a ON i.account_id = a.account_id " +
                        "WHERE a.id = ? AND i.account_id = ?";
                    try (PreparedStatement validateStmt = conn.prepareStatement(validateAccountQuery)) {
                        validateStmt.setString(1, customerId);
                        validateStmt.setString(2, accountId);

                        try (ResultSet rs = validateStmt.executeQuery()) {
                            if (!rs.next()) {
                                System.out.println("Invalid Investment Account ID. Please try again.");
                                break;
                            }
                        }
                    }

                    // Prompt for asset details
                    System.out.print("Enter the Asset Name (max 15 characters): ");
                    String assetName = in.nextLine();
                    if (assetName.length() > 15) {
                        System.out.println("Asset name too long. Please limit it to 15 characters.");
                        break;
                    }

                    System.out.print("Enter the Asset Amount: ");
                    double assetAmount = in.nextDouble();
                    in.nextLine(); // Consume the newline character

                    // Check account balance and deduct the amount
                    String getBalanceQuery = "SELECT balance FROM account WHERE account_id = ?";
                    try (PreparedStatement getBalanceStmt = conn.prepareStatement(getBalanceQuery)) {
                        getBalanceStmt.setString(1, accountId);

                        try (ResultSet balanceRs = getBalanceStmt.executeQuery()) {
                            if (balanceRs.next()) {
                                double currentBalance = balanceRs.getDouble("balance");

                                if (currentBalance < assetAmount) {
                                    System.out.println("Insufficient balance in the investment account.");
                                    break;
                                }

                                // Deduct balance
                                String updateBalanceQuery = 
                                    "UPDATE account SET balance = balance - ? WHERE account_id = ?";
                                try (PreparedStatement updateBalanceStmt = conn.prepareStatement(updateBalanceQuery)) {
                                    updateBalanceStmt.setDouble(1, assetAmount);
                                    updateBalanceStmt.setString(2, accountId);

                                    int rowsUpdated = updateBalanceStmt.executeUpdate();
                                    if (rowsUpdated > 0) {
                                        System.out.println("Balance updated successfully.");
                                    } else {
                                        System.out.println("Failed to update balance. Transaction aborted.");
                                        break;
                                    }
                                }
                            } else {
                                System.out.println("Investment account not found. Please try again.");
                                break;
                            }
                        }
                    }

                    // Insert the asset into the `asset` table
                    String insertAssetQuery = 
                        "INSERT INTO asset (account_id, asset_name, amount) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertAssetQuery)) {
                        insertStmt.setString(1, accountId);
                        insertStmt.setString(2, assetName);
                        insertStmt.setDouble(3, assetAmount);

                        int rowsAffected = insertStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Asset successfully added!");
                        } else {
                            System.out.println("Failed to add asset. Please try again.");
                        }
                    } catch (SQLException e) {
                        System.err.println("Error adding asset: " + e.getMessage());
                    }
                    break;

                case 2: // Exit
                    flag = false;
                    System.out.println("Exiting...");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("Error during operation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Invalid input. Please try again.");
            in.nextLine(); // Clear invalid input
        }
    } while (flag);
}

}