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

		boolean spanF = queryWithoutComma.contains("spanFirst");

		if (spanF) {

			final String spanFirstQueryMinimal = queryWithoutComma.replaceAll(
					"spanFirst", "");

			String s = spanFirstQueryMinimal.replaceAll("\\(", "");
			s= s.replaceAll(" ", "")
			s = s.replaceAll("\\)", "#~");

			List sfList = s.tokenize("#~")

			def spanFirstMap = [:]

			for (int x = 0; x < sfList.size; x = x + 2) {

				if (sfList[x+1]==null) continue;

				def word = sfList[x]
				if (spanFirstMap.containsKey(word)) {
					spanFirstMap.put(word, Math.max(spanFirstMap.get(word),Integer.parseInt(sfList[x +1])))
				} else{
					spanFirstMap.put((word), Integer.parseInt(sfList[x +1]).intValue());
				}
			}

			spanFirstMap = spanFirstMap.sort { it.value}

			def sfshort = ''
			spanFirstMap.each { entry ->
				sfshort += "("+ entry.key + " "+ entry.value + ")"
			}

			return sfshort;
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
