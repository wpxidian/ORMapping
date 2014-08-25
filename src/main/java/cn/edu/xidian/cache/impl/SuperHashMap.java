package cn.edu.xidian.cache.impl;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/**
 * 实现缓存的类,支持同步,但是这里利用读写锁提高了效率
 * @author WangPeng 
 * @param <K> 对应数据库中的表名
 * @param <V> 表名对应的缓存
 * @version 1.0   
 * @since JDK 1.7
 */
public class SuperHashMap<K,V> extends HashMap<K,V>{
	
	private static final long serialVersionUID = 8230537307174947436L;
	
	private final ReentrantReadWriteLock readWriteLock ;
	
	private final Lock read ;
	
	private final Lock write ;
	
	public SuperHashMap(int parentInitCount,
			float parentEntityDefaultLoadFactor) {
		
		super(parentInitCount, parentEntityDefaultLoadFactor) ;
		
		readWriteLock = new ReentrantReadWriteLock() ;
		
		read = readWriteLock.readLock() ;
		
		write = readWriteLock.writeLock() ;
	}

	@Override
	public V get(Object key) {
		try {
			read.lock() ;
			return super.get(key) ;
		}finally{
			read.unlock() ;
		} 
	}

	@Override
	public V put(K key, V value) {
		try {
			write.lock() ;
			return super.put(key, value);
		}finally{
			write.unlock() ;
		}
	}

	@Override
	public V remove(Object key) {
		try {
			write.lock() ;
			return super.remove(key);
		}finally{
			write.unlock() ;
		}
	}

	@Override
	public boolean containsValue(Object value) {
		try {
			write.lock() ;
			return super.containsValue(value);
		}finally{
			write.unlock() ;
		}
	}
	
}
