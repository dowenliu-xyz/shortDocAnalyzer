package me.dowen.solr.analyzer.shortdoc.dic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 词典， 多例
 * 
 * @author liufl / 2014年5月9日
 */
public class Dictionary {

	private static Logger log = LoggerFactory.getLogger(Dictionary.class);

	// 主词典
	private static final DictSegment mainDict;
	// 主量词词典
	private static final DictSegment mainUnitsDict;

	static {
		mainDict = loadMainDict();
		mainUnitsDict = loadMainUnitsDict();
	}

	// 用例字典库
	private static final Map<String, Dictionary> dictPool = new HashMap<String, Dictionary>();
	// 主词典文件
	private static final String MAIN_DICT = "me/dowen/analyzer/shortdoc/dic/main.ddic";
	// 量词词典文件
	private static final String MAIN_UNITS_DICT = "me/dowen/analyzer/shortdoc/dic/units.ddic";

	/**
	 * 加载主词典
	 * 
	 * @return
	 */
	private static DictSegment loadMainDict() {
		DictSegment ds = new DictSegment('\0');
		InputStream is = Dictionary.class.getClassLoader().getResourceAsStream(
				MAIN_DICT);
		if (is == null) {
			throw new RuntimeException("Main Dictionary not found!!!");
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"UTF-8"), 512);
			String theWord = null;
			do {
				theWord = br.readLine();
				if (theWord != null && !"".equals(theWord.trim())) {
					ds.fillSegment(theWord.trim().toLowerCase()
							.toCharArray());
				}
			} while (theWord != null);
		} catch (IOException ioe) {
			log.error("Main Dictionary loading exception.", ioe);
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {
				log.warn("Main Dictionary file closing exception", e);
			}
		}
		return ds;
	}

	/**
	 * 加载量词词典
	 * 
	 * @return
	 */
	private static DictSegment loadMainUnitsDict() {
		DictSegment ds = new DictSegment('\0');
		InputStream is = Dictionary.class.getClassLoader().getResourceAsStream(
				MAIN_UNITS_DICT);
		if (is == null) {
			throw new RuntimeException("Main Units Dictionary not found!!!");
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"UTF-8"), 512);
			String theWord = null;
			do {
				theWord = br.readLine();
				if (theWord != null && !"".equals(theWord.trim())) {
					ds.fillSegment(theWord.trim().toLowerCase()
							.toCharArray());
				}
			} while (theWord != null);

		} catch (IOException ioe) {
			log.error("Main Units Dictionary loading exception.", ioe);
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {
				log.warn("Main Units Dictionary file closing exception", e);
			}
		}
		return ds;
	}

	/**
	 * 获取所有用例词典库
	 * 
	 * @return
	 */
	public static Set<String> getDictUsage() {
		return dictPool.keySet();
	}

	// 扩展词典
	private DictSegment extDict;

	/**
	 * 私有构造器
	 * 
	 * @param extWords
	 *            扩展词库
	 */
	private Dictionary(Set<char[]> extWords) {
		this.extDict = new DictSegment('\0');
		if (extWords != null) {
			for (char[] extWord : extWords) {
				this.extDict.fillSegment(extWord);
			}
		}
	}

	/**
	 * 初始化词典用例
	 * 
	 * @param usage
	 *            用例名
	 * @param extWords
	 *            扩展词库
	 * @return
	 */
	public static Dictionary initInstance(String usage, Set<char[]> extWords) {
		Dictionary dict = new Dictionary(extWords);
		if (dictPool.containsKey(usage)) {
			log.warn("将覆盖已存在的词典用例：" + usage);
		}
		dictPool.put(usage, dict);
		return dict;
	}

	/**
	 * 获取词典用例
	 * 
	 * @param usage
	 *            用例名
	 * @return 词典实例 如果存在的话
	 * @throws IllegalStateException
	 *             用例词典未初始化
	 */
	public static Dictionary getInstance(String usage) {
		Dictionary dict = dictPool.get(usage);
		if (dict == null) {
			throw new IllegalStateException("词典用例" + usage + "还没有初始化！");
		}
		return dict;
	}

	/**
	 * 增加词库
	 * 
	 * @param word
	 */
	public void addWord(String word) {
		if (word != null && !"".equals(word.trim())) {
			this.addWord(word.trim().toCharArray());
		}
	}

	/**
	 * 增加词库
	 * 
	 * @param word
	 */
	public void addWord(char[] word) {
		if (word != null && !"".equals(new String(word).trim())) {
			this.extDict.fillSegment(new String(word).trim().toCharArray());
		}
	}

	/**
	 * 批量增加词库
	 * 
	 * @param words
	 */
	public void addStringWords(Collection<String> words) {
		if (words != null) {
			for (String word : words) {
				this.addWord(word);
			}
		}
	}

	/**
	 * 批量增加词库
	 * 
	 * @param words
	 */
	public void addCharArrayWords(Collection<char[]> words) {
		if (words != null) {
			for (char[] word : words) {
				this.addWord(word);
			}
		}
	}

	/**
	 * 匹配词典
	 * 
	 * @param charArray
	 * @return
	 */
	public Set<Match> match(char[] charArray, boolean fineGrained) {
		Set<Match> matchs = new HashSet<Match>();
		if (charArray != null) {
			matchs.addAll(mainDict.match(charArray, fineGrained));
			matchs.addAll(this.extDict.match(charArray, fineGrained));
		}
		return matchs;
	}

	/**
	 * 匹配量词词典
	 * 
	 * @param charArray
	 * @return
	 */
	public Set<Match> matchUnits(char[] charArray) {
		Set<Match> matchs = new HashSet<Match>();
		if (charArray != null) {
			Set<Match> umatchs = mainUnitsDict.match(charArray, true);
			for (Match um : umatchs) {
				matchs.add(new Match(MatchType.UNITS, um.getBegin(), um
						.getEnd()));
			}
		}
		return matchs;
	}

}
