package cn.edu.xidian.cache;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
/**
 * 该工厂是用来生产一个Cache的实例，该实例是单例
 * @author WangPeng 
 * @version 1.0   
 * @since JDK 1.7
 */
public class CacheFactory {
	
	private static Cache cache = null ;
	
	/**
	 * 获取一个Cache单例
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @return Cache
	 */
	public static Cache getInstance(){
		
		if(cache == null){
			
			synchronized(CacheFactory.class){
				
				if(cache == null){
					
					try {
						
						Properties properties = new Properties() ;
						
						properties.load(CacheFactory.class.getClassLoader().getResourceAsStream("Cache.properties")) ;
						
						String className = properties.getProperty("Cache") ;
						
						Class<? extends Object> cacheClass = Class.forName(className) ;
						
						Method getInstanceMethod = cacheClass.getDeclaredMethod("getInstance",new Class<?>[]{}) ;
						
						cache = (Cache) getInstanceMethod.invoke(null,new Object[]{}) ;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return cache ;
	}
}
