/**
 * 
 */
package node.server.database;

import java.sql.*;
//import java.util.Map;

import node.server.database.DatabaseConfig;

/**
 * @author Willi
 *
 */
public class DBdtcapp extends DatabaseConfig{
	
	//static Map<Integer, String> mapMemberDB = null;
	static DatabaseConfig dc;
	static Connection conn;
	static Statement stmt;
	ResultSet rs = null;
	
	public DBdtcapp(){
		conn = DBConnection();
	}
	
	public void insertMember(){
		
	}
	
	public ResultSet loadMember() throws SQLException{
		String sql = "SELECT m.* FROM member m ORDER BY m.id";
		try{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		}finally{
			stmt.close();
			conn.close();
		}
		/*try {
			ResultSet rs = dc.stmt.executeQuery(sql);
			while(rs.next()){
				int id = rs.getInt("id");
				int memberPort = rs.getInt("port");
				String memberAddress = rs.getString("member_address");
				mapMemberDB.put(id, memberAddress, memberPort);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return rs;
	}
	
	public ResultSet loadCoordinator() throws SQLException{
		String sql = "SELECT c.* FROM coordinator c ORDER BY c.id";
		try{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		}finally{
			stmt.close();
			conn.close();
		}
		return rs;
	}
}
