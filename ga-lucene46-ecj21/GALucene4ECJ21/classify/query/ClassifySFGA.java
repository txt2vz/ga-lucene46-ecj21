package query;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.index.Term;
//import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher; //import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import lucene.IndexWrapperG;
import lucene.ImportantWords;
import wordTools.TermList;
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.vector.IntegerVectorIndividual;

/**
 * To generate queries to perform binary text classification using GA string of
 * integer pairs which are translated into spanFirst queries
 * 
 * @author Laurie
 */

public class ClassifySFGA extends Problem implements SimpleProblemForm {

	private IndexSearcher searcher = IndexWrapperG.getInstance()
			.getIndexSearcher();

	private float F1train = 0;

	private String[] wordArray;

	private BooleanQuery query;

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);

		try {
			IndexWrapperG.getInstance().setFilters();

			ImportantWords importantWords = new ImportantWords();

			System.out.println("Total docs for cat  "
					+ IndexWrapperG.getInstance().getCatnumberAsString() + " "
					+ IndexWrapperG.getInstance().totalTrainDocsInCat
					+ " Total test docs for cat "
					+ IndexWrapperG.getInstance().totalTestDocsInCat);

			wordArray = importantWords.getF1WordList(true);

			System.out.println();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {

		if (ind.evaluated)
			return;

		GASFQ fitness = (GASFQ) ind.fitness;

		IntegerVectorIndividual intVectorIndividual = (IntegerVectorIndividual) ind;

		// use sorted map to remove redundant elements and improve readability
		Map<String, Integer> spanFirstMap = new TreeMap<String, Integer>();

		// create query from Map
		query = new BooleanQuery(true);

		// read through vector 2 ints at at time. 1st int retrieves word, second
		// specifies end for Lucene spanFirstQuery
		// store results in Map after removing redundant queries (i.e. same word
		// but lower end value)

		for (int i = 0; i < (intVectorIndividual.genome.length - 1); i = i + 1) {

			// any ints below 0 are ignored
			if (intVectorIndividual.genome[i] < 0)
				continue;

			if (intVectorIndividual.genome[i] < 0 ||
			// ignore clauses where end value of spanFirst is very low
					intVectorIndividual.genome[i + 1] < 0)
				continue;

			int wordInd = 0;
			if (intVectorIndividual.genome[i] >= wordArray.length
					|| intVectorIndividual.genome[i] < 0)
				wordInd = 0;
			else
				wordInd = intVectorIndividual.genome[i];

			final String word = wordArray[wordInd];
			// if (spanFirstMap.containsKey(word)) {
			//
			// final int end = spanFirstMap.get(word);
			// spanFirstMap.put(word, Math.max(end,
			// intVectorIndividual.genome[x + 1]));
			// } else
			spanFirstMap.put(word, intVectorIndividual.genome[i + 1]);
			//
		}

		for (String word : spanFirstMap.keySet()) {

			SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
					IndexWrapperG.FIELD_CONTENTS, word)),
					spanFirstMap.get(word));
			query.add(sfq, BooleanClause.Occur.SHOULD);
		}

		// for (String word : spanFirstMap.keySet()) {
		//
		// SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new
		// Term(
		// IndexWrapper.FIELD_CONTENTS, word)), intVectorIndividual.genome[x
		// + 1]);
		//
		// query.add(sfq, BooleanClause.Occur.SHOULD);
		// }

		// query.setMinimumNumberShouldMatch(termNo);

		fitness.setNumberOfTerms(spanFirstMap.size());

		try {
			TotalHitCountCollector collector = new TotalHitCountCollector();
			// TopScoreDocCollector collector = TopScoreDocCollector.create(0,
			// false);
			searcher.search(query, IndexWrapperG.getInstance().catTrainF,
					collector);
			final int positiveMatch = collector.getTotalHits();

			// collector = TopScoreDocCollector.create(0, false);
			collector = new TotalHitCountCollector();
			searcher.search(query, IndexWrapperG.getInstance().othersTrainF,
					collector);
			final int negativeMatch = collector.getTotalHits();

			F1train = ClassifyQuery.f1(positiveMatch, negativeMatch,
					IndexWrapperG.getInstance().totalTrainDocsInCat);

			fitness.setTrainValues(positiveMatch, negativeMatch);
			fitness.setF1Train(F1train);
			fitness.setQuery(query);

		} catch (IOException e) {

			e.printStackTrace();
		}

		float rawfitness = F1train;

		((SimpleFitness) intVectorIndividual.fitness).setFitness(state,
				rawfitness, false);

		ind.evaluated = true;
	}

	// }

	// @Override
	// public void describe(Individual ind, EvolutionState state,
	// int subpopulation, int threadnum, int log, int verbosity) {
	//
	// System.out.println("in describe query is: " + query + " F1 is "
	// + F1train);
	// }
}
