package performance.MatchingTransactions.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntrustOrder {

  /**
   * sequence long
   */
  private Long sequence;

  /**
   * 到达时间
   */
  private long arriveTime;

  /**
   * 订单id
   */
  private Integer orderId;

  /**
   * 操作动作
   */
  private String operationType;

}
