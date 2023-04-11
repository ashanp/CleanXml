package clean.java.xml.CleanXml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

public class ModifyXml {
	
	static String xmlpath;
	static String correct_pattern;
	static String error_pattern;

	public static void main(String[] args) {
		try {
			
			String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
			String appConfigPath = rootPath + "app.properties";
			Properties appProps = new Properties();
			appProps.load(new FileInputStream(appConfigPath));
			xmlpath=appProps.getProperty("xml_path");
			correct_pattern=appProps.getProperty("correct_pattern");
			error_pattern=appProps.getProperty("error_pattern");
			
			String xmlfile;

			File directoryPath = new File(xmlpath);

			File[] filesList = directoryPath.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xml");
				}
			});

			for (int a = 0; a < filesList.length; a++) {
				xmlfile = filesList[a].getAbsolutePath();
				DataType dataType = AnalyseXml(xmlfile);
				if (dataType != null && dataType.getPatternFound()) {
					CreateNewfile(filesList[a].getParent() + "\\modified\\" + filesList[a].getName(),
							dataType.getXmldata());
					MoveprocessedFiles(xmlfile);
				}
			}

		} catch (Exception e) {
		}
	}

	private static void MoveprocessedFiles(String xmlfile) throws IOException {
		Path path = Paths.get(xmlfile);
		Path temp = Files.move(Paths.get(xmlfile), Paths.get(path.getParent() + "\\processed\\" + path.getFileName()));

		if (temp != null) {
			System.out.println(" file moved to: [" + path.getParent() + "\\processed\\" + path.getFileName() + "]");
		} else {
			System.out.println(" Failed to move the file :[" + xmlfile + "]");
		}
	}

	private static void CreateNewfile(String xmlfile, String xmldata) throws IOException {
		Files.write(Paths.get(xmlfile), xmldata.getBytes());
	}

	public static DataType AnalyseXml(String xmlfile) throws Exception {
		BufferedReader readxml = new BufferedReader(new FileReader(xmlfile));
		String xmlfileline;
		StringBuilder lineInFile = new StringBuilder();
		DataType dataType;
		while ((xmlfileline = readxml.readLine()) != null) {
			lineInFile.append(xmlfileline).append(System.lineSeparator());
		}
		readxml.close();
		dataType = PatternReader(lineInFile);
		return dataType;
	}

	private static String patternCorrector(StringBuilder lineInFile, StringBuilder searchString) throws Exception {
		BufferedReader jobpatternReaderReplaser = new BufferedReader(new FileReader(correct_pattern));
		String jobPattern;
		StringBuilder searchStringtoReplace = new StringBuilder();
		while ((jobPattern = jobpatternReaderReplaser.readLine()) != null) {
			searchStringtoReplace.append(jobPattern);
			searchStringtoReplace.append(System.lineSeparator());
		}
		jobpatternReaderReplaser.close();
		String lineInFile_1 = lineInFile.toString().replaceAll(searchString.toString(),
				searchStringtoReplace.toString());
		return lineInFile_1;
	}

	public static long XmlLinecount(String xmlfile) throws IOException {
		long lineCount;
		File f = new File(xmlfile);
		Path p = f.toPath();
		try (Stream<String> stream = Files.lines(p, StandardCharsets.UTF_8)) {
			lineCount = stream.count();
		}
		return lineCount;
	}

	public static DataType PatternReader(StringBuilder lineInFile) throws Exception {
		BufferedReader jobpatternReader = new BufferedReader(new FileReader(
				error_pattern));
		String jobPattern;
		StringBuilder searchString = new StringBuilder();
		DataType dataType = new DataType();
		while ((jobPattern = jobpatternReader.readLine()) != null) {
			searchString.append(jobPattern);
			searchString.append(System.lineSeparator());
		}
		jobpatternReader.close();
		boolean value = lineInFile.toString().contains(searchString);

		if (value) {
			String xmldata = patternCorrector(lineInFile, searchString);
			dataType.setXmldata(xmldata);
			dataType.setPatternFound(true);
			System.out.print("The pattern identified.......>>>[" + value + "]");
		} else {
			System.out.println("The pattern identified.......>>>[" + value + "] file not moved");
		}

		return dataType;
	}

}
