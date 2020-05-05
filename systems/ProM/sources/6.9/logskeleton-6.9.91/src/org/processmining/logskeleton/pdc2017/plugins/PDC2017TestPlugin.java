package org.processmining.logskeleton.pdc2017.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.algorithms.LogSkeletonClassifierAlgorithm;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;
import org.processmining.logskeleton.pdc2017.dialogs.PDC2017TestDialog;
import org.processmining.logskeleton.pdc2017.models.PDC2017TestModel;
import org.processmining.logskeleton.pdc2017.parameters.PDC2017TestParameters;
import org.processmining.pdc2017.algorithms.PDC2017LogAlgorithm;
import org.processmining.pdc2017.algorithms.PDC2017Set;
import org.processmining.pdc2017.parameters.PDC2017Parameters;

@Plugin(name = "PDC 2017 Test", parameterLabels = {}, returnLabels = { "Results" }, returnTypes = { PDC2017TestModel.class })
public class PDC2017TestPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = {})
	public static PDC2017TestModel run(final UIPluginContext context) {
		PDC2017TestParameters testParameters = new PDC2017TestParameters();
		PDC2017TestDialog testDialog = new PDC2017TestDialog(testParameters);
		InteractionResult result = context.showWizard("Select test parameters", true, true, testDialog);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		PDC2017TestModel testModel = new PDC2017TestModel(testParameters);
		PDC2017Parameters parameters = new PDC2017Parameters();
		PDC2017LogAlgorithm logAlgorithm = new PDC2017LogAlgorithm();

		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		try {
			for (int i : testParameters.getNrs()) {
				parameters.setNr(i);
				parameters.setSet(PDC2017Set.TRAIN);
				XLog trainingLog = logAlgorithm.apply(context, parameters);
				parameters.setSet(PDC2017Set.CAL1);
				XLog testLogMay = testParameters.getSets().contains(PDC2017Set.CAL1) ? logAlgorithm.apply(context,
						parameters) : null;
				parameters.setSet(PDC2017Set.CAL2);
				XLog testLogJune = testParameters.getSets().contains(PDC2017Set.CAL2) ? logAlgorithm.apply(context,
						parameters) : null;
				parameters.setSet(PDC2017Set.TEST);
				XLog testLogFinal = testParameters.getSets().contains(PDC2017Set.TEST) ? logAlgorithm.apply(context,
						parameters) : null;

				LogSkeletonClassifierAlgorithm classifierAlgorithm = new LogSkeletonClassifierAlgorithm();
				LogPreprocessorAlgorithm preprocessor = testParameters.getPreprocessor();

				XLog classifiedTestLogCal1 = null;
				XLog classifiedTestLogCal2 = null;
				XLog classifiedTestLogTest = null;

				// Classify the logs
				if (testParameters.getSets().contains(PDC2017Set.CAL1)) {
					System.out.println("[PDC2017TestPlugin] Classify PDC2017 " + PDC2017Set.CAL1 + " number " + i);
					classifiedTestLogCal1 = classifierAlgorithm.apply(context, trainingLog, testLogMay, classifier, preprocessor);
					context.getProvidedObjectManager().createProvidedObject("PDC2017 " + PDC2017Set.CAL1 + " number " + i,
							classifiedTestLogCal1, XLog.class, context);
				}
				if (testParameters.getSets().contains(PDC2017Set.CAL2)) {
					System.out.println("[PDC2017TestPlugin] Classify PDC2017 " + PDC2017Set.CAL2 + " number " + i);
					classifiedTestLogCal2 = classifierAlgorithm.apply(context, trainingLog, testLogJune, classifier, preprocessor);
					context.getProvidedObjectManager().createProvidedObject("PDC2017 " + PDC2017Set.CAL2 + " number " + i,
							classifiedTestLogCal2, XLog.class, context);
				}
				if (testParameters.getSets().contains(PDC2017Set.TEST)) {
					System.out.println("[PDC2017TestPlugin] Classify PDC2017 " + PDC2017Set.TEST + " number " + i);
					classifiedTestLogTest = classifierAlgorithm.apply(context, trainingLog, testLogFinal, classifier, preprocessor);
					context.getProvidedObjectManager().createProvidedObject("PDC2017 " + PDC2017Set.TEST + " number " + i,
							classifiedTestLogTest, XLog.class, context);
				}
				testModel.add(i, classifiedTestLogCal1, classifiedTestLogCal2, classifiedTestLogTest);
			}
			return testModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
