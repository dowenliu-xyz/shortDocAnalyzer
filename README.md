shortDocAnalyzer
================

一个专门用于短文档（如电商商品信息）的中文分词器，提供Solr/Lucene集成接口

最初写这个分词器是一个错误导致的。去年公司网站改版，搜索功能是由我负责的，然后新新版网站上线第一周就被同事发现，搜索结果不太对劲，具体到现象就是搜索“爱世克私”这个品牌，不仅返回了这个牌子的商品，还返回了耐克＆一堆乱七八糟的其他品牌商品！这怎么可以呢？第一反应是：忘记加保护词了，品牌词被分解了。加上，居然没效果！又想了一下，新版升级了Solr版本（3.6->4.7），原来用的IK分词也得升级，是不是IK分词的问题？给IK分词加字典吧！呃，麻烦了，我Solr集群节点太多了，每个结点都要改同样的字典，还要挨个重启，晕！要是能字典能自动分发就好了（这个分词器实现了，配合solrconfig.xml就可以，不过能不能自动重新加载？好像不一定的样子）！写完这分词器，部署应用不到一个月，忽然反应过来了，我没必要写这个啊，当时好像只是忘了在schema.xml里写:&lt;solrQueryParser defaultOperator="AND"/>！惭愧啊！不过既然写了，也能正常用了，这里发一下，欢迎大大们检查、使用、提Bug!

这个分词器我本来想改下IK分词的Solr/Lucene AnalyzerFactory来用的，但是看了IK的原代码，扩展字典要在app的lib/下才行，为了能让字典在集群中分发只能从字典加载开始重写了。  
IK在分词拆词上是流式读入的，所以可以用到长文档输入上，实现上过于抽象（我得赶工，没细研究），这里使用了缓冲区全量载入的方式读入文档。缓冲区长度只设了1000（char数组），需要改大的话可以在me.dowen.solr.analyzer.shortdoc.core.AnalyzeContext类的buff(Reader)方法处修改。好处是不用关心流处理问题；当然缺点就是牺牲了通用性……（以后有空会改吧）。这样一改了之后，整个分词器的构造和IK除字典的存储/匹配方案上基本全改了（会不会有还没发现的Bug啊，好担心！）

字典使用了多例的方案替换了IK的单例，不同Core、不同FieldType之间可以使用不同的扩展字典了。

Schema.xml中FieldType声明也可以使用AnalyzerFactory了，当然像WordDelimiterFilterFactory、StopFilterFactory、LowerCaseFilterFactory也可以一起用了。搞不明白IK为什么不为4.x版本提供3.x风格的注入方式。呃，这里打下广告，这个分词器可以和本人别外开发的两个分词过滤器一起用：[拼音注解扩展过滤器](https://github.com/DowenLiu126/pinyinTokenFilter)、[（语义）范围扩展分词过滤器](https://github.com/DowenLiu126/scopeExpandTokenFilter)

提供一下我自己使用的schema.xml声明片段：

		<fieldType name="text_sd" class="solr.TextField">
			<analyzer type="index">
				<tokenizer class="me.dowen.solr.analyzer.shortdoc.lucene.SDTokenizerFactory" name="product" extDicts="brand.dic,category.dic" fineGrained="true"/>
				<filter class="me.dowen.solr.analyzers.ScopeExpandTokenFilterFactory" expands="ce.txt"/>
				<filter class="solr.WordDelimiterFilterFactory"
					splitOnCaseChange="1"
					splitOnNumerics="1"
					stemEnglishPossessive="1"
					generateWordParts="1"
					generateNumberParts="1"
					catenateWords="1"
					catenateNumbers="1"
					catenateAll="1"
					preserveOriginal="1"
					protected="protwords.txt"/>
				<filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt"/>
				<filter class="solr.LowerCaseFilterFactory"/>
				<filter class="solr.RemoveDuplicatesTokenFilterFactory"/>
				<filter class="me.dowen.solr.analyzers.PinyinTransformTokenFilterFactory"
					firstChar="true" minTermLenght="2"/>
				<filter class="me.dowen.solr.analyzers.PinyinTransformTokenFilterFactory"
					firstChar="false" minTermLenght="2"/>
			</analyzer>
			<analyzer type="query">
				<tokenizer class="me.dowen.solr.analyzer.shortdoc.lucene.SDTokenizerFactory" name="product" extDicts="brand.dic,category.dic" fineGrained="false"/>
				<filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt"
					ignoreCase="true" expand="true" />
				<filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt"/>
				<filter class="solr.LowerCaseFilterFactory"/>
				<filter class="solr.RemoveDuplicatesTokenFilterFactory"/>
			</analyzer>
		</fieldType>

SDTokenizerFactory需要三个参数：

1. name 实例名，也就是字典实例名。不要留空，代码里做了限制
2. extDicts 扩展字典文件，可以多个文件，用英文逗号隔开。可以留空（没有扩展字典）
3. fineGrained 是否最细粒度切分。默认true