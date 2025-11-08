package performance.basis;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ThreadFactory;

/**
 * base disruptor
 */
public class BaseDisruptor {

  /**
   * 创建disruptor对象
   */
  protected static Disruptor<SimpleEvent> createDisruptor() {
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
