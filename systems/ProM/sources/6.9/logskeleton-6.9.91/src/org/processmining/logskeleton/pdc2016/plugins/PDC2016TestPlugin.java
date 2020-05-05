package org.processmining.logskeleton.pdc2016.plugins;

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
import org.processmining.logskeleton.pdc2016.dialogs.PDC2016TestDialog;
import org.processmining.logskeleton.pdc2016.models.PDC2016TestModel;
import org.processmining.logskeleton.pdc2016.parameters.PDC2016TestParameters;
import org.processmining.pdc2016.algorithms.PDC2016LogAlgorithm;
import org.processmining.pdc2016.algorithms.PDC2016Set;
import org.processmining.pdc2016.parameters.PDC2016Parameters;

@Plugin(name = "PDC 2016 Test", parameterLabels = {}, returnLabels = { "Results" }, returnTypes = { PDC2016TestModel.class })
public class PDC2016TestPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = {})
	public static PDC2016TestModel run(final UIPluginContext context) {
		PDC2016TestParameters testParameters = new PDC2016TestParameters();
		PDC2016TestDialog testDialog = new PDC2016TestDialog(testParameters);
		InteractionResult result = context.showWizard("Select test parameters", true, true, testDialog);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		PDC2016TestModel testModel = new PDC2016TestModel(testParameters);
		PDC2016Parameters parameters = new PDC2016Parameters();
		PDC2016LogAlgorithm logAlgorithm = new PDC2016LogAlgorithm();
		
		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		try {
			for (int i : testParameters.getNrs()) {
				parameters.setNr(i);
				parameters.setSet(PDC2016Set.TRAIN);
				XLog trainingLog = logAlgorithm.apply(context, parameters);
				parameters.setSet(PDC2016Set.CAL1);
				XLog testLogMay = testParameters.getSets().contains(PDC2016Set.CAL1) ? logAlgorithm.apply(context, parameters) : null;
				parameters.setSet(PDC2016Set.CAL2);
				XLog testLogJune = testParameters.getSets().contains(PDC2016Set.CAL2) ? logAlgorithm.apply(context, parameters) : null;
				parameters.setSet(PDC2016Set.TEST);
				XLog testLogFinal = testParameters.getSets().contains(PDC2016Set.TEST) ? logAlgorithm.apply(context, parameters) : null;

				LogSkeletonClassifierAlgorithm classifierAlgorithm = new LogSkeletonClassifierAlgorithm();

				XLog classifiedTestLogCal1 = null;
				XLog classifiedTestLogCal2 = null;
				XLog classifiedTestLogTest = null;

				// Classify the logs
				if (testParameters.getSets().contains(PDC2016Set.CAL1)) {
					System.out.println("[PDC2016TestPlugin] Classify PDC2016 " + PDC2016Set.CAL1 + " number " + i);
					classifiedTestLogCal1 = classifierAlgorithm.apply(context, trainingLog, testLogMay, classifier, 
							new LogPreprocessorAlgorithm());
					context.getProvidedObjectManager().createProvidedObject("PDC2016 " + PDC2016Set.CAL1 + " number " + i,
							classifiedTestLogCal1, XLog.class, context);
				}
				if (testParameters.getSets().contains(PDC2016Set.CAL2)) {
					System.out.println("[PDC2016TestPlugin] Classify PDC2016 " + PDC2016Set.CAL2 + " number " + i);
					classifiedTestLogCal2 = classifierAlgorithm.apply(context, trainingLog, testLogJune, classifier, 
							new LogPreprocessorAlgorithm());
					context.getProvidedObjectManager().createProvidedObject("PDC2016 " + PDC2016Set.CAL2 + " number " + i,
							classifiedTestLogCal2, XLog.class, context);
				}
				if (testParameters.getSets().contains(PDC2016Set.TEST)) {
					System.out.println("[PDC2016TestPlugin] Classify PDC2016 " + PDC2016Set.TEST + " number " + i);
					classifiedTestLogTest = classifierAlgorithm.apply(context, trainingLog, testLogFinal, classifier, 
							new LogPreprocessorAlgorithm());
					context.getProvidedObjectManager().createProvidedObject("PDC2016 " + PDC2016Set.TEST + " number " + i,
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
