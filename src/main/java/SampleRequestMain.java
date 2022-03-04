import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SampleRequestMain {
  public static void main(String[] args) throws URISyntaxException, IOException {
    String sampleRequestFileName = "sample_request.xlsx";
    SampleRequestMain srm = new SampleRequestMain();
    File sampleRequestDataFile = srm.getFileFromResource(sampleRequestFileName);
    //    srm.segregate(sampleRequestDataFile);

    String finalRegionFileName = "final_regions.xlsx";
    File finalRegionFile = srm.getFileFromResource(finalRegionFileName);
    srm.decorateFinalRegions(finalRegionFile);
  }

  private void decorateFinalRegions(File finalRegionFile) throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("Normalised Region Data");

    FileInputStream fis = new FileInputStream(finalRegionFile);
    XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
    XSSFSheet mySheet = myWorkBook.getSheetAt(1);
    Iterator<Row> rowIterator = mySheet.iterator();
    rowIterator.next();
    int rowNum = 1;
    while (rowIterator.hasNext()) {
      XSSFRow row1 = sheet.createRow(rowNum++);
      Row row = rowIterator.next();
      Cell cell = row.getCell(2);
      String regionVal = cell.getStringCellValue();
      String[] regionArr = regionVal.split("-");
      String part2 = regionArr[1];

      if (regionArr[0].equalsIgnoreCase("SI")) {
        if (part2.length() == 1) {
          part2 = String.format("%03s", part2);
          System.out.println(part2);
        }
      } else {
        if (part2.length() == 1) {
          part2 = '0' + part2;
        }
      }

      row1.createCell(0).setCellValue(regionArr[0] + '-' + part2);
    }
    FileOutputStream fos = new FileOutputStream(new File("finalRegions.xlsx"));
    workbook.write(fos);
    fos.close();
  }

  private void segregate(File sampleRequestDataFile) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("New State Data");

    FileInputStream fis = new FileInputStream(sampleRequestDataFile);
    XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
    XSSFSheet mySheet = myWorkBook.getSheetAt(0);
    Iterator<Row> rowIterator = mySheet.iterator();
    rowIterator.next();
    int rownum = 1;
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Country");
    header.createCell(1).setCellValue("Region");
    header.createCell(2).setCellValue("Metro");
    header.createCell(3).setCellValue("City");
    header.createCell(4).setCellValue("Zip");
    header.createCell(5).setCellValue("Accuracy");
    header.createCell(6).setCellValue("Lat");
    header.createCell(7).setCellValue("Lon");
    header.createCell(8).setCellValue("UTCOffset");
    while (rowIterator.hasNext()) {
      Row row = rowIterator.next();
      Cell cell = row.getCell(0);
      JsonNode node = mapper.readValue(cell.getStringCellValue(), JsonNode.class);
      JsonNode geoNode = node.get("geo");
      if (geoNode != null) {
        System.out.println(geoNode);
        XSSFRow row1 = sheet.createRow(rownum++);
        if (geoNode.get("country") != null)
          row1.createCell(0).setCellValue(geoNode.get("country").toString().replace("\"", ""));
        if (geoNode.get("region") != null)
          row1.createCell(1).setCellValue(geoNode.get("region").toString().replace("\"", ""));
        if (geoNode.get("metro") != null)
          row1.createCell(2).setCellValue(geoNode.get("metro").toString().replace("\"", ""));
        if (geoNode.get("city") != null)
          row1.createCell(3).setCellValue(geoNode.get("city").toString().replace("\"", ""));
        if (geoNode.get("zip") != null)
          row1.createCell(4).setCellValue(geoNode.get("zip").toString().replace("\"", ""));
        if (geoNode.get("accuracy") != null)
          row1.createCell(5).setCellValue(geoNode.get("accuracy").toString());
        if (geoNode.get("lat") != null)
          row1.createCell(6).setCellValue(geoNode.get("lat").toString());
        if (geoNode.get("lon") != null)
          row1.createCell(7).setCellValue(geoNode.get("lon").toString());
      }
    }
    FileOutputStream fos = new FileOutputStream(new File("sample_request_processed.xlsx"));
    workbook.write(fos);
    fos.close();
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
