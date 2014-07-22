package me.dowen.solr.analyzer.shortdoc.core.segmenter;

import java.util.LinkedList;
import java.util.List;

import me.dowen.solr.analyzer.shortdoc.core.AnalyzeContext;

/**
 * 子分词器链
 * @author liufl / 2014年5月13日
 */
public class SegmenterChain {

	private List<ISegmenter> chain = new LinkedList<ISegmenter>();

	List<ISegmenter> getChain() {
		return chain;
	}

	/**
	 * 增加子分词器，如果当前链序允许<strong>错误的链序不会报错，但有日志输出</strong>
	 * @param segmenter
	 */
	public void append(ISegmenter segmenter) {
		if (segmenter.canAppend(this)) {
			this.chain.add(segmenter);
		}
	}

	/**
	 * 分析拆分词元。链序中的子分词器会被依次调用。
	 * @param ctx
	 */
	public void analyze(AnalyzeContext ctx) {
		for (ISegmenter segmenter : this.chain) {
			segmenter.analyze(ctx);
		}
	}

}
