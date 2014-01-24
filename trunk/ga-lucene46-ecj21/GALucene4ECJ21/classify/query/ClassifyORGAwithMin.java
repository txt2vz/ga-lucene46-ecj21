package query;

import java.io.IOException;

import lucene.ImportantWords;
import lucene.IndexInfoStaticG;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TotalHitCountCollector;

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

public class ClassifyORGAwithMin extends Problem implements SimpleProblemForm {

	private IndexSearcher searcher = IndexInfoStaticG.getIndexSearcher();

	private float F1train = 0;

	private String[] wordArray;

	private BooleanQuery query;

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);

		try {

			System.out.println("Total docs for cat  "
					+ IndexInfoStaticG.getCatnumberAsString() + " "
					+ IndexInfoStaticG.totalTrainDocsInCat
					+ " Total test docs for cat "
					+ IndexInfoStaticG.totalTestDocsInCat);

			ImportantWords iw = new ImportantWords();
			wordArray = iw.getF1WordList(false, true);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {

		if (ind.evaluated)
			return;

		GAFit fitness = (GAFit) ind.fitness;

		IntegerVectorIndividual intVectorIndividual = (IntegerVectorIndividual) ind;

		query = new BooleanQuery(true);
		BooleanQuery queryMin = new BooleanQuery(true);
		queryMin.setMinimumNumberShouldMatch(2);
		for (int i = 0; i < intVectorIndividual.genome.length; i++) {

			if (intVectorIndividual.genome[i] < 0
					|| intVectorIndividual.genome[i] >= wordArray.length)
				continue;

			int wordInd = intVectorIndividual.genome[i];
			final String word = wordArray[wordInd];
			if (i < 50) {
				queryMin.add(new TermQuery(new Term(
						IndexInfoStaticG.FIELD_CONTENTS, word)),
						BooleanClause.Occur.SHOULD);
			} else if (i == 50)
				query.add(queryMin, BooleanClause.Occur.SHOULD);
			else

				query.add(new TermQuery(new Term(
						IndexInfoStaticG.FIELD_CONTENTS, word)),
						BooleanClause.Occur.SHOULD);
		}

		try {
			TotalHitCountCollector collector = new TotalHitCountCollector();
			searcher.search(query, IndexInfoStaticG.catTrainF, collector);
			final int positiveMatch = collector.getTotalHits();

			collector = new TotalHitCountCollector();
			searcher.search(query, IndexInfoStaticG.othersTrainF, collector);
			final int negativeMatch = collector.getTotalHits();

			F1train = ClassifyQuery.f1(positiveMatch, negativeMatch,
					IndexInfoStaticG.totalTrainDocsInCat);

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
}
