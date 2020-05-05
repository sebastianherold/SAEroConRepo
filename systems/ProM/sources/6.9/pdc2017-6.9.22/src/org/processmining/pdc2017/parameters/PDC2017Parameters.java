package org.processmining.pdc2017.parameters;

import org.processmining.pdc2017.algorithms.PDC2017Set;

public class PDC2017Parameters {

	private PDC2017Set set;
	private int nr;

	public PDC2017Parameters() {
		set = PDC2017Set.TRAIN;
		nr = 1;
	}
	
	public PDC2017Set getSet() {
		return set;
	}

	public void setSet(PDC2017Set set) {
		this.set = set;
	}

	public int getNr() {
		return nr;
	}

	public void setNr(int nr) {
		this.nr = nr;
	}
}
