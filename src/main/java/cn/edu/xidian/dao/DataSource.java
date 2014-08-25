package cn.edu.xidian.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
/**
 * 这是数据源接口，接口声明了得到连接和释放连接两个方法 
 * @author WangPeng 
 * @version 1.0   
 * @since JDK 1.7
 */
public interface DataSource {
	
	public Connection getConection();
	
	public void free(Connection conn, Statement stmt, ResultSet rs);

}
