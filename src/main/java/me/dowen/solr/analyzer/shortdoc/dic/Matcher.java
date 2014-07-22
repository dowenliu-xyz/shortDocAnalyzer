package me.dowen.solr.analyzer.shortdoc.dic;

import java.util.ArrayList;
import java.util.List;

/**
 * 匹配容器
 * @author liufl / 2014年5月9日
 */
public class Matcher {

	private List<Match> matchs = new ArrayList<Match>(); // 有效匹配列表

	private int begin; // 当前容器匹配起始位偏移量
	private Match lastMatch; // 最后成功匹配（非有效匹配）

	/**
	 * 取出有效匹配列表
	 * @return
	 */
	public List<Match> getMatchs() {
		return matchs;
	}

	/**
	 * 取出当前容器匹配起始位偏移量
	 * @return
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * 设置当前容器匹配起始位偏移量
	 * @param begin
	 */
	public void setBegin(int begin) {
		this.begin = begin;
	}

	/**
	 * 取出最后成功匹配（非有效匹配）
	 * @return
	 */
	public Match getLastMatch() {
		return lastMatch;
	}

	/**
	 * 设置最后成功匹配（非有效匹配）
	 * @param lastMatch
	 */
	public void setLastMatch(Match lastMatch) {
		this.lastMatch = lastMatch;
	}

}
