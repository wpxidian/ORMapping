package cn.edu.xidian.dao;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

import cn.edu.xidian.dao.SimpleDataSource;
import cn.edu.xidian.model.Admin;

public class TestSimpleDataSource {
	
	@Test
	public void testConnection(){
		
		SimpleDataSource simpleDataSource = SimpleDataSource.getInstance() ;
		
		Connection conn = simpleDataSource.getConection() ;
		
		Statement stmt = null ;
		
		ResultSet rs = null ;
		
		try {
			
			stmt = conn.createStatement() ;
			rs = stmt.executeQuery("select * from admin where id=1") ;
			while(rs.next()){
				assertEquals(rs.getString("username"),"xappc") ;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			simpleDataSource.free(conn, stmt, rs) ;
		}
	}
	
	@Test
	public void testGetById(){
		
		ORMapping ORM = new ORMapping() ;
		
		Admin admin = ORM.get(Admin.class, 1) ;
		
		assertEquals(admin.getId(), 1) ;
		
	}
	
	@Test
	public void testInsert(){
		Admin admin = new Admin() ;
		admin.setUsername("zhangsan") ;
		admin.setPassword("123456") ;
		ORMapping ORM = new ORMapping() ;
		ORM.save(admin) ;
	}
	@Test
	public void testUpdate(){
		
		ORMapping ORM = new ORMapping() ;
		
		Admin admin = ORM.get(Admin.class, 2) ;
		
		admin.setPassword("SSSSSS") ;
		
		ORM.update(admin) ;
		
		admin = ORM.get(Admin.class, 2) ;
		
		assertEquals(admin.getPassword(), "SSSSSS") ;
	}
	@Test
	public void testDelete(){
		
		ORMapping ORM = new ORMapping() ;
		Admin admin = ORM.get(Admin.class, 2) ;
		ORM.delelte(admin) ;
	}
	@Test
	public void testIsolationLevel(){
		Connection conn = SimpleDataSource.getInstance().getConection() ;
		PreparedStatement pstmt = null ;
		
		try {
			/*int level = conn.getTransactionIsolation() ;
			 if(level == Connection.TRANSACTION_NONE)
	             System.out.println("TRANSACTION_NONE");
	            else if(level == Connection.TRANSACTION_READ_UNCOMMITTED)
	             System.out.println("TRANSACTION_READ_UNCOMMITTED");
	            else if(level == Connection.TRANSACTION_READ_COMMITTED)
	             System.out.println("TRANSACTION_READ_COMMITTED");
	            else if(level == Connection.TRANSACTION_REPEATABLE_READ)
	             System.out.println("TRANSACTION_REPEATABLE_READ");
	            else if(level == Connection.TRANSACTION_SERIALIZABLE)
	             System.out.println("TRANSACTION_SERIALIZABLE");*/
			 /** 数据库对其预编译，PrepareStatement会放入数据库的缓冲区（LRUcache）中，下次有相同的sql会调用缓冲区中的PrepareStatement，不会重新编译*/
			 String sql = "insert into admin(username,password) values(?,?)" ;
			 pstmt = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE) ;
			 /** 批处理 */
			 for(int i=100;i<110;i++){
				 pstmt.setString(1, i+"") ;
				 pstmt.setString(2, "xian" + i) ;
				 pstmt.addBatch() ;
			 }
			 pstmt.executeBatch() ;
			 
			 //conn.rollback() ;
			 //conn.commit() ;
			 conn.close() ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 测试事务读脏数据
	 * @return void
	 * @throws
	 */
	@Test
	public void testDirty(){
		Connection conn = SimpleDataSource.getInstance().getConection() ;
		Statement pstmt = null ;
		
		try {
			
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED) ;
			conn.setAutoCommit(false) ;
			String sql = "update admin set username='wdfasfd' where id = '3'" ;
			pstmt = conn.createStatement() ;
			
			pstmt.execute(sql) ;
			
			Thread.sleep(10000) ;
			conn.rollback() ;
			 //conn.rollback() ;
			 //conn.commit() ;
			conn.close() ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
