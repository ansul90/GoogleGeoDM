import dao.ExactStateDao;
import dao.FuzzyStateDao;
import dao.GoogleGeoDao;
import dao.MySQLGeoDao;
import dao.NewStateDao;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class StateCompare {

  Map<String, ArrayList<GoogleGeoDao>> googleDataMap = new HashMap<>();
  Map<String, ArrayList<MySQLGeoDao>> mySQLDataMap = new HashMap<>();
  Map<String, ArrayList<NewStateDao>> newStateDataMap = new HashMap<>();
  Map<String, ArrayList<ExactStateDao>> exactStateDataMap = new HashMap<>();
  Map<String, ArrayList<NewStateDao>> mismatchStateDataMap = new HashMap<>();
  Map<String, ArrayList<FuzzyStateDao>> fuzzyStateDataMap = new HashMap<>();
  Map<String, String> countryDataMap = new HashMap<>();

  public static void main(String[] args) throws URISyntaxException, IOException {
    String mysqlDataFileName = "mysql_region.csv";
    String googleDataFileName = "google_region.csv";
    String countryDataFileName = "country.csv";
    StateCompare sc = new StateCompare();
    File mySqlDataFile = sc.getFileFromResource(mysqlDataFileName);
    File googleDataFile = sc.getFileFromResource(googleDataFileName);
    File countryDataFile = sc.getFileFromResource(countryDataFileName);
    sc.compareStates(mySqlDataFile, googleDataFile, countryDataFile);
  }

  public void compareStates(File mySqlDataFile, File googleDataFile, File countryDataFile)
      throws IOException {
    String line = " ";
    String[] tempArr;
    int count = 0;
    String countryIso2 = "";
    FileWriter resultDataSheet = new FileWriter("/Users/ansugupt/Downloads/googlegeo/result.csv");

    FileReader mySqlDataFR = new FileReader(mySqlDataFile);
    FileReader googleDataFR = new FileReader(googleDataFile);
    FileReader countryDataFR = new FileReader(countryDataFile);
    BufferedReader mySqlBr = new BufferedReader(mySqlDataFR);
    BufferedReader googleBr = new BufferedReader(googleDataFR);
    BufferedReader countryBr = new BufferedReader(countryDataFR);
    while ((line = countryBr.readLine()) != null) {
      count++;
      if (count == 1) {
        continue;
      }
      String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
      tempArr = line.split(",");
      String internalCountryId = tokens[0];
      String countryName = tokens[1];
      //      display(tokens);
      countryIso2 = tokens[9];
      //      if (countryName.contains(",")) {
      //        countryIso2 = tempArr[11];
      //      } else {
      //        countryIso2 = tokens[9];
      //      }
      if (countryIso2 != null && !countryIso2.isEmpty())
        countryDataMap.computeIfAbsent(countryIso2, k -> internalCountryId);
    }

    while ((line = mySqlBr.readLine()) != null) {
      // Skipping 1st line i.e., header
      count++;
      if (count == 1) {
        continue;
      }
      tempArr = line.split(",");
      // country_id: internal country Id
      String internalCountryId = tempArr[0];
      // country_iso: AF,US
      countryIso2 = tempArr[2];
      // state_id: internal_state_id present in state table
      String internalStateId = tempArr[4];
      // value coming from Google in Bid request
      String mediaPropertyStateExternalId = tempArr[7];
      // state name value present in state tables and used internally
      String internalStateName = tempArr[5];
      MySQLGeoDao mySqlGeoObj =
          new MySQLGeoDao(
              internalCountryId,
              countryIso2,
              internalStateId,
              mediaPropertyStateExternalId,
              internalStateName);
      mySQLDataMap.computeIfAbsent(countryIso2, k -> new ArrayList<>()).add(mySqlGeoObj);
    }
    count = 0;

    while ((line = googleBr.readLine()) != null) {
      count++;
      if (count == 1) {
        continue;
      }
      tempArr = line.split(",");
      // country ISO code
      String iso2 = tempArr[0];
      // Region name given by Google
      String subDivName = tempArr[2];
      // Required Region code in media_property_state table
      String requiredRegionCode = tempArr[4];
      GoogleGeoDao googleGeoObj = new GoogleGeoDao(iso2, subDivName, requiredRegionCode);
      googleDataMap.computeIfAbsent(iso2, k -> new ArrayList<>()).add(googleGeoObj);
    }
    compareData(mySQLDataMap, googleDataMap);
    writeToFile(newStateDataMap, exactStateDataMap);
  }

  private void display(String[] arr) {
    for (int i = 0; i < arr.length; i++) {
      System.out.print(arr[i] + "  ");
    }
    System.out.println();
  }

  private void writeToFile(
      Map<String, ArrayList<NewStateDao>> newStateDataMap,
      Map<String, ArrayList<ExactStateDao>> exactStateDataMap)
      throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("New State Data");
    XSSFSheet sheet2 = workbook.createSheet("Exact State Data");
    XSSFSheet sheet3 = workbook.createSheet("No match State Data");
    XSSFSheet sheet4 = workbook.createSheet("Fuzzy Match");

    int rownum = 1;
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Country Code");
    header.createCell(1).setCellValue("Country Id");
    header.createCell(2).setCellValue("State Name");
    for (Map.Entry<String, ArrayList<NewStateDao>> newStateMap : newStateDataMap.entrySet()) {
      String country = newStateMap.getKey();
      for (NewStateDao newStates : newStateMap.getValue()) {
        XSSFRow row = sheet.createRow(rownum++);
        row.createCell(0).setCellValue(country);
        row.createCell(1).setCellValue(countryDataMap.get(country));
        row.createCell(2).setCellValue(newStates.getStateName());
      }
    }

    int sheet2RowNum = 1;
    Row header2 = sheet2.createRow(0);
    header2.createCell(0).setCellValue("Country Code");
    header2.createCell(1).setCellValue("State Id");
    header2.createCell(2).setCellValue("Media Property State Name");
    header2.createCell(3).setCellValue("Google Geo State Name");
    for (Map.Entry<String, ArrayList<ExactStateDao>> exactStateMap : exactStateDataMap.entrySet()) {
      String countryIso = exactStateMap.getKey();
      for (ExactStateDao exactStates : exactStateMap.getValue()) {
        XSSFRow row = sheet2.createRow(sheet2RowNum++);
        row.createCell(0).setCellValue(countryIso);
        row.createCell(1).setCellValue(exactStates.getStateId());
        row.createCell(2).setCellValue(exactStates.getMediaPropertyStateExternalStateId());
        row.createCell(3).setCellValue(exactStates.getGoogleRegionName());
      }
    }

    int sheet3RowNum = 1;
    Row header3 = sheet3.createRow(0);
    header3.createCell(0).setCellValue("Country Code");
    header3.createCell(1).setCellValue("Country Id");
    header3.createCell(2).setCellValue("State Name");
    for (Map.Entry<String, ArrayList<NewStateDao>> mismatchStateMap :
        mismatchStateDataMap.entrySet()) {
      String country = mismatchStateMap.getKey();
      for (NewStateDao newStates : mismatchStateMap.getValue()) {
        XSSFRow row = sheet3.createRow(sheet3RowNum++);
        row.createCell(0).setCellValue(country);
        row.createCell(1).setCellValue(countryDataMap.get(country));
        row.createCell(2).setCellValue(newStates.getStateName());
      }
    }

    int sheet4RowNum = 1;
    Row header4 = sheet4.createRow(0);
    header4.createCell(0).setCellValue("Country Code");
    header4.createCell(1).setCellValue("State Id");
    header4.createCell(2).setCellValue("Internal State Name");
    header4.createCell(3).setCellValue("Media Property State Name");
    header4.createCell(4).setCellValue("Google Geo State Name");
    for (Map.Entry<String, ArrayList<FuzzyStateDao>> fuzzyMap : fuzzyStateDataMap.entrySet()) {
      String countryIso = fuzzyMap.getKey();
      for (FuzzyStateDao fuzzyStates : fuzzyMap.getValue()) {
        XSSFRow row = sheet4.createRow(sheet4RowNum++);
        row.createCell(0).setCellValue(countryIso);
        row.createCell(1).setCellValue(fuzzyStates.getStateId());
        row.createCell(2).setCellValue(fuzzyStates.getStateName());
        row.createCell(3).setCellValue(fuzzyStates.getMediaPropertyStateExternalStateId());
        row.createCell(4).setCellValue(fuzzyStates.getGoogleRegionName());
      }
    }

    FileOutputStream fos = new FileOutputStream(new File("result.xlsx"));
    workbook.write(fos);
    fos.close();
  }

  private void compareData(
      Map<String, ArrayList<MySQLGeoDao>> mySQLDataMap,
      Map<String, ArrayList<GoogleGeoDao>> googleDataMap) {
    String countryCode = "";
    String countryId = "";
    Set<String> exactSet = new HashSet<>();
    for (Map.Entry<String, ArrayList<GoogleGeoDao>> googleMap : googleDataMap.entrySet()) {
      countryCode = googleMap.getKey();
      countryId = countryDataMap.get(countryCode);
      ArrayList<GoogleGeoDao> googleRegionsByCountry = googleDataMap.get(countryCode);
      ArrayList<MySQLGeoDao> mySQLRegionsByCountry = mySQLDataMap.get(countryCode);
      // Case 1: Given a country, no states present in DB
      if (mySQLRegionsByCountry == null || mySQLRegionsByCountry.isEmpty()) {
        for (GoogleGeoDao geoDao : googleMap.getValue()) {
          newStateDataMap
              .computeIfAbsent(countryCode, k -> new ArrayList<>())
              .add(new NewStateDao(geoDao.getRegionName(), countryId));
        }
        continue;
      }
      // Case 2: Given a country, exact match of state names present in Google and MySQL
      // media_property_table
      for (GoogleGeoDao googleRegions : googleRegionsByCountry) {
        for (MySQLGeoDao mySQLRegions : mySQLRegionsByCountry) {
          if (mySQLRegions
              .getMediaPropertyStateExternalId()
              .equalsIgnoreCase(googleRegions.getRegionName())) {
            exactStateDataMap
                .computeIfAbsent(countryCode, k -> new ArrayList<>())
                .add(
                    new ExactStateDao(
                        countryCode,
                        mySQLRegions.getInternalStateId(),
                        mySQLRegions.getMediaPropertyStateExternalId(),
                        googleRegions.getRegionName()));
            exactSet.add(countryCode + "-" + googleRegions.getRegionName());
          }
        }
      }
      // Case 3: Given a country, exact match of state names present in Google and MySQL state table
      for (GoogleGeoDao googleRegions : googleRegionsByCountry) {
        for (MySQLGeoDao mySQLRegions : mySQLRegionsByCountry) {
          if (mySQLRegions.getInternalStateName().equalsIgnoreCase(googleRegions.getRegionName())
              && !exactSet.contains(countryCode + "-" + googleRegions.getRegionName())) {
            exactStateDataMap
                .computeIfAbsent(countryCode, k -> new ArrayList<>())
                .add(
                    new ExactStateDao(
                        countryCode,
                        mySQLRegions.getInternalStateId(),
                        mySQLRegions.getMediaPropertyStateExternalId(),
                        googleRegions.getRegionName()));
            exactSet.add(countryCode + "-" + googleRegions.getRegionName());
          }
        }
      }
      boolean flag = false;
      // Case 3: Given a country, there is no match of state names present in Google and MySQL
      for (GoogleGeoDao googleRegions : googleRegionsByCountry) { // {Canilla, Ordino}
        flag = false;
        String customGoogleKey = countryCode + "-" + googleRegions.getRegionName();
        if (!exactSet.contains(customGoogleKey)) {
          for (MySQLGeoDao mySQLRegions : mySQLRegionsByCountry) {
            String mySQLInternalNameKey = countryCode + "-" + mySQLRegions.getInternalStateName();
            String mySQLMediaNameKey =
                countryCode + "-" + mySQLRegions.getMediaPropertyStateExternalId();
            if (checkFuzzyMatch(customGoogleKey, mySQLInternalNameKey)
                || checkFuzzyMatch(customGoogleKey, mySQLMediaNameKey)) {
              fuzzyStateDataMap
                  .computeIfAbsent(countryCode, k -> new ArrayList<>())
                  .add(
                      new FuzzyStateDao(
                          countryCode,
                          mySQLRegions.getInternalStateId(),
                          mySQLRegions.getInternalStateName(),
                          mySQLRegions.getMediaPropertyStateExternalId(),
                          googleRegions.getRegionName()));
              flag = true;
            }
          }
          if (!flag)
            mismatchStateDataMap
                .computeIfAbsent(countryCode, k -> new ArrayList<>())
                .add(new NewStateDao(googleRegions.getRegionName(), countryId));
        }
      }
    }
  }

  private boolean checkFuzzyMatch(String str1, String str2) {
    return (FuzzySearch.ratio(str1, str2) > 85) ? true : false;
  }

  private File getFileFromResource(String fileName) throws URISyntaxException {

    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(fileName);
    if (resource == null) {
      throw new IllegalArgumentException("file not found! " + fileName);
    } else {
      return new File(resource.toURI());
    }
  }
}
