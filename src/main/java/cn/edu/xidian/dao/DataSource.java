package cn.edu.xidian.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
/**
<<<<<<< HEAD
 * 这是数据源接口，接口声明了得到连接和释放连接两个方法 
 * @author WangPeng 
 * @version 1.0   
 * @since JDK 1.7
=======
 * 
 * 类名称：DataSource   
 * 类描述：这是数据源接口，接口声明了得到连接和释放连接两个方法   
 * 创建人：WangPeng  
 * 创建时间：2014-5-11 下午4:00:53   
 * 修改人：WangPeng   
 * 修改时间：2014-5-11 下午4:00:53   
 * 修改备注：   
 * @version 1.0   
 *
>>>>>>> 53089e316f00bf934408d86a9098454d9c24e223
 */
public interface DataSource {
	
	public Connection getConection();
	
	public void free(Connection conn, Statement stmt, ResultSet rs);

}
