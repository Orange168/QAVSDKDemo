package observer;

/**
 * Created by Edward.Lin on 2015/6/4 21:32
 */
public class CustomeWatcher implements Watcher {
    @Override
    public void update(String s) {
        System.out.println("Sun shine") ;
    }
}
