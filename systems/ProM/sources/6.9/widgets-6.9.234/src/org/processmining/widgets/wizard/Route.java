package org.processmining.widgets.wizard;

public interface Route<P> {

	Dialog<P> getNext(Dialog<P> current);

}
