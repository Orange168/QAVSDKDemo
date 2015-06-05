package observer;

/**
 * Created by Edward.Lin on 2015/6/4 21:28.
 */
public interface Watched {
     void addWatcher(Watcher w) ;

     void removeWatcher(Watcher w) ;

     void notifyWatchers(String w) ;
}
