package performance.basis;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.time.LocalTime;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

/**
 * 1生产者-2消费者模式
 */
public class Disruptor1P2C {

  public static void main(String[] args) {
    //1. 创建disruptor对象
    Disruptor<SimpleEvent> disruptor = createDisruptor();

    //2. 创建消费者
    EventHandler<SimpleEvent> consumerHandler1 = createConsumerHandler1();
    EventHandler<SimpleEvent> consumerHandler2 = createConsumerHandler2();

    //3. 注册消费者
    disruptor.handleEventsWith(consumerHandler1, consumerHandler2);

    //4. 启动Disruptor
    disruptor.start();

    //5. 将Ring Buffer注册到生产者上
    registerProducers(disruptor.getRingBuffer());
  }

  private static void registerProducers(RingBuffer<SimpleEvent> ringBuffer) {
    IntStream.range(0, 5).forEach(i -> {
      // 获取下一个可用位置的下标
      long sequence = ringBuffer.next();
      try {
        // 返回可用位置的元素
        SimpleEvent event = ringBuffer.get(sequence);
        // 设置该位置元素的值
        event.setI(i);
        event.setMsg(LocalTime.now().toString());
      } finally {
        ringBuffer.publish(sequence);
      }
    });
  }

  private static EventHandler<SimpleEvent> createConsumerHandler1() {
    return (event, sequence, endOfBatch) ->
        System.out.println(Thread.currentThread().getName() + ",createConsumerHandler1方法消费事件: "
            + event.getI() + ", " + event.getMsg());
  }

  private static EventHandler<SimpleEvent> createConsumerHandler2() {
    return (event, sequence, endOfBatch) ->
        System.out.println(Thread.currentThread().getName() + ",createConsumerHandler2方法消费事件: "
            + event.getI() + ", " + event.getMsg());
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
