/* 文件名：       SystemInfo.java
 * 描述：           该文件定义了类SystemInfo，该类包含了一些用于获取当前操作
 *         系统信息的静态方法。
 * 创建人：       psrain
 * 创建时间：   2014.4.8
 * 修改人：       psrain
 * 修改时间：   2014.5.2
 * 修改内容：    增加方法getHostIP()，该方法用于获取服务器IP。
 */

package psrain.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * 类SystemInfo，提供了一些用于获取当前操作系统信息的静态方法。
 * 
 * @author psrain
 * 
 */
public class SystemInfo
{
    /**
     * XP系统下Config.cfg文件的除去盘符的路径
     */
    private static final String xpConfigFilePath = "/Documents and Settings/All Users/Application Data/NetKuu/Config.cfg";

    /**
     * 其他系统下（WIN7、WIN8）Config.cfg文件的除去盘符的路径
     */
    private static final String otherConfigFilePath = "/ProgramData/NetKuu/Config.cfg";

    /**
     * 获取当前操作系统中用于存储浏览器历史记录的文件的绝对路径
     * 
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
     * 
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
     * 
     * @return 当前用户的用户目录的绝对路径
     */
    private static String getUserPath()
    {
        String userPath = System.getProperty("user.home");
        return userPath.replaceAll("\\\\", "/");
    }

    /**
     * 该方法用于获取服务器端IP
     * @return 若找到，则返回找到的IP，否则返回空字符串
     */
    public static String getHostIP()
    {
        //得到系统分区的盘符
        String systemPartition = System.getProperty("user.home").substring(0, 2);
        File configFile = null;
        File otherConfigFile = new File(systemPartition + otherConfigFilePath);
        File xpConfigFile = new File(systemPartition + xpConfigFilePath);
        if(otherConfigFile.exists())
        {
            configFile = otherConfigFile;
        }
        else if(xpConfigFile.exists())
        {
            configFile = xpConfigFile;
        }
        
        if(configFile != null)
        {
            Scanner in = null;
            try
            {
                in = new Scanner(configFile, "GBK");
                int lineNumber = 0;
                
                //服务器IP记录在Config.cfg文件的第三行，
                //如：IP=1.2.3.4
                while(in.hasNextLine() && lineNumber < 2)
                {
                    in.nextLine();
                    lineNumber++;
                }
                
                if(in.hasNextLine() && lineNumber == 2)
                {
                    String line = in.nextLine();
                    if(line.length() > 3)
                    {
                        String hostIP = line.substring(3);
                        if(RegexUtil.isIP(hostIP))
                        {
                            return hostIP;
                        }
                    }
                }
               
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(in != null)
                {
                    in.close();
                }
            }
        }
        return "";
    }
}
