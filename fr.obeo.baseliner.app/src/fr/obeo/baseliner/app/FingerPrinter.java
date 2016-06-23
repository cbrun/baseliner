package fr.obeo.baseliner.app;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class FingerPrinter implements IApplication {

	@Option(name = "-input", usage = "Specify a folder to inspect. If not specified, then the current host installation will be inspected.", metaVar = "FOLDER")
	private File inputFolder;
	
	@Option(name = "-reports-folder", usage = "Specify a folder to generate the reports to.", metaVar = "FOLDER")
	private File reportsFolder;
	

	/**
	 * Workspace location. This argument is here only to mimic the OSGi
	 * applications common arguments so that they are displayed in usage.
	 */
	@Option(name = "-data", usage = "Specify the folder which will keep the workspace.", metaVar = "FOLDER")
	private File dataFolder;

	/**
	 * consoleLog. This argument is here only to mimic the OSGi applications
	 * common arguments so that they are displayed in usage.
	 */
	@Option(name = "-consoleLog", usage = "Log messages in the console.")
	private boolean consoleLog;

	/**
	 * Return code for the application in case of error.
	 */
	private static final int APPLICATION_ERROR = -1;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		return new FingerPrinter().doMain(args);
	}

	private Object doMain(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);

			final Monitor monitor = new BasicMonitor.Printing(System.out);
			try {
				new PDEInspector().inspectProduct(inputFolder,reportsFolder, BasicMonitor.toIProgressMonitor(monitor));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (CmdLineException e) {

			/*
			 * print the list of available options
			 */
			parser.printUsage(System.err);
			System.err.println();
			

			return APPLICATION_ERROR;
		}

		return IApplication.EXIT_OK;

	}

	@Override
	public void stop() {
		/*
		 * nothing special to do when the application stops.
		 */

	}

}
