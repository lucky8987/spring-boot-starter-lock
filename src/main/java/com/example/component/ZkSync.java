package com.example.component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

public class ZkSync extends AbstractQueuedSynchronizer{

    private String path;

    private ZooKeeper zk;

    private String waitPath;

    /**
     * 子节点-data
     */
    private ThreadLocal<Long> tmpData = ThreadLocal.withInitial(() -> System.currentTimeMillis());


    /**
     * 尝试访问临界区时，调用这个方法。如果线程调用这个方法可以访问临界区，那么这个方法返回true，否则，返回false。
     * @param arg
     * @return
     */
    @Override
    protected boolean tryAcquire(int arg) {
        try {
            String nodeName = path + "/" + tmpData.get();
            zk.create(nodeName, String.valueOf(tmpData.get()).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.err.println(path + "create success");
            List<String> childs = zk.getChildren(path, false);
            return checkMinPath(childs, nodeName);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查自己是不是最小的节点
     * @return
     */
    public boolean checkMinPath(List<String> childs, String currentNode) throws KeeperException, InterruptedException {
        Collections.sort(childs);
        int index = childs.indexOf(tmpData.get().toString());
        switch (index){
            case -1:{
                System.err.println("本节点已不在了..."+currentNode);
                return false;
            }
            case 0:{
                System.err.println("子节点中，我果然是老大"+currentNode);
                return true;
            }
            default:{
                this.waitPath = childs.get(index - 1);
                System.err.println("获取子节点中，排在我前面的"+waitPath);
                try{
                    zk.getData(waitPath, true, new Stat());
                    return false;
                }catch(KeeperException e){
                    if(zk.exists(waitPath,false) == null){
                        System.err.println("子节点中，排在我前面的"+waitPath+"已失踪，幸福来得太突然?");
                        return checkMinPath(childs, currentNode);
                    }else{
                        throw e;
                    }
                }
            }

        }

    }

    /**
     * 尝试翻译临界区的访问，调用这个方法。如果线程调用这个方法可以释放临界区的访问，那么这个方法返回true，否则，返回false.
     * @param arg
     * @return
     */
    @Override
    protected boolean tryRelease(int arg) {
        try {
            zk.delete(path + "/" + tmpData.get(), -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

}
