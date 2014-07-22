package me.dowen.solr.analyzer.shortdoc.dic;

/**
 * 匹配类型
 * @author liufl / 2014年7月21日
 *
 */
public enum MatchType {

	/**
	 * 字典匹配
	 */
	DICTIONARY,
	/**
	 * 量词匹配
	 */
	UNITS,
	/**
	 * 数量
	 */
	QUANTITY,
	/**
	 * 标点，但不包括-_'
	 */
	INTERPUNCTION,
	/**
	 * 英文字母
	 */
	ENGLISH,
	/**
	 * 空白
	 */
	BLANK,
	/**
	 * 数字
	 */
	DECIMAL,
	/**
	 * 中文数字
	 */
	CHINESE_DECIMAL,
	/**
	 * 中文，不包括中文数字
	 */
	CHINESE,
	/**
	 * 未匹配、未识别的文字类型
	 */
	UNMATCH;

	private static final String CHINESE_DECIMALS = "一二两三四五六七八九十〇壹贰叁肆伍陆柒捌玖零拾百千万亿佰仟萬億";

	/**
	 * 在无词典状态下，字符类型匹配
	 * 
	 * @param input
	 * @return
	 */
	public static MatchType charType(char input) {
		if (input >= '0' && input <= '9') { // 数字
			return DECIMAL;
		} else if ((input >= 'a' && input <= 'z')
				|| (input >= 'A' && input <= 'Z')) { // 英文字符
			return ENGLISH;
		} else {
			String str = "" + input;
			if (str.matches("\\s+")) { // 空白字符
				return BLANK;
			} else if (str.matches("\\pP")) { // 标点字符
				if (input == '-' || input == '_' || input == '\'') // 短横线、下划线、单引号 XXX 没有考虑前后配对
					return ENGLISH; // 当作英文字符处理
				return INTERPUNCTION; // 其他标点
			} else {
				Character.UnicodeBlock charBlock = Character.UnicodeBlock
						.of(input);
				if (charBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS // 4E00-9FBF：CJK
																				// 统一表意符号
						|| charBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS // F900-FAFF：CJK
																							// 兼容象形文字
						|| charBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A // 3400-4DBF：CJK
																									// 统一表意符号扩展A
						|| charBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B // CJK统一表意符号扩展B
				) { // 中文
					if (CHINESE_DECIMALS.indexOf(input) != -1) { // 是中文数字
						return CHINESE_DECIMAL;
					} else { // 普通中文字符
						return CHINESE;
					}
				} else if (charBlock == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION // 3000-303F：CJK 符号和标点
						|| charBlock == Character.UnicodeBlock.GENERAL_PUNCTUATION // 2000-206F：常用标点
						|| charBlock == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS // FF00-FFEF：半角及全角形式
				) { // 中文标点
					return INTERPUNCTION;
				} else { // 未识别
					return UNMATCH;
				}
			}
		}
	}

}
