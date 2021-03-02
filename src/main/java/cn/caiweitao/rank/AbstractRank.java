package cn.caiweitao.rank;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author caiweitao
 * @Date 2020年3月5日
 * @Description 排行榜
 */
public abstract class AbstractRank<K,RankObject> {
	private List<RankObject> rankList;
	private Map<Object,Integer> rankIndex ;//<key,排名序号>
	
	private int max;//储存的最大排名
	private int top;//显示的最大排名 (max >= top)
	
	private final Lock readLock; // 读锁,读之间不会阻塞。读写锁 适合 读的多 ，写的少
	private final Lock writeLock; // 写锁，读写互斥，写写互斥。读读互容
	
	public AbstractRank(int max,int top) {
		if (max < top) {
			throw new RuntimeException("max < top");
		}
		this.max = max;
		this.top = top;
		ReadWriteLock look = new ReentrantReadWriteLock();
		this.readLock = look.readLock();
		this.writeLock = look.writeLock();
	}
	
	public AbstractRank() {
		this(200, 100);
	}
	
	/**
	 * 初始化排行榜数据
	 */
	public void init () {
		try {
			writeLock.lock();
			rankList = getInitData(max);
			if (rankList.size() > max) {
				rankList = rankList.subList(0, max);
			}
			resetRankIndex(rankList,0);
			
			Type type = ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[1];
			System.out.println(("调用 init(),重置了排行榜数据，【rankObject:"+type+"】"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * 检查对象是否需要更新排行榜
	 * @param rankObject
	 */
	public void check (RankObject rankObject) {
		if (rankObject == null) {
			return;
		}

		Object key = key(rankObject);
		try {
			writeLock.lock();
			Integer rankObjectIndex = rankIndex.get(key);
			if (rankObjectIndex != null) {
				// 原本上榜，而且榜名单已满，此时降到榜外，必须重置排行榜数据，因为不知道新上榜的人是谁（极端情况）
				if (rankList.size() >= max && !gt(rankObject, rankList.get(rankList.size() - 1))) {
					init();
					return;
				}
				rankList.remove(rankObjectIndex.intValue());
				rankIndex.remove(key);
			}

			int gtIndex = binarySearchPosition(rankObject);
			if (gtIndex < 0) {//没上榜
				return;
			}
			rankList.add(gtIndex, rankObject);
			rankIndex.put(key, gtIndex);

			//只保存 max 个
			int size = rankList.size();
			while (size > this.max) {
				RankObject remove = rankList.remove(size - 1);
				rankIndex.remove(key(remove));
				size--;
			}
			
			// 更新受到影响的排名
			int begin = 0;
			if (rankObjectIndex != null && gtIndex > rankObjectIndex.intValue()) {// 比原本的位置更后
				begin = rankObjectIndex.intValue();
			} else if (rankObjectIndex == null || gtIndex < rankObjectIndex.intValue()) {//原本没上榜 || 原本有上榜而且比原来的更靠前
				begin = gtIndex + 1;
			}
			List<RankObject> updateIndexList = rankList.subList(begin,rankList.size());
			resetRankIndex(updateIndexList,begin);
			
//			print(this.rankList);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * 二分法查找要插入的位置
	 * @param rankObject
	 * @return 返回-1代表没位置插入
	 */
	private int binarySearchPosition (RankObject rankObject) {
		if (rankObject == null) 
			return -1;
		if (rankList.size() == 0) 
			return 0;
		int low = 0;//列表头部，对应位置存放第一名对象
		int high = rankList.size() - 1;//列表尾部，对应位置存放最后一名对象
		
		if (!gt(rankObject,rankList.get(high))) {// 比最小的都还小
			if (rankList.size() < max) {// 没满，插到最后
				return rankList.size();
			} else {
				return -1;
			}
		} else if (gt(rankObject,rankList.get(low))) {// 比最大的还大
			return 0;
		}
		
        while (low < high) {
            int mid = (low + high) >>> 1;//取中间值
            RankObject midObject = rankList.get(mid);
            
            if (gt(rankObject, midObject)) {
            	high = mid;
            } else {
            	low = mid;
            }
            
            if (low == high || low + 1 == high) {
            	if (gt(rankObject, rankList.get(high))) {
            		return high;
            	} else {
            		return high + 1;
            	}
            }
        }
		
		return -1;
	}
	
	/**
	 * 得到排名
	 * @param key
	 * @return 返回-1时表示超出 max值(没上榜)
	 */
	public int getRank (K key) {
		int rank = -1;
		if (key == null) {
			return rank;
		}
		try {
			readLock.lock();
			Integer index = rankIndex.get(key);
			if (index != null) {
				rank = index + 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			readLock.unlock();
		}
		return rank;
	}
	
	/**
	 * 得到前 top个对象
	 * @return
	 */
	public List<RankObject> getTopList () {
		try {
			readLock.lock();
			if (rankList != null) {
				int size = rankList.size();
				return rankList.subList(0, size >= top ? top : size);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			readLock.unlock();
		}
		return null;
	}
	
	/**
	 * 重置 排名记录
	 * @param rankList 
	 * @param beginIndex
	 */
	private void resetRankIndex (List<RankObject> rankList,int beginIndex) {
		if (rankList == null) {
			return;
		}
		if (rankIndex == null) {
			rankIndex = new HashMap<Object, Integer>(rankList.size());
		}
		RankObject temp;
		for (int i = 0, len = rankList.size(); i < len; i++) {
			temp = rankList.get(i);
			rankIndex.put(key(temp), beginIndex + i);
		}
	}
	
	/**
	 * 获得排行榜初始数据
	 * @param max 最大数量
	 * @return
	 */
	protected abstract List<RankObject> getInitData (int max);
	
	/**
	 * 排行榜中每个对象的唯一标示，用于识别对象
	 * @param rankObject
	 * @return
	 */
	public abstract K key (RankObject rankObject);
	
	/**
	 * 比较两个对象谁排前面
	 * @param rankObject1
	 * @param rankObject2
	 * @return rankObject1在rankObject2之前 返回true
	 */
	protected abstract boolean gt (RankObject rankObject1,RankObject rankObject2);
	
	public void print (List<RankObject> list) {
		for (RankObject t : list) {
			System.out.println(t+"rankIndex:"+rankIndex.get(key(t)));
		}
		System.out.println("----------------");
	}
 }
