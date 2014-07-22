package me.dowen.solr.analyzer.shortdoc.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 中文数词转数字工具
 * @author liufl / 2014年5月13日
 */
public abstract class ChineseNumUtil {

	// 数
	private static final Map<Character, Integer> CNUM_MAP = new HashMap<Character, Integer>();
	// 位
	private static final Map<Character, Integer> CNUM_UNIT_MAP = new HashMap<Character, Integer>();

	static {
		CNUM_MAP.put('〇', 0);
		CNUM_MAP.put('一', 1);
		CNUM_MAP.put('二', 2);
		CNUM_MAP.put('两', 2);
		CNUM_MAP.put('三', 3);
		CNUM_MAP.put('四', 4);
		CNUM_MAP.put('五', 5);
		CNUM_MAP.put('六', 6);
		CNUM_MAP.put('七', 7);
		CNUM_MAP.put('八', 8);
		CNUM_MAP.put('九', 9);
		CNUM_UNIT_MAP.put('十', 10);
		CNUM_UNIT_MAP.put('百', 100);
		CNUM_UNIT_MAP.put('千', 1000);
		CNUM_UNIT_MAP.put('万', 10000);
		CNUM_UNIT_MAP.put('亿', 100000000);
		CNUM_MAP.put('零', 0);
		CNUM_MAP.put('壹', 1);
		CNUM_MAP.put('贰', 2);
		CNUM_MAP.put('叁', 3);
		CNUM_MAP.put('肆', 4);
		CNUM_MAP.put('伍', 5);
		CNUM_MAP.put('陆', 6);
		CNUM_MAP.put('柒', 7);
		CNUM_MAP.put('捌', 8);
		CNUM_MAP.put('玖', 9);
		CNUM_UNIT_MAP.put('拾', 10);
		CNUM_UNIT_MAP.put('佰', 100);
		CNUM_UNIT_MAP.put('仟', 1000);
		CNUM_UNIT_MAP.put('萬', 10000);
		CNUM_UNIT_MAP.put('億', 100000000);
	}

	/**
	 * 将中文数字转换为int<br/>
	 * <strong>仅在输入是完全的合法的中文数字时保证转换结果</strong>
	 * @param charArray
	 * @return
	 */
	public static int chineseNumToInt(char[] charArray) {
		int result = 0; // 返回结果变量
		boolean num = false;  // 前一字符是数而不是位标记
		for (int i = 0; i < charArray.length; i++) { // 遍历每个字符
			char c = charArray[i];
			if (CNUM_MAP.containsKey(c)) { // 是中文数
				if (num) { // 前一字符也是数，进位
					result *= 10;
				}
				result += CNUM_MAP.get(c); // 取和
				if (CNUM_MAP.get(c) != 0) {
					// 非“零”、“〇”时，标记是数字。因为位数后出现“零”、“〇”时不一定进多少位
					num = true;
				}
			} else if (CNUM_UNIT_MAP.containsKey(c)) { // 是数位
				if (c == '十' && i == 0) { // “十”特别处理：若此时表示数字10而不是数位“十”时
					result += 1;
				}
				result *= CNUM_UNIT_MAP.get(c); // 原数按数位对应阶数进位
				num = false; // 取消数字标记
			} else { // 非中文数字字符，抛出异常
				throw new NumberFormatException(new String(charArray));
			}
		}
		return result;
	}

}
