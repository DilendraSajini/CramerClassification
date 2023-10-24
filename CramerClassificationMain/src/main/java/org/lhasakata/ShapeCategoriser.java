package org.lhasakata;

import static org.katas.SvgLocations.CAT1_DIR;
import static org.katas.SvgLocations.CAT2_DIR;
import static org.katas.SvgLocations.CAT3_DIR;
import static org.katas.SvgLocations.INPUT_DIR;
import static org.katas.SvgLocations.OUTPUT_DIRS_ALL;
import static org.katas.SvgLocations.UNCLASSIFIED_DIR;
import static org.lhasakata.StartupUtils.INPUT_FOLDER;
import static org.lhasakata.StartupUtils.ROOT_DIR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.katas.graph.DecisionBuilder;
import org.lhasakata.assess.Assess;

import com.kitfox.svg.SVGElement;

public class ShapeCategoriser
{
	public static void main(String[] args)
	{
		String arg0 = args.length > 0 ? args[0] : null;
		String arg1 = args.length > 1 ? args[1] : null;
		help(arg0);
		testOnly(arg0, arg1);
		String workingDir = setupWorkingDir(arg0);
		
		checkDirectoriesExist();
		removeAllFilesFromOutputDirectory();
		
		var reader = SvgReader.getSvgReader();
		var files = SvgReader.getAllSvgs(workingDir + "/" + INPUT_FOLDER);
		for (File file : files)
		{
			List<SVGElement> elements = reader.readSvgFile(file);
			int category = calculateCategory(elements);
			System.out.println(file.getName() +" --> " + category);
			//copyFileToTargetCategoryFolder(file, category);
		}
		Assess.assess(workingDir);
		
	}

	static int calculateCategory(List<SVGElement> elements)
	{
		return DecisionBuilder.testSvg(elements);
	}

	static void copyFileToTargetCategoryFolder(File file, int category)
	{
		try {
			Files.copy(file.toPath(), categoryToOutputDir(category, file.getName()).toPath());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static File categoryToOutputDir(int category, String filename) {
		switch (category) {
			case 3: return new File(CAT3_DIR + "/" + filename);
			case 2: return new File(CAT2_DIR + "/" + filename);
			case 1: return new File(CAT1_DIR + "/" + filename);
			case -1:
			default: return new File(UNCLASSIFIED_DIR + "/" + filename);
		}
	}
	
	private static void checkDirectoriesExist() {
		File inputDir = checkDirectoryExists(INPUT_DIR);
		checkDirContainsSvgs(inputDir);
		OUTPUT_DIRS_ALL.forEach(ShapeCategoriser::checkDirectoryExists);
	}

	private static void checkDirContainsSvgs(File directory) {
		var files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".svg"));
		if (files == null || files.length == 0) {
			throw new IllegalStateException("Directory contains no svgs: " + directory);
		}
	}

	private static File checkDirectoryExists(String dir) {
		var directory = new File(dir);
		if (!directory.exists()) {
			throw new IllegalStateException("Directory does not exist: " + directory);
		}
		return directory;
	}

	private static void removeAllFilesFromOutputDirectory() {
		OUTPUT_DIRS_ALL.forEach(dir -> {
			var file = new File(dir);
			if (file.exists()) {
				var contents = file.listFiles();
				if (contents != null) {
					Arrays.stream(contents).forEach(File::delete);
				}
			}
		});
	}

	private static String setupWorkingDir(String arg0) {
		var workingDir = arg0 != null && !arg0.isBlank() ? arg0 : ROOT_DIR;
		ROOT_DIR = workingDir;
		System.out.println("Using 'input' and 'output' folders in parent directory: " + workingDir);
		return workingDir;
	}

	private static void help(String arg0) {
		if ("help".equals(arg0) || "h".equals(arg0) || "--help".equals(arg0)) {
			System.out.println("""
		Run using Java 17
		To run using default folder (C:/kata-svg), pass no arguments.
		To run using custom folder, pass absolute path as first argument: java -jar java-quickstart-1.0-SNAPSHOT.jar C:/Test/svgClassificationFiles
		To run only test using default folders (C:/kata-svg): java -jar java-quickstart-1.0-SNAPSHOT.jar test
		To run detailed test using custom folders: java -jar java-quickstart-1.0-SNAPSHOT.jar "C:/Test/kata files" test
		""");
			System.exit(0);
		}
	}

	private static void testOnly(String arg0, String arg1) {
		if ("test".equals(arg0)) {
			System.out.println("Running tests only");
			Assess.assess(null);
			System.exit(0);
		}
		else if ("test".equals(arg1)) {
			System.out.println("Running tests only");
			Assess.assess(arg0);
			System.exit(0);
		}
	}
}
