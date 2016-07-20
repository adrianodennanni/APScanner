package map.net.apscanner.helpers;

public class UserInfo {

    private static UserInfo ourInstance = new UserInfo();
    private static String mUserEmail;
    private static String mUserToken;

    private UserInfo() {

    }

    public static UserInfo getInstance() {
        return ourInstance;
    }

    public static void setUserToken(String userToken) {
        mUserEmail = userToken;
    }

    public static String getUserEmail() {
        return mUserEmail;
    }

    public static void setUserEmail(String userEmail) {
        mUserEmail = userEmail;
    }

    public static String setUserToken() {
        return mUserEmail;
    }

}
