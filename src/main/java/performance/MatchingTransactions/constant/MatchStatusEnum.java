package performance.MatchingTransactions.constant;

public enum MatchStatusEnum {

  MATCH_ING("MATCH_ING", "匹配中"),
  CANCEL_ING("CANCEL_ING", "取消中");

  private String code;
  private String desc;


  MatchStatusEnum(String code, String desc) {
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
