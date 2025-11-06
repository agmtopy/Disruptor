package performance;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;
import performance.domain.SimpleEvent;
import performance.handler.SimpleWorkHandler;

public class DisruptorMain {

  public static void main(String[] args) throws InterruptedException {
    // 消费者的线程工厂
    ThreadFactory threadFactory = r -> new Thread(r, "simpleThread");

    // RingBuffer生产工厂,初始化RingBuffer的时候,初始化Event对象
    EventFactory<SimpleEvent> factory = SimpleEvent::new;

    // 设置消费者消费的阻塞策略
    WaitStrategy strategy = new BusySpinWaitStrategy();

    // 指定RingBuffer的大小,必须为2的次方
    int bufferSize = 1024 * 64;

    // 创建disruptor，采用单生产者模式
    Disruptor<SimpleEvent> disruptor = new Disruptor<>(factory, bufferSize, threadFactory,
        ProducerType.SINGLE, strategy);

    // 消费event的handler
    WorkHandler<SimpleEvent>[] workHandlers = new WorkHandler[]{
        new SimpleWorkHandler("1"),
        new SimpleWorkHandler("2"),
        new SimpleWorkHandler("3"),
    };

    // 设置EventHandler
    disruptor.handleEventsWithWorkerPool(workHandlers);

    // 启动disruptor的线程
    disruptor.start();

    RingBuffer<SimpleEvent> ringBuffer = disruptor.getRingBuffer();

    ExecutorService executorService = Executors.newFixedThreadPool(8);

    IntStream.range(0, 8).forEach(i -> executorService.submit(() -> taskEvent(ringBuffer)));
  }

  private static void taskEvent(RingBuffer<SimpleEvent> ringBuffer) {
    for (int l = 0; true; l++) {
      // 获取下一个可用位置的下标
      long sequence = ringBuffer.next();
      try {
        // 返回可用位置的元素
        SimpleEvent event = ringBuffer.get(sequence);
        // 设置该位置元素的值
        event.set(l);
      } finally {
        ringBuffer.publish(sequence);
      }
    }
  }
}
