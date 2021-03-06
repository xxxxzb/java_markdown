# java_markdown 
* .java文件->编译(javac)->.class文件->.class文件->`jvm`
---
## jvm
> .class文件进来, `Class Loader SubSystem`-> `Runtime Data Areas`  
> .class文件以te

### 1. Class Loader SubSystem
> Loading-> Linking-> Initialization  
> Loading, Linking, Initialization, 统一叫Class Loader SubSystem(类加载子系统)  
#### Loading
* ExtClassLoader和AppClassLoader处于同级, 均继承自URLClassLoader. 
* URLClassLoader继承自SecureClassLoader，SecureClassLoader继承自ClassLoader，ClassLoader即为最终的顶级抽象类。
* 自定义ClassLoader也可以继承URLClassLoader. 
> Bootstrap Class Loader(引导 类加载器)  
> `Bootstrap Class Loader加载JDK中的核心类库, 如：rt.jar/resources.jar等`  

> Extension Class Loader(扩展 类加载器).     
> `加载java.ext.dirs的系统属性设定的文件夹的jar 和 jdk目录下jre/lib/ext文件夹下的jar`  

> Application/System Class Loader(应用/系统 类加载器).   
> `Application Class Loader加载来自在java命令中的classpath` 

> User-Defined Class Loader(自定义 类加载器). 作用:  
> 1. `隔离class loader`
> 2. `修改load的方式`
> 3. `扩展class loader`

* Loading 做了3件事: 
1. 根据类名, 以binary byte stream 加载 (二进制字节流)
2. 将binary byte stream 转化为Method Area(方法区)运行时的数据结构
3. 在内存中生成class对象(在Method Area, 作为该class各种数据的访问入口)

#### Linking
* Linking 做了3件事: 
1. Verify(验证)
    > * 验证class的正确性, 不会危害jvm自身安全
    > * 主要包括4种: 文件格式验证/元数据验证/字节码验证/符号引用验证
2. Prepare(准备)
    > 为static变量分配内存并且设置初始值,即零值. 不包含下面:  
     不包含final修饰的static,因为final修饰为常量. 在javac编译时已经分配值,在prepare阶段直接初始化值
3. Resolve(解析)
    > 将Constant Pool(常量池)的符号引用转换为直接引用的过程.  
    * resolve会在Initialization后执行
#### Initialization
> 执行`<clinit>()`
* `<clinit>()`是javac编译时, 收集class中**static**`变量`/`代码块中语句`后生成. 如果方法中没有static, 就没有`<clinit>()`.
* 一个class的`<clinit>()`在多线程下会被同步加锁. 保证每个类只会加载一次.

### 2. Runtime Data Areas(运行时数据区)
#### Method Area
> 存放class信息&常量&字面量
1. 
2. Constant Pool
#### Heap Area `整个jvm(进程)一份`
> 堆解决数据存储问题, 即数据怎么放/放哪里. 主体都放堆空间.
#### Stack Area `每个线程一份`
> 栈解决程序运行问题, 即程序如何处理数据
#### Program Conter Registers`每个线程一份`
> 存储下一条指令地址(cpu切走后切回来会根据PC Registers来确定执行的位置)
#### Native Method Stack

### 3. Execution Engine(执行引擎)
#### Interpreter
1. Interpreter
#### JIT Compiler
#### Garbage Collection

### tips
* 双亲委派机制 
> 当一个ClassLoader收到加载请求, 它不会自己加载, 而去向上委派父加载器, 一直往上委派

* 优化重点对象: Method Area, Heap Area
---

## Thread
- 线程状态:  
New, Runnable, Blocked, Waiting, Timed Waiting, Terminated
- 线程中断:  
interrupt, 标志位
- 线程通信:  
wait/notify (局限:对顺序有要求. 如果notify先执行,会永远waiting),   
park/unpark (局限:不能写在`synchronized`同步代码块中)  
ps: 线程判断中,要用while代替if,防止cpu伪唤醒 
- 线程封闭:  
ThreadLocal, 栈封闭(局部变量)
- 可见性问题:   
volatile (禁止cpu缓存和jvm重排序)
- 原子操作:   
CAS机制 `Compare and swap`   
(jdk底层提供unsafe.compareAndSwapInt, 很多Atomic..的API都调用这个.  
视频看网易java高级开发第一章1.2.2[43:00])
---

## 锁
- 锁的概念:  
自旋锁: CAS+循环  
悲观锁: 从读数据就加锁    
乐观锁: CAS  
独享锁: 写锁  
共享锁: 读锁  
可重入锁: 线程拿到锁后, 可自由进入同一把锁的其他同步代码  
公平锁, 非公平锁: 争抢锁的顺序. FIFO为公平, 否则是所有一起抢  
- 锁的几种实现:  
CAS: Compare and Swap  
synchronized:  
Lock:  
---

## JCU(java.util.concurrent)
- AQS```Abstract Queus Synchronize```抽象队列同步器:  
... //todo 
... //todo 
... //todo 
... //todo 
... //todo 
- ForkJoin

--- 
## 网络
- 网络7层模型:   
物理层: 物理介质  
数据链路层: 校验/确认/重发  
网络层: IP协议  
传输层: TCP/UDP协议  
会话层: 建立会话/管理会话  
表示层: 数据转换/压缩  
应用层: 用户进程  

TCP: 3次握手/4次挥手  
UDP: 无连接/数据不可靠/快  
Socket: 操作系统提供的编程接口  

BIO: ```Blocking IO```阻塞  
NIO: ```non-Blocking IO``` NIO是由Buffer,Channel,Selector三大核心组成

- Netty: 网络框架
netty实现Reactor线程模型(创造2个线程池,Main线程池负责接受accept, Sub线程池负责处理io)
Reactor模型有4个核心概念:  
1. Resources资源(请求/任务)
2. Sync Even Demultiplexer同步事件复用器
3. Dispatcher分配器
4. Request Handler请求处理器



