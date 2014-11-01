package cn.aofeng.event4j;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.aofeng.threadpool4j.ThreadPool;

/**
 * {@link EventDispatch}的单元测试用例
 * 
 * @author <a href="mailto:aofengblog@163.com">聂勇</a>
 */
public class EventDispatchTest {

    private EventDispatch dispatch;
    
    @Before
    public void setUp() throws Exception {
        dispatch = EventDispatch.getInstance();
        dispatch.eventMap = new HashMap<String, Delegator>();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * 测试用例： 分发事件时监听器被调用的情况 <br/>
     * 前置条件：事件TEST_TYPE_ONE有一个事件监听器，事件TEST_TYPE_TWO有两个事件监听器 <br/>
     * 结果：分发事件TEST_TYPE_ONE和TEST_TYPE_TWO，三个事件监听器都被调用一次
     */
    @Test
    public void testDispatch() throws InterruptedException {
        // 初始化线程池
        ThreadPool.getInstance().init();
        
        // 事件类型 TEST_TYPE_ONE有一个监听器
        String eventType1 = "TEST_TYPE_ONE";
        EventListener<?> mock1 = createEventListenerMock(1);
        addDelegatorWithEventListener(eventType1, mock1);
        
        dispatch.dispatch(createEvent(eventType1));
        Thread.sleep(100); // 异步调用，需等待一会儿
        EasyMock.verify(mock1);
        
        // 事件类型 TEST_TYPE_TWO有两个监听器
        String eventType2 = "TEST_TYPE_TWO";
        EventListener<?> mock2 = createEventListenerMock(1);
        EventListener<?> mock3 = createEventListenerMock(1);
        addDelegatorWithEventListener(eventType2, mock2, mock3);
        
        dispatch.dispatch(createEvent(eventType2));
        Thread.sleep(100); // 异步调用，需等待一会儿
        EasyMock.verify(mock2, mock3);
    }

    private void addDelegatorWithEventListener(String eventType,
            EventListener<?>... mocks) {
        Delegator deletator = new Delegator();
        for (EventListener<?> mock : mocks) {
            deletator.addListener(mock);
        }
        dispatch.eventMap.put(eventType, deletator);
    }

    @SuppressWarnings("unchecked")
    private EventListener<?> createEventListenerMock(int callTimes) {
        EventListener<?> mock = EasyMock.createMock(EventListener.class);
        mock.execute( EasyMock.anyObject(Event.class) );
        EasyMock.expectLastCall().times(callTimes);
        EasyMock.replay(mock);
        
        return mock;
    }

    @SuppressWarnings("rawtypes")
    private Event createEvent(String eventType) {
        Event event = new Event();
        event.setEventType(eventType);
        
        return event;
    }
    
    /**
     * 测试用例：载入符合要求的事件监听器配置内容 <br/>
     * 前置条件：配置文件中有两个事件类型 <br/>
     * <ul>
     *     <li>事件TEST_TYPE_ONE配置有一个事件监听器</li>
     *     <li>事件TEST_TYPE_TWO配置有两个事件监听器</li>
     * </ul>
     * 结果：从配置文件载入内容解析后 <br/>
     * <ul>
     *     <li>事件TEST_TYPE_ONE有一个事件监听器</li>
     *     <li>事件TEST_TYPE_TWO有两个事件监听器</li>
     * </ul>
     */
    @Test
    public void testInit() {
        dispatch.setConfigFile("/cn/aofeng/event4j/event4j_sample.xml");
        dispatch.init();
        
        // 事件TEST_TYPE_ONE有一个事件监听器
        Delegator deletator = dispatch.eventMap.get("TEST_TYPE_ONE");
        assertNotNull(deletator);
        assertEquals(1, deletator.getListenerCount());
        
        // 事件TEST_TYPE_TWO有两个事件监听器
        deletator = dispatch.eventMap.get("TEST_TYPE_TWO");
        assertNotNull(deletator);
        assertEquals(2, deletator.getListenerCount());
    }

    /**
     * 测试用例：载入带有异常信息的事件监听器配置内容 <br/>
     * 前置条件：配置文件中有三个事件类型 <br/>
     * <ul>
     *     <li>事件TEST_TYPE_THREE配置有一个事件监听器完整类名，但前后有空格</li>
     *     <li>事件TEST_TYPE_FOUR配置两个listener，一个有完整的类名，一个是空格</li>
     *     <li>事件TEST_TYPE_FIVE配置对应的事件监听器不存在</li>
     * </ul>
     * 结果：从配置文件载入内容解析后 <br/>
     * <ul>
     *     <li>事件TEST_TYPE_THREE在事件类型映射表中存在，且有一个事件监听器</li>
     *     <li>事件TEST_TYPE_FOUR在事件类型映射表中存在，且有一个事件监听器</li>
     *     <li>事件TEST_TYPE_FIVE在在事件类型映射表中存在，但没有事件监听器</li>
     * </ul>
     */
    @Test
    public void testInit4AnomalousConfig() { 
        dispatch.setConfigFile("/cn/aofeng/event4j/event4j_anomalous.xml");
        dispatch.init();
        
        // 事件TEST_TYPE_THREE在事件类型映射表中存在，且有一个事件监听器
        Delegator deletator = dispatch.eventMap.get("TEST_TYPE_THREE");
        assertNotNull(deletator);
        assertEquals(1, deletator.getListenerCount());
        
        // 事件TEST_TYPE_FOUR在事件类型映射表中存在，但没有事件监听器
        deletator = dispatch.eventMap.get("TEST_TYPE_FOUR");
        assertNotNull(deletator);
        assertEquals(1, deletator.getListenerCount());
        
        // 事件TEST_TYPE_FIVE在事件类型映射表中不存在
        deletator = dispatch.eventMap.get("TEST_TYPE_FIVE");
        assertNotNull(deletator);
        assertEquals(0, deletator.getListenerCount());
    }

}