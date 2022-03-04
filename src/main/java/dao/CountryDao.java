package dao;

public class CountryDao {
  String countryCode;
  String countryId;
  String countryName;

  public CountryDao(String countryCode, String countryId, String countryName) {
    this.countryCode = countryCode;
    this.countryId = countryId;
    this.countryName = countryName;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }
}
