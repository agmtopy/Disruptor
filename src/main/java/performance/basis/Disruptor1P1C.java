package performance.basis;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.time.LocalTime;
import java.util.concurrent.ThreadFactory;

/**
 * 1生产者-1消费者模式
 */
public class Disruptor1P1C {

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

  /**
   * 创建disruptor对象
   */
  private static Disruptor<SimpleEvent> createDisruptor() {
    //1. 定义event factory
    EventFactory<SimpleEvent> eventFactory = SimpleEvent::new;

    //2. 创建消费者线程工厂
    ThreadFactory threadFactory = r -> new Thread(r, "simpleThread");

    //3. 设置RingBuffer size
    int bufferSize = 1 << 8; // 256

    //4. 设置消费者阻塞策略
    WaitStrategy waitStrategy = new BusySpinWaitStrategy();

    //5. 创建disruptor对象
    Disruptor<SimpleEvent> disruptor = new Disruptor<>(
        eventFactory,
        bufferSize,
        threadFactory,
        ProducerType.SINGLE,
        waitStrategy
    );

    return disruptor;
  }

  public static class SimpleEvent {
    private int i;
    private String msg;

    public SimpleEvent(int i, String msg) {
      this.i = i;
      this.msg = msg;
    }

    public SimpleEvent() {
      this(0, "");
    }

    public int getI() {
      return i;
    }

    public void setI(int i) {
      this.i = i;
    }

    public String getMsg() {
      return msg;
    }

    public void setMsg(String msg) {
      this.msg = msg;
    }
  }
}
