package org.processmining.framework.util.ui.widgets.traceview.masterdetail;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.framework.util.ui.widgets.ProMSplitPane;
import org.processmining.framework.util.ui.widgets.traceview.model.SortableListModel;

public class ProMTraceListMasterDetail<M, D> extends JPanel {

	private static final long serialVersionUID = -289071473358339232L;

	private final MasterView<M, D> masterView;
	private final DetailView<D> detailView;
	private final TransparentSplitPane splitPane;

	private boolean doUpdates = true;

	public ProMTraceListMasterDetail(final MasterView<M, D> masterView, final DetailView<D> detailView) {
		super();
		this.masterView = masterView;
		this.detailView = detailView;

		masterView.getMasterList().addTraceSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() || !doUpdates) {
					return;
				}

				final JList<M> list = masterView.getMasterList().getList();
				final int[] selectedTraces = list.getSelectedIndices();

				onBeforeUpdate(selectedTraces);

				SwingWorker<Collection<D>, Object> bgWorker = new SwingWorker<Collection<D>, Object>() {

					@Override
					public Collection<D> doInBackground() {
						List<D> detailedCollection = new ArrayList<D>();
						for (int index : selectedTraces) {
							if (index < list.getModel().getSize()) {
								Collection<D> detailElements = masterView
										.getDetailElements(list.getModel().getElementAt(index));
								detailedCollection.addAll(detailElements);
							}
						}
						return detailedCollection;
					}

					@Override
					protected void done() {
						detailView.getDetailList().clear();
						detailView.getDetailList().getList().getSelectionModel().clearSelection();
						try {
							detailView.getDetailList().addAll(get());
							if (detailView.getSortOrder() != null) {
								ListModel<D> listModel = detailView.getDetailList().getListModel();
								if (listModel instanceof SortableListModel) {
									((SortableListModel<D>) listModel).sort(detailView.getSortOrder());
								}
							}
							onAfterUpdate();
						} catch (ExecutionException e) {
							JOptionPane.showMessageDialog(ProMTraceListMasterDetail.this,
									"Error loading detail elements " + e.getMessage(), "Loading error",
									JOptionPane.ERROR_MESSAGE);
						} catch (InterruptedException e) {
							JOptionPane.showMessageDialog(ProMTraceListMasterDetail.this,
									"Error loading detail elements " + e.getMessage(), "Loading error",
									JOptionPane.ERROR_MESSAGE);
						} catch (HeadlessException e) {
							JOptionPane.showMessageDialog(ProMTraceListMasterDetail.this,
									"Error loading detail elements " + e.getMessage(), "Loading error",
									JOptionPane.ERROR_MESSAGE);
						}
					}

				};
				bgWorker.execute();
			}

		});

		setBackground(null);
		setForeground(null);
		setOpaque(false);
		setLayout(new BorderLayout());
		splitPane = createSplitPane();
		splitPane.setLeftComponent(masterView.getMasterComponent());
		splitPane.setRightComponent(detailView.getDetailComponent());
		splitPane.setResizeWeight(1.0d);
		splitPane.setOneTouchExpandable(true);
		add(splitPane, BorderLayout.CENTER);
	}

	protected TransparentSplitPane createSplitPane() {
		return new TransparentSplitPane(ProMSplitPane.HORIZONTAL_SPLIT);
	}

	public MasterView<M, D> getMasterView() {
		return masterView;
	}

	public DetailView<D> getDetailView() {
		return detailView;
	}

	public TransparentSplitPane getSplitPane() {
		return splitPane;
	}

	public void disableDetailUpdates() {
		doUpdates = false;
	}

	public void enableDetailUpdates() {
		doUpdates = true;
	}

	protected void onBeforeUpdate(int[] selectedIndicies) {
		detailView.getDetailList().beforeUpdate();
	}

	private void onAfterUpdate() {
		detailView.getDetailList().afterUpdate();
	}

}
