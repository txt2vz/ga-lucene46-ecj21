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

public class ClassifyANDORGAv2 extends Problem implements SimpleProblemForm {

	private IndexSearcher searcher = IndexInfoStaticG.getIndexSearcher();

	private float F1train = 0;

	private String[] wordArray;

	private BooleanQuery query, subq;

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

		// create query from Map
		query = new BooleanQuery(true);
		int wordInd0, wordInd1;
		
		for (int i = 0; i < (intVectorIndividual.genome.length - 2); i = i + 2) {
			
			if (       intVectorIndividual.genome[i] >= wordArray.length
					|| intVectorIndividual.genome[i] < 0
					|| intVectorIndividual.genome[i +1] >= wordArray.length
					|| intVectorIndividual.genome[i +1] < 0	
					|| intVectorIndividual.genome[i] == intVectorIndividual.genome[i +1]
					)
				continue;
			else
			{
				 wordInd0 = intVectorIndividual.genome[i];
				 wordInd1 = intVectorIndividual.genome[i+1];
			}
			final String word0 = wordArray[wordInd0];
			final String word1 = wordArray[wordInd1];
			
			subq = new BooleanQuery(true);
			subq.add(new TermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS,
					word0)), BooleanClause.Occur.MUST);

			subq.add(new TermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS,
					word1)), BooleanClause.Occur.MUST);
			query.add(subq, BooleanClause.Occur.SHOULD);
		}

		try {
			TotalHitCountCollector collector = new TotalHitCountCollector();
			searcher.search(query, IndexInfoStaticG.catTrainF, collector);
			final int positiveMatch = collector.getTotalHits();

			// collector = TopScoreDocCollector.create(0, false);
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
