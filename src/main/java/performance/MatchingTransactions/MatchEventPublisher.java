package performance.MatchingTransactions;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import performance.MatchingTransactions.domain.EntrustOrder;

public class MatchEventPublisher {

  /**
   * 定义事件转换器
   */
  private static final EventTranslatorOneArg<DisruptorEvent, EntrustOrder> TRANSLATOR =
      (event, sequence, entrustOrder) -> event.setEntrustOrder(entrustOrder);

  private Disruptor<DisruptorEvent> disruptor;

  public MatchEventPublisher(Disruptor<DisruptorEvent> disruptor) {
    this.disruptor = disruptor;
  }

  /**
   * publish event
   */
  public void publish(EntrustOrder taker) {
    RingBuffer<DisruptorEvent> ringBuffer = disruptor.getRingBuffer();

    //设置事件值
    taker.setSequence(ringBuffer.getCursor());
    taker.setArriveTime(System.currentTimeMillis());

    //发布事件
    ringBuffer.publishEvent(TRANSLATOR, taker);
  }
}
