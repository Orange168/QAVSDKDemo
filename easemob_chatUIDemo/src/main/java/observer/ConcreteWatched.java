package observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edward.Lin on 2015/6/4 21:33
 */
public class ConcreteWatched implements Watched {

    private List<Watcher> list = new ArrayList<Watcher>();
    @Override
    public void addWatcher(Watcher w) {
        list.add(w);
    }

    @Override
    public void removeWatcher(Watcher w) {
        list.remove(w);
    }

    @Override
    public void notifyWatchers(String s) {

        for (Watcher watcher : list)
        {
            watcher.update(s);
        }
    }
}
