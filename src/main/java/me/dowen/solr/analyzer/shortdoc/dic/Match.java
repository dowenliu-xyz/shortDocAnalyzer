package me.dowen.solr.analyzer.shortdoc.dic;

/**
 * 匹配
 * @author liufl / 2014年5月9日
 */
public class Match implements Comparable<Match> {

	private MatchType type; // 匹配类型

	private int begin; // 起始偏移量
	private int end; // 结束偏移量

	/**
	 * 构造器
	 * @param type 匹配类型
	 * @param begin 起始偏移量
	 * @param end 结束偏移量
	 */
	public Match(MatchType type, int begin, int end) {
		this.type = type;
		this.begin = begin;
		this.end = end;
	}

	/**
	 * 取出匹配类型
	 * @return
	 */
	public MatchType getType() {
		return type;
	}

	/**
	 * 取出起始偏移量
	 * @return
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * 取出结束偏移量
	 * @return
	 */
	public int getEnd() {
		return end;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + begin;
		result = prime * result + end;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Match other = (Match) obj;
		if (begin != other.begin)
			return false;
		if (end != other.end)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	/**
	 * 匹配排序<br/>
	 * 起始偏移量靠前>结束偏移量靠前
	 */
	@Override
	public int compareTo(Match o) {
		int a = this.getBegin() - o.getBegin();
		if (a != 0)
			return a;
		a = this.getEnd() - o.getEnd();
		return a;
	}

}
