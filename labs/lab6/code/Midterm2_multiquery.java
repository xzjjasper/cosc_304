import java.sql.*;

public class Midterm2_multiquery
{	public static void main(String[] args)
	{	String url = "jdbc:mysql://localhost/workson";
		String uid = "testuser";
		String pw = "304testpw";

		// Loading a driver is not required
		try 
		(Connection con=DriverManager.getConnection(url, uid, pw);
		Statement stmt = con.createStatement(); )
		{
			String sql = "SELECT D.dno, dname, COUNT(eno), SUM(salary) FROM emp E JOIN dept D ON E.dno=D.dno"
							+ " WHERE bdate > '1950-12-01' GROUP BY D.dno, dname ORDER BY D.dno ASC";

			String sql2 = "SELECT eno, ename, salary FROM emp WHERE bdate > '1950-12-01' and dno = ? ORDER BY salary DESC";
			PreparedStatement pstmt = con.prepareStatement(sql2);
				
			ResultSet rst = stmt.executeQuery(sql);
			// Iterate through list of departments
			while (rst.next())
			{
				String dno = rst.getString(1);
				String dname = rst.getString(2);
				System.out.println("Department: " + dno + " Name: " + dname);
				
				// Retrieve employees for this department
				pstmt.setString(1, dno);
				ResultSet rst2 = pstmt.executeQuery();
				while (rst2.next())
					System.out.println(rst2.getString(1)+"\t"+rst2.getString(2)+"\t"+rst2.getString(3));
					
				// Print summary line for department
				System.out.println("Total employees: "+rst.getInt(3)+"\tTotal salary: "+rst.getDouble(4)+"\n");	
				rst2.close();
			}
		}
		catch (SQLException ex)
		{
			System.out.println("SQLException: " + ex);
		}		
	}
}


