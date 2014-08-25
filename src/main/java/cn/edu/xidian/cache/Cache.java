package cn.edu.xidian.cache;

/**
 * Cache，这是一个实现缓存功能的接口。
 * @author JiLu
 * @version 1.0
 */
public interface Cache {
	
	/**
	 * 得到某个缓存对象。
	 * @param parentKey 关键字1
	 * @param childKey 关键字2
	 * @return 若该缓存已存在则返回该旧缓存对象，否则返回null
	 */
	Object get(String parentKey,String childKey);
	
	/**
	 * 放入某个要缓存的对象
	 * @param parentKey 关键字1
	 * @param childKey 关键字2
	 * @param obj 要缓存的对象
	 * @return 若缓存中没有缓存对象则返回null
	 */
	Object put(String parentKey,String childKey,Object obj);
	
	/**
	 * 删除某个要缓存的对象
	 * @param parentKey 关键字1
	 * @param childKey 关键字2
	 * @return 删除缓存中某个缓存对象，若缓存中没有缓存对象则返回null
	 */
	Object remove(String parentKey,String childKey);
	
	/**
	 * 删除某个关键字1指定的一堆对象
	 * @param parentKey 关键字1
	 */
	void removeAll(String parentKey);

}
