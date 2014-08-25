package cn.edu.xidian.dao;

import static org.junit.Assert.*;

import java.sql.Connection;
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
}
