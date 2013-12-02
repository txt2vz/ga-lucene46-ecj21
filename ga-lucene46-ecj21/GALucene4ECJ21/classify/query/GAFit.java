package query;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.search.Query;

import lucene.IndexWrapperG;

import ec.EvolutionState;
import ec.simple.SimpleFitness;

/**
 * Store information about classification query and test/train values
 * 
 * @author Laurie
 * 
 */

public class GAFit extends SimpleFitness {

	private float f1train, f1Test, BEPTest;

	private int positiveMatchTrain, negativeMatchTrain;

	private int positiveMatchTest, negativeMatchTest, numberOfTerms = 0;

	private Query query;

	private int neutralHit = -1;

	public void setQuery(Query q) {
		query = q;
	}

	public Query getQuery() {
		return query;
	}

	public String getQueryMinimal() {
		final String queryWithoutComma = query.toString(
				IndexWrapperG.FIELD_CONTENTS).replaceAll(", ", "#~");

		final String spanFirstQueryMinimal = queryWithoutComma.replaceAll(
				"spanFirst", "");

		// System.out.println("S " + spanFirstQueryMinimal);
		String s = spanFirstQueryMinimal.replaceAll("\\(", "");

		s = s.replaceAll("\\)", "#~");
		String[] al = s.split("#~");

//		Map<String, Integer> spanFirstMap = new TreeMap<String, Integer>();
//
//		for (int x = 0; x < al.length; x = x + 2) {// (String s2: al){
//			// System.out.println("x " + x + " is " + al[x]);
//			if (spanFirstMap.containsKey(al[x])) {
//				System.err
//						.println("error in gasfq should not have duplicate term");
//
//				// final int end = spanFirstMap.get(word);
//				// spanFirstMap.put(word, Math.max(end,
//				// intVectorIndividual.genome[x + 1]));
//			} else
//				spanFirstMap.put(al[x].trim(), Integer.parseInt(al[x + 1]));
//
//		}
//		// }
//		Map<String, Integer> r = sortByValue(spanFirstMap);
//	String sr = "";
//		for (String word : r.keySet()) {
//			sr = sr + "(" + word + " " + r.get(word) + ")";
//		}
		//
		// SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new
		// Term(
		// IndexWrapper.FIELD_CONTENTS, word)), intVectorIndividual.genome[x
		// + 1]);
		//			
		// query.add(sfq, BooleanClause.Occur.SHOULD);
		// }

		return s;
	}

	static Map<String, Integer> sortByValue(Map<String, Integer> map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		// logger.info(list);
		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public void setTrainValues(int posMatchTrain, int negMatchTrain) {
		positiveMatchTrain = posMatchTrain;
		negativeMatchTrain = negMatchTrain;
	}

	public void setTestValues(int posMatchTest, int negMatchTest) {

		setPositiveMatchTest(posMatchTest);
		setNegativeMatchTest(negMatchTest);
	}

	public void setF1Train(final float f1) {
		f1train = f1;
	}

	public float getF1Train() {
		return f1train;
	}

	public void setF1Test(final float f1) {
		f1Test = f1;
	}

	public float getF1Test() {
		return f1Test;
	}

	public void setBEPTest(final float bep) {
		BEPTest = bep;
	}

	public float getBEPTest() {
		return BEPTest;
	}

	public void setNumberOfTerms(int numberOfTerms) {
		this.numberOfTerms = numberOfTerms;
	}

	public void setPositiveMatchTest(int positiveMatchTest) {
		this.positiveMatchTest = positiveMatchTest;
	}

	public int getPositiveMatchTest() {
		return positiveMatchTest;
	}

	public void setNegativeMatchTest(int negativeMatchTest) {
		this.negativeMatchTest = negativeMatchTest;
	}

	public int getNegativeMatchTest() {
		return negativeMatchTest;
	}

	public int getNumberOfTerms() {
		return numberOfTerms;
	}

	public void printFitnessForHumans(final EvolutionState state,
			final int log, final int verbosity) {

		super.printFitnessForHumans(state, log, verbosity);
		super.printFitnessForHumans(state, 0, verbosity);

		state.output.println(this.toString(state.generation), verbosity, log);
		state.output.println(this.toString(state.generation), verbosity, 0);
	}

	public String toString(int gen) {
		return "Gen: " + gen + " F1: " + f1train + " Positive Match: "
				+ positiveMatchTrain + " Negative Match: " + negativeMatchTrain
				+ " Total positive Docs: "
				+ IndexWrapperG.getInstance().totalTrainDocsInCat
				// + " neutral Hit " + neutralHit
				+ '\n' + "QueryString: "
				+ query.toString(IndexWrapperG.FIELD_CONTENTS) + '\n';
	}

	public void setNeutralHits(int neutralHit) {
		this.neutralHit = neutralHit;

	}

	public int getNeutralHit() {

		return neutralHit;
	}
}
