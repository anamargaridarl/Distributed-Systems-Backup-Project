package base;

public class ChannelLogger extends Logger {
    public static void packetParseFail() {
        warning("Message received unable to parse, ignoring.");
    }

    public static void messageSendFail() {
        error("Failed to send message.");
    }

}
