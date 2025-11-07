package org.agmtopy.basis

import com.lmax.disruptor.BusySpinWaitStrategy
import com.lmax.disruptor.EventFactory
import com.lmax.disruptor.EventHandler
import com.lmax.disruptor.RingBuffer
import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor.dsl.ProducerType
import java.time.LocalTime
import java.util.concurrent.ThreadFactory

/**
 * 1生产者-1消费者模式
 */
fun main() {
    //1. 创建disruptor对象
    val disruptor = createDisruptor()

    //2. 创建消费者
    val consumerHandler = createConsumerHandler()

    //3. 注册消费者
    disruptor.handleEventsWith(consumerHandler)

    //4. 启动Disruptor
    disruptor.start()

    //5. 将Ring Buffer注册到生产者上
    registerProducers(disruptor.ringBuffer)
}

fun registerProducers(ringBuffer: RingBuffer<SimpleEvent>) {
    var l = 0
    while (true) {
        // 获取下一个可用位置的下标
        val sequence = ringBuffer.next()
        try {
            // 返回可用位置的元素
            val event: SimpleEvent = ringBuffer.get(sequence)
            // 设置该位置元素的值
            event.i = l
            event.msg = LocalTime.now().toString()
        } finally {
            ringBuffer.publish(sequence)
        }
        l++
    }
}

fun createConsumerHandler(): EventHandler<SimpleEvent> {
    return EventHandler<SimpleEvent> { event, _, _ -> println(Thread.currentThread().name + ",消费事件: ${event.i}, ${event.msg}") }
}

/**
 * 创建disruptor对象
 */
fun createDisruptor(): Disruptor<SimpleEvent> {
    //1. 定义event factory
    val eventFactory = EventFactory<SimpleEvent> {
        SimpleEvent(0, "")
    }

    //2. 创建消费者线程工厂
    val threadFactory = ThreadFactory { r: Runnable? -> Thread(r, "simpleThread") }

    //3. 设置RingBuffer size
    val bufferSize = 1 shl 8

    //4. 设置消费者阻塞策略
    val waitStrategy = BusySpinWaitStrategy()

    //5. 创建disruptor对象
    val disruptor = Disruptor<SimpleEvent>(
        eventFactory,
        bufferSize,
        threadFactory,
        ProducerType.SINGLE,
        waitStrategy
    )

    return disruptor
}

data class SimpleEvent(var i: Int, var msg: String)
