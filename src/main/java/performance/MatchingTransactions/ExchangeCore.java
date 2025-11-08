package performance.MatchingTransactions;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.dsl.Disruptor;
import java.util.concurrent.ThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import performance.MatchingTransactions.domain.EntrustOrder;

@Slf4j
public class ExchangeCore {

  @Getter
  private final Disruptor<DisruptorEvent> disruptor;

  private MatchEventPublisher publisher;

  private ResultsHandler eventHandler;

  public ExchangeCore(ResultsHandler matchHandler, int ringBufferSize,
      ThreadFactory threadFactory) {
    EventFactory eventFactory = () -> new DisruptorEvent();

    this.disruptor = new Disruptor<>(eventFactory, ringBufferSize, threadFactory);

    publisher = new MatchEventPublisher(this.disruptor);

    disruptor.setDefaultExceptionHandler(
        new DisruptorExceptionHandler("DisruptorExceptionHandler"));
    this.eventHandler = matchHandler;
    disruptor.handleEventsWith(eventHandler);
    disruptor.start();
  }

  public void publish(EntrustOrder taker) {
    publisher.publish(taker);
  }

}
