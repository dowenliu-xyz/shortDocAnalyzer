package me.dowen.solr.analyzer.shortdoc.lucene;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;

/**
 * Solr/Lucene分词器工厂类
 * @author liufl / 2014年5月14日
 */
public class SDTokenizerFactory extends TokenizerFactory implements
		ResourceLoaderAware {

	private Set<String> extDictFiles; // 扩展字典文件
	private String usageName; // 用例名
	private Set<char[]> extWords; // 扩展字典
	private boolean fineGrained; // 是否最细粒度切分

	/**
	 * 构造器
	 * @param args 参数
	 */
	public SDTokenizerFactory(Map<String, String> args) {
		super(args);
		// 字典文件
		String files = get(args, "extDicts");
		this.extDictFiles = new HashSet<String>();
		if (files != null) {
			this.extDictFiles.addAll(splitFileNames(files));
		}
		// 用例名
		this.usageName = get(args, "name");
		if (this.usageName == null || "".equals(this.usageName.trim())) {
			throw new IllegalArgumentException("SDTokenizer name is blank!");
		}
		this.usageName = this.usageName.trim();
		// 是否最细粒度切分。默认true
		this.fineGrained = getBoolean(args, "fineGrained", true);
		if (!args.isEmpty()) {
			throw new IllegalArgumentException("Unknown parameters: " + args);
		}
	}

	@Override
	public Tokenizer create(AttributeFactory factory, Reader input) {
		SDTokenizer tokenizer = new SDTokenizer(factory, input);
		tokenizer.setUsageName(this.usageName);
		tokenizer.setExtWords(this.extWords);
		tokenizer.setFineGrained(this.fineGrained);
		tokenizer.initSegmenter();
		return tokenizer;
	}

	@Override
	public void inform(ResourceLoader loader) throws IOException {
		// 加载扩展字典
		extWords = new HashSet<char[]>();
		for (String fileName : this.extDictFiles) {
			CharArraySet charArraySet = getWordSet(loader, fileName, true);
			for (Object charArray : charArraySet) {
				this.extWords.add((char[]) charArray);
			}
		}
	}

}
