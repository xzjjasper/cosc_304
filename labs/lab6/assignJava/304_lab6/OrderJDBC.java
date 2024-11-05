/*
OrderJDBC.java - A JDBC program for accessing and updating an order database on MySQL.

Lab Assignment #6

Student name:   <your student name>
University id:  <your university id>
*/

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;


/**
 * An application for querying and updating an order database.
 */
public class OrderJDBC
{
	/**
	 * Connection to database
	 */
	private Connection con;
	
	
	/**
	 * Main method is only used for convenience.  Use JUnit test file to verify your answer.
	 * 
	 * @param args
	 * 		none expected
	 * @throws SQLException
	 * 		if a database error occurs
	 */	
    public static void main(String[] args) throws SQLException
	{
		OrderJDBC q = new OrderJDBC();
		q.connect();
		q.init();
					
		// Application operations
		System.out.println(q.listAllCustomers());
		q.listCustomerOrders("00001");
		q.listLineItemsForOrder("01000");
		q.computeOrderTotal("01000");
		q.addCustomer("11111", "Fred Smith");
		q.updateCustomer("11111", "Freddy Smithers");			
		q.newOrder("22222", "11111", "2014-10-31", "E0001");
		q.newLineItem("22222", "P0005", 5, "3.10");
		q.newLineItem("22222", "P0007", 5, "2.25");
		q.newLineItem("22222", "P0008", 5, "2.50");
		q.deleteCustomer("11111");		
		
        // Queries
		// Re-initialize all data
		q.init();
        System.out.println(OrderJDBC.resultSetToString(q.query1(), 100));
        System.out.println(OrderJDBC.resultSetToString(q.query2(), 100));
        System.out.println(OrderJDBC.resultSetToString(q.query3(), 100));
        System.out.println(OrderJDBC.resultSetToString(q.query4(), 100));
        
        q.close();
	}

	/**
	 * Makes a connection to the database and returns connection to caller.
	 * 
	 * @return
	 * 		connection
	 * @throws SQLException
	 * 		if an error occurs
	 */
	public Connection connect() throws SQLException
	{
	    // TODO: Verify connection information is correct
		String uid = "testuser";
		String url = "jdbc:mysql://localhost/testuser"; 		
		String pw = "304testpw";
	    
		System.out.println("Connecting to database.");
		// Note: Must assign connection to instance variable as well as returning it back to the caller
		con = DriverManager.getConnection(url, uid, pw);
		return con;		                       
	}
	
	/**
	 * Closes connection to database.
	 */
	public void close()
	{
		System.out.println("Closing database connection.");
		try
		{
			if (con != null)
	            con.close();
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
	}
		
	/**
	 * Creates the database and initializes the data.
	 */
	public void init()
	{
	    String fileName = "assignJava/304_lab6/order.ddl";
		
	    try
	    {
	        // Create statement
	        Statement stmt = con.createStatement();
	        
	        Scanner scanner = new Scanner(new File(fileName));
	        // Read commands separated by ;
	        scanner.useDelimiter(";");
	        while (scanner.hasNext())
	        {
	            String command = scanner.next();
	            if (command.trim().equals(""))
	                continue;
	            // System.out.println(command);        // Uncomment if want to see commands executed
	            stmt.execute(command);
	        }	  
	        scanner.close();
	    }
	    catch (Exception e)
	    {
	        System.out.println(e);
	    }        
	}
	
	/**
	 * Returns a String with all the customers in the order database.  
	 * Format:
	 * CustomerId, CustomerName
	 * 00000, A. Anderson 
	 * 
	 * @return
	 *       String containing customers
	 */
    public String listAllCustomers() throws SQLException
    {
        System.out.println("Executing list all customers.");
        
        StringBuilder output = new StringBuilder();

		Statement stmt = con.createStatement();
		ResultSet result = stmt.executeQuery("SELECT * FROM Customer");
		output.append("CustomerId, CustomerName");
		while(result.next()){
			output.append("\n" + result.getString("CustomerId") +", "
					+ result.getString("CustomerName"));
		}
        
        return output.toString();
    }
    
    /**
     * Returns a String with all the orders for a given customer id.
     * 
     * Note: May need to use getDate(). You should not retrieve all values as Strings.
     * 
     * Format:
     * OrderId, OrderDate, CustomerId, EmployeeId, Total
     * 01001, 2023-11-08, 00001, E0000, 1610.59
     * 
     * @return
     *       String containing orders
     */
    public String listCustomerOrders(String customerId) throws SQLException
    {
		StringBuilder output = new StringBuilder();
		PreparedStatement pstmt = con.prepareStatement("SELECT * FROM Orders WHERE CustomerId = ?");
		pstmt.setString(1, customerId);
		ResultSet result = pstmt.executeQuery();
		output.append("OrderId, OrderDate, CustomerId, EmployeeId, Total");
		while (result.next()){
			output.append("\n" + result.getString("OrderId") + ", ");
			output.append(result.getDate("OrderDate") + ", ");
			output.append(result.getString("CustomerId") + ", ");
			output.append(result.getString("EmployeeId") + ", ");
			output.append(result.getString("Total"));
		}
		return output.toString();
    }
    
    /**
     * Returns a ResultSet with all line items for a given order id.
     * You must use a PreparedStatement.
     * 
     * @return
     *       ResultSet containing line items
     */
    public ResultSet listLineItemsForOrder(String orderId) throws SQLException
    {
        PreparedStatement pstmt = con.prepareStatement("SELECT * FROM OrderedProduct WHERE OrderId = ?");
		pstmt.setString(1, orderId);
		ResultSet result = pstmt.executeQuery();
        return result;
    }
    
    /**
     * Returns a ResultSet with a row containing the computed order total from the lineitems (named as orderTotal) for a given order id.
     * You must use a PreparedStatement.
     * Note: Do NOT just return the Orders.Total value.
     * 
     * @return
     *       ResultSet containing order total
     */
    public ResultSet computeOrderTotal(String orderId) throws SQLException
    {
		PreparedStatement pstmt = con.prepareStatement("SELECT SUM(Quantity * Price) as orderTotal FROM OrderedProduct WHERE OrderId = ?");
		pstmt.setString(1, orderId);
		ResultSet result = pstmt.executeQuery();
		return result;
    }
    
    /**
     * Inserts a customer into the databases.
     * You must use a PreparedStatement.
     */
    public void addCustomer(String customerId, String customerName) throws SQLException
    {
		String sql = "INSERT INTO Customer values (?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, customerId);
		pstmt.setString(2, customerName);
		int rows = pstmt.executeUpdate();
    }
    
    /**
     * Deletes a customer from the databases.
     * You must use a PreparedStatement.
     * @throws SQLException 
     */
    public void deleteCustomer(String customerId) throws SQLException
    {
		String sql = "DELETE FROM Customer where CustomerId = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, customerId);
		int rows = pstmt.executeUpdate();
    }
    
    /**
     * Updates a customer in the databases.
     * You must use a PreparedStatement.
     * @throws SQLException 
     */
    public void updateCustomer(String customerId, String customerName) throws SQLException
    {
		String sql = "UPDATE Customer SET CustomerName = ? WHERE CustomerId = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, customerName);
		pstmt.setString(2, customerId);
		int rows = pstmt.executeUpdate();
    }
    
    /**
     * Creates an order in the database.
     * You must use a PreparedStatement.
     * 
     * @throws SQLException 
     */    
    public void newOrder(String orderId, String customerId, String orderDate, String employeeId) throws SQLException
    {
		String sql = "INSERT INTO Orders(OrderId, OrderDate, CustomerId, EmployeeId) VALUES (?,?,?,?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, orderId);
		pstmt.setString(2, orderDate);
		pstmt.setString(3, customerId);
		pstmt.setString(4, employeeId);
		int rows = pstmt.executeUpdate();
    }
    
    /**
     * Creates a lineitem in the database.
     * You must use a PreparedStatement.
     * 
     * @throws SQLException 
     */    
    public void newLineItem(String orderId, String productId, int quantity, String price) throws SQLException
    {
		String sql = "INSERT INTO OrderedProduct VALUES (?,?,?,?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, orderId);
		pstmt.setString(2, productId);
		pstmt.setInt(3, quantity);
		pstmt.setString(4, price);
		int rows = pstmt.executeUpdate();
    }
    
    /**
     * Updates an order total in the database.
     * You must use a PreparedStatement.
     * 
     * @throws SQLException 
     */    
    public void updateOrderTotal(String orderId, BigDecimal total) throws SQLException
    {
		String sql = "UPDATE Orders SET Total = ? WHERE OrderId = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setBigDecimal(1, total);
		pstmt.setString(2, orderId);
		int rows = pstmt.executeUpdate();
    }	
    
    
    /**
     * Return the list of products that have not been in any order. Hint: Left join can be used instead of a subquery.
     * 
     * @return
     *      ResultSet
     * @throws SQLException
     *      if an error occurs
     */
    public ResultSet query1() throws SQLException
    {
        System.out.println("\nExecuting query #1.");

		String sql = "SELECT a.ProductId FROM Product a LEFT JOIN OrderedProduct b ON a.ProductId = b.ProductId" +
				" WHERE b.ProductId IS NULL";
		Statement stmt = con.createStatement();
		ResultSet result = stmt.executeQuery(sql);
		return result;
    }
    
    /**
     * Return the order ids and total amount where the order total does not equal the sum of quantity*price for all ordered products in the order.
     * 
     * @return
     *      ResultSet
     * @throws SQLException
     *      if an error occurs
     */
    public ResultSet query2() throws SQLException
    {
        System.out.println("\nExecuting query #2.");
		String sql = "SELECT OrderId, Total FROM Orders NATURAL JOIN " +
				"(SELECT OrderId, SUM(Quantity * Price) as sums FROM OrderedProduct GROUP BY OrderId) b " +
				"WHERE Total > sums OR Total < sums";
		Statement stmt = con.createStatement();
		ResultSet result = stmt.executeQuery(sql);
		return result;
    }
    
    /**
     * Return for each customer their id, name and average total order amount for orders starting on January 1, 2024 (inclusive). Only show customers that have placed at least 2 orders.
     * Sort customers in ascending order by customer id.
     * Format:
     * CustomerId, CustomerName, avgTotal
     * 00001, B. Brown, 489.952000
     * 
     * @return
     *      ResultSet
     * @throws SQLException
     *      if an error occurs
     */
    public ResultSet query3() throws SQLException
    {
        System.out.println("\nExecuting query #3.");
		String sql = "SELECT CustomerId, CustomerName, AVG(Total) as avgTotal " +
				"FROM Customer NATURAL JOIN Orders " +
				" WHERE OrderDate >= '2024-01-01' " +
				"GROUP BY CustomerId HAVING COUNT(OrderId) >= 2 " +
				"ORDER BY CustomerId ASC";
		Statement stmt = con.createStatement();
		ResultSet result = stmt.executeQuery(sql);
		return result;
    }
    
	/**
	 * Return the employees who have had at least 2 distinct orders where some product on the order had quantity >= 5.
	 * Format:
	 * EmployeeId, EmployeeName, orderCount
	 * 
	 * @return
	 * 		ResultSet
	 * @throws SQLException
	 * 		if an error occurs
	 */
	public ResultSet query4() throws SQLException
	{
		System.out.println("\nExecuting query #4.");
		String sql = "SELECT EmployeeId, EmployeeName, COUNT(OrderId) as orderCount " +
				"FROM Employee NATURAL JOIN " +
				"(SELECT OrderId, EmployeeId, SUM(Quantity) as qtt FROM Orders NATURAL JOIN OrderedProduct GROUP BY OrderId) b" +
				" WHERE qtt >= 5 " +
				"GROUP BY EmployeeId " +
				"HAVING orderCount >= 2" +
				"";
		Statement stmt = con.createStatement();
		ResultSet result = stmt.executeQuery(sql);
		return result;
	}
		
	/*
	 * Do not change anything below here.
	 */
	/**
     * Converts a ResultSet to a string with a given number of rows displayed.
     * Total rows are determined but only the first few are put into a string.
     * 
     * @param rst
     * 		ResultSet
     * @param maxrows
     * 		maximum number of rows to display
     * @return
     * 		String form of results
     * @throws SQLException
     * 		if a database error occurs
     */    
    public static String resultSetToString(ResultSet rst, int maxrows) throws SQLException
    {                       
        if (rst == null)
            return "No Resultset.";
        
        StringBuffer buf = new StringBuffer(5000);
        int rowCount = 0;
        ResultSetMetaData meta = rst.getMetaData();
        buf.append("Total columns: " + meta.getColumnCount());
        buf.append('\n');
        if (meta.getColumnCount() > 0)
            buf.append(meta.getColumnName(1));
        for (int j = 2; j <= meta.getColumnCount(); j++)
            buf.append(", " + meta.getColumnName(j));
        buf.append('\n');
                
        while (rst.next()) 
        {
            if (rowCount < maxrows)
            {
                for (int j = 0; j < meta.getColumnCount(); j++) 
                { 
                	Object obj = rst.getObject(j + 1);                	 	                       	                                	
                	buf.append(obj);                    
                    if (j != meta.getColumnCount() - 1)
                        buf.append(", ");                    
                }
                buf.append('\n');
            }
            rowCount++;
        }            
        buf.append("Total results: " + rowCount);
        return buf.toString();
    }        
}
