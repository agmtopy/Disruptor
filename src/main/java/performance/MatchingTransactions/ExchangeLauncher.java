package performance.MatchingTransactions;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import performance.MatchingTransactions.domain.EntrustOrder;

/**
 * disruptor启动入口类
 */
@Slf4j
public class ExchangeLauncher {

  private static int BUFFER_SIZE = 1024 * 16;

  private int workSize = 5;

  //一个交易对对应一个disruptor
  private Map<Integer, ExchangeCore> exchangeCoreMap = new ConcurrentHashMap<>();

  private List<ExchangeCore> exchangeCoreList = new CopyOnWriteArrayList<>();

  public void start() {
    try {
      Set<Integer> symbolIdListSet = IntStream.range(0, 5)
          .collect(HashSet::new, HashSet::add, HashSet::addAll);

      if (!symbolIdListSet.isEmpty()) {
        List<Integer> allSymbolIds = new ArrayList<>(symbolIdListSet);
        List<List<Integer>> pageList = Lists.partition(allSymbolIds, workSize);
        pageList.forEach(symbolIds -> {
          ResultsHandler handler = new ResultsHandler(new HashSet<>(symbolIds), workSize);

          ExchangeCore exchangeCore = new ExchangeCore(handler, BUFFER_SIZE,
              Executors.defaultThreadFactory());

          exchangeCoreList.add(exchangeCore);

          symbolIds.forEach(symbolId -> exchangeCoreMap.put(symbolId, exchangeCore));
        });
      }
    } catch (Exception e) {
      log.error("exchangeLauncher start error:{}", e.getMessage(), e);
    }
  }

  public void publish(EntrustOrder taker) {
    exchangeCoreList.forEach(i -> i.publish(taker));
  }

}
