package cn.edu.xidian.cache.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import cn.edu.xidian.cache.Cache;

/**
 * Cache接口的实现类
 * @author WangPeng 
 * @version 1.0   
 * @since JDK 1.7
 */
public class FastSimpleCache implements Cache{
	
	private static FastSimpleCache fastSimpleCache ;
	
	private int parentInitCount = 5 ;
	
	private int childEntityCount = 80 ;
	
	private float parentEntityDefaultLoadFactor = 0.75F ;
	
	private float childEntityDefaultLoadFactor = 0.75F ;
	
	private Map<String,Map<String,Object>> cache ;
	
	private FastSimpleCache(){
		
		try {
			Logger log = Logger.getLogger(FastSimpleCache.class);
			
			log.info("use faster simple cache as cache");
			
			Properties properties = new Properties() ;
			
			properties.load(FastSimpleCache.class.getClassLoader().getResourceAsStream("fasterSimpleCache.properties")) ;
			
			parentInitCount = Integer.parseInt(properties.getProperty("parentInitCount")) ;
			
			childEntityCount = Integer.parseInt(properties.getProperty("childEntityCount")) ;
			
			parentEntityDefaultLoadFactor = Float.parseFloat(properties.getProperty("parentEntityDefaultLoadFactor")) ;
			
			childEntityDefaultLoadFactor = Float.parseFloat(properties.getProperty("childEntityDefaultLoadFactor")) ;
			
			cache = new SuperHashMap<String, Map<String, Object>>(parentInitCount,parentEntityDefaultLoadFactor) ;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static FastSimpleCache getInstance(){
		
		if(fastSimpleCache == null){
			
			synchronized(FastSimpleCache.class){
				
				if(fastSimpleCache == null){
					
					fastSimpleCache = new FastSimpleCache() ;
				}
			}
		}
		return fastSimpleCache ;
	}

	@Override
	public Object get(String parentKey, String childKey) {
		
		Map<String,Object> subCache = cache.get(parentKey) ;
		
		if(subCache != null){
			
			subCache.get(childKey) ;
		}
		
		return null;
	}

	@Override
	public Object put(String parentKey, String childKey, Object obj) {
		
		Map<String,Object> subCache = cache.get(parentKey) ;
		
		if(subCache == null){
			
			subCache = new SubLinkedHashMap<String,Object>(childEntityCount,childEntityDefaultLoadFactor) ;
			
			subCache.put(childKey, obj) ;
			
			cache.put(parentKey, subCache) ;
			
		}else{
			
			subCache.put(childKey, obj) ;
		}
		return obj;
	}

	@Override
	public Object remove(String parentKey, String childKey) {
		
		Map<String,Object> subCache = cache.get(parentKey) ;
		
		if(subCache != null){
			
			subCache.remove(childKey) ;
		}
		return null;
	}

	@Override
	public void removeAll(String parentKey) {
		
		cache.remove(parentKey) ;
	}
}
