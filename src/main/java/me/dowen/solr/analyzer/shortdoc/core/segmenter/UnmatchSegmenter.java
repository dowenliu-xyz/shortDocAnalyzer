package me.dowen.solr.analyzer.shortdoc.core.segmenter;

import java.util.List;

import me.dowen.solr.analyzer.shortdoc.core.AnalyzeContext;
import me.dowen.solr.analyzer.shortdoc.core.Segment;
import me.dowen.solr.analyzer.shortdoc.dic.Match;
import me.dowen.solr.analyzer.shortdoc.dic.MatchType;
import me.dowen.solr.analyzer.shortdoc.util.ChineseNumUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字典未匹配部分处理<br/>
 * 此子分词器主要处理以下工作：<br/>
 * <ul>
 * <li>数字区段提取</li>
 * <li>中文数字区段提取</li>
 * <li>英文数字段提取</li>
 * </ul>
 * @author liufl / 2014年5月13日
 */
public class UnmatchSegmenter implements ISegmenter {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public boolean canAppend(SegmenterChain chain) {
		List<ISegmenter> _chain = chain.getChain();
		boolean hasCharType = false;
		boolean hasDictMatch = false;
		for (ISegmenter seg : _chain) {
			if (seg instanceof CharTypeSegmenter) {
				hasCharType = true;
			} else if (seg instanceof DictMatchSegmenter) {
				hasDictMatch = true;
			}
		}
		if (!hasCharType) {
			log.error("非词典匹配处理分词器需要字符类型标记处理分词器");
		}
		if (!hasDictMatch) {
			log.error("非词典匹配处理分词器需要词典匹配处理分词器");
		}
		return hasCharType && hasDictMatch;
	}

	@Override
	public void analyze(AnalyzeContext ctx) {
		MatchType[] charTypes = ctx.getCharTypes(); // 字符类型标识数组
		boolean[] matchFlags = ctx.getMatchFlags(); // 字典匹配段标识数组
		int idx = -1; // 词元起始位下标
		int cnumIdx = -1; // 中文数词起始位下标
		for (int i = 0; i < charTypes.length; i++) { // 遍历每个字符
			MatchType t = charTypes[i]; // 字符类型
			if (matchFlags[i]
					|| t == MatchType.BLANK
					|| t == MatchType.CHINESE
					|| t == MatchType.INTERPUNCTION
					|| t == MatchType.UNMATCH) { // 已字典匹配或是空白/普通汉字/标点/未识别字符
				if (idx != -1) { // 已标记起始位。未识别词元提取
					Segment seg = Segment.match(ctx.getBuff(), new Match(MatchType.UNMATCH, idx, i - 1), 0);
					ctx.addSegment(seg);
				}
				if (cnumIdx != -1) { // 已标记中文数词起始位，当前位已不是中文数字。中文数词词元提取
					Segment seg = Segment.match(ctx.getBuff(), new Match(MatchType.CHINESE_DECIMAL, cnumIdx, i - 1), 0);
					ctx.addSegment(seg);
					// 扩展对应数词词元
					Segment alaisSeg = new Segment();
					alaisSeg.setEnd(seg.getEnd());
					alaisSeg.setLength(seg.getLength());
					alaisSeg.setStart(seg.getStart());
					alaisSeg.setType(MatchType.DECIMAL);
					alaisSeg.setValue("" + ChineseNumUtil.chineseNumToInt(seg.getValue().toCharArray()));
					ctx.addSegment(alaisSeg);
				}
				if (t == MatchType.CHINESE || t == MatchType.UNMATCH) { // 中文或不识别字符
					if (!matchFlags[i]) { // 未被字典匹配。
						// 中文单字或未识别单字
						Segment seg = Segment.match(ctx.getBuff(), new Match(t, i, i), 0);
						ctx.addSegment(seg);
					}
				}
				// reset起始位索引
				idx = -1;
				cnumIdx = -1;
			} else { // 否则，是字典匹配/量词/数量/英文字母/数字/中文数字
				if (t != MatchType.CHINESE_DECIMAL) { // 不是中文数字
					if (idx == -1) { // 未标记词元起始位索引
						idx = i; // 标记
					}
					if (cnumIdx != -1) { // 已标记中文数词起始位，当前位已不是中文数字。中文数词词元提取
						Segment seg = Segment.match(ctx.getBuff(), new Match(MatchType.CHINESE_DECIMAL, cnumIdx, i - 1), 0);
						ctx.addSegment(seg);
						// 扩展对应数词词元
						Segment alaisSeg = new Segment();
						alaisSeg.setEnd(seg.getEnd());
						alaisSeg.setStart(seg.getStart());
						alaisSeg.setType(MatchType.DECIMAL);
						alaisSeg.setValue("" + ChineseNumUtil.chineseNumToInt(seg.getValue().toCharArray()));
						alaisSeg.setLength(alaisSeg.getValue().length());
						ctx.addSegment(alaisSeg);
						// reset中文数词起始位索引
						cnumIdx = -1;
					}
				} else if (t == MatchType.CHINESE_DECIMAL) { // 是中文数字
					if (idx != -1) { // 已标记起始位。未识别词元提取
						Segment seg = Segment.match(ctx.getBuff(), new Match(MatchType.UNMATCH, idx, i - 1), 0);
						ctx.addSegment(seg);
						// reset起始位索引
						idx = -1;
					}
					if (cnumIdx == -1) { // 未标记中文数词起始位索引
						cnumIdx = i; // 标记
					}
					// 记录，作为中文单字。考虑是否去除？
					Segment seg = Segment.match(ctx.getBuff(), new Match(MatchType.CHINESE, i, i), 0);
					ctx.addSegment(seg);
				}
			}
		}
		// 收尾词元
		int i = charTypes.length;
		if (idx != -1) { // 未识别
			Segment seg = Segment.match(ctx.getBuff(), new Match(MatchType.UNMATCH, idx, i - 1), 0);
			ctx.addSegment(seg);
			idx = -1;
		}
		if (cnumIdx != -1) { // 中文数词
			Segment seg = Segment.match(ctx.getBuff(), new Match(MatchType.CHINESE_DECIMAL, idx, i - 1), 0);
			ctx.addSegment(seg);
			Segment alaisSeg = new Segment();
			alaisSeg.setEnd(seg.getEnd());
			alaisSeg.setStart(seg.getStart());
			alaisSeg.setType(MatchType.DECIMAL);
			alaisSeg.setValue("" + ChineseNumUtil.chineseNumToInt(seg.getValue().toCharArray()));
			alaisSeg.setLength(alaisSeg.getValue().length());
			ctx.addSegment(alaisSeg);
			cnumIdx = -1;
		}
	}

}
