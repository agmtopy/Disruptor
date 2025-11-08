package performance.MatchingTransactions.constant;

public enum OperationTypeEnum {

  MATCH("MATCH", "匹配"),
  CANCEL("BUY", "取消");

  private String code;
  private String desc;

  OperationTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public String getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }
}
