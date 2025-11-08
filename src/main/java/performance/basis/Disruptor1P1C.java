package performance.basis;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import java.time.LocalTime;

/**
 * 1生产者-1消费者模式
 */
public class Disruptor1P1C  extends BaseDisruptor{

  public static void main(String[] args) {
    //1. 创建disruptor对象
    Disruptor<SimpleEvent> disruptor = createDisruptor();

    //2. 创建消费者
    EventHandler<SimpleEvent> consumerHandler = createConsumerHandler();

    //3. 注册消费者
    disruptor.handleEventsWith(consumerHandler);

    //4. 启动Disruptor
    disruptor.start();

    //5. 将Ring Buffer注册到生产者上
    registerProducers(disruptor.getRingBuffer());
  }

  private static void registerProducers(RingBuffer<SimpleEvent> ringBuffer) {
    long l = 0;
    while (true) {
      // 获取下一个可用位置的下标
      long sequence = ringBuffer.next();
      try {
        // 返回可用位置的元素
        SimpleEvent event = ringBuffer.get(sequence);
        // 设置该位置元素的值
        event.setI((int) l);
        event.setMsg(LocalTime.now().toString());
      } finally {
        ringBuffer.publish(sequence);
      }
      l++;
    }
  }

  private static EventHandler<SimpleEvent> createConsumerHandler() {
    return (event, sequence, endOfBatch) ->
        System.out.println(Thread.currentThread().getName() + ",消费事件: " + event.getI() + ", " + event.getMsg());
  }
}
