/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class GameRental {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of GameRental store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public GameRental(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end GameRental

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            GameRental.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      GameRental esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the GameRental object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new GameRental (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Catalog");
                System.out.println("4. Place Rental Order");
                System.out.println("5. View Full Rental Order History");
                System.out.println("6. View Past 5 Rental Orders");
                System.out.println("7. View Rental Order Information");
                System.out.println("8. View Tracking Information");

                //the following functionalities basically used by employees & managers
                System.out.println("9. Update Tracking Information");

                //the following functionalities basically used by managers
                System.out.println("10. Update Catalog");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: authorisedUser = updateProfile(esql, authorisedUser); break;
                   case 3: viewCatalog(esql); break;
                   case 4: placeOrder(esql, authorisedUser); break;
                   case 5: viewAllOrders(esql, authorisedUser); break;
                   case 6: viewRecentOrders(esql, authorisedUser); break;
                   case 7: viewOrderInfo(esql, authorisedUser); break;
                   case 8: viewTrackingInfo(esql, authorisedUser); break;
                   case 9: updateTrackingInfo(esql, authorisedUser); break;
                   case 10: updateCatalog(esql, authorisedUser); break;
                   case 11: updateUser(esql, authorisedUser); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice


   public static void CreateUser(GameRental esql){
      String login = ""; 
      String password = "";
      String phoneNum = ""; 
      // prompts user for necessary information
      try {
         System.out.print("Enter username: ");
         login = in.readLine();
         
         System.out.print("Enter password: ");
         password = in.readLine();
         
         System.out.print("Enter phone number: ");
         phoneNum = in.readLine();
      } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }
      // initialize other default values for a user
      String role = "customer";  
      String favGames = ""; 
      int numOverDueGames = 0;   

      try { 
         // constructing INSERT query from provided + default information
         String query = String.format("INSERT INTO Users (login, password, role, favGames, phoneNum, numOverDueGames)" + 
                                       "VALUES ('%s', '%s', '%s', '%s', '%s', %d);",
                                       login, password, role, favGames, phoneNum, numOverDueGames);
         // use provided executeUpdate function to send query to DB 
         esql.executeUpdate(query);
      } catch ( SQLException e ) { 
         // if the message is about primary key, username must already exist! -- prints special error statement
         if (e.getMessage() != null && e.getMessage().contains("violates unique constraint")) {
            System.err.println("Username unavailable. Please try again.\n");
         }
         // for other types of errors, default error message printed
         else { 
            System.err.println("Error building query: " + e.getMessage());
         }
         return;
      }
      // if no errors, print a success message
      System.out.println("User created successfully! Returning to menu...");
   }


   public static String LogIn(GameRental esql){
      try {
            // prompt user for username + password
            System.out.print("Enter username: ");
            String login = in.readLine();

            System.out.print("Enter password: ");
            String password = in.readLine();

            // constructing SELECT query to find user with matching username + password 
            String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s';", login, password);

            // use provided executeQuery function to send query to DB 
	         int rs = esql.executeQuery(query); 

            // check if a match was found in the DB
            if (rs == 1) {
                // user found, return login
                System.out.println("\nWelcome, " + login + "!\n");
                return login;
            } else {
                // user not found
                System.out.println("Invalid username or password.");
                return null;
            }

        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }
        return null;
   }

   public static void viewProfile(GameRental esql, String authorisedUser) {
      List<List<String>> result = new ArrayList<>();
      // retrieve entire instance information for the user currently logged in
      try {
         String query = String.format("SELECT * FROM Users WHERE login = '%s';", authorisedUser); 
         result = esql.executeQueryAndReturnResult(query);
      } catch (SQLException e) {
            // print the exception message if an SQL error occurs
            System.err.println("Error executing query: " + e.getMessage());
      }
      // output all information, preceded by the type of information (i.e. Username) 
      System.out.println("\nProfile Information:");
      List<String> row = result.get(0);
      List<String> info = Arrays.asList("Username", "Password", "role", "Favorite Games", "Phone Number", "# of Overdue Games");
      for (int i = 0; i < row.size(); i++) {
         if ( i != 2 ) {
            System.out.println(info.get(i) + ": " + row.get(i));
         }
      }
      System.out.println();
   }

   public static String updateProfile(GameRental esql, String authorisedUser) {
      List<List<String>> result = new ArrayList<>();
      // retrieve entire instance information for the user currently logged in
      try {
         String query = String.format("SELECT * FROM Users WHERE login = '%s';", authorisedUser); 
         result = esql.executeQueryAndReturnResult(query);
      } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
      }
      String role = result.get(0).get(2); 
      System.out.println("\n1. Change Password");
      System.out.println("2. Update Phone Number");
      // more actions if they are a manager
      if (role.contains("manager")) {
         System.out.println("3. Change Login (Manager only)");
         System.out.println("4. Update Role (Manager only)");
         System.out.println("5. Update Number of Overdue Games (Manager only)");
      }

      try {
         System.out.print("Enter your choice: ");
         int choice = Integer.parseInt(in.readLine()); 
         String currentPassword = result.get(0).get(1);
         String oldPassword = ""; 
         System.out.print("\n");
         // perform update based on user choice
         switch (choice) {
            case 1:
               System.out.print("Enter your old password: ");
               oldPassword = in.readLine();
               if (currentPassword.equals(oldPassword)){ 
                  System.out.print("Enter your new password: ");
                  String newPassword = in.readLine();
                  // updates user's password in the database
                  String updateQuery = String.format("UPDATE Users SET password = '%s' WHERE login = '%s';", newPassword, authorisedUser);
                  esql.executeUpdate(updateQuery);

                  System.out.println("Password updated successfully.\n");
               }
               else { 
                  System.out.println("Incorrect password... returning to menu.\n");
               }
               break;
            case 2:
               System.out.print("Enter your old password: ");
               oldPassword = in.readLine();
               if (currentPassword.equals(oldPassword)){ 
                  System.out.print("Enter your new phone number: ");
                  String newPhoneNum = in.readLine();
                  // updates user's phone number in the database
                  String updateQuery = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s';", newPhoneNum, authorisedUser);
                  esql.executeUpdate(updateQuery);

                  System.out.println("Phone number updated successfully.\n");
               }
               else { 
                  System.out.println("Incorrect password... returning to menu.\n");
               }
               break;
            case 3: 
               // makes sure to check manager status in each case as well 
               if (role.contains("manager")) {
                  System.out.print("Enter your new login: ");
                  String newLogin = in.readLine();
                  // updates user's login in the database
                  String updateQuery = String.format("UPDATE Users SET login = '%s' WHERE login = '%s';", newLogin, authorisedUser);
                  esql.executeUpdate(updateQuery);
                  authorisedUser = newLogin; 

                  System.out.println("Username updated successfully.\n");
               } else {
                     System.out.println("Invalid choice.\n");
               }
               break;
            case 4:  
               if (role.contains("manager")) {
                  // extra warning since it will remove permissions
                  System.out.print("WARNING: Changing your role to non-manager is irreversable without another manager's authority!\n");
                  System.out.print("Enter your new role (manager, employee, customer): ");
                  String newRole = in.readLine();
                  if ( newRole.equals("customer") || newRole.equals("employee") || newRole.equals("manager") ) {
                     // updates user's role in the database
                     String updateQuery = String.format("UPDATE Users SET role = '%s' WHERE login = '%s';", newRole, authorisedUser);
                     esql.executeUpdate(updateQuery);
                     System.out.println("Role updated successfully.\n");
                  }
                  else { 
                     System.out.print("Invalid role type provided.");
                  }
               } else {
                     System.out.println("Invalid choice.\n");
               }
               break;
            case 5:  
               if (role.contains("manager")) {
                  System.out.print("Enter new # of overdue games (>= 0): ");
                  int newOverdue = Integer.parseInt(in.readLine()); 
                  // updates user's overdue games in the database
                  String updateQuery = String.format("UPDATE Users SET numOverDueGames = '%d' WHERE login = '%s';", newOverdue, authorisedUser);
                  esql.executeUpdate(updateQuery);
                  System.out.println("Overdue games updated successfully.\n");
               } else {
                     System.out.println("Invalid choice.\n");
               }
               break;
            default:
               System.out.println("Invalid choice.\n");
               break;
         }
      } catch (IOException e) {
         System.err.println("Error reading input: " + e.getMessage());
      } catch (SQLException e) {
         System.err.println("Error executing query: " + e.getMessage());
      }
      return authorisedUser; 
   }

   public static void viewCatalog(GameRental esql) {
      try {
            // prompt user for filter options
            System.out.println("Choose filter option:");
            System.out.println("1. No Filter");
            System.out.println("2. Filter by Genre");
            System.out.println("3. Filter by Price");
            System.out.print("Enter choice: ");
            int filterChoice = Integer.parseInt(in.readLine());

            String genreFilter = "";
            double priceFilter = -1.0;

            // read in filter info based on user choice
            if (filterChoice == 2) {
                  System.out.print("Enter genre: ");
                  genreFilter = in.readLine();
            } else if (filterChoice == 3) {
                  System.out.print("Enter maximum price: ");
                  priceFilter = Double.parseDouble(in.readLine());
            }

            // prompt the user for price sorting options
            System.out.println("Choose sorting option:");
            System.out.println("1. Price: Lowest to Highest");
            System.out.println("2. Price: Highest to Lowest");
            System.out.print("Enter choice: ");
            int sortChoice = Integer.parseInt(in.readLine());

            String sortOrder = (sortChoice == 1) ? "ASC" : "DESC";

            // constructing SELECT query, adding additional filtering information if chosen 
            String query = "SELECT * FROM Catalog";
            boolean hasFilter = false;

            if (filterChoice == 2) {
                  query += String.format(" WHERE genre = '%s'", genreFilter);
                  hasFilter = true;
            } else if (filterChoice == 3) {
                  query += String.format(" WHERE price <= %.2f", priceFilter);
                  hasFilter = true;
            }

            // adds sorting order to the query
            query += " ORDER BY price " + sortOrder + ";";
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            // calls helper function
            displayCatalog(result);

         } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
         } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
         }
   }

   private static void displayCatalog(List<List<String>> result) {
      // define column widths as the longest title in each column 
      int[] columnWidths = {longestIn(result,0), longestIn(result,1), 
                            longestIn(result,2), longestIn(result,3),
                            longestIn(result,4)};

      // print header
      System.out.printf("%-" + columnWidths[0] + "s %-"+ columnWidths[1] + "s %-"+ columnWidths[2] + "s %-"+ columnWidths[3] + "s %-"+ columnWidths[4] + "s\n", 
      "Game ID |", "Name", "| Genre |", "Price |", "Details");

      // print rows
      for (List<String> row : result) {
         System.out.printf("%-" + columnWidths[0] + "s %-"+ columnWidths[1] + "s %-"+ columnWidths[2] + "s %-"+ columnWidths[3] + "s %-"+ columnWidths[4] + "s\n",
               row.get(0), row.get(1), row.get(2), "$" + row.get(3), row.get(4));
      }
   }

   private static int longestIn(List<List<String>> result, int index) { 
      //simple find max algorithm 
      int maxLength = 0;
      for (List<String> row : result) {
         String value = row.get(index);
         if (value.length() >= maxLength) {
               maxLength = value.length();
         }
      }
      return maxLength; 
   }

   public static void placeOrder(GameRental esql, String authorisedUser) {

      System.out.println("\n---ORDER PLACEMENT---\n");
      String queries = ""; 
      int totalGames = 0; 
      double totalPrice = 0;
      String newId = ""; 
      String newTrackingId = ""; 
      int lastNum = 0; 
      int lastNumTrackingId = 0; 

      try { 

         // finding new uniqueID for the new rentalOrder
         String query = "SELECT rentalorderid FROM RentalOrder ORDER BY rentalorderid DESC LIMIT 1";
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         String lastId = result.get(0).get(0);
         lastNum = Integer.parseInt(lastId.replaceAll("[^0-9]", ""));
         newId = "gamerentalorder" + (lastNum + 1);

         // finding new uniqueID for the new TrackingInfo
         String trackingQuery = "SELECT trackingid FROM TrackingInfo ORDER BY TrackingID DESC LIMIT 1";
         List<List<String>> trackingResult = esql.executeQueryAndReturnResult(trackingQuery);
         String lastTrackingId = trackingResult.get(0).get(0);
         lastNumTrackingId = Integer.parseInt(lastTrackingId.replaceAll("[^0-9]", ""));
         newTrackingId = "trackingid" + (lastNumTrackingId + 1);

         // keep prompting for more games to add to order if user wants
         boolean isOrdering = true; 
         while (isOrdering) { 
            System.out.print("Enter the game ID you would like to rent: ");
            String gameIDToOrder = in.readLine();

            //finding current game to extract price -> sum to totalPrice 
            String currQuery = "SELECT price, gameName FROM Catalog WHERE gameID = \'" + gameIDToOrder + "\';"; 
            List<List<String>> currentGame = esql.executeQueryAndReturnResult(currQuery);
            if (!currentGame.isEmpty() && !currentGame.get(0).isEmpty()) {
               //prompting # of copies 
               System.out.print("Enter how many copies of " + currentGame.get(0).get(1) + " you would like to order: ");
               int numOrders = Integer.parseInt(in.readLine());
               totalGames += numOrders;

               double price = Double.parseDouble(currentGame.get(0).get(0)); 
               totalPrice += (price*numOrders); 
               //forming query for current game, concat into list of queries for GamesInOrder
               if ( numOrders > 0 ) { 
                  queries += String.format("INSERT INTO GamesInOrder (rentalorderID, gameID, unitsOrdered) VALUES ('%s', '%s', '%d'); ",
                                          newId, gameIDToOrder, numOrders);
               }
            } 
            else { 
               System.out.println("Game not found or no price available.");
            }

            System.out.print("Would you like to add another game to your cart? (y/n): ");
            String isContinue = in.readLine();

            isOrdering = (isContinue.equals("y") || isContinue.equals("Y")) ? true : false; 
         }
      } catch ( SQLException e ) { 
         System.err.println("SQL Error Retrieving Game: " + e.getMessage());
      } catch ( IOException e ) { 
         System.err.println("IO Error Retrieving Input: " + e.getMessage());
      }

      // only run this part if they chose to add any games to their order 
      // i.e. entering game ID but selecting 0 copies -> no order 
      if ( totalGames >= 0 ) { 

         // produces order timestamp in sql syntax
         String currentTimeStamp = "current_timestamp"; 
         // assuming due date is 7 days from now 
         String dueTimeStamp = "current_timestamp + interval '7 days'";


         String newOrder = String.format("INSERT INTO RentalOrder (rentalorderID, login, noOfGames, totalPrice, orderTimeStamp, dueDate)" + 
                                          "VALUES ('%s', '%s', %d, %.2f, %s, %s); ",
                                          newId, authorisedUser, totalGames, totalPrice, currentTimeStamp, dueTimeStamp);
         // ASSUMING: 
         // 1) "Order Processing" is a good default status,
         // 2) all order start in Riverside as default, 
         // 3) and courier isn't known yet (as order hasn't been placed yet). 
         String newTrackingInfo = String.format("INSERT INTO TrackingInfo (trackingID, rentalorderID, status, currentLocation, courierName, lastUpdateDate, additionalComments) " + 
                                    "VALUES ('%s', '%s', '%s', '%s', '%s', %s, '%s'); ",
                                    newTrackingId, newId, "Order Processing", "Riverside, CA", "TBD", currentTimeStamp, "");
         
         // turning OFF autocommit to allow all 3 queries to be ran simultaneously, if one fails, 
         // rolling back entire transation
         try { 
            try {
               // disable auto-commit
               esql._connection.setAutoCommit(false);
               
               // executing all queries
               esql.executeUpdate(newOrder);
               esql.executeUpdate(queries);
               esql.executeUpdate(newTrackingInfo);
               
               // commiting transaction as a whole
               esql._connection.commit();
               
               System.out.println("\nRental Order #" + lastNum + " placed, with Tracking ID #" + lastNumTrackingId + " has successfully been placed.");
               System.out.println("Order total: $" + String.format("%.2f", totalPrice) + " for " + totalGames + " games. \n");
            } catch (SQLException e) {

               // rollback the transaction if any statement fails in the commit
               esql._connection.rollback();
               
               System.err.println("\nSQL Error: " + e.getMessage());
               System.err.println("System rollback, no changes made.");
            } finally {
               // reset auto-commit mode to true
               esql._connection.setAutoCommit(true);
            }
         } catch (SQLException e) {
            System.err.println("\nSQL Error Rollbacking/Committing: " + e.getMessage());
         }
      }
   }

   public static void viewAllOrders(GameRental esql, String authorisedUser) {
      try {
        // constructing SELECT query to get rental order IDs for the authorised user
        String query = "SELECT rentalOrderID FROM RentalOrder WHERE login = '" + authorisedUser + "' ORDER BY orderTimeStamp DESC;";

        List<List<String>> orderIDs = esql.executeQueryAndReturnResult(query);
        
        // print list of IDs with only numbers
        if (!orderIDs.isEmpty()) {
            System.out.println("Your order history:");
            for (List<String> row : orderIDs) {
               String orderID = row.get(0);
               int orderNum = Integer.parseInt(orderID.replaceAll("[^0-9]", ""));
               System.out.println("- #" + orderNum);
            }
        } else {
            System.out.println("You have no order history.");
        }
      } catch (SQLException e) {
        System.err.println("Error: " + e.getMessage());
      }
      System.out.print("\n");
   }
   
   public static void viewRecentOrders(GameRental esql, String authorisedUser) {
      try {
         // constructing SELECT query to get 5 most recent rental order IDs for the authorised user
         String query = "SELECT rentalOrderID FROM RentalOrder WHERE login = '" + authorisedUser + "' " +
                        "ORDER BY orderTimeStamp DESC LIMIT 5;";
         
         List<List<String>> orderIDs = esql.executeQueryAndReturnResult(query);
         
         // print list of IDs with only numbers
         if (!orderIDs.isEmpty()) {
            System.out.println("Your 5 most recent orders:");
            for (List<String> row : orderIDs) {
                  String orderID = row.get(0);
                  int orderNum = Integer.parseInt(orderID.replaceAll("[^0-9]", ""));
                  System.out.println("- #" + orderNum);
            }
         } else {
            System.out.println("You have no order history.");
         }
      } catch (SQLException e) {
         System.err.println("Error: " + e.getMessage());
      }
      System.out.print("\n");
   }

   public static void viewOrderInfo(GameRental esql, String authorisedUser) {
      try {
         System.out.print("Enter the ID # of the order you'd like to view: ");
         String orderID = in.readLine();
         // constructing SELECT query to retrieve details of the specific rental order
         String query = "SELECT orderTimeStamp, dueDate, totalPrice " +
                        "FROM RentalOrder " +
                        "WHERE login = '" + authorisedUser + "' AND rentalOrderID = 'gamerentalorder" + orderID + "';";
        
         List<List<String>> orderDetails = esql.executeQueryAndReturnResult(query);
         
         // print the order details if found
         if (!orderDetails.isEmpty()) {
               List<String> details = orderDetails.get(0);

               String trackingIDQuery = "SELECT trackingID FROM TrackingInfo WHERE rentalorderID = 'gamerentalorder" + orderID + "';";
               List<List<String>> TrackingInfoID = esql.executeQueryAndReturnResult(trackingIDQuery);

               System.out.println("Order details:");
               System.out.println("- Order Timestamp: " + details.get(0));
               System.out.println("- Due Date: " + details.get(1));
               System.out.println("- Total Price: $" + details.get(2));
               System.out.println("- Tracking ID: " + (TrackingInfoID.get(0).get(0).replaceAll("[^0-9]", "")));
               
               // retrieve and print the list of games associated with the order
               printGamesInOrder(esql, "gamerentalorder" + orderID);
         } else {
               System.out.println("Order not found or does not belong to you.");
         }
      } catch (SQLException e) {
         System.err.println("Error: " + e.getMessage());
      }
      catch (IOException e) {
         System.err.println("Error: " + e.getMessage());
      }
   }

   private static void printGamesInOrder(GameRental esql, String orderID) {
      try {
         // constructing SELECT query to retrieve the list of games associated with the given order
         String query = "SELECT gameID, unitsOrdered " +
                        "FROM GamesInOrder " +
                        "WHERE rentalOrderID = '" + orderID + "';";

         List<List<String>> gamesInOrder = esql.executeQueryAndReturnResult(query);
         
         if (!gamesInOrder.isEmpty()) {
               System.out.println("Games in this order:");
               for (List<String> game : gamesInOrder) {
                  System.out.println("- Game ID: " + game.get(0) + ", Units Ordered: " + game.get(1));
               }
         } else {
               System.out.println("No games found for this order.");
         }
      } catch (SQLException e) {
         System.err.println("Error: " + e.getMessage());
      }
   }

   public static void viewTrackingInfo(GameRental esql, String authorisedUser) {
      try {
         System.out.print("Enter the tracking ID # of the order you'd like to view: ");
         String trackingId = in.readLine();
         // construct the SQL query to retrieve details of the specific tracking info 
         String query = "SELECT t.trackingid, t.rentalorderid, t.status, t.currentLocation, t.couriername, t.lastupdatedate, t.additionalcomments " +
                        "FROM trackinginfo t JOIN rentalorder r ON t.rentalorderid = r.rentalorderid " +
                        "WHERE t.trackingid = 'trackingid" + trackingId + "' AND r.login = '" + authorisedUser + "';";

         List<List<String>> trackingDetails = esql.executeQueryAndReturnResult(query);
         
         // print the order details if found
         if (!trackingDetails.isEmpty()) {
               List<String> details = trackingDetails.get(0);

               System.out.println("Tracking Info details:");
               System.out.println("- Tracking ID: #" + details.get(0).replaceAll("[^0-9]", ""));
               System.out.println("- Rental Order ID: #" + details.get(1).replaceAll("[^0-9]", ""));
               System.out.println("- Status: " + details.get(2));
               System.out.println("- Current Location: " + details.get(3));
               System.out.println("- Courier: " + details.get(4));
               System.out.println("- Last Updated Date: " + details.get(5));
               System.out.println("- Additional Comments: " + details.get(6));
               
         } else {
               System.out.println("Tracking info not found or does not belong to you.");
         }
      } catch (SQLException e) {
         System.err.println("Error: " + e.getMessage());
      }
      catch (IOException e) {
         System.err.println("Error: " + e.getMessage());
      }
   }
   public static void updateTrackingInfo(GameRental esql, String authorisedUser) {
      List<List<String>> result = new ArrayList<>();
      String query;
      String trackingId = "";

      try {
         query = String.format("SELECT * FROM Users WHERE login = '%s';", authorisedUser); 
         result = esql.executeQueryAndReturnResult(query);
         String role = result.get(0).get(2);

         if (role.contains("manager") || role.contains("employee")) {
               System.out.println("\nEnter trackingID to update: ");

               try {
                  trackingId = in.readLine();
               } catch (Exception e) {
                  System.out.println("Error reading input: " + e.getMessage());
                  return; // exit function if theres an input error
               }

               query = "SELECT * FROM trackinginfo WHERE trackingid = 'trackingid" + trackingId + "';";
               List<List<String>> trackingInfo = new ArrayList<>();
               trackingInfo = esql.executeQueryAndReturnResult(query);

               if (!trackingInfo.isEmpty()) {

                  System.out.println("\n1. Update Status");
                  System.out.println("2. Update Location");
                  System.out.println("3. Update Courier");
                  System.out.println("4. Update Additional Comments");
                  System.out.print("Enter your choice: ");
                  int choice = Integer.parseInt(in.readLine());
                  
                  switch (choice) {
                     case 1:
                        System.out.print("Enter the updated status: ");
                        String update = in.readLine();
                        // updates the tracking status in the database
                        String updateQuery = String.format("UPDATE trackinginfo SET status = '%s' WHERE trackingid = 'trackingid" + trackingId + "';", update);
                        esql.executeUpdate(updateQuery);
                        break;

                     case 2:
                           System.out.print("Enter the updated location: ");
                           update = in.readLine();
                           // updates the tracking location in the database
                           updateQuery = String.format("UPDATE trackinginfo SET currentlocation = '%s' WHERE trackingid = 'trackingid" + trackingId + "';", update);
                           esql.executeUpdate(updateQuery);
                        break;
                  
                     case 3:
                           System.out.print("Enter the updated courier: ");
                           update = in.readLine();
                           // updates the tracking courier in the database
                           updateQuery = String.format("UPDATE trackinginfo SET couriername = '%s' WHERE trackingid = 'trackingid" + trackingId + "';", update);
                           esql.executeUpdate(updateQuery);
                        break;

                     case 4:
                           System.out.print("Enter the updated additional comments: ");
                           update = in.readLine();
                           // updates the tracking comments in the database
                           updateQuery = String.format("UPDATE trackinginfo SET additionalcomments = '%s' WHERE trackingid = 'trackingid" + trackingId + "';", update);
                           esql.executeUpdate(updateQuery);
                        break;
               }
               String updateQuery = String.format("UPDATE trackinginfo SET lastupdatedate = CURRENT_TIMESTAMP WHERE trackingid = 'trackingid" + trackingId + "';");
               esql.executeUpdate(updateQuery);
            } else {
               System.out.println("Tracking info not found.");
            }
         } else { //user isn't faculty 
               System.out.println("You are not authorized to update tracking information.");
         }
      } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
      } catch (SQLException e) {
         System.err.println("Error executing query: " + e.getMessage());
      }
   }

   public static void updateCatalog(GameRental esql, String authorisedUser) {
      List<List<String>> result = new ArrayList<>();
      String query;
      String gameId = "";

      try {
         query = String.format("SELECT * FROM Users WHERE login = '%s';", authorisedUser); 
         result = esql.executeQueryAndReturnResult(query);
         String role = result.get(0).get(2); // Assuming role is the third column in your users table

         if (role.contains("manager")) {
            System.out.println("\nEnter gameID to update: ");

            try {
               gameId = in.readLine();
            } catch (Exception e) {
               System.out.println("Error reading input: " + e.getMessage());
               return; // Exit if there's an input error
            }

            query = "SELECT * FROM catalog WHERE gameid = 'game" + gameId + "';";
            List<List<String>> gameInfo = new ArrayList<>();
            gameInfo = esql.executeQueryAndReturnResult(query);

            if (!gameInfo.isEmpty()) {

               System.out.println("\n1. Update Game Name");
               System.out.println("2. Update Genre");
               System.out.println("3. Update Price");
               System.out.println("4. Update Description");
               System.out.println("5. Update Image URL");
               System.out.print("Enter your choice: ");
		         int choice = Integer.parseInt(in.readLine());
                
		         switch (choice) {
                  case 1:
               		System.out.print("Enter the updated game name: ");
               		String update = in.readLine();
                  	// Update the game name in the database
                  	String updateQuery = String.format("UPDATE catalog SET gamename = '%s' WHERE gameid = 'game" + gameId + "';", update);
                  	esql.executeUpdate(updateQuery);
                     System.out.println("Game name successfully updated.\n");
			            break;

		            case 2:
                        System.out.print("Enter the updated game genre: ");
                        update = in.readLine();
                        // Update the game genre in the database
                        updateQuery = String.format("UPDATE catalog SET genre = '%s' WHERE gameid = 'game" + gameId + "';", update);
                        esql.executeUpdate(updateQuery);
                        System.out.println("Game genre successfully updated.\n");
                        break;
		  
		            case 3:
                        System.out.print("Enter the updated game price: ");
                        update = in.readLine();
                        // Update the game price in the database
                        updateQuery = String.format("UPDATE catalog SET price = '%s' WHERE gameid = 'game" + gameId + "';", update);
                        esql.executeUpdate(updateQuery);
                        System.out.println("Game price successfully updated.\n");
                        break;
		            case 4:
                        System.out.print("Enter the updated game description: ");
                        update = in.readLine();
                        // Update the game description in the database
                        updateQuery = String.format("UPDATE catalog SET description = '%s' WHERE gameid = 'game" + gameId + "';", update);
                        esql.executeUpdate(updateQuery);
                        System.out.println("Game description successfully updated.\n");
                        break;
		            case 5:
                        System.out.print("Enter the game's updated image URL: ");
                        update = in.readLine();
                        // Update the image URL in the database
                        updateQuery = String.format("UPDATE catalog SET imageURL = '%s' WHERE gameid = 'game" + gameId + "';", update);
                        esql.executeUpdate(updateQuery);
                        System.out.println("Game image URL successfully updated.\n");
                        break;
               }
            } else {
                System.out.println("Game info not found.");
            }
         } else {
            System.out.println("You are not authorized to update the game catalog.");
         }
      } catch (IOException e) {
         System.err.println("Error reading input: " + e.getMessage());
      } catch (SQLException e) {
        System.err.println("Error executing query: " + e.getMessage());
      }
   }

   public static void updateUser(GameRental esql,String authorisedUser) {
      List<List<String>> result = new ArrayList<>();
      String query;
      String userLogin = "";

      try {
         query = String.format("SELECT * FROM Users WHERE login = '%s';", authorisedUser); 
         result = esql.executeQueryAndReturnResult(query);
         String role = result.get(0).get(2);

         if (role.contains("manager")) {
            System.out.println("\nEnter user login to update: ");

            try {
               userLogin = in.readLine();
            } catch (Exception e) {
               System.out.println("Error reading input: " + e.getMessage());
               return; // exit if there's an input error
            }

            query = "SELECT * FROM users WHERE login = '" + userLogin + "';";
            List<List<String>> userInfo = new ArrayList<>();
            userInfo = esql.executeQueryAndReturnResult(query);

            if (!userInfo.isEmpty()) {

               System.out.println("\n1. Update User Password");
               System.out.println("2. Update User Role");
               System.out.println("3. Update User Favorite Games");
               System.out.println("4. Update User Phone Number");
	            System.out.println("5. Update User Number of Overdue Games");
               System.out.print("Enter your choice: ");
		         int choice = Integer.parseInt(in.readLine());
                
		         switch (choice) {
                  case 1:
               		System.out.print("Enter the updated user password: ");
               		String update = in.readLine();
                  	// update the user's password in the database
                  	String updateQuery = String.format("UPDATE users SET password = '%s' WHERE login = '" + userLogin + "';", update);
                  	esql.executeUpdate(updateQuery);
                     System.out.println("User's password successfully updated.\n");
			            break;

		            case 2:
                     System.out.print("Enter the updated user role: ");
                     update = in.readLine();
                     // update the user's role in the database
                     updateQuery = String.format("UPDATE users SET role = '%s' WHERE login = '" + userLogin + "';", update);
                     esql.executeUpdate(updateQuery);
                     System.out.println("User's role successfully updated.\n");
                     break;
				    
                  case 3:
                     System.out.print("Enter the updated user's favorite games: ");
                     update = in.readLine();
                     // update the user's favorite games in the database
                     updateQuery = String.format("UPDATE users SET favgames = '%s' WHERE login = '" + userLogin + "';", update);
                     esql.executeUpdate(updateQuery);
                     System.out.println("User's favorite games successfully updated.\n");
                     break;

                  case 4:
                     System.out.print("Enter the updated user's phone number: ");
                     update = in.readLine();
                     // update the user's phone number in the database
                     updateQuery = String.format("UPDATE users SET phonenum = '%s' WHERE login = '" + userLogin + "';", update);
                     esql.executeUpdate(updateQuery);
                     System.out.println("User's phone number successfully updated.\n");
                     break;

		            case 5:
                     System.out.print("Enter the updated user's number of overdue games (>= 0): ");
                     update = in.readLine();
                     // update the user's overdue games in the database
                     updateQuery = String.format("UPDATE users SET numoverduegames = '%s' WHERE login = '" + userLogin + "';", update);
                     esql.executeUpdate(updateQuery);
                     System.out.println("User's overdue games successfully updated.\n");
                     break;
			      }
            } else {
                System.out.println("User not found.");
            }
         } else {
            System.out.println("You are not authorized to update user information.");
         }
      } catch (IOException e) {
         System.err.println("Error reading input: " + e.getMessage());
      } catch (SQLException e) {
        System.err.println("Error executing query: " + e.getMessage());
      }
   }
}//end GameRental

