import dao.CountryDao;
import dao.ExactStateDao;
import dao.FuzzyStateDao;
import dao.GoogleGeoDao;
import dao.MySQLGeoDao;
import dao.NewStateDao;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class StateCompareMain {

  Map<String, ArrayList<GoogleGeoDao>> googleDataMap = new HashMap<>();
  Map<String, ArrayList<MySQLGeoDao>> mySQLDataMap = new HashMap<>();
  Map<String, ArrayList<NewStateDao>> newStateDataMap = new HashMap<>();
  Map<String, ArrayList<ExactStateDao>> exactStateMediaDataMap = new HashMap<>();
  Map<String, ArrayList<ExactStateDao>> exactStateDataMap = new HashMap<>();
  Map<String, ArrayList<NewStateDao>> mismatchStateDataMap = new HashMap<>();
  Map<String, ArrayList<FuzzyStateDao>> fuzzyStateDataMap = new HashMap<>();
  Map<String, CountryDao> countryDataMap = new HashMap<>();
  Map<String, Integer> gMap = new HashMap<>();

  public static void main(String[] args) throws URISyntaxException, IOException {
    String mysqlDataFileName = "mysql_region.csv";
    String googleDataFileName = "google_region.csv";
    String countryDataFileName = "country.csv";
    StateCompareMain sc = new StateCompareMain();
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
        countryDataMap.computeIfAbsent(
            countryIso2, k -> new CountryDao(tokens[9], internalCountryId, countryName));
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
      //      tempArr = line.split(",");
      String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
      // country ISO code
      String iso2 = tokens[0];
      // Region name given by Google
      String subDivName = tokens[2];
      // Required Region code in media_property_state table
      String requiredRegionCode = tokens[4];
      GoogleGeoDao googleGeoObj = new GoogleGeoDao(iso2, subDivName, requiredRegionCode);
      googleDataMap.computeIfAbsent(iso2, k -> new ArrayList<>()).add(googleGeoObj);
      gMap.put(iso2, gMap.getOrDefault(iso2, 0) + 1);
    }
    compareData(mySQLDataMap, googleDataMap);
    writeToFile();
  }

  private void display(String[] arr) {
    for (int i = 0; i < arr.length; i++) {
      System.out.print(arr[i] + "  ");
    }
    System.out.println();
  }

  private void writeToFile() throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("New State Data - new country");
    XSSFSheet sheet2 = workbook.createSheet("No changes");
    XSSFSheet sheet3 = workbook.createSheet("States present - code diff");
    XSSFSheet sheet4 = workbook.createSheet("New States Data");
    XSSFSheet sheet5 = workbook.createSheet("Fuzzy Match");
    XSSFSheet sheet6 = workbook.createSheet("Sheet 3 insert");
    XSSFSheet sheet7 = workbook.createSheet("Merged existing and fuzzy states");
    XSSFSheet sheet8 = workbook.createSheet("Count");
    XSSFSheet sheet9 = workbook.createSheet("MySQL Fuzzy match insert");

    // Sheet 1
    int rownum = 1;
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Country Code");
    header.createCell(1).setCellValue("Internal country id");
    header.createCell(2).setCellValue("Country Name");
    header.createCell(3).setCellValue("State Name");
    header.createCell(4).setCellValue("Normalized State Name");
    header.createCell(5).setCellValue("Google final region code");
    for (Map.Entry<String, ArrayList<NewStateDao>> newStateMap : newStateDataMap.entrySet()) {
      String country = newStateMap.getKey();
      for (NewStateDao newStates : newStateMap.getValue()) {
        XSSFRow row = sheet.createRow(rownum++);
        row.createCell(0).setCellValue(country);
        row.createCell(1).setCellValue(countryDataMap.get(country).getCountryId());
        row.createCell(2)
            .setCellValue(countryDataMap.get(country).getCountryName().replace("\"", ""));
        row.createCell(3).setCellValue(newStates.getGoogleRegionName());
        String stateName =
            Normalizer.normalize(newStates.getGoogleRegionName(), Normalizer.Form.NFD);
        stateName = stateName.replaceAll("\\p{M}", "");
        row.createCell(4).setCellValue(stateName);
        row.createCell(5).setCellValue(newStates.getFinalRegionCode());
      }
    }

    // Sheet 2
    int sheet2RowNum = 1;
    Row header2 = sheet2.createRow(0);
    header2.createCell(0).setCellValue("Country Code");
    header2.createCell(1).setCellValue("State Id");
    header2.createCell(2).setCellValue("Media Property State Name");
    header2.createCell(3).setCellValue("Google Geo State Name");
    header2.createCell(4).setCellValue("Required region code");
    for (Map.Entry<String, ArrayList<ExactStateDao>> exactStateMap :
        exactStateMediaDataMap.entrySet()) {
      String countryIso = exactStateMap.getKey();
      for (ExactStateDao exactStates : exactStateMap.getValue()) {
        XSSFRow row = sheet2.createRow(sheet2RowNum++);
        row.createCell(0).setCellValue(countryIso);
        row.createCell(1).setCellValue(exactStates.getStateId());
        row.createCell(2).setCellValue(exactStates.getMediaPropertyStateExternalStateId());
        row.createCell(3).setCellValue(exactStates.getGoogleRegionName());
        row.createCell(4).setCellValue(exactStates.getFinalGoogleRegionCode());
      }
    }

    int sheet3RowNum = 1;
    Row header3 = sheet3.createRow(0);
    header3.createCell(0).setCellValue("Country Code");
    header3.createCell(1).setCellValue("State Id");
    header3.createCell(2).setCellValue("State Name");
    header3.createCell(3).setCellValue("Google Geo State Name");
    header3.createCell(4).setCellValue("Media Property State Name");
    header3.createCell(5).setCellValue("Required region code");

    for (Map.Entry<String, ArrayList<ExactStateDao>> exactStateMap : exactStateDataMap.entrySet()) {
      String countryIso = exactStateMap.getKey();
      for (ExactStateDao exactStates : exactStateMap.getValue()) {
        XSSFRow row = sheet3.createRow(sheet3RowNum++);
        row.createCell(0).setCellValue(countryIso);
        row.createCell(1).setCellValue(exactStates.getStateId());
        row.createCell(2).setCellValue(exactStates.getStateName());
        row.createCell(3).setCellValue(exactStates.getGoogleRegionName());
        row.createCell(4).setCellValue(exactStates.getMediaPropertyStateExternalStateId());
        row.createCell(5).setCellValue(exactStates.getFinalGoogleRegionCode());
      }
    }

    int sheet4RowNum = 1;
    Row header4 = sheet4.createRow(0);
    header4.createCell(0).setCellValue("Country Code");
    header4.createCell(1).setCellValue("Internal Country Id");
    header4.createCell(2).setCellValue("Country Name");
    header4.createCell(3).setCellValue("State Name");
    header4.createCell(4).setCellValue("Normalized State Name");
    header4.createCell(5).setCellValue("Google final region code");
    for (Map.Entry<String, ArrayList<NewStateDao>> mismatchStateMap :
        mismatchStateDataMap.entrySet()) {
      String country = mismatchStateMap.getKey();
      for (NewStateDao newStates : mismatchStateMap.getValue()) {
        XSSFRow row = sheet4.createRow(sheet4RowNum++);
        row.createCell(0).setCellValue(country);
        row.createCell(1).setCellValue(countryDataMap.get(country).getCountryId());
        row.createCell(2)
            .setCellValue(countryDataMap.get(country).getCountryName().replace("\"", ""));
        row.createCell(3).setCellValue(newStates.getGoogleRegionName());
        String stateName =
            Normalizer.normalize(newStates.getGoogleRegionName(), Normalizer.Form.NFD);
        stateName = stateName.replaceAll("\\p{M}", "");
        row.createCell(4).setCellValue(stateName);
        row.createCell(5).setCellValue(newStates.getFinalRegionCode());
      }
    }

    int sheet5RowNum = 1;
    Row header5 = sheet5.createRow(0);
    header5.createCell(0).setCellValue("Country Code");
    header5.createCell(1).setCellValue("State Id");
    header5.createCell(2).setCellValue("Internal State Name");
    header5.createCell(3).setCellValue("Media Property State Name");
    header5.createCell(4).setCellValue("Google Geo State Name");
    header5.createCell(5).setCellValue("Final region code");
    for (Map.Entry<String, ArrayList<FuzzyStateDao>> fuzzyMap : fuzzyStateDataMap.entrySet()) {
      String countryIso = fuzzyMap.getKey();
      for (FuzzyStateDao fuzzyStates : fuzzyMap.getValue()) {
        XSSFRow row = sheet5.createRow(sheet5RowNum++);
        row.createCell(0).setCellValue(countryIso);
        row.createCell(1).setCellValue(fuzzyStates.getStateId());
        row.createCell(2).setCellValue(fuzzyStates.getStateName());
        row.createCell(3).setCellValue(fuzzyStates.getMediaPropertyStateExternalStateId());
        row.createCell(4).setCellValue(fuzzyStates.getGoogleRegionName());
        row.createCell(5).setCellValue(fuzzyStates.getFinalRegionCode());
      }
    }

    int sheet6RowNum = 1;
    Row header6 = sheet6.createRow(0);
    header6.createCell(0).setCellValue("media_property_id");
    header6.createCell(1).setCellValue("state_id");
    header6.createCell(2).setCellValue("media_property_state_external_id");
    for (Map.Entry<String, ArrayList<ExactStateDao>> exactStateMap : exactStateDataMap.entrySet()) {
      for (ExactStateDao exactStates : exactStateMap.getValue()) {
        XSSFRow row = sheet6.createRow(sheet6RowNum++);
        row.createCell(0).setCellValue(171);
        row.createCell(1).setCellValue(exactStates.getStateId());
        row.createCell(2).setCellValue(exactStates.getFinalGoogleRegionCode());
      }
    }

    int sheet7RowNum = 1;
    Row header7 = sheet7.createRow(0);
    header7.createCell(0).setCellValue("Country Code");
    header7.createCell(1).setCellValue("Google Geo State Name");
    header7.createCell(2).setCellValue("Internal State Name");
    header7.createCell(3).setCellValue("Media Property State Name");
    header7.createCell(4).setCellValue("Final region code");
    header7.createCell(5).setCellValue("Source");
    header7.createCell(6).setCellValue("State Id");
    Map<String, Integer> hMap = new HashMap<>();
    for (Map.Entry<String, ArrayList<ExactStateDao>> exactStateMap : exactStateDataMap.entrySet()) {
      String countryIso = exactStateMap.getKey();

      for (ExactStateDao exactStates : exactStateMap.getValue()) {
        hMap.put(countryIso, hMap.getOrDefault(countryIso, 0) + 1);
        XSSFRow row = sheet7.createRow(sheet7RowNum++);
        row.createCell(0).setCellValue(countryIso);
        row.createCell(1).setCellValue(exactStates.getGoogleRegionName());
        row.createCell(2).setCellValue(exactStates.getStateName());
        row.createCell(3).setCellValue(exactStates.getMediaPropertyStateExternalStateId());
        row.createCell(4).setCellValue(exactStates.getFinalGoogleRegionCode());
        row.createCell(5).setCellValue("States present");
        row.createCell(6).setCellValue(exactStates.getStateId());
      }
    }
    for (Map.Entry<String, ArrayList<FuzzyStateDao>> fuzzyMap : fuzzyStateDataMap.entrySet()) {
      String countryIso = fuzzyMap.getKey();
      for (FuzzyStateDao fuzzyStates : fuzzyMap.getValue()) {
        XSSFRow row = sheet7.createRow(sheet7RowNum++);
        hMap.put(countryIso, hMap.getOrDefault(countryIso, 0) + 1);
        row.createCell(0).setCellValue(countryIso);
        row.createCell(1).setCellValue(fuzzyStates.getGoogleRegionName());
        row.createCell(2).setCellValue(fuzzyStates.getStateName());
        row.createCell(3).setCellValue(fuzzyStates.getMediaPropertyStateExternalStateId());
        row.createCell(4).setCellValue(fuzzyStates.getFinalRegionCode());
        row.createCell(5).setCellValue("Fuzzy match");
        row.createCell(6).setCellValue(fuzzyStates.getStateId());
      }
    }

    for (Map.Entry<String, ArrayList<NewStateDao>> mismatchStateMap :
        mismatchStateDataMap.entrySet()) {
      String country = mismatchStateMap.getKey();
      for (NewStateDao newStates : mismatchStateMap.getValue()) {
        hMap.put(country, hMap.getOrDefault(country, 0) + 1);
        XSSFRow row = sheet7.createRow(sheet7RowNum++);
        row.createCell(0).setCellValue(country);
        row.createCell(1).setCellValue(newStates.getGoogleRegionName());
        row.createCell(2).setCellValue("");
        row.createCell(3).setCellValue("");
        row.createCell(4).setCellValue(newStates.getFinalRegionCode());
        row.createCell(5).setCellValue("New states");
        row.createCell(6).setCellValue("");
      }
    }

    int sheet8RowNum = 1;
    Row header8 = sheet8.createRow(0);
    header8.createCell(0).setCellValue("Country code");
    header8.createCell(1).setCellValue("MySQL  Count");
    header8.createCell(2).setCellValue("Google Count");
    for (Entry<String, Integer> hM : hMap.entrySet()) {
      XSSFRow row = sheet8.createRow(sheet8RowNum++);
      row.createCell(0).setCellValue(hM.getKey());
      row.createCell(1).setCellValue(hM.getValue());
      row.createCell(2).setCellValue(gMap.get(hM.getKey()));
    }

    int sheet9RowNum = 1;
    Row header9 = sheet9.createRow(0);
    header9.createCell(0).setCellValue("media_property_id");
    header9.createCell(1).setCellValue("state_id");
    header9.createCell(2).setCellValue("media_property_state_external_id");
    for (Map.Entry<String, ArrayList<FuzzyStateDao>> exactStateMap : fuzzyStateDataMap.entrySet()) {
      for (FuzzyStateDao exactStates : exactStateMap.getValue()) {
        XSSFRow row = sheet9.createRow(sheet9RowNum++);
        row.createCell(0).setCellValue(171);
        row.createCell(1).setCellValue(exactStates.getStateId());
        row.createCell(2).setCellValue(exactStates.getFinalRegionCode());
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
      countryId = countryDataMap.get(countryCode).getCountryId();
      ArrayList<GoogleGeoDao> googleRegionsByCountry = googleDataMap.get(countryCode);
      ArrayList<MySQLGeoDao> mySQLRegionsByCountry = mySQLDataMap.get(countryCode);

      // Case 1: Given a country, no states present in DB.
      if (mySQLRegionsByCountry == null || mySQLRegionsByCountry.isEmpty()) {
        for (GoogleGeoDao geoDao : googleMap.getValue()) {
          newStateDataMap
              .computeIfAbsent(countryCode, k -> new ArrayList<>())
              .add(new NewStateDao(geoDao.getRegionName(), countryId, geoDao.getFinalRegionCode()));
        }
        continue;
      }

      // Case 2: Matching region codes present in media_property_state table
      // Expected region code is already present in our DB
      for (GoogleGeoDao googleRegions : googleRegionsByCountry) {
        for (MySQLGeoDao mySQLRegions : mySQLRegionsByCountry) {
          if (mySQLRegions
              .getMediaPropertyStateExternalId()
              .equalsIgnoreCase(googleRegions.getFinalRegionCode())) {
            exactStateMediaDataMap
                .computeIfAbsent(countryCode, k -> new ArrayList<>())
                .add(
                    new ExactStateDao(
                        countryCode,
                        mySQLRegions.getInternalStateId(),
                        mySQLRegions.getMediaPropertyStateExternalId(),
                        googleRegions.getRegionName(),
                        mySQLRegions.getInternalStateName(),
                        googleRegions.getFinalRegionCode()));
            exactSet.add(countryCode + "-" + googleRegions.getRegionName());
          }
        }
      }

      // Case 3: Given a country, exact match of state names present in Google and MySQL
      // media_property_table
      // TODO: Insert required only in media_property_state table
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
                        googleRegions.getRegionName(),
                        mySQLRegions.getInternalStateName(),
                        googleRegions.getFinalRegionCode()));
            exactSet.add(countryCode + "-" + googleRegions.getRegionName());
          }
        }
      }
      // Case 4: Given a country, there is no match of state names present in Google and MySQL
      // Fuzzy match also checked here
      boolean flag = false;
      for (GoogleGeoDao googleRegions : googleRegionsByCountry) {
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
                          googleRegions.getRegionName(),
                          googleRegions.getFinalRegionCode()));
              flag = true;
            }
          }
          if (!flag)
            mismatchStateDataMap
                .computeIfAbsent(countryCode, k -> new ArrayList<>())
                .add(
                    new NewStateDao(
                        googleRegions.getRegionName(),
                        countryId,
                        googleRegions.getFinalRegionCode()));
        }
      }
    }
  }

  private boolean checkFuzzyMatch(String str1, String str2) {
    return (FuzzySearch.ratio(str1, str2) > 80) ? true : false;
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
