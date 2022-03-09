package dao.state;

public class NewStateDao {
  String googleRegionName;
  String countryId;
  String finalRegionCode;

  public NewStateDao(String googleRegionName, String countryId, String finalRegionCode) {
    this.googleRegionName = googleRegionName;
    this.countryId = countryId;
    this.finalRegionCode = finalRegionCode;
  }

  public String getGoogleRegionName() {
    return googleRegionName;
  }

  public void setGoogleRegionName(String googleRegionName) {
    this.googleRegionName = googleRegionName;
  }

  public String getFinalRegionCode() {
    return finalRegionCode;
  }

  public void setFinalRegionCode(String finalRegionCode) {
    this.finalRegionCode = finalRegionCode;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }
}
