package org.processmining.logskeleton.pdc2016.parameters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.processmining.pdc2016.algorithms.PDC2016Set;

public class PDC2016TestParameters {

	private Set<PDC2016Set> sets;
	private Set<PDC2016Set> allSets;
	private Set<Integer> nrs;
	private Set<Integer> allNrs;

	public PDC2016TestParameters() {

		sets = new HashSet<PDC2016Set>();
		allSets = new HashSet<PDC2016Set>();
		sets.addAll(Arrays.asList(PDC2016Set.values()));
		allSets.addAll(Arrays.asList(PDC2016Set.values()));
		sets.remove(PDC2016Set.TRAIN);
		allSets.remove(PDC2016Set.TRAIN);
		
		nrs = new HashSet<Integer>();
		allNrs = new HashSet<Integer>();
		for(int i = 1; i < 11; i++) {
			nrs.add(i);
			allNrs.add(i);
		}

	}

	public Set<PDC2016Set> getSets() {
		return sets;
	}

	public void setSets(Set<PDC2016Set> sets) {
		this.sets = sets;
	}

	public Set<PDC2016Set> getAllSets() {
		return allSets;
	}

	public void setAllSets(Set<PDC2016Set> allSets) {
		this.allSets = allSets;
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
