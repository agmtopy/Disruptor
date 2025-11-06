package performance.handler;

import com.lmax.disruptor.WorkHandler;
import performance.domain.SimpleEvent;

public class SimpleWorkHandler implements WorkHandler<SimpleEvent> {

  private String workHandlerName;

  public SimpleWorkHandler(String workHandlerName) {
    this.workHandlerName = workHandlerName;
  }

  @Override
  public void onEvent(SimpleEvent event) throws Exception {
    System.out.println(
        "WorkHandler.Name:" + workHandlerName + ", Thread.Name:" + Thread.currentThread().getName()
            + ", Element: " + event.get());
  }
}
