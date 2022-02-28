package dao;

public class MySQLGeoDao {
  String countryId;
  String countryIso;
  String internalStateId;
  String mediaPropertyStateExternalId;
  String internalStateName;

  public MySQLGeoDao(
      String countryId,
      String countryIso,
      String internalStateId,
      String mediaPropertyStateExternalId,
      String internalStateName) {
    this.countryId = countryId;
    this.countryIso = countryIso;
    this.internalStateId = internalStateId;
    this.mediaPropertyStateExternalId = mediaPropertyStateExternalId;
    this.internalStateName = internalStateName;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  public String getInternalStateId() {
    return internalStateId;
  }

  public void setInternalStateId(String internalStateId) {
    this.internalStateId = internalStateId;
  }

  public String getCountryIso() {
    return countryIso;
  }

  public void setCountryIso(String countryIso) {
    this.countryIso = countryIso;
  }

  public String getMediaPropertyStateExternalId() {
    return mediaPropertyStateExternalId;
  }

  public void setMediaPropertyStateExternalId(String mediaPropertyStateExternalId) {
    this.mediaPropertyStateExternalId = mediaPropertyStateExternalId;
  }

  public String getInternalStateName() {
    return internalStateName;
  }

  public void setInternalStateName(String internalStateName) {
    this.internalStateName = internalStateName;
  }
}
