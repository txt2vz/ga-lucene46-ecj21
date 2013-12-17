package query

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lucene.IndexInfoStaticG;
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query
import org.apache.lucene.search.spans.SpanFirstQuery
import org.apache.lucene.search.spans.SpanTermQuery

class QueryReadable {

	public static String getQueryMinimal(Query query) {
		final String queryWithoutComma = query.toString(
				IndexInfoStaticG.FIELD_CONTENTS).replaceAll(", ", "#~");
		//query is spanFirst(contents:angina, 139) spanFirst(contents:aortic, 84) spanFirst(contents:aortic, 24) spanFirst(contents:angioplasty, 281) spanFirst(contents:antihypertensive, 135)
			
		boolean spanF = queryWithoutComma.contains("spanFirst");

		if (spanF) {

			final String spanFirstQueryMinimal = queryWithoutComma.replaceAll(
					"spanFirst", "");

			String s = spanFirstQueryMinimal.replaceAll("\\(", "");

			s = s.replaceAll("\\)", "#~");

			println "s is $s"
			
			List l = s.tokenize("#~")

			println "l is $l"
						String[] al = s.split("#~");			
	
			def spanFirstMap = [:]
			
			
		//	words.each { word ->
		//		wordFrequency[word] = wordFrequency.get(word,0) + 1 //#1
		//		}
				

			for (int x = 0; x < l.size; x = x + 2) {
				
				if (al[x+1]==null) continue;

				if (spanFirstMap.containsKey((l[x]))) 
				{
					def word = al[x]
					println " words is     88888888888888888888888   $word"
					final int end = spanFirstMap.get(word);
					spanFirstMap.put({word}, Math.max(end, al[x +1]))
				} else{

					println "alxtrim is " + al[x].trim()

					spanFirstMap.put((al[x].trim().toString()),   Integer.parseInt(al[x +1]).intValue());
				}
			}
			// }

			spanFirstMap.each{ key, value -> println "zzzzzzzzzzzzzzzz   ${key} == ${value}" };

			spanFirstMap = spanFirstMap.sort { it.value}

			String sr = "";
			for (String word : spanFirstMap.keySet()) {
				sr = sr + "(" + word + " " + spanFirstMap.get(word) + ")";
			}

			return sr;
		} else
			return queryWithoutComma;
	}


	static main(args) {
		BooleanQuery query = new BooleanQuery(true);
		SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexInfoStaticG.FIELD_CONTENTS, "angina")), 139);
		query.add(sfq, BooleanClause.Occur.SHOULD);

		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexInfoStaticG.FIELD_CONTENTS, "aortic")), 84);
		query.add(sfq, BooleanClause.Occur.SHOULD);

		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexInfoStaticG.FIELD_CONTENTS, "aortic")), 24);
		query.add(sfq, BooleanClause.Occur.SHOULD);

		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexInfoStaticG.FIELD_CONTENTS, "angioplasty")), 281);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexInfoStaticG.FIELD_CONTENTS, "antihypertensive")), 135);
		query.add(sfq, BooleanClause.Occur.SHOULD);

println " "
		println "query is $query"
		println getQueryMinimal(query)
	}
}
