package org.processmining.logskeleton.pdc2017.parameters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.pdc2017.algorithms.PDC2017LogPreprocessorAlgorithm;
import org.processmining.pdc2017.algorithms.PDC2017Set;

public class PDC2017TestParameters {

	private Set<PDC2017Set> sets;
	private Set<PDC2017Set> allSets;
	private LogPreprocessorAlgorithm preprocessor;
	private Set<LogPreprocessorAlgorithm> allPreprocessors;
	private Set<Integer> nrs;
	private Set<Integer> allNrs;

	public PDC2017TestParameters() {

		sets = new HashSet<PDC2017Set>();
		allSets = new HashSet<PDC2017Set>();
		sets.addAll(Arrays.asList(PDC2017Set.values()));
		allSets.addAll(Arrays.asList(PDC2017Set.values()));
		sets.remove(PDC2017Set.TRAIN);
		allSets.remove(PDC2017Set.TRAIN);
		
		nrs = new HashSet<Integer>();
		allNrs = new HashSet<Integer>();
		for(int i = 1; i < 11; i++) {
			nrs.add(i);
			allNrs.add(i);
		}

		preprocessor = new PDC2017LogPreprocessorAlgorithm();
		allPreprocessors = new HashSet<LogPreprocessorAlgorithm>();
		allPreprocessors.add(new LogPreprocessorAlgorithm());
		allPreprocessors.add(preprocessor);
		allPreprocessors.add(new PDC2017LogPreprocessorAlgorithm(false, false, true));
		allPreprocessors.add(new PDC2017LogPreprocessorAlgorithm(true, false, false));
		allPreprocessors.add(new PDC2017LogPreprocessorAlgorithm(true, false, true));
		allPreprocessors.add(new PDC2017LogPreprocessorAlgorithm(false, true, false));
		allPreprocessors.add(new PDC2017LogPreprocessorAlgorithm(false, true, true));
		allPreprocessors.add(new PDC2017LogPreprocessorAlgorithm(true, true, false));
	}

	public Set<PDC2017Set> getSets() {
		return sets;
	}

	public void setSets(Set<PDC2017Set> sets) {
		this.sets = sets;
	}

	public LogPreprocessorAlgorithm getPreprocessor() {
		return preprocessor;
	}

	public void setPreprocessor(LogPreprocessorAlgorithm preprocessor) {
		this.preprocessor = preprocessor;
	}

	public Set<PDC2017Set> getAllSets() {
		return allSets;
	}

	public void setAllSets(Set<PDC2017Set> allSets) {
		this.allSets = allSets;
	}

	public Set<LogPreprocessorAlgorithm> getAllPreprocessors() {
		return allPreprocessors;
	}

	public void setAllPreprocessors(Set<LogPreprocessorAlgorithm> allPreprocessors) {
		this.allPreprocessors = allPreprocessors;
	}

	public Set<Integer> getNrs() {
		return nrs;
	}

	public void setNrs(Set<Integer> nrs) {
		this.nrs = nrs;
	}

	public Set<Integer> getAllNrs() {
		return allNrs;
	}

	public void setAllNrs(Set<Integer> allNrs) {
		this.allNrs = allNrs;
	}

}
