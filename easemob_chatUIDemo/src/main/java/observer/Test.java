package observer;

public class Test
{
    public static void main(String[] args)
    {
        Watched girl = new ConcreteWatched();
        
        Watcher watcher1 = new CustomeWatcher();
        Watcher watcher2 = new CustomeWatcher();
        Watcher watcher3 = new CustomeWatcher();
        
        girl.addWatcher(watcher1);
        girl.addWatcher(watcher2);
        girl.addWatcher(watcher3);
        
        girl.notifyWatchers("开心");
    }

}