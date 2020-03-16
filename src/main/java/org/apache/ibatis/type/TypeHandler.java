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
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 类型处理器接口
 *
 * @author Clinton Begin
 */
public interface TypeHandler<T> {

  /**
   * 看参数描述，应该是为sql设置参数大家接口
   *
   * @param ps        数据库交互对象， 里面有sql信息
   * @param i         第几个参数
   * @param parameter 要设置的参数的值
   * @param jdbcType  sql参数的类型
   */
  void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

  /**
   * 这个应该是从结果集中取指定列名的值，只有当userColumnLabel 为false的时候才能生效
   *
   * @param columnName Colunm name, when configuration <code>useColumnLabel</code> is <code>false</code>
   */
  T getResult(ResultSet rs, String columnName) throws SQLException;

  /**
   * 从结果集中获取指定某一列的值
   */
  T getResult(ResultSet rs, int columnIndex) throws SQLException;

  /**
   * 获取指定列下标的值
   */
  T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
