package node.server.database;

import java.sql.*;

public class DatabaseConfig {

		// Database driver name & driver url
		private static final String JDBC_driver = "com.mysql.jdbc.Driver";
		private static final String DB_url = "jdbc:mysql://localhost:3306/db_dtcapp";
		
		// Database credentials
		static final String USER = "root";
		static final String PASS = "";
		
		static Connection conn = null;
		
		public Connection DBConnection(){
			try{
				Class.forName(JDBC_driver);
				conn = DriverManager.getConnection(DB_url, USER, PASS);
			}catch(SQLException se){
				// Error Handling for JDBC
				se.printStackTrace();
			}catch(Exception e){
				// Error Handling for Class.forName
				e.printStackTrace();
			}
			return conn;
		} 
	
}
