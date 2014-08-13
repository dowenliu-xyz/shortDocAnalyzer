package me.dowen.solr.analyzer.shortdoc.lucene;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.dowen.solr.analyzer.shortdoc.dic.Dictionary;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Solr/Lucene分词器工厂类
 * @author liufl / 2014年5月14日
 */
public class SDTokenizerFactory extends TokenizerFactory implements
		ResourceLoaderAware {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Set<String> extDictFiles; // 扩展字典文件
	private String usageName; // 用例名
	private Set<char[]> extWords; // 扩展字典
	private boolean fineGrained; // 是否最细粒度切分
	private boolean extDict = false; // 是否加载扩展字典，此字段非配置参数字段
	private int reloadInteval; // 字典重新加载时间间隔，单位：分钟

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
			this.extDict = true;
			this.extDictFiles.addAll(splitFileNames(files));
			this.reloadInteval = getInt(args, "reloadInteval", 15); // 默认15分钟自动重新加载
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
		tokenizer.setFineGrained(this.fineGrained);
		tokenizer.initSegmenter();
		return tokenizer;
	}

	@Override
	public void inform(final ResourceLoader loader) throws IOException {
		// 加载扩展字典
		if (this.extDict) {
			extWords = new HashSet<char[]>();
			for (String fileName : this.extDictFiles) {
				CharArraySet charArraySet = getWordSet(loader, fileName, true);
				for (Object charArray : charArraySet) {
					this.extWords.add((char[]) charArray);
				}
			}
			synchronized (Dictionary.class) {
				try {
					Dictionary dictionary = Dictionary.getInstance(this.usageName);
					dictionary.addCharArrayWords(this.extWords);
				} catch (IllegalStateException e) {
					Dictionary.initInstance(this.usageName, this.extWords);
				}
			}
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(1000 * 60 * reloadInteval);
						} catch (InterruptedException e) {
						}
						Set<char[]> _extWords = new HashSet<char[]>();
						boolean reload = true;
						for (String fileName : extDictFiles) {
							CharArraySet charArraySet;
							try {
								charArraySet = getWordSet(loader, fileName, true);
								for (Object charArray : charArraySet) {
									_extWords.add((char[]) charArray);
								}
							} catch (IOException e) {
								log.warn("重新加载字典异常", e);
								reload = false;
							}
						}
						if (reload) {
							synchronized (Dictionary.class) {
								Dictionary.initInstance(usageName, _extWords);
							}
						}
					}
				}
			});
			t.setDaemon(true);
			t.start();
		}
	}

}
