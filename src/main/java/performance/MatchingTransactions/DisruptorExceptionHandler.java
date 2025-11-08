package performance.MatchingTransactions;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisruptorExceptionHandler<T> implements ExceptionHandler<T> {

  private static final Logger log = LoggerFactory.getLogger(DisruptorExceptionHandler.class);

  private final String handlerName;

  /**
   * 构造函数
   * @param handlerName 异常处理器（通常是 EventHandler）的名称，用于日志追踪。
   */
  public DisruptorExceptionHandler(String handlerName) {
    this.handlerName = handlerName;
  }

  /**
   * 【核心方法】处理 EventHandler 在处理事件时抛出的异常。
   *
   * @param ex 抛出的异常
   * @param sequence 发生异常的事件的序列号
   * @param event 发生异常时正在处理的事件对象
   */
  @Override
  public void handleEventException(Throwable ex, long sequence, T event) {
    log.error(
        "[{}] 处理事件时发生未捕获异常. 序列号: {}, 事件内容: {}",
        handlerName,
        sequence,
        event, // Disruptor通常要求事件对象实现toString()以提供可读性
        ex // 打印完整的堆栈信息
    );

    // 🚨 注意：
    // Disruptor 在调用 handleEventException 后，默认会尝试让线程继续处理下一个事件。
    // 如果您的业务逻辑要求在严重异常发生时停止整个应用，您可能需要在此处添加 System.exit() 或其他停止机制。
  }

  /**
   * 处理 EventHandler 在启动时（onStart）抛出的异常。
   *
   * @param ex 抛出的异常
   */
  @Override
  public void handleOnStartException(Throwable ex) {
    log.error(
        "[{}] EventHandler 启动时 (onStart) 发生异常，线程可能无法正常启动.",
        handlerName,
        ex
    );
  }

  /**
   * 处理 EventHandler 在关闭时（onShutdown）抛出的异常。
   *
   * @param ex 抛出的异常
   */
  @Override
  public void handleOnShutdownException(Throwable ex) {
    log.warn(
        "[{}] EventHandler 关闭时 (onShutdown) 发生异常.",
        handlerName,
        ex
    );
  }
}
