package query.functions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import lucene.IndexInfoStaticG;
import query.*;


import ec.*;
import ec.gp.*;
import ec.util.*;

/**
 * Problem with strong typing in ECJ. This dummy function provided as a dummy
 * terminal Any tree containing this function should be given high fitness value
 * to deter use.
 * 
 * @todo find a better way to do this
 */

public class Dummy extends GPNode {

	// highly unlikely to occur.
	public static final String DUMMY_STRING = "DUMxy77£%";

	public String toString() {
		return "dummy";
	}

	public void checkConstraints(final EvolutionState state, final int tree,
			final GPIndividual typicalIndividual, final Parameter individualBase) {
		super.checkConstraints(state, tree, typicalIndividual, individualBase);
		if (children.length != 0) {
			state.output.error("Incorrect number of children for node "
					+ toStringForError() + " at " + individualBase);
		}
	}

	public void eval(final EvolutionState state, final int thread,
			final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {

		QueryData rd = ((QueryData) (input));

		rd.query = new BooleanQuery(true);

		rd.query.add(new TermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS, DUMMY_STRING)),
				BooleanClause.Occur.MUST);
		//rd.query = new SpanTermQuery(new Term(PFTData.FIELD_CONTENTS, DUMMY_STRING));	
	}
}