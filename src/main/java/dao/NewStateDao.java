package dao;

public class NewStateDao {
  String stateName;
  String countryId;

  public NewStateDao(String stateName, String countryId) {
    this.stateName = stateName;
    this.countryId = countryId;
  }

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String stateName) {
    this.stateName = stateName;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }
}
