/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.ibatis.cache.Cache;

/**
 * 弱引用缓存， 弱引用是当一个对象只有弱引用对象引用时，它活不过下一次gc
 * Weak Reference cache decorator.
 * Thanks to Dr. Heinz Kabutz for his guidance here.
 *
 * @author Clinton Begin
 */
public class WeakCache implements Cache {
  //强连接队列，避免垃圾收集
  private final Deque<Object> hardLinksToAvoidGarbageCollection;
  //垃圾收集后的队列，如果某一个弱引用的值被垃圾回收了，那么这个弱引用值会被加入到这个队列中
  private final ReferenceQueue<Object> queueOfGarbageCollectedEntries;
  private final Cache delegate;
  //强连接的数量
  private int numberOfHardLinks;

  public WeakCache(Cache delegate) {
    this.delegate = delegate;
    this.numberOfHardLinks = 256;
    this.hardLinksToAvoidGarbageCollection = new LinkedList<>();
    this.queueOfGarbageCollectedEntries = new ReferenceQueue<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    removeGarbageCollectedItems();
    return delegate.getSize();
  }

  public void setSize(int size) {
    this.numberOfHardLinks = size;
  }

  @Override
  public void putObject(Object key, Object value) {
    removeGarbageCollectedItems();
    //这里构建的WeakEntry传了queueOfGarbageCollectedEntries，
    // 那如果value被回收了，那WeakEntry将会出现在queueOfGarbageCollectedEntries中
    delegate.putObject(key, new WeakEntry(key, value, queueOfGarbageCollectedEntries));
  }

  @Override
  public Object getObject(Object key) {
    Object result = null;
    @SuppressWarnings("unchecked") // assumed delegate cache is totally managed by this cache
      //缓存中存放的时候弱引用
      WeakReference<Object> weakReference = (WeakReference<Object>) delegate.getObject(key);
    if (weakReference != null) {
      result = weakReference.get();
      //弱引用里的值为空，说明value对象已经被gc了，所以缓存中的弱引用对象也应该被清除
      if (result == null) {
        delegate.removeObject(key);
      } else {
        //如果不为空的话，那这个value值会被加入到强引用队列中，这意味着即使外部没有了这个对象的引用，
        // 这个对象会因为强引用队列的作用多活一段时间，直到被挤出队列后的下一个gc回收
        hardLinksToAvoidGarbageCollection.addFirst(result);
        if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
          hardLinksToAvoidGarbageCollection.removeLast();
        }
      }
    }
    return result;
  }

  @Override
  public Object removeObject(Object key) {
    removeGarbageCollectedItems();
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    hardLinksToAvoidGarbageCollection.clear();
    removeGarbageCollectedItems();
    delegate.clear();
  }

  /**
   * 清除所有被gc的key值
   * queueOfGarbageCollectedEntries 中保存了所有的被gc的弱引用
   */
  private void removeGarbageCollectedItems() {
    WeakEntry sv;
    //如果某个弱引用出现在这个队列中，说明这个弱引用的值被gc 了，此时移除缓存中的该弱引用
    while ((sv = (WeakEntry) queueOfGarbageCollectedEntries.poll()) != null) {
      delegate.removeObject(sv.key);
    }
  }

  //继承了弱引用，增加了缓存的key字段用来在value被gc后删掉缓存中的弱引用
  private static class WeakEntry extends WeakReference<Object> {
    private final Object key;

    private WeakEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
      super(value, garbageCollectionQueue);
      this.key = key;
    }
  }

}
