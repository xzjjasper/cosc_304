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
			String sql = "SELECT e.eno, e.ename, count(s.eno) as supervisees, SUM(s.salary) as salaries " +
					"FROM emp e LEFT OUTER JOIN emp s ON e.eno = s.supereno " +
					"WHERE e.ename LIKE '%s%' OR e.ename LIKE '%e%' " +
					"GROUP BY e.eno " +
					"ORDER BY supervisees DESC, salaries DESC";

			String sql2 = "SELECT eno, ename, salary, bdate, supereno FROM emp WHERE supereno = ? ORDER BY salary ASC";
			PreparedStatement pstmt = con.prepareStatement(sql2);
				
			ResultSet rst = stmt.executeQuery(sql);
			// Iterate through list of departments
			while (rst.next())
			{
				String supereno = rst.getString(1);
				int superviseeNo = rst.getInt(3);
				//print supervisor
				System.out.println(rst.getString(1) + "\t" + rst.getString(2) + "\t" +
						rst.getString(3) + "\t" + rst.getDouble(4));
				System.out.println("Employees supervised:");
				//print supervisee
				if (superviseeNo != 0){
					pstmt.setString(1, supereno);
					ResultSet rst2 = pstmt.executeQuery();
					while (rst2.next()){
						System.out.println(rst2.getString(1) + "\t" + rst2.getString(2) + "\t" +
								rst2.getString(3) + "\t" + rst2.getString(4) + "\t" + rst2.getString(5));
					}
					rst2.close();
				}
				System.out.println();
			}
			rst.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			System.out.println("SQLException: " + ex);
		}		
	}
}


