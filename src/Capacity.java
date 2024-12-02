import java.sql.*;
import java.util.*;
import java.io.*;

public class Capacity{
    static final String DB_URL = "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241";

    public static void main(String[]args){
        Connection conn = null;

        Scanner in = new Scanner(System.in);
        do{
            try {
                // Getting user inpur for user and password
                System.out.print("Enter Oracle user ID: ");
                String user = in.nextLine();
                Console console = System.console();

                String pass;
                if (console != null) {
                    char[] passwordChars = console.readPassword("Enter Oracle user password: (Does not display)");
                    pass = new String(passwordChars); // Convert char[] to String
                    conn = DriverManager.getConnection(DB_URL, user, pass);
                } 
                
                System.out.println("Sucess! Connected.");             

                inputRoleLoop(in, conn); // Select roles interface

                conn.close();
            } catch (SQLException se){
                se.printStackTrace();
                System.out.println("Failure...");
            }
        } while (conn == null);
        in.close();
    }
    public static void inputRoleLoop(Scanner in, Connection conn) {
        boolean flag = true; // Controls the loop

        do {
            System.out.println("\nWelcome to the Bank Database System\n-----------------------------------------");
            System.out.println("[1] Customer Interface");
            System.out.println("[2] Manager Interface");
            System.out.println("[3] Exit");

            if (in.hasNextInt()) { // Validate input is an integer
                int choice = in.nextInt();
                in.nextLine(); // Clear the newline character

                switch (choice) {
                    case 1:
                        CustomerUtil.customerInterface(in, conn);
                        break;
                    case 2:
                        managementInterface(in, conn);
                        break;
                    case 3:
                        System.out.println("\nExiting the system. Thank you!");
                        flag = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter an integer 1-7.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                in.next(); 
            }
        } while (flag);
    }
    public static void managementInterface(Scanner in, Connection conn){
        boolean flag = true;
        do{
            System.out.println("\nWelcome to the Manager Interface\n-----------------------------------------");
            System.out.println("[1] View Account(s)");
            System.out.println("[2] ");
            System.out.println("[3] Withdraw");
            System.out.println("[4] Deposit");
            System.out.println("[5] Pay loans");
            System.out.println("[6] Exit");

        } while (flag);
    }
}