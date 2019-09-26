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
package org.apache.ibatis.reflection.wrapper;

import java.util.List;

import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * 对象包装器的接口，定义了对象封装的一个功能方法
 *
 * @author Clinton Begin
 */
public interface ObjectWrapper {

  /**
   * 通过表达式来获取值，prop里存的就是表达式的封装，如：order[0].item[0].name
   * 返回最终表达式的值
   */
  Object get(PropertyTokenizer prop);

  /**
   * 对应上面的获取方法，这个是表达式和值保存起来
   */
  void set(PropertyTokenizer prop, Object value);

  /**
   * {@link MetaClass#findProperty(String, boolean)}
   */
  String findProperty(String name, boolean useCamelCaseMapping);

  /**
   * {@link MetaClass#getGetterNames()}
   * BeanWrapper里实现的是直接调用MetaClass中的方法
   */
  String[] getGetterNames();

  /**
   * {@link MetaClass#getSetterNames()}
   * BeanWrapper里实现的是直接调用MetaClass中的方法
   */
  String[] getSetterNames();

  /**
   * {@link MetaClass#getSetterType(String)}
   * BeanWrapper里实现的是直接调用MetaClass中的方法
   */
  Class<?> getSetterType(String name);

  /**
   * {@link MetaClass#getGetterType(String)}
   * BeanWrapper里实现的是直接调用MetaClass中的方法
   */
  Class<?> getGetterType(String name);

  /**
   * {@link MetaClass#hasSetter(String)}
   * BeanWrapper里实现的是直接调用MetaClass中的方法
   */
  boolean hasSetter(String name);

  /**
   * {@link MetaClass#hasGetter(String)}
   * BeanWrapper里实现的是直接调用MetaClass中的方法
   */
  boolean hasGetter(String name);

  /**
   * {@link MetaObject#forObject(Object, ObjectFactory, ObjectWrapperFactory, ReflectorFactory)}
   * 主要是调用MetaObject类的forObject方法
   */
  MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

  /**
   * 判断是否为集合
   */
  boolean isCollection();

  /**
   * 添加元素到集合中，非集合类添加元素将报错
   */
  void add(Object element);

  /**
   * 批量添加元素到集合，非集合类添加元素将报错
   */
  <E> void addAll(List<E> element);

}
