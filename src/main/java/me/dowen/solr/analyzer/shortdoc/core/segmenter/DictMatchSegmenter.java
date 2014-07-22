package me.dowen.solr.analyzer.shortdoc.core.segmenter;

import java.util.Set;

import me.dowen.solr.analyzer.shortdoc.core.AnalyzeContext;
import me.dowen.solr.analyzer.shortdoc.core.Segment;
import me.dowen.solr.analyzer.shortdoc.dic.Dictionary;
import me.dowen.solr.analyzer.shortdoc.dic.Match;

/**
 * 词典匹配
 * @author liufl / 2014年5月13日
 */
public class DictMatchSegmenter implements ISegmenter {

	@Override
	public boolean canAppend(SegmenterChain chain) {
		return true;
	}

	@Override
	public void analyze(AnalyzeContext ctx) {
		char[] charArray = ctx.getBuff();
		Dictionary dict = Dictionary.getInstance(ctx.getName()); // 字典实例
		Set<Match> matchs = dict.match(charArray, ctx.isFineGrained()); // 匹配
		// 转换成词元，并记录
		for (Match match : matchs) {
			Segment segment = Segment.match(charArray, match, 0);
			ctx.addSegment(segment);
		}
	}

}
