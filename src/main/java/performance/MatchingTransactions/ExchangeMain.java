package performance.MatchingTransactions;

import java.util.stream.IntStream;
import performance.MatchingTransactions.constant.OperationTypeEnum;
import performance.MatchingTransactions.domain.EntrustOrder;

public class ExchangeMain {

  public static void main(String[] args) {
    ExchangeLauncher launcher = new ExchangeLauncher();
    launcher.start();

    IntStream.range(0,5).forEach(i -> {
      EntrustOrder order = new EntrustOrder();
      order.setOrderId(i);
      order.setOperationType(OperationTypeEnum.MATCH.getCode());
      launcher.publish(order);
    });
  }
}
