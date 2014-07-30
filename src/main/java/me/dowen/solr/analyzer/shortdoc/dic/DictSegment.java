package me.dowen.solr.analyzer.shortdoc.dic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 词典片段<br/>
 * 树状存储，方便快速查找。
 * 第一层片段包含一个字符。
 * 查找单词时，从第一层开始，逐层检索匹配，遇到标识token为true时即命中word。
 * 若token为true时，还有子层片段，表示存在更长word。
 * 
 * @author liufl / 2014年5月9日
 */
public class DictSegment {

	// 子片段树
	private Map<Character, DictSegment> childrenSegIndex = new HashMap<Character, DictSegment>(
			128, 0.95F);
	// 当前结点字符key值
	private Character keyChar = '\0'; // root结点为\0
	// 当前是否可取出word
	private boolean token = false;

	/**
	 * 构造器。构造一层片段
	 * @param keyChar 当前层片段字符
	 */
	public DictSegment(char keyChar) {
		this.keyChar = keyChar;
	}

	/**
	 * 当前是否可取出单word
	 * 
	 * @return
	 */
	public boolean isToken() {
		return token;
	}

	/**
	 * 结束当前单word录入
	 * 
	 * @param token
	 */
	public void endToken() {
		this.token = true;
	}

	/**
	 * 当前结点存储的char值
	 * 
	 * @return
	 */
	public Character getKeyChar() {
		return keyChar;
	}

	/**
	 * 是否有子片段
	 * 
	 * @return
	 */
	boolean hasChildSeg() {
		return this.childrenSegIndex.size() > 0;
	}

	/**
	 * 匹配字符串<br/>
	 * 只有root执行此方法才有意义，代码中未作此限制，但主分词器类调用时保证了这一点。
	 * 
	 * @param charArray 要匹配的字符串输入
	 * @param fineGrained 是否最细粒度切分。
	 * @return
	 */
	Set<Match> match(char[] charArray, boolean fineGrained) {
		Set<Match> matchs = new HashSet<Match>();
		for (int i = 0; i < charArray.length;) { // 对输入字符串，逐位作为word开头尝试匹配
			Matcher matcher = this.match(charArray, i, charArray.length - i,
					null, fineGrained);
			if (!fineGrained) { // 如果不是最细粒度切分
				if (matcher.getMatchs().isEmpty()) { // 没匹配上
					i++; // 下一位
				} else { // 有匹配结果
					for (Match m : matcher.getMatchs()) { // 对每个匹配结果
						if (m.getEnd() >= i) { // 匹配在当前位之后结束
							i = m.getEnd() + 1; // 在此匹配结束位之后位置继续匹配，即不匹配可能的重叠词
						}
					}
				}
			} else { // 最细粒度切分
				i++;
			}
			matchs.addAll(matcher.getMatchs());
		}
		return matchs;
	}

	/**
	 * 在输入字符串的指定区域进行匹配
	 * @param charArray 输入的字符串序列
	 * @param begin 区域起始偏移量
	 * @param length 区域长度
	 * @param matcher 匹配容器
	 * @param fineGrained 是否最细粒度切分
	 * @return
	 */
	private Matcher match(char[] charArray, int begin, int length,
			Matcher matcher, boolean fineGrained) {
		// 容器不存在，新建
		if (matcher == null) {
			matcher = new Matcher();
			matcher.setBegin(begin); // 记录起始偏移量。
		}
		// 非法区域不处理
		if (length <= 0 || (begin + length > charArray.length))
			return matcher;
		// 开始匹配
		DictSegment ds = null;
		char cc = charArray[begin]; // 开始字符
		cc = Character.toLowerCase(cc); // 转换小写
		ds = this.childrenSegIndex.get(cc); // 取出子字典片段
		if (ds != null) { // 存在子字典片段
			if (ds.isToken()) { // word结尾
				Match match = new Match(MatchType.DICTIONARY,
						matcher.getBegin(), begin);
				matcher.setLastMatch(match);
				if (fineGrained || !ds.hasChildSeg() || length == 1) {
					// 如果最细粒度切分或是没有更长word或输入匹配区域结束
					matcher.getMatchs().add(match);
					matcher.setLastMatch(null);
				}
			} else { // 不是word结尾
				if (length == 1) { // 但输入区域结束了，不能继续匹配
					if (matcher.getLastMatch() != null) { // 存在最近匹配成功match
						matcher.getMatchs().add(matcher.getLastMatch());
						matcher.setLastMatch(null);
					}
				}
			}
			if (length > 1) { // 从输入字符串的下一位置再匹配剩余字符串区域
				ds.match(charArray, begin + 1, length - 1, matcher, fineGrained);
			}
		} else { // 不存在子字典片段
			if (matcher.getLastMatch() != null) { // 有上次成功匹配match
				matcher.getMatchs().add(matcher.getLastMatch());
				matcher.setLastMatch(null);
			}
		}
		return matcher;
	}

	/**
	 * 填充字典片段
	 * 
	 * @param charArray 用作字典word的字符串
	 */
	void fillSegment(char[] charArray) {
		this.fillSegment(charArray, 0, charArray.length);
	}

	/**
	 * 为当前片段填充子片段,递归调用
	 * @param charArray word全长度字符串
	 * @param begin 起始偏移量。当前子片段字符在全字符数组中的索引下标值
	 * @param length 子片段长度/层数
	 */
	private void fillSegment(char[] charArray, int begin, int length) {
		char c = Character.toLowerCase(charArray[begin]);
		DictSegment ds = this.childrenSegIndex.get(c); // 当前子片段集中取出begin字符对应子片段，可能不存在
		if (ds == null) { // 不存在，新建并写入
			ds = new DictSegment(c);
			this.childrenSegIndex.put(c, ds);
		}
		if (length > 1) { // 存在子层片段。为子层片段填充子片段
			ds.fillSegment(charArray, begin + 1, length - 1);
		} else if (length == 1) { // 不存在子片段
			ds.endToken(); // word结束标记
		}
	}

	/**
	 * 深复制
	 * @return
	 */
	public DictSegment copy() {
		DictSegment copy = new DictSegment(this.getKeyChar());
		if (this.isToken()) {
			copy.endToken();
		}
		if (this.hasChildSeg()) {
			for (DictSegment childSeg : this.childrenSegIndex.values()) {
				copy.childrenSegIndex.put(childSeg.getKeyChar(), childSeg.copy());
			}
		}
		return copy;
	}

}
