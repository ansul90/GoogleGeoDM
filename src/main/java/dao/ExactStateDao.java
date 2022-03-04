package dao;

public class ExactStateDao {
  String countryCode;
  String stateId;
  String mediaPropertyStateExternalStateId;
  String googleRegionName;
  String stateName;
  String finalGoogleRegionCode;

  public ExactStateDao(
      String countryCode,
      String stateId,
      String mediaPropertyStateExternalStateId,
      String googleRegionName,
      String stateName,
      String finalGoogleRegionCode) {
    this.countryCode = countryCode;
    this.stateId = stateId;
    this.mediaPropertyStateExternalStateId = mediaPropertyStateExternalStateId;
    this.googleRegionName = googleRegionName;
    this.stateName = stateName;
    this.finalGoogleRegionCode = finalGoogleRegionCode;
  }

  public String getFinalGoogleRegionCode() {
    return finalGoogleRegionCode;
  }

  public void setFinalGoogleRegionCode(String finalGoogleRegionCode) {
    this.finalGoogleRegionCode = finalGoogleRegionCode;
  }

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String stateName) {
    this.stateName = stateName;
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
