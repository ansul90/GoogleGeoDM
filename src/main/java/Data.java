public enum Data {
  prod("prod"),
  mappingData("mappingData"),
  resultantData("resultantData"),
  merge("merge"),
  fuzzy("fuzzy"),
  google("google");

  private final String text;

  /**
   * @param text
   */
  Data(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
