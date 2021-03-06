package dao.state;

public class FuzzyStateDao {
  String countryCode;
  String stateId;
  String stateName;
  String stateStatus;
  String mpsName;
  String mpsStatus;
  String googleRegionName;
  String finalRegionCode;

  public FuzzyStateDao(
      String countryCode,
      String stateId,
      String stateName,
      String stateStatus,
      String mpsName,
      String mpsStatus,
      String googleRegionName,
      String finalRegionCode) {
    this.countryCode = countryCode;
    this.stateId = stateId;
    this.stateName = stateName;
    this.stateStatus = stateStatus;
    this.mpsName = mpsName;
    this.mpsStatus = mpsStatus;
    this.googleRegionName = googleRegionName;
    this.finalRegionCode = finalRegionCode;
  }

  public String getStateStatus() {
    return stateStatus;
  }

  public void setStateStatus(String stateStatus) {
    this.stateStatus = stateStatus;
  }

  public String getMpsStatus() {
    return mpsStatus;
  }

  public void setMpsStatus(String mpsStatus) {
    this.mpsStatus = mpsStatus;
  }

  public String getFinalRegionCode() {
    return finalRegionCode;
  }

  public void setFinalRegionCode(String finalRegionCode) {
    this.finalRegionCode = finalRegionCode;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getStateId() {
    return stateId;
  }

  public void setStateId(String stateId) {
    this.stateId = stateId;
  }

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String stateName) {
    this.stateName = stateName;
  }

  public String getMpsName() {
    return mpsName;
  }

  public void setMpsName(String mpsName) {
    this.mpsName = mpsName;
  }

  public String getGoogleRegionName() {
    return googleRegionName;
  }

  public void setGoogleRegionName(String googleRegionName) {
    this.googleRegionName = googleRegionName;
  }
}
