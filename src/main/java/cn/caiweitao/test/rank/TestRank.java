package cn.caiweitao.test.rank;

import java.util.ArrayList;
import java.util.List;

import cn.caiweitao.rank.AbstractRank;

/**
 * 排行榜
 * @author caiweitao
 * @Date 2020年3月11日
 * @Description
 */
public class TestRank extends AbstractRank<Integer,TestRankObject> {
	
	public TestRank(int max, int top) {
		super(max, top);
	}
	
	public TestRank() {}

	/**
	 * 初始化排行榜数据，一般在程序启动时，查询数据库得到数据
	 */
	@Override
	protected List<TestRankObject> getInitData() {
		List<TestRankObject> list = new ArrayList<TestRankObject>();

		for (int i=1000;i>0;i-=2) {
			list.add(new TestRankObject(i,i));
		}
		return list;
	}

	@Override
	public Integer key(TestRankObject rankObject) {
		return rankObject.getId();
	}

	/**
	 * 每个排行榜的排行依据不同，根据情况实现
	 */
	@Override
	protected boolean gt(TestRankObject rankObject1, TestRankObject rankObject2) {
		// 假如根据score和createTme来判断排名
		if (rankObject1.getScore() > rankObject2.getScore()) {
			return true;
		} else if (rankObject1.getScore() < rankObject2.getScore()) {
			return false;
		} else {
			if (rankObject1.getCreateTime() < rankObject2.getCreateTime()) {
				return true;
			}
		}
		return false;
	}

}
