package cn.edu.xidian.cache.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
/**
 * 利用LinkedHashMap实现简单的缓存LRU， 必须实现removeEldestEntry方法
 * @author WangPeng 
 * @param <K> 对应的查询语句
 * @param <V> 查询得到的实体对象，或实体对象集合
 * @version 1.0   
 * @since JDK 1.7
 */
public class SubLinkedHashMap<K,V> extends LinkedHashMap<K,V>{

	private static final long serialVersionUID = -86913028213019458L;
	
	private final ReentrantReadWriteLock readWriteLock ;
	
	/**
	 * 对应缓存中实体对象的个数，用于控制缓存的大小,LRU缓存
	 */
	private final int maxCapacity ;
	
	private volatile int currentEntityCount ;
	
	private final Lock read ;
	
	private final Lock write ;
	
	public SubLinkedHashMap(int childEntityCount,
			float childEntityDefaultLoadFactor) {
		super(childEntityCount, childEntityDefaultLoadFactor,true) ;
		
		maxCapacity = childEntityCount ;
		
		currentEntityCount = 0 ;
		
		readWriteLock = new ReentrantReadWriteLock() ;
		
		read = readWriteLock.readLock() ;
		
		write = readWriteLock.writeLock() ;
	}

	/**
	 * 
	 */
	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		
		Logger log  = Logger.getLogger(SubLinkedHashMap.class);
		
    	log.debug("removeEldestEntryBefore:" + currentEntityCount + ">" + maxCapacity + ":" + (currentEntityCount > maxCapacity));
    	
		boolean isOverFlow = currentEntityCount > maxCapacity ;
		
		if(isOverFlow){
			currentEntityCount -= getValueLength(eldest.getValue()) ;
		}
		
		log.debug("removeEldestEntryBeforeReturn:" + currentEntityCount + ">" + maxCapacity + ":" + (currentEntityCount > maxCapacity));
		return isOverFlow ;
	}

	/**
	 * 获取指定的sql语句对应的实体对象，或实体集合
	 */
	@Override
	public V get(Object key) {
		try {
			read.lock() ;
			return super.get(key) ;
		}finally{
			read.unlock() ;
		} 
	}
	/**
	 * 添加元素，并在currentEntityCount基础上增加相应的实体对象个数
	 */
	@Override
	public V put(K key, V value) {
		try {
			
			write.lock() ;
			
			V oldValue = super.put(key, value);
			
			int oldValueLen = getValueLength(oldValue) ;
			
			int valueLen = getValueLength(value) ;
			
			int incLen = valueLen - oldValueLen ;
			
			currentEntityCount += incLen ;
			
			return oldValue ;
		}finally{
			write.unlock() ;
		}
	}

	/**
	 * 删除指定的元素，并从currentEntityCount减去相应的实体对象个数
	 */
	@Override
	public V remove(Object key) {
		try {
			
			write.lock() ;
			
			V oldValue = super.remove(key) ;
			
			int oldValueLen = getValueLength(oldValue) ;
			
			currentEntityCount -= oldValueLen ;
			
			return oldValue ;
		}finally{
			write.unlock() ;
		}
	}
	/**
	 * 清空该Map对象中的元素
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void clear() {
		try {
			write.lock() ;
			
			for(Iterator iterator = this.keySet().iterator();iterator.hasNext();){
				remove(iterator.next()) ;
			}
			
		}finally{
			write.unlock() ;
		}
	}
	
	@Override
	public int size() {
		try {
			read.lock() ;
			return super.size();
		}finally{
			read.unlock() ;
		}
	}

	@Override
	public boolean containsKey(Object key) {
		try {
			read.lock() ;
			return super.containsKey(key);
		}finally{
			read.unlock() ;
		}
	}

	/**
	 * 获取指定键对应的实体元素集合的长度
	 * @param value
	 * @return int
	 */
	@SuppressWarnings("rawtypes")
	private int getValueLength(V value) {
		
		if(value != null){
			
			if(value instanceof Collection){
				
				Collection collection = (Collection)value ;
				
				return collection.size() ;
			}else if(value.getClass().isArray()){
				
				Object[] objs = (Object[]) value ;
				
				return objs.length ;
			}else{
				
				return 1 ;
			}
		}
		return 0 ;
	}
	
}
