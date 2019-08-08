MyBatis SQL Mapper Framework for Java
=====================================

[![Build Status](https://travis-ci.org/mybatis/mybatis-3.svg?branch=master)](https://travis-ci.org/mybatis/mybatis-3)
[![Coverage Status](https://coveralls.io/repos/mybatis/mybatis-3/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/mybatis-3?branch=master)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/org.mybatis/mybatis.svg)](https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/mybatis)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Stack Overflow](http://img.shields.io/:stack%20overflow-mybatis-brightgreen.svg)](http://stackoverflow.com/questions/tagged/mybatis)
[![Project Stats](https://www.openhub.net/p/mybatis/widgets/project_thin_badge.gif)](https://www.openhub.net/p/mybatis)

![mybatis](http://mybatis.github.io/images/mybatis-logo.png)

The MyBatis SQL mapper framework makes it easier to use a relational database with object-oriented applications.
MyBatis couples objects with stored procedures or SQL statements using a XML descriptor or annotations.
Simplicity is the biggest advantage of the MyBatis data mapper over object relational mapping tools.

Essentials
----------

* [See the docs](http://mybatis.github.io/mybatis-3)
* [Download Latest](https://github.com/mybatis/mybatis-3/releases)
* [Download Snapshot](https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/mybatis/)


![avatar](http://static2.iocoder.cn/images/MyBatis/2020_01_04/04.png)

模块定义
-------
基础支持层：包含整个 MyBatis 的基础模块，这些模块为核心处理层的功能提供了良好的支撑。
核心处理层：实现了 MyBatis 的核心处理流程，其中包括 MyBatis 的初始化以及完成一次数据库操作的涉及的全部流程 。
接口层：  
---
 模块      | 分层       | 定义       | 描述 
-----------|------------|------------|------------
 mapping   | 核心处理层 | 映射的处理 |  SQL操作解析后的映射   
 builder   | 核心处理层 | 配置的解析   |  配置解析过程    
 scripting | 核心处理层 | SQL解析     |   动态SQL语句的解析例如 <where>、<if>、<foreach> ，根据传入的实参，解析映射文件中定义的动态SQL并形成数据库可执行的SQL语句，处理占位符绑定参数 
 plugin    | 核心处理层 | 插件模块   |     
 cursor    | 核心处理层 | 游标模块 |  结果的游标    
 executor  | 核心处理层 | SQL执行模块 |   执行器，负责维护缓存，提供事务管理的操作  
 datasource | 基础支持层 | 数据源模块 |  自己的数据源实现以及第三方数据源集成的接口   
 transaction | 基础支持层 | 事务模块 |  提供了相应的事务接口和简单实现，多数情况下与spring框架继承，有spring transaction提供事务功能    
 cache | 基础支持层 | 缓存模块 |  一级缓存和二级缓存，需要注意的是这两级缓存和应用在同一个jvm中共享一块内存，如果缓存过大会影响应用的运行   
 parsing | 基础支持层 | 解析器模块 |  对xpath进行了封装，处理动态SQL语句中的占位符提供支持   
 reflection | 基础支持层 | 反射模块 |  对java原生反射进行了一系列的封装，提供了简洁易用的api，方便上层使用   
 loggin | 基础支持层 | 日志模块 |     
 binding | 基础支持层 | binding 模块 |     
 io | 基础支持层 | 资源加载模块 |  对类加载器进行封装，确定类加载器的使用顺序，提供了加载类文件以及其他资源文件的功能   
 type | 基础支持层 | 类型转化模块 |  mybatis为配置文件提供的别名机制；以及实现jdbc类型和java类型之间的装换，SQL绑定参数时将java类型装换为SQL类型，映射结果集时将jdbc类型转换为java类型   
 annotations | 基础支持层 | 注解模块 |     
 exceptions | 基础支持层 | 异常模块 |     
 session | 接口层 | 会话模块 |   定义了暴露给应用程序调用的api  
 jdbc | 其他 | jdbc模块 |     
 lang | 其他 | lang模块 |     


	