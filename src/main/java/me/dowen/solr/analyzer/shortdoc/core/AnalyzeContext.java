package me.dowen.solr.analyzer.shortdoc.core;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.dowen.solr.analyzer.shortdoc.dic.Dictionary;
import me.dowen.solr.analyzer.shortdoc.dic.MatchType;

/**
 * 分析器上下文
 * @author liufl / 2014年5月13日
 */
public class AnalyzeContext {

	private final String name; // 上下文名称（字典实例名）
	private final Set<char[]> extWords; // 扩展字典
	private final boolean fineGrained; // 是否最细粒度切分

	// 去重用片段集合
	private Set<Segment> segmentSet = new HashSet<Segment>();
	// 片段分析结果列表，排序
	private List<Segment> segments = new LinkedList<Segment>();

	// 去重用分析过程片段集合
	private Set<Segment> tempSegmentSet = new HashSet<Segment>();
	private Map<Integer, Set<Segment>> segsOfStart = new HashMap<Integer, Set<Segment>>(); // 起始索引Map
	private Map<Integer, Set<Segment>> segsOfEnd = new HashMap<Integer, Set<Segment>>(); // 结束索引Map

	// 输入处理块缓存
	private char[] buff;
	// Char MatchType记录
	private MatchType[] charTypes;
	private boolean[] matchFlags; // 字典匹配标记

	// 片段分析结果列表的迭代器
	private Iterator<Segment> ite;

	/**
	 * 构造器
	 * @param name 上下文名称
	 * @param extWords 扩展字典
	 * @param fineGrained 是否最细粒度切分
	 */
	public AnalyzeContext(String name, Set<char[]> extWords, boolean fineGrained) {
		this.name = name;
		this.extWords = extWords;
		this.fineGrained = fineGrained;
		initDict();
	}

	/**
	 * 构造器。无扩展字典
	 * @param name 上下文名称
	 * @param fineGrained 是否最细粒度切分
	 */
	public AnalyzeContext(String name, boolean fineGrained) {
		this.name = name;
		this.extWords = null;
		this.fineGrained = fineGrained;
		initDict();
	}

	/**
	 * 初始化字典实例
	 */
	private void initDict() {
		try {
			Dictionary.getInstance(name);
		} catch (IllegalStateException e) {
			Dictionary.initInstance(name, extWords);
		}
	}

	/**
	 * 取出上下文名称
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 取出扩展字典
	 * @return
	 */
	public Set<char[]> getExtWords() {
		return extWords;
	}

	/**
	 * 是否最细粒度切分
	 * @return
	 */
	public boolean isFineGrained() {
		return fineGrained;
	}

	/**
	 * 获取当前缓存数据片段<br/>
	 * 可用长度不及buff长度时仅返回数据有效部分
	 * @return 
	 */
	public char[] getBuff() {
		return this.buff;
	}

	/**
	 * 获取当前缓存数据片段字符类型标记数组
	 * @return
	 */
	public MatchType[] getCharTypes() {
		return charTypes;
	}

	/**
	 * 字典匹配标记数组
	 * @return
	 */
	public boolean[] getMatchFlags() {
		return matchFlags;
	}

	/**
	 * 读入缓存
	 * @param in 文本输入源 长度应小于等于10000
	 * @return 若成功读入，返回<code>true</code>，否则文本长度超出，返回<code>false</code>
	 * @throws IOException
	 */
	public boolean buff(Reader in) throws IOException {
		CharBuffer cb = CharBuffer.allocate(10000);
		int position = in.read(cb);
		if (position == 10000) { // buffer读满
			if (in.read() != -1) { // 文本没有完全读取
				return false; // 超长，失败
			}
		} else if (position == -1) { // 没有读入
			return false;
		}
		// 组织数组们
		this.buff = new char[position];
		System.arraycopy(cb.array(), 0, this.buff, 0, position);
		this.charTypes = new MatchType[position];
		this.matchFlags = new boolean[position];
		return true;
	}

	/**
	 * 插入新分析结果<br/>分析结果片段将去重和排序
	 * @param segment
	 */
	public void addSegment(Segment segment) {
		if (segment == null)
			return; // 不处理null
		if (this.segmentSet.contains(segment)) {
			return; // 不处理已有词元
		}
		this.segmentSet.add(segment); // 去重
		this.segments.add(segment); // 插入
		// 记录起始/结束索引
		Set<Segment> startSegs = this.getSegsOfStart(segment.getStart());
		startSegs.add(segment);
		Set<Segment> endSegs = this.getSegsOfEnd(segment.getEnd());
		endSegs.add(segment);
		// 标记字典匹配
		for (int i = segment.getStart(); i < segment.getEnd(); i++) {
			if (i < this.matchFlags.length) {
				this.matchFlags[i] = true;
			}
		}
		Collections.sort(this.segments); // 排序
	}

	/**
	 * 取出临时词元集。主要用于存储量词单位词元，以后可能保存其它类型临时词元。
	 * @return
	 */
	public Set<Segment> getTempSegmentSet() {
		return tempSegmentSet;
	}

	/**
	 * 取出start起始的词元集
	 * @param start
	 * @return
	 */
	public Set<Segment> getSegsOfStart(int start) {
		Set<Segment> segs = null;
		segs = this.segsOfStart.get(start);
		if (segs == null) {
			segs = new HashSet<Segment>();
			this.segsOfStart.put(start, segs);
		}
		return segs;
	}

	/**
	 * 取出end结束的词元集
	 * @param end
	 * @return
	 */
	public Set<Segment> getSegsOfEnd(int end) {
		Set<Segment> segs = null;
		segs = this.segsOfEnd.get(end);
		if (segs == null) {
			segs = new HashSet<Segment>();
			this.segsOfEnd.put(end, segs);
		}
		return segs;
	}

	/**
	 * 迭代读取排序结果片段
	 * @return 若存在下一结果，返回结果片段，否则返回<code>null</code>
	 */
	public Segment nextSegment() {
		if (this.ite == null) {
			ite = this.segments.iterator();
		}
		if (ite.hasNext()) {
			return ite.next();
		} else {
			return null;
		}
	}

	/**
	 * 重置片段分析结果列表的迭代器
	 */
	public void resetIterator() {
		this.ite = this.segments.iterator();
	}

}
