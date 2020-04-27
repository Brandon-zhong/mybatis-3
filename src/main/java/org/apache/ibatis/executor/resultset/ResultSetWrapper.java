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
package org.apache.ibatis.executor.resultset;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.UnknownTypeHandler;

/**
 * result set 的包装类
 *
 * @author Iwao AVE!
 */
public class ResultSetWrapper {

  //结果集本省
  private final ResultSet resultSet;
  //类型处理注册中心
  private final TypeHandlerRegistry typeHandlerRegistry;
  private final List<String> columnNames = new ArrayList<>();
  private final List<String> classNames = new ArrayList<>();
  private final List<JdbcType> jdbcTypes = new ArrayList<>();
  private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();
  //匹配上的列名， key是resultMap的id和指定的前缀组成
  private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
  //未匹配上的列名
  private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

  public ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
    super();
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.resultSet = rs;
    final ResultSetMetaData metaData = rs.getMetaData();
    final int columnCount = metaData.getColumnCount();
    //遍历每列，处理每列的列名，jdbc类型以及java类型
    for (int i = 1; i <= columnCount; i++) {
      columnNames.add(configuration.isUseColumnLabel() ? metaData.getColumnLabel(i) : metaData.getColumnName(i));
      jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
      classNames.add(metaData.getColumnClassName(i));
    }
  }

  public ResultSet getResultSet() {
    return resultSet;
  }

  public List<String> getColumnNames() {
    return this.columnNames;
  }

  public List<String> getClassNames() {
    return Collections.unmodifiableList(classNames);
  }

  public List<JdbcType> getJdbcTypes() {
    return jdbcTypes;
  }

  public JdbcType getJdbcType(String columnName) {
    for (int i = 0; i < columnNames.size(); i++) {
      if (columnNames.get(i).equalsIgnoreCase(columnName)) {
        return jdbcTypes.get(i);
      }
    }
    return null;
  }

  /**
   * Gets the type handler to use when reading the result set.
   * Tries to get from the TypeHandlerRegistry by searching for the property type.
   * If not found it gets the column JDBC type and tries to get a handler for it.
   *
   * @param propertyType javaType
   * @param columnName   数据库的列名
   * @return 返回一个类型处理器
   */
  public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
    TypeHandler<?> handler = null;
    //获取列名关联的类型处理器
    Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
    if (columnHandlers == null) {
      columnHandlers = new HashMap<>();
      typeHandlerMap.put(columnName, columnHandlers);
    } else {
      handler = columnHandlers.get(propertyType);
    }
    //类型处理器为空
    if (handler == null) {
      JdbcType jdbcType = getJdbcType(columnName);
      //根据javaType和JdbcType去处理器注册中心获取处理器
      handler = typeHandlerRegistry.getTypeHandler(propertyType, jdbcType);
      // Replicate logic of UnknownTypeHandler#resolveTypeHandler
      // See issue #59 comment 10   处理器还是为空或者是未知的类型处理器
      if (handler == null || handler instanceof UnknownTypeHandler) {
        //获取列名对应的javaType
        final int index = columnNames.indexOf(columnName);
        final Class<?> javaType = resolveClass(classNames.get(index));
        //根据这些情况获取类型处理器
        if (javaType != null && jdbcType != null) {
          handler = typeHandlerRegistry.getTypeHandler(javaType, jdbcType);
        } else if (javaType != null) {
          handler = typeHandlerRegistry.getTypeHandler(javaType);
        } else if (jdbcType != null) {
          handler = typeHandlerRegistry.getTypeHandler(jdbcType);
        }
      }
      //以后仍然找不到处理器，使用Object的类型处理器
      if (handler == null || handler instanceof UnknownTypeHandler) {
        handler = new ObjectTypeHandler();
      }
      //缓存起来
      columnHandlers.put(propertyType, handler);
    }
    return handler;
  }

  private Class<?> resolveClass(String className) {
    try {
      // #699 className could be null
      if (className != null) {
        return Resources.classForName(className);
      }
    } catch (ClassNotFoundException e) {
      // ignore
    }
    return null;
  }

  private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
    List<String> mappedColumnNames = new ArrayList<>();
    List<String> unmappedColumnNames = new ArrayList<>();
    final String upperColumnPrefix = columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);
    //resultMap是<resultMap>标签的封装，mappedColumns是里面的<result>子标签的property的大写值
    final Set<String> mappedColumns = prependPrefixes(resultMap.getMappedColumns(), upperColumnPrefix);
    //遍历数据库返回结果的列名，分别提出resultMap中匹配上的和未匹配上的列名
    for (String columnName : columnNames) {
      final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
      if (mappedColumns.contains(upperColumnName)) {
        mappedColumnNames.add(upperColumnName);
      } else {
        unmappedColumnNames.add(columnName);
      }
    }
    //分别是匹配上的和未匹配上的列名
    mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumnNames);
    unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
  }

  public List<String> getMappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
    List<String> mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    if (mappedColumnNames == null) {
      loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
      mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    }
    return mappedColumnNames;
  }

  //获取未匹配的列名
  public List<String> getUnmappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
    List<String> unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    //如果之前未加载的话，加载并返回
    if (unMappedColumnNames == null) {
      loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
      unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    }
    return unMappedColumnNames;
  }

  //map key, 由resultMap的id和前缀组成
  private String getMapKey(ResultMap resultMap, String columnPrefix) {
    return resultMap.getId() + ":" + columnPrefix;
  }

  //生成包含指定前缀的列名集合
  private Set<String> prependPrefixes(Set<String> columnNames, String prefix) {
    if (columnNames == null || columnNames.isEmpty() || prefix == null || prefix.length() == 0) {
      return columnNames;
    }
    final Set<String> prefixed = new HashSet<>();
    for (String columnName : columnNames) {
      prefixed.add(prefix + columnName);
    }
    return prefixed;
  }

}
