package compareapi;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class APIComparator {

	private static final Gson gson = new Gson();

	private String requestAPIAndFetchRepsonse(String requestAPI) throws MalformedURLException {

		URL url = new URL(requestAPI);
		HttpURLConnection conn = null;
		BufferedReader bufferedReader = null;
		StringBuilder result = new StringBuilder();
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.addRequestProperty("User-Agent", "webdriver.chrome.driver");
			conn.setRequestMethod("GET");
			InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
			bufferedReader = new BufferedReader(inputStreamReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			System.out.println("Could not connect to API. Error: " + e);
		} finally {
			if (bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return result.toString();
	}

	public static boolean isResponseJSON(String response) {
		try {
			gson.fromJson(response, Object.class);
			return true;
		} catch (com.google.gson.JsonSyntaxException ex) {
			return false;
		}
	}

	public static boolean isResponseXML(String response) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			builder.parse(new InputSource(new StringReader(response)));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static boolean compareJSONString(String response1, String response2) {
		ObjectMapper om = new ObjectMapper();
		try {
			Map<String, Object> m1 = (Map<String, Object>) (om.readValue(response1, Map.class));
			Map<String, Object> m2 = (Map<String, Object>) (om.readValue(response2, Map.class));
			return m1.equals(m2);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean compareXMLString(String response1, String response2) {

		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreAttributeOrder(true);

		DetailedDiff diff;
		try {
			diff = new DetailedDiff(XMLUnit.compareXML(response1, response2));
		} catch (SAXException | IOException e) {
			return false;
		}

		if (diff.getAllDifferences().size() > 0)
			return false;

		return true;
	}

	private boolean compareResponses(String response1, String response2) {

		if ((isResponseJSON(response1) && isResponseJSON(response2))) {
			return compareJSONString(response1, response2);
		} else if ((isResponseXML(response1) && isResponseXML(response2))) {
			return compareXMLString(response1, response2);
		}
		return false;
	}

	public static void main(String[] args) throws IOException {

		if (args.length < 2) {
			System.out.println("Insufficient Args.java APIComparator C:\\Users\\Rahul.R\\Documents\file1.txt C:\\Users\\Rahul.R\\Documents\file2.txt");
		}

		APIComparator apiComparator = new APIComparator();

		FileInputStream inputStreamFile1 = null;
		FileInputStream inputStreamFile2 = null;
		Scanner sc1 = null;
		Scanner sc2 = null;
		try {
			inputStreamFile1 = new FileInputStream(args[0]);
			inputStreamFile2 = new FileInputStream(args[1]);
			sc1 = new Scanner(inputStreamFile1, "UTF-8");
			sc2 = new Scanner(inputStreamFile2, "UTF-8");
			while (sc1.hasNextLine() && sc2.hasNextLine()) {
				String lineFi1e1 = sc1.nextLine();
				String lineFi1e2 = sc2.nextLine();
				String lineFile1Reponse = apiComparator.requestAPIAndFetchRepsonse(lineFi1e1);
				String lineFile2Reponse = apiComparator.requestAPIAndFetchRepsonse(lineFi1e2);
				if(apiComparator.compareResponses(lineFile1Reponse, lineFile2Reponse)){
					System.out.println(lineFi1e1 + " equals " + lineFi1e2 );
				}
				else {
					System.out.println(lineFi1e1 + " not equals " + lineFi1e2 );
				}
			}
			if (sc1.ioException() != null) {
				throw sc1.ioException();
			}
			if (sc2.ioException() != null) {
				throw sc2.ioException();
			}
		} finally {
			if (inputStreamFile1 != null) {
				inputStreamFile1.close();
			}
			if (inputStreamFile2 != null) {
				inputStreamFile2.close();
			}
			if (sc1 != null) {
				sc1.close();
			}
			if (sc2 != null) {
				sc2.close();
			}
		}

	}

}
