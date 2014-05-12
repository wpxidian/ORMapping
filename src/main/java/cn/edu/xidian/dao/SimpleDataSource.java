package cn.edu.xidian.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;

import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * 
 * 类名称：SimpleDataSource   
 * 类描述：这是数据源(DataSource)的一个简单实现，该数据源是线程安全的单例模式。
 * 创建人：WangPeng  
 * 创建时间：2014-5-11 下午3:20:09   
 * 修改人：WangPeng   
 * 修改时间：2014-5-11 下午3:20:09   
 * 修改备注：   
 * @version 1.0   
 *
 */
public class SimpleDataSource implements DataSource{
 
	private int currentCount = 0;
	private int InitCount = 5;
	private int MaxActive = 10;
	private int MaxCount = 100;
	private String DatabaseUrl = null;
	private String DatabaseUser = null;
	private String DatabasePassword = null;
	private String JDBCClass = null;
		
	private static SimpleDataSource simpleDataSourseInstance;
	
	/**
	 * 用一个链表来存储数据库连接池的连接
	 */
	private LinkedList<Connection>  connectionsPool = new LinkedList<Connection>();
	
	/**
	 * 用私有的构造器初始化数据库连接池
	 */
	private SimpleDataSource() {
		try {

			Properties properties = new Properties();
			
			/**
			 * 用ClassLoader来读取制定文件的输入流
			 */
			properties.load(this.getClass().getClassLoader()
					.getResourceAsStream("Database.properties"));

			DatabaseUrl = properties.getProperty("DatabaseUrl");
			DatabaseUser = properties.getProperty("DatabaseUser");
			DatabasePassword = properties.getProperty("DatabasePassword");
			JDBCClass = properties.getProperty("JDBCClass");
			
			InitCount = Integer.parseInt(properties.getProperty("InitCount"));
			MaxCount = Integer.parseInt(properties.getProperty("MaxCount"));
			MaxActive = Integer.parseInt(properties.getProperty("MaxActive"));
			/**
			 * 用反射来加载数据库驱动程序类
			 */
			Class.forName(JDBCClass);
			
			/**
			 * 初始化数据库连接池，创建制定数量的数据库连接
			 */
			for (int i = 0; i < InitCount; i++) {
				this.connectionsPool.addLast(this.createConnection());
			}
			this.currentCount = this.InitCount;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 用一个类方法来创建数据库连接池SimpleDataSource的实例
	 * @return SimpleDataSource
	 * @throws
	 */
	public static SimpleDataSource getInstance() {
		/**
		 * Double-Check Locking双重检查锁定
		 */
		if (simpleDataSourseInstance == null) {
			
			//对类的静态锁加锁
			synchronized (SimpleDataSource.class) {
				
				if (simpleDataSourseInstance == null) {
					Logger log = Logger.getLogger(SimpleDataSource.class);
					log.info("createing datasource:SimpleDataSource!");
					simpleDataSourseInstance = new SimpleDataSource();
					return simpleDataSourseInstance;
				}
				
			}
			
		}
		
		return simpleDataSourseInstance;

	}

	/**
	 * 获取数据库的连接
	 * 1、如果连接池中的连接数不为空，则从链表中取出第一个元素，1）若该元素为空或已关闭，表明该元素处于无效状态，应该移除，则当前连接池中的连接数量减一，
	 * 此时链接数量没有超过最大值，为了保证连接池的连接数量，就得新创建一个连接，并将当前连接数加一，2）若该元素不为空且处于打开状态，则将改连接返回。
	 * 2、如果连接池中的连接数为空，则新建一个连接，并将当前连接数加一。
	 */
	@Override
	public synchronized Connection getConection() {
		Connection conn = null;

		while (this.connectionsPool.size() > 0) {

			conn = this.connectionsPool.removeFirst();
			
			try{
				if(conn == null){
					this.currentCount--;
				}else if (conn.isClosed()) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						this.currentCount--;
					}
				} else {
					return conn;
				}
			}catch (SQLException e) {
				e.printStackTrace();
				this.currentCount--;
			} 

		}

		// 没有超过最大长度(maxCount)
		if (this.currentCount < MaxCount) {

			this.currentCount++;
			try {
				return this.createConnection();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException("connection out of max count!");
			}
		}

		// 超过最大长度(maxCount)
		throw new RuntimeException("connection out of max count!");

	}

	/**
	 * 释放连接
	 * 1、关闭结果集
	 * 2、关闭Statement
	 * 3、如果连接为空或已关闭，表明该连接已失效，则当前的数据库连接池中的连接数量应该减一
	 * 	    如果当前的链接数量超过了最大值，为了保证连接数量，关闭该连接，并将当前的连接数减一
	 * 	  如果以上两种情况都不满足，则将连接放回连接池，即重新添加到链表中
	 */
	@Override
	public synchronized void free(Connection conn, Statement stmt, ResultSet rs) {
		try{
			if(rs !=null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			//释放Connenction
			try{
				if (conn == null || conn.isClosed()) {     //该连接已失效
					this.currentCount--;
				}else if (this.connectionsPool.size() > MaxActive) {  //大于持有的应最大连接
					try {
						conn.close();
					} catch (Throwable e) {
						e.printStackTrace();
					} finally {
						this.currentCount--;
					}
				} else {
					this.connectionsPool.addLast(conn);
				}
			}catch (SQLException e) {
				//一定是conn.isClosed造成的Exception
				e.printStackTrace();
				this.currentCount--;
			}
		}
		
	}

	/**
	 * 根据数据源创建一个新的数据库连接
	 */
	private Connection createConnection() throws SQLException {
		return DriverManager.getConnection(this.DatabaseUrl, this.DatabaseUser,
				this.DatabasePassword);
	}

} 
