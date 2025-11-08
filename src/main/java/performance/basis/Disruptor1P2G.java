package performance.basis;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import java.time.LocalTime;
import java.util.stream.IntStream;

/**
 * 1生产者-2消费组模式
 */
public class Disruptor1P2G  extends BaseDisruptor{

  public static void main(String[] args) {
    //1. 创建disruptor对象
    Disruptor<SimpleEvent> disruptor = createDisruptor();

    //2. 创建消费者
    WorkHandler<SimpleEvent> consumerHandler1_1 = createConsumerHandler1_1();
    WorkHandler<SimpleEvent> consumerHandler1_2 = createConsumerHandler1_2();

    WorkHandler<SimpleEvent> consumerHandler2_1 = createConsumerHandler2_1();
    WorkHandler<SimpleEvent> consumerHandler2_2 = createConsumerHandler2_2();

    //3. 注册同一个消费组多个消费组
    //3.1 注册第一个消费组
    disruptor.handleEventsWithWorkerPool(consumerHandler1_1, consumerHandler1_2);
    //3.2 注册第二个消费组
    disruptor.handleEventsWithWorkerPool(consumerHandler2_1, consumerHandler2_2);

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

  private static WorkHandler<SimpleEvent> createConsumerHandler1_1() {
    return (event) -> System.out.println(
        Thread.currentThread().getName() + ",createConsumerHandler1_1方法消费事件: "
            + event.getI() + ", " + event.getMsg());
  }

  private static WorkHandler<SimpleEvent> createConsumerHandler1_2() {
    return (event) -> System.out.println(
        Thread.currentThread().getName() + ",createConsumerHandler1_2方法消费事件: "
            + event.getI() + ", " + event.getMsg());
  }

  private static WorkHandler<SimpleEvent> createConsumerHandler2_1() {
    return (event) -> System.out.println(
        Thread.currentThread().getName() + ",createConsumerHandler2_1方法消费事件: "
            + event.getI() + ", " + event.getMsg());
  }

  private static WorkHandler<SimpleEvent> createConsumerHandler2_2() {
    return (event) -> System.out.println(
        Thread.currentThread().getName() + ",createConsumerHandler2_2方法消费事件: "
            + event.getI() + ", " + event.getMsg());
  }

}
