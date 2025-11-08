package performance.MatchingTransactions;

import com.lmax.disruptor.EventHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import performance.MatchingTransactions.constant.MatchStatusEnum;
import performance.MatchingTransactions.domain.EntrustOrder;

/**
 * 撮合事件处理器
 */
@Slf4j
public class ResultsHandler implements EventHandler<DisruptorEvent> {

  private final Set<Integer> symbolIdSet = new HashSet<>();

  public ResultsHandler(Set<Integer> symbolIdSet, int queueSize) {
    this.symbolIdSet.addAll(symbolIdSet);
  }

  @Override
  public void onEvent(DisruptorEvent disruptorEvent, long sequence, boolean endOfBatch) {

    try {
      //获取订单
      EntrustOrder entrustOrder = disruptorEvent.getEntrustOrder();
      // 执行撮合
      this.doMatch(entrustOrder);
    } catch (Exception e) {
      log.error("match disruptor event handler error: {}", e.getMessage(), e);
    }
  }

  /**
   * 执行撮合交易
   */
  private void doMatch(EntrustOrder takerOrder) {
    log.info("{}选择撮合策略", takerOrder.getOrderId());
    log.info("{}执行撮合", takerOrder.getOrderId());
    log.info("{}撮合完成", takerOrder.getOrderId());
  }
}
