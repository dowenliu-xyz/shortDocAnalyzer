package me.dowen.solr.analyzer.shortdoc.lucene;

import java.io.IOException;
import java.io.Reader;

import me.dowen.solr.analyzer.shortdoc.core.Segment;
import me.dowen.solr.analyzer.shortdoc.core.Segmenter;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * Lucene分词器类
 * @author liufl / 2014年7月22日
 *
 */
public class SDTokenizer extends Tokenizer {

	private String usageName; // 用例名
	private boolean fineGrained = true; // 是否最细粒度切分

	private Segmenter segmenter; // 主分词器类

	// 词元文本属性
	private final CharTermAttribute termAtt;
	// 词元位移属性
	private final OffsetAttribute offsetAtt;
	// 词元分类属性（该属性分类参考org.wltea.analyzer.core.Lexeme中的分类常量）
	private final TypeAttribute typeAtt;

	/**
	 * 构造器。因父类无无参构造器。
	 * @param factory
	 * @param input
	 */
	protected SDTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
		this.offsetAtt = addAttribute(OffsetAttribute.class);
		this.termAtt = addAttribute(CharTermAttribute.class);
		this.typeAtt = addAttribute(TypeAttribute.class);
	}

	/**
	 * 取出用例名
	 * @return
	 */
	public String getUsageName() {
		return usageName;
	}

	/**
	 * 设置用例名<br/>
	 * <strong>仅应在{@link SDTokenizerFactory#create(AttributeFactory, Reader)}方法中调用</strong>
	 * @param usageName
	 */
	public void setUsageName(String usageName) {
		this.usageName = usageName;
	}

	/**
	 * 是否最细粒度切分
	 * @return
	 */
	public boolean isFineGrained() {
		return fineGrained;
	}

	/**
	 * 设置是否最细粒度切分<br/>
	 * <strong>仅应在{@link SDTokenizerFactory#create(AttributeFactory, Reader)}方法中调用</strong>
	 * @param fineGrained
	 */
	public void setFineGrained(boolean fineGrained) {
		this.fineGrained = fineGrained;
	}

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		Segment seg = null;
		try {
			seg = this.segmenter.next(); // 取词元
		} catch (IllegalStateException e) { // 未分析拆词
			this.segmenter.analyze(); // 分析拆词
			seg = this.segmenter.next(); // 取词元
		}
		if (seg != null) { // 取出
			termAtt.append(seg.getValue()); // 字面值
			termAtt.setLength(seg.getLength()); // 字面值最大长度
			offsetAtt.setOffset(seg.getStart(), seg.getEnd()); // 词元在全文中的位置
			typeAtt.setType(seg.getType().toString()); // 词性。这里填入了词元类型
			return true;
		}
		// 未取出
		return false;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		this.segmenter.reset(input);
	}

	/**
	 * 生成主分词器对象
	 */
	public void initSegmenter() {
		this.segmenter = new Segmenter(this.input, this.usageName,
				this.fineGrained);
	}

}
