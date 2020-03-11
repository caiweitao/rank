package cn.caiweitao.test.rank;

/**
 * @author caiweitao
 * @Date 2020年3月11日
 * @Description 进行排行的对象
 */
public class TestRankObject{

	private int id;
	private int score;
	private long createTime;
	
	public TestRankObject(int id,int score){
		this.score = score;
		this.id = id;
		this.createTime = System.currentTimeMillis();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "TestRankObject [id=" + id + ", score=" + score + ", createTime=" + createTime + "]";
	}

}
