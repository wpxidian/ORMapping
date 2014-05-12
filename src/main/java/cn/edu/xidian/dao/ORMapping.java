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

/**
 * 
 * 类名称：ORMapping   
 * 类描述：OR映射类，该类是一个泛型类，通过该类实现对数据对象的增删查改   
 * 创建人：WangPeng  
 * 创建时间：2014-5-11 下午5:33:03   
 * 修改人：WangPeng   
 * 修改时间：2014-5-11 下午5:33:03   
 * 修改备注：   
 * @version 1.0   
 *
 */
public class ORMapping {
	
	private Class<?> objectClass ;
	
	private Object object ;
	
	private String tableName ;
	
	private Field[] fields ;
	
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

		Connection conn = null ;
		PreparedStatement pstmt = null ;
		ResultSet rs = null ;
		
		try{
			
			init(objectClass) ;
			
			conn = SimpleDataSource.getInstance().getConection() ;
			pstmt = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE id=?") ;
			pstmt.setInt(1, id) ;
			rs = pstmt.executeQuery() ;
			try{
				while(rs.next()){
					getValuesFromResultSet(rs) ;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}finally{
				SimpleDataSource.getInstance().free(conn, pstmt, rs) ;
			}
			return  (T) object ;
		}catch(Exception e){
			e.printStackTrace() ;
			return null ;
		}
	}
	
	/**
	 * 
	 * 插入数据
	 * @param object
	 * @return void
	 */
	public <T> void save(T object) {
		
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
			
			if(pstmt.executeUpdate()>0){
				System.out.println("insert success");
			}
			
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
			
		}catch(Exception e){
			e.printStackTrace() ;
		}finally{
			SimpleDataSource.getInstance().free(conn, pstmt, null) ;
		}
	}
	
	
	
	/** #########################################################以下是一些辅助方案############################################################ */
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
