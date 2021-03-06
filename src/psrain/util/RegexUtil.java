/* 文件名：       RegexUtil.java
 * 描述：           该文件定义了类RegexUtil，该类仅包含一个方法isIP，该方法用来验证
 *         给定的字符串是否表示一个合法的IP地址。
 * 创建人：       psrain
 * 创建时间：   2014.4.8
 */

package psrain.util;


/**
 * 类RegexUtil，提供了静态方法isIP，用来验证
 * 给定的字符串是否表示一个合法的IP地址。
 * @author psrain
 *
 */
public class RegexUtil
{
    private static final String IP = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}"
                                     + "|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]"
                                     + "|2[0-4][0-9]|[0-1]{1}[0-9]{2}|"
                                     + "[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|"
                                     + "[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|"
                                     + "[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|"
                                     + "[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";

    /**
     * 验证参数s是否表示一个合法的IP地址。
     * @param s 被验证的表示IP地址的字符串
     * @return 若s表示了一个合法的IP地址，则返回true，否则返回falses
     */
    public static boolean isIP(String s)
    {
        return s.matches(IP);
    }
}
