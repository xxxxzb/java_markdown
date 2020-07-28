IO多路复用模型演进：
* selector 用bitmap存储要复用东东，bitmap存储最大1024个。  
* poll改为用链表存储，解决存储限制，其他与selector一样。  
* epoll的到来，解决上面模型的2个问题：
  1. selector/poll先要存储所有的东东。例如socket，selector/poll先要存储所有的socket。
  2. selector/poll要遍历所有东东，才知道哪个东东是激活的。例如socket，selector/poll要遍历所有socket，找到socket的fd(文件标志符)打了标签的。这里涉及内核态和用户态的切换。
针对问题1，epoll用eventloop来存储要复用的socket
