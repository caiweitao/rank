package cn.caiweitao.test.rank;

public class TestRankMain {

	public static void main(String[] args) {
		TestRank tr = new TestRank(100,100);
		tr.init();
		
		System.out.println("最开始排名："+tr.getRank(123));
		
		//更新数据
		TestRankObject tro = new TestRankObject(123, 988);
		tr.check(tro);
		System.out.println("更新数据后排名："+tr.getRank(tr.key(tro)));
		
		System.out.println("排行榜数据：");
		tr.print(tr.getTopList());
	}
}
