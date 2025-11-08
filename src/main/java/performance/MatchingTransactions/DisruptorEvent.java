package performance.MatchingTransactions;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import performance.MatchingTransactions.domain.EntrustOrder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisruptorEvent {

  private static final long serialVersionUID = -5886259612924517631L;

  //成交单
  private EntrustOrder entrustOrder;
}
