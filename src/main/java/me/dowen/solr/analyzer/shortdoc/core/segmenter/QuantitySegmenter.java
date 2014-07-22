package me.dowen.solr.analyzer.shortdoc.core.segmenter;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import me.dowen.solr.analyzer.shortdoc.core.AnalyzeContext;
import me.dowen.solr.analyzer.shortdoc.core.Segment;
import me.dowen.solr.analyzer.shortdoc.dic.MatchType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数字量词组合处理
 * @author liufl / 2014年5月13日
 */
public class QuantitySegmenter implements ISegmenter {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public boolean canAppend(SegmenterChain chain) {
		List<ISegmenter> _chain = chain.getChain();
		boolean hasCharType = false;
		boolean hasUnitsMatch = false;
		boolean hasUnmatch = false;
		for (ISegmenter seg : _chain) {
			if (seg instanceof CharTypeSegmenter) {
				hasCharType = true;
			} else if (seg instanceof UnitsMatchSegmenter) {
				hasUnitsMatch = true;
			} else if (seg instanceof UnmatchSegmenter) {
				hasUnmatch = true;
			}
		}
		if (!hasCharType) {
			log.error("数量词组合处理分词器需要字符类型标记处理分词器");
		}
		if (!hasUnitsMatch) {
			log.error("数量词组合处理分词器需要量词匹配处理分词器");
		}
		if (!hasUnmatch) {
			log.error("数量词组合处理分词器需要词典未匹配部分处理分词器");
		}
		return hasCharType && hasUnitsMatch && hasUnmatch;
	}

	@Override
	public void analyze(AnalyzeContext ctx) {
		// 取出量词分词
		List<Segment> ums = new LinkedList<Segment>();
		Set<Segment> tmpSegs = ctx.getTempSegmentSet();
		for (Segment tmpSeg : tmpSegs) {
			if (tmpSeg.getType() == MatchType.UNITS) {
				ums.add(tmpSeg);
			}
		}
		// 遍历量词，查找是否有前缀数词一起组成数量词
		for (Segment um : ums) {
			int preIdx = um.getStart() - 1; // 前缀数词（如果有）的结束位置
			if (preIdx > -1) { // 当前量词不在开头位置
				Set<Segment> segsOfEnd = ctx.getSegsOfEnd(preIdx); // 所有在preIdx结尾的词元
				for (Segment seg : segsOfEnd) { // 遍历
					if (seg.getType() == MatchType.DECIMAL
							|| seg.getType() == MatchType.CHINESE_DECIMAL) { // 找出数词
						// 组成数量词
						Segment us = new Segment();
						us.setEnd(um.getEnd());
						us.setLength(seg.getLength() + um.getLength());
						us.setStart(seg.getStart());
						us.setType(MatchType.QUANTITY);
						us.setValue(seg.getValue() + um.getValue());
						ctx.addSegment(us);
					}
				}
			}
		}
	}

}
