package me.dowen.solr.analyzer.shortdoc.core;

import java.util.Arrays;

import me.dowen.solr.analyzer.shortdoc.dic.Match;
import me.dowen.solr.analyzer.shortdoc.dic.MatchType;

/**
 * 分词片段
 * @author liufl / 2014年5月13日
 */
public class Segment implements Comparable<Segment> {

	private String value; // 字面值
	private int length; // 长度
	private int start; // 起始位索引
	private int end; // 结束位索引
	private MatchType type; // 词元类型

	/**
	 * 取出字面值
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 设置字面值
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 取出长度。可能不等于 (end - start + 1)。此值与value输出长度直接相关，决定value输出的最大长度
	 * @return
	 */
	public int getLength() {
		return length;
	}

	/**
	 * 设置长度。可能不等于 (end - start + 1)。此值与value输出长度直接相关，决定value输出的最大长度
	 * @param length
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * 取出起始位索引
	 * @return
	 */
	public int getStart() {
		return start;
	}

	/**
	 * 设置起始位索引
	 * @param start
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * 取出结束位索引
	 * @return
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * 设置结束位索引
	 * @param end
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * 取出词元类型
	 * @return
	 */
	public MatchType getType() {
		return type;
	}

	/**
	 * 设置词元类型
	 * @param type
	 */
	public void setType(MatchType type) {
		this.type = type;
	}

	/**
	 * 由数据片段、匹配、偏移量构建分词片段
	 * @param charArray 数据片段。全量数据中的一段
	 * @param match 匹配
	 * @param offset 数据片段偏移量。片段开头位置相对全部数据开头的偏移量
	 * @return
	 */
	public static Segment match(char[] charArray, Match match, int offset) {
		Segment segment = new Segment();
		segment.setLength(match.getEnd() - match.getBegin() + 1);
		segment.setStart(offset + match.getBegin());
		segment.setEnd(offset + match.getEnd());
		try {
		segment.setValue(new String(Arrays.copyOfRange(charArray,
				match.getBegin(), match.getEnd() + 1)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		segment.setType(match.getType());
		return segment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + length;
		result = prime * result + start;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Segment other = (Segment) obj;
		if (end != other.end)
			return false;
		if (length != other.length)
			return false;
		if (start != other.start)
			return false;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/**
	 * 分词片段排序<br/>
	 * 起始偏移量靠前>结束偏移量靠前>结果值排序结果
	 */
	@Override
	public int compareTo(Segment o) {
		int t = this.start - o.start;
		if (t == 0) {
			t = this.end - o.end;
		}
		if (t == 0) {
			t = this.value.compareTo(o.value);
		}
		return t;
	}

}
