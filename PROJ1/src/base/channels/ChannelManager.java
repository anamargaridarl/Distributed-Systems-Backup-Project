package base.channels;

import java.io.IOException;

public class ChannelManager {

    public static BackupChannel bck_channel;
    public static ControlChannel cntr_channel;
    public static RestoreChannel rstr_channel;

    public void setChannels(ControlChannel cntr, BackupChannel bck, RestoreChannel rstr) throws IOException {
        cntr_channel = cntr;
        bck_channel = bck;
        rstr_channel = rstr;
    }

    public static BackupChannel getBckChannel() {
        return bck_channel;
    }

    public static ControlChannel getCntrChannel() {
        return cntr_channel;
    }

    public static RestoreChannel getRstrChannel() {
        return rstr_channel;
    }
}
