/* 文件名：       SystemInfo.java
 * 描述：           该文件定义了类SystemInfo，该类包含了一些用于获取当前操作
 *         系统信息的静态方法。
 * 创建人：       psrain
 * 创建时间：   2014.4.8
 */

package psrain.util;

/**
 * 类SystemInfo，提供了一些用于获取当前操作系统信息的静态方法。
 * @author psrain
 *
 */
public class SystemInfo
{
    /**
     * 获取当前操作系统中用于存储浏览器历史记录的文件的绝对路径
     * @return 当前操作系统中用于存储浏览器历史记录的文件的绝对路径
     */
    public static String getHistoryPath()
    {
        if (System.getProperty("os.name").contains("XP"))
        {
            return getUserPath()
                   + "/Local Settings/History/History.IE5/index.dat";
        }
        else
        {
            return getUserPath()
                   + "/AppData/Local/Microsoft/Windows/History/History.IE5/index.dat";
        }
    }
    
    /**
     * 获取当前操作系统中用于存储浏览器cookies的文件的绝对路径
     * @return 前操作系统中用于存储浏览器cookies的文件的绝对路径
     */
    public static String getCookiesPath()
    {
        if (System.getProperty("os.name").contains("XP"))
        {
            return getUserPath() + "/Cookies/index.dat";
        }
        else
        {
            return getUserPath()
                   + "/AppData/Roaming/Microsoft/Windows/Cookies/index.dat";
        }
    }

    /**
     * 获取当前用户的用户目录的绝对路径
     * @return 当前用户的用户目录的绝对路径
     */
    private static String getUserPath()
    {
        String userPath = System.getProperty("user.home");
        return userPath.replaceAll("\\\\", "/");
    }
}
