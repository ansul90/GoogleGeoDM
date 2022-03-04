package dao;

public class GoogleGeoDao {
  String iso2;
  String regionName;
  String finalRegionCode;

  public GoogleGeoDao(String iso2, String regionName, String finalRegionCode) {
    this.iso2 = iso2;
    this.regionName = regionName;
    this.finalRegionCode = finalRegionCode;
  }

  @Override
  public String toString() {
    return "GoogleGeoDao{"
        + "iso2='"
        + iso2
        + '\''
        + ", regionName='"
        + regionName
        + '\''
        + ", finalRegionCode='"
        + finalRegionCode
        + '\''
        + '}';
  }

  public String getIso2() {
    return iso2;
  }

  public void setIso2(String iso2) {
    this.iso2 = iso2;
  }

  public String getRegionName() {
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public String getFinalRegionCode() {
    return finalRegionCode;
  }

  public void setFinalRegionCode(String finalRegionCode) {
    this.finalRegionCode = finalRegionCode;
  }
}
