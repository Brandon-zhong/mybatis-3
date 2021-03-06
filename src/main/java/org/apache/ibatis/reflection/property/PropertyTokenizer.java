/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 * @author Clinton Begin
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {
  private String name;
  private final String indexedName;
  private String index;
  private final String children;

  /**
   * 解析调用 ，例如：order[0].item[0].name
   *
   * @param fullname
   */
  public PropertyTokenizer(String fullname) {
    //第一个"."的位置
    int delim = fullname.indexOf('.');
    if (delim > -1) {
      //如果有的话，那截取小数点前的一段
      name = fullname.substring(0, delim);
      //小数点后的复制给children
      children = fullname.substring(delim + 1);
    } else {
      name = fullname;
      children = null;
    }
    //记录当前name
    indexedName = name;
    //解析name的[]坐标
    delim = name.indexOf('[');
    if (delim > -1) {
      //获取[]里的坐标index
      index = name.substring(delim + 1, name.length() - 1);
      //解析[前的name
      name = name.substring(0, delim);
    }
  }

  public String getName() {
    return name;
  }

  public String getIndex() {
    return index;
  }

  public String getIndexedName() {
    return indexedName;
  }

  public String getChildren() {
    return children;
  }

  @Override
  public boolean hasNext() {
    return children != null;
  }

  @Override
  public PropertyTokenizer next() {
    return new PropertyTokenizer(children);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
  }
}
