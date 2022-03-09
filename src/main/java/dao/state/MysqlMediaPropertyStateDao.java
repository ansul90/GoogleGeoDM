package dao.state;

public class MysqlMediaPropertyStateDao {
  String countryId;
  String countryIso;
  String internalStateId;
  String stateName;
  String stateStatus;
  String mpsName;
  String mpsStatus;

  public MysqlMediaPropertyStateDao(
      String countryId,
      String countryIso,
      String internalStateId,
      String mpsName,
      String stateName,
      String mpsStatus,
      String stateStatus) {
    this.countryId = countryId;
    this.countryIso = countryIso;
    this.internalStateId = internalStateId;
    this.mpsName = mpsName;
    this.stateName = stateName;
    this.mpsStatus = mpsStatus;
    this.stateStatus = stateStatus;
  }

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String stateName) {
    this.stateName = stateName;
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

  public String getMpsName() {
    return mpsName;
  }

  public void setMpsName(String mpsName) {
    this.mpsName = mpsName;
  }
}
