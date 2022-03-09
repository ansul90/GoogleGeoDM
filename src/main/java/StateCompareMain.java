import dao.CountryDao;
import dao.state.ExactStateDao;
import dao.state.FuzzyStateDao;
import dao.state.GoogleRegionDao;
import dao.state.MysqlMediaPropertyStateDao;
import dao.state.MysqlStateDao;
import dao.state.NewStateDao;
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
import java.util.Set;
import java.util.TreeMap;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class StateCompareMain {

  String splitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
  Map<String, CountryDao> countryDataMap = new HashMap<>();
  Map<String, ArrayList<GoogleRegionDao>> googleRegionMap = new HashMap<>();
  Map<String, Integer> googleRegionCountMap = new HashMap<>();
  Map<String, ArrayList<MysqlStateDao>> mysqlStateMap = new HashMap<>();
  Map<String, Integer> mysqlStateCountMap = new TreeMap<>();
  Map<String, ArrayList<MysqlMediaPropertyStateDao>> mysqlMpsMap = new HashMap<>();
  Map<String, Integer> mysqlMpsCountMap = new TreeMap<>();
  Map<String, ArrayList<NewStateDao>> newStateDataMap = new HashMap<>();
  Map<String, ArrayList<ExactStateDao>> exactMediaStateMap = new HashMap<>();
  Map<String, ArrayList<ExactStateDao>> exactStateMap = new HashMap<>();
  Map<String, ArrayList<FuzzyStateDao>> fuzzyStateMap = new HashMap<>();
  Map<String, ArrayList<NewStateDao>> mismatchStateMap = new HashMap<>();
  Map<String, Integer> mysqlFinalCountMap = new HashMap<>();

  public static void main(String[] args) throws URISyntaxException, IOException {
    String mysqlStatsFileName = "region/mysql_state.csv";
    String mysqlMpsFileName = "region/mysql_mps.csv";
    String googleRegionFileName = "region/google_region.csv";
    String countryDataFileName = "country.csv";
    //    String mysqlStateMpsFileName = "region/mysql_state_mps.csv";

    StateCompareMain sc = new StateCompareMain();

    File mySqlStateFile = sc.getFileFromResource(mysqlStatsFileName);
    File mySqlMpsFile = sc.getFileFromResource(mysqlMpsFileName);
    File googleRegionFile = sc.getFileFromResource(googleRegionFileName);
    File countryDataFile = sc.getFileFromResource(countryDataFileName);
    //    File mySqlStateMpsFile = sc.getFileFromResource(mysqlStateMpsFileName);
    //    sc.compareStates(mySqlStateMpsFile, googleRegionFile, countryDataFile);
    sc.compareStatesNew(mySqlStateFile, mySqlMpsFile, googleRegionFile, countryDataFile);
  }

  public void compareStatesNew(
      File mySqlStateFile, File mySqlMpsFile, File googleRegionFile, File countryDataFile)
      throws IOException {
    String line = " ";
    String[] tempArr;
    int count = 0;
    String countryIso2 = "";
    FileReader mySqlStateFR = new FileReader(mySqlStateFile);
    FileReader mySqlMpsFR = new FileReader(mySqlMpsFile);
    FileReader googleRegionFR = new FileReader(googleRegionFile);
    FileReader countryDataFR = new FileReader(countryDataFile);
    BufferedReader mySqlStateBr = new BufferedReader(mySqlStateFR);
    BufferedReader mySqlMpsBr = new BufferedReader(mySqlMpsFR);
    BufferedReader googleRegionBr = new BufferedReader(googleRegionFR);
    BufferedReader countryBr = new BufferedReader(countryDataFR);

    // Country map creation. Ex: { IN -> CountryDao(IN, <id>, India) }
    while ((line = countryBr.readLine()) != null) {
      count++;
      if (count == 1) {
        continue;
      }
      String[] tokens = line.split(splitRegex, -1);
      String internalCountryId = tokens[0];
      String countryName = tokens[1];
      countryIso2 = tokens[9];
      if (countryIso2 != null && !countryIso2.isEmpty()) {
        countryDataMap.computeIfAbsent(
            countryIso2, k -> new CountryDao(tokens[9], internalCountryId, countryName));
      }
    }

    // Google region map. Ex: { IN -> {GoogleRegionDao(IN, Karnataka, IN-KA)}}
    count = 0;
    while ((line = googleRegionBr.readLine()) != null) {
      count++;
      if (count == 1) {
        continue;
      }
      String[] tokens = line.split(splitRegex, -1);
      // country ISO code
      countryIso2 = tokens[0];
      // Region name given by Google
      String subDivName = tokens[3];
      // Required Region code in media_property_state table
      String requiredRegionCode = tokens[5];

      GoogleRegionDao googleGeoObj =
          new GoogleRegionDao(countryIso2, subDivName, requiredRegionCode);
      googleRegionMap.computeIfAbsent(countryIso2, k -> new ArrayList<>()).add(googleGeoObj);
      googleRegionCountMap.put(countryIso2, googleRegionCountMap.getOrDefault(countryIso2, 0) + 1);
    }

    // MySQL state table map
    count = 0;
    while ((line = mySqlStateBr.readLine()) != null) {
      // Skipping 1st line i.e., header
      count++;
      if (count == 1) {
        continue;
      }
      tempArr = line.split(splitRegex, -1);
      // country_id: internal country Id
      String internalCountryId = tempArr[0];
      // country_iso: AF, US
      countryIso2 = tempArr[2];
      // state_id: internal_state_id present in state table
      String internalStateId = tempArr[4];
      // state name value present in state tables and used internally
      String internalStateName = tempArr[5];
      // State status
      String stateStatus = tempArr[6];
      MysqlStateDao mysqlStateDao =
          new MysqlStateDao(
              internalCountryId, countryIso2, internalStateId, stateStatus, internalStateName);
      mysqlStateMap.computeIfAbsent(countryIso2, k -> new ArrayList<>()).add(mysqlStateDao);
      mysqlStateCountMap.put(countryIso2, mysqlStateCountMap.getOrDefault(countryIso2, 0) + 1);
    }

    // MySQL media_property_state map
    count = 0;
    while ((line = mySqlMpsBr.readLine()) != null) {
      // Skipping 1st line i.e., header
      count++;
      if (count == 1) {
        continue;
      }
      tempArr = line.split(splitRegex, -1);
      // country_id: internal country Id
      String internalCountryId = tempArr[0];
      // country_iso: AF, US
      countryIso2 = tempArr[2];
      // state_id: internal_state_id present in state table
      String internalStateId = tempArr[4];
      // state name value present in state tables and used internally
      String stateName = tempArr[5];
      // State status
      String stateStatus = tempArr[6];
      // state name value present in state tables and used internally
      String mpsName = tempArr[7];
      // Media property state status
      String mpsStatus = tempArr[8];

      MysqlMediaPropertyStateDao mysqlMediaPropertyStateDao =
          new MysqlMediaPropertyStateDao(
              internalCountryId,
              countryIso2,
              internalStateId,
              mpsName,
              stateName,
              mpsStatus,
              stateStatus);
      mysqlMpsMap
          .computeIfAbsent(countryIso2, k -> new ArrayList<>())
          .add(mysqlMediaPropertyStateDao);
      mysqlMpsCountMap.put(countryIso2, mysqlMpsCountMap.getOrDefault(countryIso2, 0) + 1);
    }
    compareNewData(mysqlStateMap, mysqlMpsMap, googleRegionMap);
  }

  private void compareNewData(
      Map<String, ArrayList<MysqlStateDao>> mysqlStateMap,
      Map<String, ArrayList<MysqlMediaPropertyStateDao>> mysqlMpsMap,
      Map<String, ArrayList<GoogleRegionDao>> googleRegionMap)
      throws IOException {
    String countryCode = "";
    String countryId = "";
    Set<String> exactMpsSet = new HashSet<>();
    Set<String> exactStateSet = new HashSet<>();

    for (Map.Entry<String, ArrayList<GoogleRegionDao>> gMap : googleRegionMap.entrySet()) {
      countryCode = gMap.getKey();
      countryId = countryDataMap.get(countryCode).getCountryId();
      ArrayList<GoogleRegionDao> googleRegionsByCountry = gMap.getValue();
      ArrayList<MysqlStateDao> mysqlStates = mysqlStateMap.get(countryCode);
      ArrayList<MysqlMediaPropertyStateDao> mysqlMps = mysqlMpsMap.get(countryCode);

      // Case 1: Given a country, no states present in DB.
      if (mysqlStates == null || mysqlStates.isEmpty()) {
        for (GoogleRegionDao geoDao : gMap.getValue()) {
          newStateDataMap
              .computeIfAbsent(countryCode, k -> new ArrayList<>())
              .add(new NewStateDao(geoDao.getRegionName(), countryId, geoDao.getFinalRegionCode()));
        }
        continue;
      }

      // Case 2: GoogleRegionCode matching media_property_state values.
      // No inserts required in any table for this data
      for (GoogleRegionDao googleRegions : googleRegionsByCountry) {
        for (MysqlMediaPropertyStateDao mps : mysqlMps) {
          if (mps.getMpsName().equalsIgnoreCase(googleRegions.getFinalRegionCode())) {
            exactMediaStateMap
                .computeIfAbsent(countryCode, k -> new ArrayList<>())
                .add(
                    new ExactStateDao(
                        countryCode,
                        mps.getInternalStateId(),
                        mps.getStateName(),
                        mps.getStateStatus(),
                        mps.getMpsName(),
                        mps.getMpsStatus(),
                        googleRegions.getRegionName(),
                        googleRegions.getFinalRegionCode()));
            exactMpsSet.add(countryCode + "-" + googleRegions.getFinalRegionCode());
          }
        }
      }

      // Case 3: Inserts required in media_property_state table only.
      // State is present in state table but the final region code is different
      for (GoogleRegionDao googleRegions : googleRegionsByCountry) {
        for (MysqlStateDao state : mysqlStates) {
          if (state.getStateName().equalsIgnoreCase(googleRegions.getRegionName())
              && !exactMpsSet.contains(countryCode + '-' + googleRegions.getFinalRegionCode())) {
            exactStateMap
                .computeIfAbsent(countryCode, k -> new ArrayList<>())
                .add(
                    new ExactStateDao(
                        countryCode,
                        state.getInternalStateId(),
                        state.getStateName(),
                        state.getStateStatus(),
                        null,
                        null,
                        googleRegions.getRegionName(),
                        googleRegions.getFinalRegionCode()));
            exactStateSet.add(countryCode + "-" + googleRegions.getRegionName().toLowerCase());
          }
        }
      }

      boolean newState = true;
      for (GoogleRegionDao googleRegions : googleRegionsByCountry) {
        String googleRegionNameKey =
            countryCode + "-" + googleRegions.getRegionName().toLowerCase();
        String googleRegionCodeKey = countryCode + "-" + googleRegions.getFinalRegionCode();
        if (!exactMpsSet.contains(googleRegionCodeKey)) {
          newState = true;
          for (MysqlMediaPropertyStateDao mps : mysqlMps) {
            if (!exactStateSet.contains(googleRegionNameKey)) {
              String stateKey = countryCode + "-" + mps.getStateName().toLowerCase();
              if (checkFuzzyMatch(googleRegionNameKey, stateKey)) {
                // Add to fuzzyMap
                fuzzyStateMap
                    .computeIfAbsent(countryCode, k -> new ArrayList<>())
                    .add(
                        new FuzzyStateDao(
                            countryCode,
                            mps.getInternalStateId(),
                            mps.getStateName(),
                            mps.getStateStatus(),
                            mps.getMpsName(),
                            mps.getMpsStatus(),
                            googleRegions.getRegionName(),
                            googleRegions.getFinalRegionCode()));
                newState = false;
                break;
              }
            } else {
              newState = false;
              break;
            }
          }
          if (newState) {
            mismatchStateMap
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
    System.out.println(exactStateSet);
    writeToFile();
  }

  private void writeToFile() throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();

    // Countries present but no states present for these countries
    XSSFSheet sheet = workbook.createSheet("New States");
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
            Normalizer.normalize(
                newStates.getGoogleRegionName().replace("\"", ""), Normalizer.Form.NFD);
        stateName = stateName.replaceAll("\\p{M}", "");
        row.createCell(4).setCellValue(stateName);
        row.createCell(5).setCellValue(newStates.getFinalRegionCode());
        mysqlFinalCountMap.put(country, mysqlFinalCountMap.getOrDefault(country, 0) + 1);
      }
    }

    // Entries present in both state and media_property_state table.
    // No changes needed in any table
    XSSFSheet sheet2 = workbook.createSheet("States (P), MPS (P)");
    int sheet2RowNum = 1;
    Row header2 = sheet2.createRow(0);
    header2.createCell(0).setCellValue("Country Code");
    header2.createCell(1).setCellValue("State Id");
    header2.createCell(2).setCellValue("State name");
    header2.createCell(3).setCellValue("State status");
    header2.createCell(4).setCellValue("Media property state name");
    header2.createCell(5).setCellValue("Media property state status");
    header2.createCell(6).setCellValue("Google region name");
    header2.createCell(7).setCellValue("Final region code");
    for (Map.Entry<String, ArrayList<ExactStateDao>> exactStateMap :
        exactMediaStateMap.entrySet()) {
      String countryIso = exactStateMap.getKey();
      for (ExactStateDao exactStates : exactStateMap.getValue()) {
        XSSFRow row = sheet2.createRow(sheet2RowNum++);
        row.createCell(0).setCellValue(countryIso);
        row.createCell(1).setCellValue(exactStates.getStateId());
        row.createCell(2).setCellValue(exactStates.getStateName());
        row.createCell(3).setCellValue(exactStates.getStateStatus());
        row.createCell(4).setCellValue(exactStates.getMpsName());
        row.createCell(5).setCellValue(exactStates.getMpsStatus());
        row.createCell(6).setCellValue(exactStates.getGoogleRegionName());
        row.createCell(7).setCellValue(exactStates.getFinalGoogleRegionCode());
        mysqlFinalCountMap.put(countryIso, mysqlFinalCountMap.getOrDefault(countryIso, 0) + 1);
      }
    }

    // Data present in state table. Need to insert in media_property_state
    XSSFSheet sheet3 = workbook.createSheet("States (P), MPS (NP)");
    int sheet3RowNum = 1;
    Row header3 = sheet3.createRow(0);
    header3.createCell(0).setCellValue("Country Code");
    header3.createCell(1).setCellValue("State Id");
    header3.createCell(2).setCellValue("State name");
    header3.createCell(3).setCellValue("State status");
    //    header3.createCell(4).setCellValue("Media property state name");
    //    header3.createCell(5).setCellValue("Media property state status");
    header3.createCell(4).setCellValue("Google region name");
    header3.createCell(5).setCellValue("Final region code");

    for (Map.Entry<String, ArrayList<ExactStateDao>> stateMap : exactStateMap.entrySet()) {
      String countryIso = stateMap.getKey();
      for (ExactStateDao exactStates : stateMap.getValue()) {
        XSSFRow row = sheet3.createRow(sheet3RowNum++);
        row.createCell(0).setCellValue(countryIso);
        row.createCell(1).setCellValue(exactStates.getStateId());
        row.createCell(2).setCellValue(exactStates.getStateName());
        row.createCell(3).setCellValue(exactStates.getStateStatus());
        //        row.createCell(4).setCellValue(exactStates.getMpsName());
        //        row.createCell(5).setCellValue(exactStates.getMpsStatus());
        row.createCell(4).setCellValue(exactStates.getGoogleRegionName());
        row.createCell(5).setCellValue(exactStates.getFinalGoogleRegionCode());
        mysqlFinalCountMap.put(countryIso, mysqlFinalCountMap.getOrDefault(countryIso, 0) + 1);
      }
    }

    XSSFSheet sheet4 = workbook.createSheet("States (NP)");
    int sheet4RowNum = 1;
    Row header4 = sheet4.createRow(0);
    header4.createCell(0).setCellValue("Country Code");
    header4.createCell(1).setCellValue("Internal Country Id");
    header4.createCell(2).setCellValue("Country Name");
    header4.createCell(3).setCellValue("State Name");
    header4.createCell(4).setCellValue("Normalized State Name");
    header4.createCell(5).setCellValue("Google final region code");
    for (Map.Entry<String, ArrayList<NewStateDao>> newStateMap : mismatchStateMap.entrySet()) {
      String country = newStateMap.getKey();
      for (NewStateDao newStates : newStateMap.getValue()) {
        XSSFRow row = sheet4.createRow(sheet4RowNum++);
        row.createCell(0).setCellValue(country);
        row.createCell(1).setCellValue(countryDataMap.get(country).getCountryId());
        row.createCell(2)
            .setCellValue(countryDataMap.get(country).getCountryName().replace("\"", ""));
        row.createCell(3).setCellValue(newStates.getGoogleRegionName());
        String stateName =
            Normalizer.normalize(
                newStates.getGoogleRegionName().replace("\"", ""), Normalizer.Form.NFD);
        stateName = stateName.replaceAll("\\p{M}", "");
        row.createCell(4).setCellValue(stateName);
        row.createCell(5).setCellValue(newStates.getFinalRegionCode());
        mysqlFinalCountMap.put(country, mysqlFinalCountMap.getOrDefault(country, 0) + 1);
      }
    }

    XSSFSheet sheet5 = workbook.createSheet("Fuzzy Match");
    int sheet5RowNum = 1;
    Row header5 = sheet5.createRow(0);
    header5.createCell(0).setCellValue("Country Code");
    header5.createCell(1).setCellValue("State id");
    header5.createCell(2).setCellValue("State name");
    header5.createCell(3).setCellValue("State status");
    header5.createCell(4).setCellValue("Media property state name");
    header5.createCell(5).setCellValue("Media property state status");
    header5.createCell(6).setCellValue("Google region name");
    header5.createCell(7).setCellValue("Final region code");
    for (Map.Entry<String, ArrayList<FuzzyStateDao>> fuzzyMap : fuzzyStateMap.entrySet()) {
      String countryIso = fuzzyMap.getKey();
      for (FuzzyStateDao fuzzyStates : fuzzyMap.getValue()) {
        XSSFRow row = sheet5.createRow(sheet5RowNum++);
        row.createCell(0).setCellValue(countryIso);
        row.createCell(1).setCellValue(fuzzyStates.getStateId());
        row.createCell(2).setCellValue(fuzzyStates.getStateName());
        row.createCell(3).setCellValue(fuzzyStates.getStateStatus());
        row.createCell(4).setCellValue(fuzzyStates.getMpsName());
        row.createCell(5).setCellValue(fuzzyStates.getMpsStatus());
        row.createCell(6).setCellValue(fuzzyStates.getGoogleRegionName());
        row.createCell(7).setCellValue(fuzzyStates.getFinalRegionCode());
        mysqlFinalCountMap.put(countryIso, mysqlFinalCountMap.getOrDefault(countryIso, 0) + 1);
      }
    }
    FileOutputStream fos = new FileOutputStream(new File("resultnew.xlsx"));
    workbook.write(fos);
    fos.close();
  }

  private void display(String[] arr) {
    for (int i = 0; i < arr.length; i++) {
      System.out.print(arr[i] + "  ");
    }
    System.out.println();
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
