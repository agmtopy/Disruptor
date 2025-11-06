package performance;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DisruptorVsBlockingQueueTest {

  private static final int BUFFER_SIZE = 1024 * 1024; // RingBuffer大小
  private static final int EVENT_COUNT = 10_000_000_00;  // 测试事件数量

  public static void main(String[] args) throws Exception {
    System.out.println("=== 性能测试开始 ===");
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

    // 消费者
    Runnable consumer = () -> {
      try {
        while (counter.get() < EVENT_COUNT) {
          LongEvent event = queue.take();
          counter.incrementAndGet();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    };

    executor.submit(consumer);

    // 生产者
    long start = System.currentTimeMillis();
    for (int i = 0; i < EVENT_COUNT; i++) {
      queue.put(new LongEvent(i));
    }

    // 等待消费完成
    while (counter.get() < EVENT_COUNT) {
      Thread.sleep(10);
    }

    long duration = System.currentTimeMillis() - start;
    System.out.printf("[ArrayBlockingQueue] 总耗时: %d ms, 吞吐: %.2f Mops/s%n",
        duration, EVENT_COUNT / (duration / 1000.0) / 1_000_000);

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
        new BusySpinWaitStrategy()
    );

    AtomicLong counter = new AtomicLong(0);
    disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
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

    // 等待消费完成
    while (counter.get() < EVENT_COUNT) {
      Thread.sleep(10);
    }

    long duration = System.currentTimeMillis() - start;
    System.out.printf("[Disruptor] 总耗时: %d ms, 吞吐: %.2f Mops/s%n",
        duration, EVENT_COUNT / (duration / 1000.0) / 1_000_000);

    disruptor.shutdown();
    executor.shutdownNow();
  }

  // ===========================
  // 事件模型
  // ===========================
  public static class LongEvent {
    private long value;

    public LongEvent() {}

    public LongEvent(long value) {
      this.value = value;
    }

    public void setValue(long value) {
      this.value = value;
    }

    public long getValue() {
      return value;
    }
  }
}

