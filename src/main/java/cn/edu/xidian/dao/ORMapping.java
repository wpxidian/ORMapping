package cn.edu.xidian.dao;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import cn.edu.xidian.cache.Cache;
import cn.edu.xidian.cache.CacheFactory;

/**
 * OR映射类，该类是一个泛型类，通过该类实现对数据对象的增删查改 
 * @author WangPeng 
 * @version 1.0   
 * @since JDK 1.7
 */
public class ORMapping {
	
	private Class<?> objectClass ;
	
	private Object object ;
	
	private String tableName ;
	
	private Field[] fields ;
	
	private static Cache cache = CacheFactory.getInstance() ;
	
	private static volatile long sqlCount = 0L ;
	
	private static volatile long correctSqlCount = 0L;
	
	private static volatile long updateCount = 0L;
	
	public static long getSqlCount() {
		return sqlCount;
	}
	
	public static long getCorrectSqlCount() {
		return correctSqlCount;
	}
	
	public static double getCorrectRate()
	{
		return ((double)correctSqlCount)/sqlCount;
	}
	
	public static double getUpdate_query()
	{
		return ((double)updateCount)/sqlCount;
	}
	
	public <T> void init(Class<T> objectClass) throws InstantiationException, IllegalAccessException{
		
		this.objectClass = objectClass ;
		
		this.object = objectClass.newInstance() ;
		
		this.tableName = objectClass.getSimpleName().toLowerCase() ;
		
		this.fields = objectClass.getDeclaredFields() ;
	}
	/**
	 * 根据ID属性查询
	 * @param id
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> objectClass, int id){
		Logger log = Logger.getLogger(ORMapping.class) ;
		
		sqlCount++;
		
		boolean isCorrect = false ;
		
		long startTime = System.currentTimeMillis() ;
		
		Connection conn = null ;
		
		PreparedStatement pstmt = null ;
		
		ResultSet rs = null ;
		
		try{
			
			init(objectClass) ;
			
			conn = SimpleDataSource.getInstance().getConection() ;
			
			pstmt = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE id=?") ;
			
			pstmt.setInt(1, id) ;
			
			Object value = cache.get(tableName, getSQL(pstmt)) ;
			
			if(value != null){
				correctSqlCount++ ;
				isCorrect = true;
				return (T)value ;
			}
			
			rs = pstmt.executeQuery() ;
			
			while(rs.next()){
				
				getValuesFromResultSet(rs) ;
			}
			
			cache.put(tableName, getSQL(pstmt), object) ;
			log.info("cache info:\n\tcorrectRate:"+ORMapping.getCorrectRate()+"\n\tupdate/query:"+
					ORMapping.getUpdate_query()+"\n\tsqlCount:"+ORMapping.getSqlCount()+"\tcorrectSQLCount:"+
					ORMapping.getCorrectSqlCount()+"\tupdateCount:"+ORMapping.getUpdate_query());
			
			return  (T) object ;
		}catch(Exception e){
			e.printStackTrace() ;
			return null ;
		}finally{
			log.info("ORMappingInfo[SQL:"+getSQL(pstmt)+",IsCorrect:"+isCorrect+",Timespan:"+(System.currentTimeMillis()-startTime)+"ms]");
			SimpleDataSource.getInstance().free(conn, pstmt, rs) ;
		}
	}
	
	/**
	 * 
	 * 插入数据
	 * @param object
	 * @return void
	 */
	public <T> void save(T object) {
		Logger log = Logger.getLogger(ORMapping.class) ;
		
		long startTime = System.currentTimeMillis() ;
		
		Connection conn = null ;
		
		PreparedStatement pstmt = null ;
		
		StringBuilder key = new StringBuilder() ;
		
		StringBuilder value = new StringBuilder() ;
		/**
		 * 保存需要操作的属性
		 */
		List<Field> fieldList = new ArrayList<Field>() ;
		
		try{
			
			init(object.getClass()) ;
			
			this.object = object ;
			
			fullKeyValue(fieldList, key, value) ;
				
			String sql = "INSERT INTO " + tableName + "(" + key + ") VALUES" + "(" + value + ")" ;
			
			conn = SimpleDataSource.getInstance().getConection() ;
			
			pstmt = conn.prepareStatement(sql) ;
			
			fullPrepareStatement(pstmt,fieldList) ;
			
			pstmt.executeUpdate() ;
			
			updateCount++ ;
			
			cache.removeAll(tableName) ;
			
			log.info("ORMappingInfo[SQL:"+getSQL(pstmt)+",Timespan:"+(System.currentTimeMillis()-startTime)+"ms]");
		}catch(Exception e){
			e.printStackTrace() ;
		}finally{
			SimpleDataSource.getInstance().free(conn, pstmt, null) ;
		}
	}
	/**
	 * 更新数据
	 * @param object
	 * @return void
	 */
	public <T> void update(T object){
		Logger log = Logger.getLogger(ORMapping.class) ;
		
		long startTime = System.currentTimeMillis() ;
		
		Connection conn = null ;
		
		PreparedStatement pstmt = null ;
		
		/**
		 * 保存需要操作的属性
		 */
		List<Field> fieldList = new ArrayList<Field>() ;
		try{
			
			init(object.getClass()) ;
			
			this.object = object ;
			
			StringBuilder setStatement = new StringBuilder() ;
			
			for(int i=0;i<fields.length-1;i++){
				
				String fieldName = fields[i].getName() ;
				
				if(!("id".equals(fieldName))){
					
					fieldList.add(fields[i]) ;
					setStatement.append(fieldName + "=?,") ;
				}
			}
			if(!("id".equals(fields[fields.length-1].getName()))){
				
				fieldList.add(fields[fields.length-1]) ;
				setStatement.append(fields[fields.length-1].getName() + "=?") ;
				
			}
			
			String sql = "UPDATE " + tableName + " SET " + setStatement + " WHERE id=?" ;
			
			conn = SimpleDataSource.getInstance().getConection() ;
			
			pstmt = conn.prepareStatement(sql) ;
			
			fullPrepareStatement(pstmt, fieldList) ;
			
			Method method = objectClass.getMethod("getId") ;
			
			pstmt.setInt(fieldList.size()+1, (Integer) method.invoke(object)) ;
			
			pstmt.executeUpdate() ;
			
			updateCount++ ;
			
			cache.removeAll(tableName) ;
			
			log.info("ORMappingInfo[SQL:"+getSQL(pstmt)+",Timespan:"+(System.currentTimeMillis()-startTime)+"ms]");
		}catch(Exception e){
			e.printStackTrace() ;
		}finally{
			SimpleDataSource.getInstance().free(conn, pstmt, null) ;
		}
	}
	
	/**
	 * 删除数据
	 * @param object
	 * @return void
	 */
	public <T> void delelte(T object){
		Logger log = Logger.getLogger(ORMapping.class) ;
		
		long startTime = System.currentTimeMillis() ;
		
		Connection conn = null ;
		
		PreparedStatement pstmt = null ;
		
		try{
			
			init(object.getClass()) ;
			
			this.object = object ;
			
			String sql = "DELETE FROM " + tableName + " WHERE id=?" ;
			
			conn = SimpleDataSource.getInstance().getConection() ;
			pstmt = conn.prepareStatement(sql) ;
			
			Method method = objectClass.getMethod("getId") ;
			
			pstmt.setInt(1, (Integer) method.invoke(object)) ;
			
			pstmt.execute() ;
			
			updateCount++ ;
			
			cache.removeAll(tableName) ;
			
			log.info("ORMappingInfo[SQL:"+getSQL(pstmt)+",Timespan:"+(System.currentTimeMillis()-startTime)+"ms]");
		}catch(Exception e){
			e.printStackTrace() ;
		}finally{
			SimpleDataSource.getInstance().free(conn, pstmt, null) ;
		}
	}
	
	
	
	/** #########################################################以下是一些辅助方案############################################################ */
	
	/**
	 * @return String
	 * @throws
	 */
	private String getSQL(PreparedStatement pstmt) {
		String[] strs = pstmt.toString().split(":") ;
		return strs[1];
	}
	
	/**
	 * 为PreparedStatement填充所需要的参数
	 * @param pstmt
	 * @return void
	 * @throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	 */
	public void fullPrepareStatement(PreparedStatement pstmt, List<Field> fieldList) 
			throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, 
				   IllegalArgumentException, InvocationTargetException{
		
		for(int i=0;i<fieldList.size();i++){
			
			Field field = fieldList.get(i) ;
			
			String fieldName = field.getName() ;
			Class<?> fieldType = field.getType() ;
			
			Method method = objectClass.getMethod("get" + formatString(fieldName)) ;
			
			if(fieldType == String.class){
				pstmt.setString(i+1, (String)method.invoke(object)) ;
			}else if(fieldType == int.class){
				pstmt.setInt(i+1, (Integer)method.invoke(object)) ;
			}else if(fieldType == float.class){
				pstmt.setFloat(i+1, (Float)method.invoke(object)) ;
			}else if(fieldType == double.class){
				pstmt.setDouble(i+1, (Double)method.invoke(object)) ;
			}else if(fieldType == boolean.class){
				pstmt.setBoolean(i+1, (Boolean)method.invoke(object)) ;
			}else if(fieldType == Date.class){
				pstmt.setDate(i+1, new java.sql.Date(((Date)method.invoke(object)).getTime()));
			}
				
			
		}
	}
	/**
	 * 调用目标对象属性相应的Setter方法，将查询结果集的数值赋值给目标对象相应的属性
	 * @param rs
	 * @return void
	 */
	public void getValuesFromResultSet(ResultSet rs)
			throws NoSuchMethodException, SecurityException, IllegalAccessException,
				   IllegalArgumentException, InvocationTargetException, SQLException{
		
		for(int i=0;i<fields.length;i++){
			
			String fieldName = fields[i].getName() ;
			Class<?> fieldType = fields[i].getType() ;
			Method method = objectClass.getMethod("set" + formatString(fieldName), fieldType) ;
			
			if(fieldType == String.class){
				method.invoke(object, rs.getString(fieldName)) ;
			}else if(fieldType == int.class){
				method.invoke(object, rs.getInt(fieldName)) ;
			}else if(fieldType == float.class){
				method.invoke(object, rs.getFloat(fieldName)) ;
			}else if(fieldType == double.class){
				method.invoke(object, rs.getDouble(fieldName)) ;
			}else if(fieldType == boolean.class){
				method.invoke(object, rs.getBoolean(fieldName)) ;
			}else if(fieldType == Date.class){
				method.invoke(object, rs.getDate(fieldName)) ;
			}
		}
	}
	/**
	 * 将属性名的首字符转化成大写
	 * @param  str
	 * @return String
	 */
	public String formatString(String str){
		return str.substring(0,1).toUpperCase() + str.substring(1) ;
	}
	/**
	 * 获取除ID以外的所有属性
	 * @return void
	 */
	public void fullKeyValue(List<Field> fieldList, StringBuilder key, StringBuilder value){
		
		for(int i=0;i<fields.length-1;i++){
			
			String fieldName = fields[i].getName() ;
			
			if(!("id".equals(fieldName))){
				
				fieldList.add(fields[i]) ;
				key.append(fields[i].getName() + ",") ;
				value.append("?,") ;
			}
		}
		if(!("id".equals(fields[fields.length-1].getName()))){
			
			fieldList.add(fields[fields.length-1]) ;
			key.append(fields[fields.length-1].getName()) ;
			value.append("?") ;
		}
	}
}
