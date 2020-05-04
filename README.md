# java_markdown
* .java文件->编译(javac)->.class文件->.class文件->`jvm`
---
# jvm
> .class文件进来, `Class Loader SubSystem`-> `Runtime Data Areas`  
> .class文件以te

## Class Loader SubSystem
> Loading-> Linking-> Initialization  
> Loading, Linking, Initialization, 统一叫Class Loader SubSystem(类加载子系统)  
### Loading
> Bootstrap Class Loader(引导 类加载器) `Bootstrap Class Loader只能加载包名为java/javax/sun开头的class`  
> Extension Class Loader(扩展 类加载器) `会加载java.ext.dirs的系统属性设定的文件夹的jar 和 jdk目录下jre/lib/ext文件夹的jar`  
> Application/System Class Loader(应用/系统 类加载器) `Application Class Loader加载自定义class`
* Loading 做了3件事: 
1. 根据类名, 以binary byte stream 加载 (二进制字节流)
2. 将binary byte stream 转化为Method Area(方法区)运行时的数据结构
3. 在内存中生成class对象(在Method Area, 作为该class各种数据的访问入口)

### Linking
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
### Initialization
> 执行`<clinit>()`
* `<clinit>()`是javac编译时, 收集class中**static**`变量`/`代码块中语句`后生成. 如果方法中没有static, 就没有`<clinit>()`.
* 一个class的`<clinit>()`在多线程下会被同步加锁. 保证每个类只会加载一次.

## Runtime Data Areas
### Method Area
> 存放class信息&常量&字面量
1. 
2. Constant Pool
### Heap Area
### Stack Area
### PC Registers
### Native Method Stack

## Execution Engine(执行引擎)
### Interpreter
1. Interpreter
### JIT Compiler
### Garbage Collection

## 