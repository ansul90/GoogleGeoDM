package dao.state;

public class MysqlStateDao {
  String countryId;
  String countryIso;
  String internalStateId;
  String stateStatus;
  String stateName;

  public MysqlStateDao(
      String countryId,
      String countryIso,
      String internalStateId,
      String stateStatus,
      String stateName) {
    this.countryId = countryId;
    this.countryIso = countryIso;
    this.internalStateId = internalStateId;
    this.stateStatus = stateStatus;
    this.stateName = stateName;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  public String getCountryIso() {
    return countryIso;
  }

  public void setCountryIso(String countryIso) {
    this.countryIso = countryIso;
  }

  public String getInternalStateId() {
    return internalStateId;
  }

  public void setInternalStateId(String internalStateId) {
    this.internalStateId = internalStateId;
  }

  public String getStateStatus() {
    return stateStatus;
  }

  public void setStateStatus(String stateStatus) {
    this.stateStatus = stateStatus;
  }

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String stateName) {
    this.stateName = stateName;
  }
}
