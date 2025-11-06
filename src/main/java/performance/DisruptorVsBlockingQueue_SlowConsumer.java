package performance;


import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消费者消费速率对于Disruptor和普通队列的影响
 */
public class DisruptorVsBlockingQueue_SlowConsumer {

  private static final int BUFFER_SIZE = 1024 * 1024;
  private static final int EVENT_COUNT = 10_000;  // 调小点，因为消费者会慢
  private static final int CONSUMER_DELAY_MS = 1; // 模拟耗时处理 (1ms)

  public static void main(String[] args) throws Exception {
    System.out.println("=== 测试：消费者每个事件处理约 1ms ===");
    testArrayBlockingQueue();
    testDisruptor();
  }

  // ===========================
  // 方案1：ArrayBlockingQueue
  // ===========================
  private static void testArrayBlockingQueue() throws Exception {
    ArrayBlockingQueue<LongEvent> queue = new ArrayBlockingQueue<>(1024);
    ExecutorService executor = Executors.newFixedThreadPool(2);
    AtomicLong counter = new AtomicLong(0);

    Runnable consumer = () -> {
      try {
        while (counter.get() < EVENT_COUNT) {
          LongEvent event = queue.take();
          // 模拟耗时处理
          Thread.sleep(CONSUMER_DELAY_MS);
          counter.incrementAndGet();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    };

    executor.submit(consumer);

    long start = System.currentTimeMillis();
    for (int i = 0; i < EVENT_COUNT; i++) {
      queue.put(new LongEvent(i));
    }

    while (counter.get() < EVENT_COUNT) {
      Thread.sleep(10);
    }

    long duration = System.currentTimeMillis() - start;
    System.out.printf("[ArrayBlockingQueue] 总耗时: %d ms, 吞吐: %.2f ops/s%n",
        duration, EVENT_COUNT / (duration / 1000.0));

    executor.shutdownNow();
  }

  // ===========================
  // 方案2：Disruptor
  // ===========================
  private static void testDisruptor() throws Exception {
    ExecutorService executor = Executors.newCachedThreadPool();
    EventFactory<LongEvent> factory = LongEvent::new;
    int ringBufferSize = BUFFER_SIZE;

    Disruptor<LongEvent> disruptor = new Disruptor<>(
        factory,
        ringBufferSize,
        executor,
        ProducerType.SINGLE,
        new YieldingWaitStrategy()  // 更节能的等待策略
    );

    AtomicLong counter = new AtomicLong(0);
    disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
      // 模拟耗时处理
      Thread.sleep(CONSUMER_DELAY_MS);
      counter.incrementAndGet();
    });

    disruptor.start();
    RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

    long start = System.currentTimeMillis();
    for (int i = 0; i < EVENT_COUNT; i++) {
      long seq = ringBuffer.next();
      try {
        LongEvent event = ringBuffer.get(seq);
        event.setValue(i);
      } finally {
        ringBuffer.publish(seq);
      }
    }

    while (counter.get() < EVENT_COUNT) {
      Thread.sleep(10);
    }

    long duration = System.currentTimeMillis() - start;
    System.out.printf("[Disruptor]          总耗时: %d ms, 吞吐: %.2f ops/s%n",
        duration, EVENT_COUNT / (duration / 1000.0));

    disruptor.shutdown();
    executor.shutdownNow();
  }

  // ===========================
  // 事件模型
  // ===========================
  public static class LongEvent {

    private long value;

    public LongEvent() {
    }

    public LongEvent(long value) {
      this.value = value;
    }

    public long getValue() {
      return value;
    }

    public void setValue(long value) {
      this.value = value;
    }
  }
}

