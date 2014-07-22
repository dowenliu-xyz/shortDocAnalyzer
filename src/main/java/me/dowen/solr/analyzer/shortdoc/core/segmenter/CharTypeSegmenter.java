package me.dowen.solr.analyzer.shortdoc.core.segmenter;

import me.dowen.solr.analyzer.shortdoc.core.AnalyzeContext;
import me.dowen.solr.analyzer.shortdoc.dic.MatchType;

/**
 * 单字类型标记
 * @author liufl / 2014年5月13日
 */
public class CharTypeSegmenter implements ISegmenter {

	@Override
	public boolean canAppend(SegmenterChain chain) {
		return true;
	}

	@Override
	public void analyze(AnalyzeContext ctx) {
		char[] charArray = ctx.getBuff();
		MatchType[] charTypes = ctx.getCharTypes(); // 引用，直接操作ctx内部数组
		// 标记每个字符的字符类型
		for (int i = 0; i < charArray.length; i++) {
			charTypes[i] = MatchType.charType(charArray[i]);
		}
	}

}
