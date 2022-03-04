package dao;

public class FuzzyStateDao {
  String countryCode;
  String stateId;
  String stateName;
  String mediaPropertyStateExternalStateId;
  String googleRegionName;
  String finalRegionCode;

  public FuzzyStateDao(
      String countryCode,
      String stateId,
      String stateName,
      String mediaPropertyStateExternalStateId,
      String googleRegionName,
      String finalRegionCode) {
    this.countryCode = countryCode;
    this.stateId = stateId;
    this.stateName = stateName;
    this.mediaPropertyStateExternalStateId = mediaPropertyStateExternalStateId;
    this.googleRegionName = googleRegionName;
    this.finalRegionCode = finalRegionCode;
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

  public String getMediaPropertyStateExternalStateId() {
    return mediaPropertyStateExternalStateId;
  }

  public void setMediaPropertyStateExternalStateId(String mediaPropertyStateExternalStateId) {
    this.mediaPropertyStateExternalStateId = mediaPropertyStateExternalStateId;
  }

  public String getGoogleRegionName() {
    return googleRegionName;
  }

  public void setGoogleRegionName(String googleRegionName) {
    this.googleRegionName = googleRegionName;
  }
}
