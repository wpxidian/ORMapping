package cn.edu.xidian.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.* ;
public class Test {
	
	private static String url = "jdbc:mysql://219.245.92.226/xxx" ;
	private static String username = "root" ;
	private static String password = "123456" ;
	public static void main(String[] args) {
		
		Connection conn = null ;
		CallableStatement cstmt = null ;
		ResultSet rs = null ;
		
		try {
			Class.forName("com.mysql.jdbc.Driver") ;
			conn = DriverManager.getConnection(url,username,password) ;
			String sql = "call pro_book(2)" ;
			cstmt = conn.prepareCall(sql) ;
			rs = cstmt.executeQuery() ;
			while(rs.next()){
				System.out.println(rs.getString("bookName"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
