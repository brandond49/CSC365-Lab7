import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/*
 * Introductory JDBC examples based loosely on the BAKERY dataset from CSC 365 labs.
 */
public class HastyPastry {

    private final String JDBC_URL = "jdbc:h2:~/csc365_lab7";
    private final String JDBC_USER = "mcalanog";
    private final String JDBC_PASSWORD = "Spr2021-365-014628155";
    
    public static void main(String[] args) {
	try {
	    HastyPastry hp = new HastyPastry();
            hp.initDb();
        hp.createTables();
	    String option = hp.demo3();
	    while (!input.equals("x")){
				if (input.equals("1")) {
					ir.FR1();
				} else if (input.equals("2")){
					ir.FR2();
				} else if (input.equals("3")){
					ir.FR3();
				} else if (input.equals("4")){
					ir.FR4();
				} else if (input.equals("5")){
					ir.FR5();
				}
				input = ir.demo3();
			}
	} catch (SQLException e) {
	    System.err.println("SQLException: " + e.getMessage());
	}
    }


    public void createTables() throws SQLException {
		try (Connection conn = DriverManager.getConnection(JDBC_URL,
				JDBC_USER,
				JDBC_PASSWORD)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute("CREATE TABLE `rooms` (" +
						"  `RoomCode` char(5) NOT NULL," +
						"  `RoomName` varchar(30) DEFAULT NULL," +
						"  `Beds` int(11) DEFAULT NULL," +
						"  `bedType` varchar(8) DEFAULT NULL," +
						"  `maxOcc` int(11) DEFAULT NULL," +
						"  `basePrice` float DEFAULT NULL," +
						"  `decor` varchar(20) DEFAULT NULL," +
						"  PRIMARY KEY (`RoomCode`)," +
						"  UNIQUE KEY `RoomName` (`RoomName`)" +
						")");
			}
				stmt.execute("CREATE TABLE `reservations` (" +
						"  `CODE` int(11) NOT NULL," +
						"  `Room` char(5) DEFAULT NULL," +
						"  `CheckIn` date DEFAULT NULL," +
						"  `Checkout` date DEFAULT NULL," +
						"  `Rate` float DEFAULT NULL," +
						"  `LastName` varchar(15) DEFAULT NULL," +
						"  `FirstName` varchar(15) DEFAULT NULL," +
						"  `Adults` int(11) DEFAULT NULL," +
						"  `Kids` int(11) DEFAULT NULL," +
						"  PRIMARY KEY (`CODE`)," +
						"  UNIQUE KEY `Room` (`Room`,`CheckIn`)," +
						"  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`Room`) REFERENCES `rooms` (`roomcode`)" +
						")");

		}
	}

	//where is this called?
    // Demo1 - Establish JDBC connection, execute DDL statement
    private void demo1() throws SQLException {

	// Step 1: Establish connection to RDBMS
	try (Connection conn = DriverManager.getConnection(JDBC_URL,
							   JDBC_USER,
							   JDBC_PASSWORD)) {
	    // Step 2: Construct SQL statement
	    String sql = "ALTER TABLE hp_goods ADD COLUMN AvailUntil DATE";

	    // Step 3: (omitted in this example) Start transaction

	    try (Statement stmt = conn.createStatement()) {

		// Step 4: Send SQL statement to DBMS
		boolean exRes = stmt.execute(sql);
		
		// Step 5: Handle results
		System.out.format("Result from ALTER: %b %n", exRes);
	    }

	    // Step 6: (omitted in this example) Commit or rollback transaction
	}
	// Step 7: Close connection (handled by try-with-resources syntax)
    }
    


    // Demo3 - Establish JDBC connection, execute DML query (UPDATE)
    // -------------------------------------------
    // Never (ever) write database code like this!
    // -------------------------------------------
    private void demo3() throws SQLException {

        //demo2(); // print contents of goods table
        
	// Step 1: Establish connection to RDBMS
	try (Connection conn = DriverManager.getConnection(JDBC_URL,
							   JDBC_USER,
							   JDBC_PASSWORD)) {
	    // Step 2: Construct SQL statement
	    Scanner scanner = new Scanner(System.in);
	    //added:
	    System.out.print("\n Command options: \n");
		System.out.print("\n View Rooms and Rates: (1)");
		System.out.print("\n Make Reservation: (2)");
		System.out.print("\n Make Change to a Current Reservation: (3)");
		System.out.print("\n Cancel Reservation: (4)");
		System.out.print("\n Show Revenue Summaries (5)");
		System.out.println("\n I am done! : (x)");

		System.out.print("\n Enter selection here: ");
		//changed to roomcode
	    String option = scanner.nextLine();
	    return option;
	    
	}
	// Step 7: Close connection (handled implcitly by try-with-resources syntax)
        
    }
    private void FR1() throws SQLException {

		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(JDBC_URL,
				JDBC_USER,
				JDBC_PASSWORD)) {

			java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
			//NotAvailNow as Occupied
			//AvailNow as Vacant
			String fr1 =
					"with Occupied as (\n" +
							"    select RoomName, CheckOut as NextAvail\n" +
							"    from lab7_rooms as rooms, lab7_reservations as reservations\n" +
							"	 where rooms.RoomId = reservations.Room \n" +
							"    and CheckIn <= CURRENT_DATE\n" +
							"        and Checkout >= CURRENT_DATE\n" +
							"),\n" +
							"Vacant as (\n" +
							"    select RoomName, CURRENT_DATE AS NextAvail\n" +
							"    from lab7_rooms as rooms \n" +
							"    where RoomName NOT IN (select RoomName from Occupied)\n" +
							"    group by RoomName \n" +
							"),\n" +
							"NextReservation as (\n" +
							"    select RoomName, min(CheckIn) as NextCheckIn\n" +
							"    from lab7_rooms as rooms, lab7_reservations as reservations\n" +
							"	 where rooms.RoomId = reservations.Room\n" +
							"    and CheckIn > CURRENT_DATE\n" +
							"    group by RoomName)\n" +
							"select avail.RoomName, NextAvail, NextCheckIn from (select *\n" +
							"FROM Vacant\n" +
							"UNION \n" +
							"select * \n" +
							"FROM Occupied) as available, NextReservation\n"+
							"where NextReservation.RoomName = available.RoomName";

			try (PreparedStatement stmt = conn.prepareStatement(fr1);) {
				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					String name = rs.getString("RoomName");
					java.sql.Date nextAvail = rs.getDate("NextAvail");
					java.sql.Date nextcheck = rs.getDate("NextCheckIn");
					System.out.format("%s %s %s %n", name, nextAvail, nextcheck);
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}



    // Demo4 - Establish JDBC connection, execute DML query (UPDATE) using PreparedStatement / transaction    
    private void demo4() throws SQLException {

	// Step 1: Establish connection to RDBMS
	try (Connection conn = DriverManager.getConnection(JDBC_URL,
							   JDBC_USER,
							   JDBC_PASSWORD)) {
	    // Step 2: Construct SQL statement
	    Scanner scanner = new Scanner(System.in);
	    System.out.print("Enter a flavor: ");
	    String flavor = scanner.nextLine();
	    System.out.format("Until what date will %s be available (YYYY-MM-DD)? ", flavor);
	    LocalDate availDt = LocalDate.parse(scanner.nextLine());
	    
	    String updateSql = "UPDATE hp_goods SET AvailUntil = ? WHERE Flavor = ?";

	    // Step 3: Start transaction
	    conn.setAutoCommit(false);
	    
	    try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
		
		// Step 4: Send SQL statement to DBMS
		pstmt.setDate(1, java.sql.Date.valueOf(availDt));
		pstmt.setString(2, flavor);
		int rowCount = pstmt.executeUpdate();
		
		// Step 5: Handle results
		System.out.format("Updated %d records for %s pastries%n", rowCount, flavor);

		// Step 6: Commit or rollback transaction
		conn.commit();
	    } catch (SQLException e) {
		conn.rollback();
	    }

	}
	// Step 7: Close connection (handled implcitly by try-with-resources syntax)
    }



    // Demo5 - Construct a query using PreparedStatement
    private void demo5() throws SQLException {

	// Step 1: Establish connection to RDBMS
	try (Connection conn = DriverManager.getConnection(JDBC_URL,
							   JDBC_USER,
							   JDBC_PASSWORD)) {
	    Scanner scanner = new Scanner(System.in);
	    System.out.print("Find pastries with price <=: ");
	    Double price = Double.valueOf(scanner.nextLine());
	    System.out.print("Filter by flavor (or 'Any'): ");
	    String flavor = scanner.nextLine();

	    List<Object> params = new ArrayList<Object>();
	    params.add(price);
	    StringBuilder sb = new StringBuilder("SELECT * FROM hp_goods WHERE price <= ?");
	    if (!"any".equalsIgnoreCase(flavor)) {
		sb.append(" AND Flavor = ?");
		params.add(flavor);
	    }
	    
	    try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
		int i = 1;
		for (Object p : params) {
		    pstmt.setObject(i++, p);
		}

		try (ResultSet rs = pstmt.executeQuery()) {
		    System.out.println("Matching Pastries:");
		    int matchCount = 0;
		    while (rs.next()) {
			System.out.format("%s %s ($%.2f) %n", rs.getString("Flavor"), rs.getString("Food"), rs.getDouble("price"));
			matchCount++;
		    }
		    System.out.format("----------------------%nFound %d match%s %n", matchCount, matchCount == 1 ? "" : "es");
		}
	    }

	}
    }


    private void initDb() throws SQLException {
	try (Connection conn = DriverManager.getConnection(JDBC_URL,
							   JDBC_USER,
							   JDBC_PASSWORD)) {
	    try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS hp_goods");
                stmt.execute("CREATE TABLE hp_goods (GId varchar(15) PRIMARY KEY, Food varchar(100), Flavor varchar(100), Price DECIMAL(5,1), AvailUntil DATE)");
                stmt.execute("INSERT INTO hp_goods (GId, Flavor, Food, Price) VALUES ('L1', 'Lemon', 'Cake', 20.0)");
                stmt.execute("INSERT INTO hp_goods (GId, Flavor, Food, Price) VALUES ('L2', 'Lemon', 'Twist', 3.50)");
                stmt.execute("INSERT INTO hp_goods (GId, Flavor, Food, Price) VALUES ('A3', 'Almond', 'Twist', 4.50)");
                stmt.execute("INSERT INTO hp_goods (GId, Flavor, Food, Price) VALUES ('A4', 'Almond', 'Cookie', 4.50)");
                stmt.execute("INSERT INTO hp_goods (GId, Flavor, Food, Price) VALUES ('L5', 'Lemon', 'Cookie', 1.50)");
                stmt.execute("INSERT INTO hp_goods (GId, Flavor, Food, Price) VALUES ('A6', 'Almond', 'Danish', 2.50)");
	    }
	}
    }
    

}
