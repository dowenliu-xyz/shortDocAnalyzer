package me.dowen.solr.analyzer.shortdoc.core;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import me.dowen.solr.analyzer.shortdoc.core.segmenter.CharTypeSegmenter;
import me.dowen.solr.analyzer.shortdoc.core.segmenter.DictMatchSegmenter;
import me.dowen.solr.analyzer.shortdoc.core.segmenter.QuantitySegmenter;
import me.dowen.solr.analyzer.shortdoc.core.segmenter.SegmenterChain;
import me.dowen.solr.analyzer.shortdoc.core.segmenter.UnitsMatchSegmenter;
import me.dowen.solr.analyzer.shortdoc.core.segmenter.UnmatchSegmenter;

/**
 * 分词器主类
 * 
 * @author liufl / 2014年5月13日
 */
public class Segmenter {

	private Reader input; // 输入源
	private AnalyzeContext ctx; // 上下文
	private SegmenterChain segmenterChain; // 子分词器链
	private boolean anaylyzed = false; // 分析完成标识

	/**
	 * 构造器
	 * @param input 输入源
	 * @param usage 用例名（上下文名称、字典实例名）
	 * @param extWords 扩展字典
	 * @param fineGrained 是否最细粒度切分
	 */
	public Segmenter(Reader input, String usage, Set<char[]> extWords, boolean fineGrained) {
		this.input = input;
		this.ctx = new AnalyzeContext(usage, extWords, fineGrained);
		this.segmenterChain = new SegmenterChain();
		this.segmenterChain.append(new CharTypeSegmenter());
		this.segmenterChain.append(new DictMatchSegmenter());
		this.segmenterChain.append(new UnmatchSegmenter());
		this.segmenterChain.append(new UnitsMatchSegmenter());
		this.segmenterChain.append(new QuantitySegmenter());
	}

	/**
	 * 执行分析
	 * 
	 * @throws IOException
	 */
	public synchronized void analyze() throws IOException {
		if (this.anaylyzed)
			return; // 不做第二次分析
		if (this.ctx.buff(input)) { // 读入
			this.segmenterChain.analyze(ctx); // 分析
		}
		this.anaylyzed = true;
	}

	/**
	 * 取下一个分词结果
	 * 
	 * @return
	 */
	public Segment next() {
		if (!this.anaylyzed) {
			throw new IllegalStateException("未进行分词器分析！");
		}
		return this.ctx.nextSegment();
	}

	/**
	 * 重置分词结果迭代器
	 */
	public void resetIterator() {
		this.ctx.resetIterator();
	}

	/**
	 * 重置分词器
	 * 
	 * @param input
	 */
	public synchronized void reset(Reader input) {
		this.input = input;
		this.anaylyzed = false;
		this.ctx = new AnalyzeContext(this.ctx.getName(),
				this.ctx.getExtWords(), this.ctx.isFineGrained());
	}

}
