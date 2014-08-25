package cn.edu.xidian.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.TestCase;

public class TestTransaction extends TestCase {
	
	public void testTransaction(){
		Connection conn = SimpleDataSource.getInstance().getConection() ;
		Statement pstmt = null ;
		
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED) ;
			conn.setAutoCommit(false) ;
			String sql = "select * from admin where id = '3'" ;
			pstmt = conn.createStatement() ;
			ResultSet rs = pstmt.executeQuery(sql) ;
			while(rs.next()){
				System.out.println(rs.getString("username"));
			}
			//conn.commit() ;
			conn.close() ;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
