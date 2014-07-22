package me.dowen.solr.analyzer.shortdoc.core.segmenter;

import me.dowen.solr.analyzer.shortdoc.core.AnalyzeContext;

/**
 * 分词器接口
 * @author liufl / 2014年5月13日
 */
public interface ISegmenter {

	/**
	 * 是否可追加到子分词器链尾部
	 * @param chain 子分词器链
	 * @return
	 */
	boolean canAppend(SegmenterChain chain);

	/**
	 * 分析操作<br/>
	 * 从上下文中读取缓存数据块分析，结果写回上下文
	 * @param ctx 分析器上下文
	 */
	void analyze(AnalyzeContext ctx);

}
