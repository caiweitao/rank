# rank-实时排行榜
--------
## 功能 ##

包括排行榜的基本


1. 检测新对象是否上榜——更新排行榜数据（check 方法）

2. 得到我的排名（getRank 方法）


3. 得到排行榜数据（getTopList 方法）

## 实现 ##
使用 List 存储排行榜数据，通过二分法查找插入位置

## 用法 ##
通过继承 AbstractRank 类，实现一个排行榜，排行对象数据更新时，调用 check()方法检查更新排行榜数据。


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