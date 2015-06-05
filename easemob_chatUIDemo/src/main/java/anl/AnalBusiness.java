package anl;

/**
 * Created by Edward.Lin on 2015/6/5 17:00
 */
public class AnalBusiness implements AnalBusinessImp  {

    private QQOperation qqOperation = new QQOperation() ;

    @Override
    public void VidoCall() {
        qqOperation.VideoCall();
    }

    @Override
    public void PhoneCall() {
        qqOperation.PhoneCall();
    }

    @Override
    public void LogIn() {
        qqOperation.Login();
    }

    @Override
    public void LogOut() {

    }
}
