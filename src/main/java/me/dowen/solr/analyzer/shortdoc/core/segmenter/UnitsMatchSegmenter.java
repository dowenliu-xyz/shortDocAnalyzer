package me.dowen.solr.analyzer.shortdoc.core.segmenter;

import java.util.Set;

import me.dowen.solr.analyzer.shortdoc.core.AnalyzeContext;
import me.dowen.solr.analyzer.shortdoc.core.Segment;
import me.dowen.solr.analyzer.shortdoc.dic.Dictionary;
import me.dowen.solr.analyzer.shortdoc.dic.Match;

/**
 * 量词匹配
 * @author liufl / 2014年5月13日
 */
public class UnitsMatchSegmenter implements ISegmenter {

	@Override
	public boolean canAppend(SegmenterChain chain) {
		return true;
	}

	@Override
	public void analyze(AnalyzeContext ctx) {
		char[] charArray = ctx.getBuff();
		Dictionary dict = Dictionary.getInstance(ctx.getName()); // 字典实例
		Set<Match> matchs = dict.matchUnits(charArray); // 匹配量词
		for (Match match : matchs) {
			// 转换成词原
			Segment segment = Segment.match(charArray, match, 0);
			ctx.getTempSegmentSet().add(segment); // 临时暂存，不能输出。单纯的量词无意义
		}
	}

}
