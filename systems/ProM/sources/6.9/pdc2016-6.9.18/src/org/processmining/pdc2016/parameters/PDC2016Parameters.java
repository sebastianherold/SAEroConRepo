package org.processmining.pdc2016.parameters;

import org.processmining.pdc2016.algorithms.PDC2016Set;

public class PDC2016Parameters {

	private PDC2016Set set;
	private int nr;

	public PDC2016Parameters() {
		set = PDC2016Set.TRAIN;
		nr = 1;
	}
	
	public PDC2016Set getSet() {
		return set;
	}

	public void setSet(PDC2016Set set) {
		this.set = set;
	}

	public int getNr() {
		return nr;
	}

	public void setNr(int nr) {
		this.nr = nr;
	}
}
