/* 文件名：       MovieUtil.java
 * 描述：           该文件定义了类MovieUtil，该类提供了一些静态方法，用来获取用户最近一次
 *         使用云窗所播放的影片的Id和下载地址。
 * 创建人：       psrain
 * 创建时间：   2014.4.8
 */

package psrain.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 类MovieUtil，提供了一些静态方法，用来获取用户最近一次 使用云窗所播放的影片的Id和下载地址。
 * 
 * @author psrain
 * 
 */
public class MovieUtil
{
    /**
     * 在遍历存储了浏览器历史记录的index.dat文件时，若某哈希表中，某条活动记录的标记等于HASH_TABLE_END， 则意味着该哈希表结束。
     */
    private static final byte[] HASH_TABLE_END = { 0x00, 0x00, 0x00, 0x00 };

    /**
     * 在遍历存储了浏览器历史记录的index.dat文件时，若某哈希表中，某条活动记录的标记等于INVALID_FLAG_1，
     * 说明该活动记录的指针无效。
     */
    private static final byte[] INVALID_FLAG_1 = { 0x03, 0x00, 0x00, 0x00 };

    /**
     * 在遍历存储了浏览器历史记录的index.dat文件时，若某哈希表中，某条活动记录的标记等于INVALID_FLAG_2，
     * 说明该活动记录的指针无效。
     */
    private static final byte[] INVALID_FLAG_2 = { (byte) 0x0d, (byte) 0xf0,
                                                  (byte) 0xad, (byte) 0x0b };

    /**
     * 在遍历存储了浏览器历史记录的index.dat文件时，若某哈希表中，某条活动记录的标记等于INVALID_FLAG_3，
     * 说明该活动记录的指针无效。
     */
    private static final byte[] INVALID_FLAG_3 = { (byte) 0xef, (byte) 0xbe,
                                                  (byte) 0xad, (byte) 0xde };

    /**
     * 若某条活动记录的类型为URL_TYPE，则表示该条记录为URL类型
     */
    private static final byte[] URL_TYPE = { 0x55, 0x52, 0x4c, 0x20 };

    /**
     * 该方法将从浏览器历史记录中查找最近一次播放的影片的Id和最近一次播放的剧集的contentNum（即第几集）。
     * 对于剧集，一部剧集只有一个Id，而要提供准确的下载地址，必须知道用户刚才播放了哪一集。
     * 对于电影，一部电影对应一个Id，但对于同一部电影，云窗通常会提供高分辨率（电脑观看）和低分辨率（手机观看）两种版本，
     * 本程序会将2种版本的下载地址都提供给用户。
     * 
     * @return 返回一个Map<String, String>类型的对象，包含2个键“movieId”和“contentNum”，
     *         对于每个键来说，若其所查找的内容未找到，则该键对应的值为空字符串。 若在查找过程中，发生异常，则这2个键对应的值都为空字符串。
     */
    public static Map<String, String> getMovieInfoByHistory()
    {
        // error用来标记该函数执行过程中是否发生了异常
        boolean error = false;

        // result用来当做该函数的结果返回，包含2个键“movieId”和“contentNum”，
        // 都初始化为空字符串。
        Map<String, String> result = new HashMap<String, String>();
        result.put("movieId", "");
        result.put("contentNum", "");

        // 查找最近一次播放的影片的Id，是通过比较index.dat中，所有满足要求的访问记录的最后修改时间是来实现的，
        // 变量latestModMovIdTime则用来存储当前所找到的满足要求的最近一条记录的最后修改时间
        long latestModMovIdTime = Long.MIN_VALUE;

        // 查找最近一次播放的剧集的contentNum（即第几集），是通过比较index.dat中，所有满足要求的访问记录的最后修改时间是来实现的，
        // 变量latestModContentNumTime则用来存储当前所找到的满足要求的最近一条记录的最后修改时间
        long latestModContentNumTime = Long.MIN_VALUE;

        // history对象用来读取index.dat文件
        RandomAccessFile history = null;
        try
        {
            history = new RandomAccessFile(SystemInfo.getHistoryPath(), "r");

            // 从0x20开始的4个字节，存储了第一个哈希表的偏移量
            history.seek(0x20);
            byte[] tmp = new byte[4];
            history.read(tmp);
            
            //hashTable存储了当前哈希表的起始地址
            long hashTable = NumUtil.Bytes2Long(tmp);

            // 若第一个哈希表的偏移量为0，则说明index.dat中，未记载任何历史记录
            if (hashTable == 0L)
            {
                history.close();
                DebugOut.println("index.dat历史记录为空");
                return result;
            }

            // 该while循环遍历每一张哈希表
            while (hashTable != 0)
            {
                // 从 hashTable + 4 开始的4个字节，存储了该哈希表的长度，
                // 以0x80字节为单位
                history.seek(hashTable + 4);
                history.read(tmp);
                long hashTablelength = NumUtil.Bytes2Long(tmp);
                hashTablelength = hashTablelength * 0x80;

                // 从hashTable + 16 + 8 * n 处开始的4个字节，存储了每条活动记录的标记，
                // 其中 n = 0,1,2...
                int n = 0;
                history.seek(hashTable + 16 + 8 * n);
                byte[] recordFlag = new byte[4];
                history.read(recordFlag);
                
                //若还未到达该哈希表的结尾
                while ((hashTable + 16 + 8 * n) != (hashTable + hashTablelength)
                       && !Arrays.equals(recordFlag, HASH_TABLE_END))
                {
                    boolean hashTableEnd = false;
                    
                    //若当前的活动记录的指针无效，则判断下一条活动记录的标记，
                    //直至找到有效的活动记录，或当前哈希表结束
                    while (Arrays.equals(recordFlag, INVALID_FLAG_1)
                           || Arrays.equals(recordFlag, INVALID_FLAG_2)
                           || Arrays.equals(recordFlag, INVALID_FLAG_3))
                    {
                        n++;
                        history.skipBytes(4);
                        history.read(recordFlag);
                        
                        //若到达了该哈希表的结尾
                        if ((hashTable + 16 + 8 * n) == (hashTable + hashTablelength)
                            ||    Arrays.equals(recordFlag, HASH_TABLE_END))
                        {
                            hashTableEnd = true;
                            break;
                        }
                    }
                    
                    //若已到达了当前哈希表的结尾
                    if (hashTableEnd)
                    {
                        break;
                    }
                    
                    //程序执行到这里，说明找到了一条有效的活动记录，
                    //则取该条活动记录的指针，并跳转到该处
                    history.read(tmp);
                    
                    //recordItem存储了当前活动记录的起始地址
                    long recordItem = NumUtil.Bytes2Long(tmp);
                    history.seek(recordItem);
                    
                    //对于一条活动记录，从其起始地址开始的4个字节，保存了该条活动记录的类型
                    byte[] recordType = new byte[4];
                    history.read(recordType);
                    
                    //如果当前的这条活动记录的类型为URL
                    if (Arrays.equals(recordType, URL_TYPE))
                    {
                        //从recordItem + 0x34 开始的4个字节，记录了
                        //当前活动记录所对应的URL的偏移量，该偏移量以recordItem为基准
                        history.seek(recordItem + 0x34);
                        history.read(tmp);
                        
                        //offset保存了当前活动记录所对应的URL的偏移量，
                        //该偏移量以recordItem为基准
                        long offset = NumUtil.Bytes2Long(tmp);
                        
                        //跳转到当前活动记录所对应的URL地址的起始处
                        history.seek(recordItem + offset);
                        
                        //URL地址以0x00结束，count用来保存该URL的字节数
                        int count = 0;
                        while (history.read() != 0)
                        {
                            count++;
                        }
                        history.seek(recordItem + offset);
                        
                        //recordContent用来保存二进制形式的URL
                        byte[] recordContent = new byte[count];
                        history.read(recordContent);
                        
                        //url用来保存URL字符串
                        String url = new String(recordContent);
                        
                        //因为对于用户播放的每一部影片，都会生成一条包含
                        //"tongji.html?a=movieId"的URL记录，
                        //其中的movieId就是该影片的Id，
                        //所以只需遍历所有包含"tongji.html"的URL
                        //记录，就可以找到用户最近一次播放的影片的Id
                        if (url.contains("tongji.html"))
                        {
                            //recordItem + 8 开始的8个字节，记录该条活动记录的最后修改时间
                            history.seek(recordItem + 8);
                            byte[] lastModifyTime = new byte[8];
                            history.read(lastModifyTime);
                            if (NumUtil.Bytes2Long(lastModifyTime) > latestModMovIdTime)
                            {
                                latestModMovIdTime = NumUtil.Bytes2Long(lastModifyTime);
                                int beginIndex = url.indexOf("=") + 1;
                                
                                //提取找到的movieId，放入result中
                                result.put("movieId", url.substring(beginIndex));
                            }
                        }
                        //若用户点播了剧集，除了会生成一条包含"tongji.html?a=movieId"的URL记录，
                        //还会生成一条包含"playlist.html?Contentnumber=contentNum"的记录，
                        //其中contentNum就代表了刚才用户播放的是第几集（从0开始）
                        else if (url.contains("playlist.html?Contentnumber"))
                        {
                            history.seek(recordItem + 8);
                            byte[] lastModifyTime = new byte[8];
                            history.read(lastModifyTime);
                            if (NumUtil.Bytes2Long(lastModifyTime) > latestModContentNumTime)
                            {
                                latestModContentNumTime = NumUtil.Bytes2Long(lastModifyTime);
                                int beginIndex = url.indexOf("=") + 1;
                                int endIndex = url.indexOf("&");
                                result.put("contentNum",
                                        url.substring(beginIndex, endIndex));
                            }
                        }
                    }
                    
                    //跳转到下一条活动记录处，并读取其标记
                    n++;
                    history.seek(hashTable + 16 + 8 * n);
                    history.read(recordFlag);
                }
                
                //取得下一张哈希表的偏移量
                history.seek(hashTable + 8);
                history.read(tmp);
                hashTable = NumUtil.Bytes2Long(tmp);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            DebugOut.println("历史记录文件未找到");
            error = true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            DebugOut.println("index.dat文件读取出错");
            error = true;
        }
        finally
        {
            try
            {
                history.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        //如果前面的过程发生了异常，则返回的Map对象中，2个键对应的值都为空字符串
        if (error)
        {
            Map<String, String> map = new HashMap<String, String>();
            map.put("movieId", "");
            map.put("contentNum", "");
            return map;
        }
        else
        {
            return result;
        }
    }

    /**
     * 根据给定的movieId和contentNum（封装在movieInfo中），以及服务器IP，查找对应的下载地址
     * @param movieInfo 保存了movieId和contentNum的Map对象
     * @param hostIP 服务器IP
     * @return 该函数返回Map<String, String>类型的对象，包含2个键"highResolution"和
     * "lowResolution"。
     * <br>对于电影，键"highResolution"的值为该电影的高分辨率版本，键"lowResolution"
     * 的值对应该电影的低分辨率版本。
     * <br>对于其他类型（剧集、综艺、讲座等），由于云窗只提供一种版本，所以将这些类型的影片的下载地址
     * 放入键"highResolution"的值中，而键"lowResolution"的值为空字符串。
     * @throws DocumentException 若需要访问XML文件在服务器上不存在时，抛出该异常
     * @throws IOException 若需要访问的XML文件的元素不存在时，抛出该异常
     */
    public static Map<String, String> getMovieAddressByInfo(Map<String, String> movieInfo,
                                                          String hostIP)
            throws DocumentException, IOException
    {
        //movieAddress用来当做该函数的结果返回
        Map<String, String> movieAddress = new HashMap<String, String>();
        movieAddress.put("highResolution", "");
        movieAddress.put("lowResolution", "");
        
        SAXReader reader = new SAXReader();
        reader.setEncoding("GBK");
        Document doc;
        doc = reader.read(new URL("http://" + hostIP + "/mov/"
                                  + movieInfo.get("movieId") + "/film.xml"));
        Element root = doc.getRootElement();
        
        //若film.xml中没有元素"Contentnumber"，则该函数无法继续执行，此时应抛出异常
        if (root.element("Contentnumber") == null)
        {
            throw new IOException();
        }
        
        //film.xml中的"Contentnumber"元素，记录了该影片共有多少集，对于电影，该值通常为2（高分辨率与低分辨率），
        //对于剧集，则为实际的集数，对于其他类型（讲座等），该值通常为1
        int contentSize = Integer.parseInt(root.element("Contentnumber").getTextTrim());
        
        //每个movieId对应的下载地址，都放在对应的url2.xml的"c"元素中
        doc = reader.read(new URL("http://" + hostIP + "/mov/"
                                  + movieInfo.get("movieId") + "/url2.xml"));
        root = doc.getRootElement();
        
        //若url2.xml中没有元素"c"，则该函数无法继续执行，此时应抛出异常
        if (root.element("c") == null)
        {
            throw new IOException();
        }
        String tmp = root.element("c").getTextTrim();
        
        //读取到的"c"元素的值是以逗号分隔的
        String[] addresses = tmp.split(",", -1);
        
        //若该movieId对应的不是剧集
        if (contentSize == 1 || contentSize == 2)
        {
            //因为从"c"元素中，读取到的其实是服务器上的本地路径（如E:\xxx\xxx.mp4）
            //所以下面的for循环把本地路径转换为下载地址
            for (int i = 0; i < addresses.length; i++)
            {
                if (!addresses[i].isEmpty())
                {
                    addresses[i] = "http://"
                                   + hostIP
                                   + "/kuu"
                                   + addresses[i].charAt(0)
                                   + "/"
                                   + addresses[i].substring(addresses[i].lastIndexOf("\\") + 1);
                }
            }
            
            //若该movieId对应的不是电影
            if (contentSize == 1)
            {
                movieAddress.put("highResolution", addresses[0]);
            }
            //若该movieId对应的是电影
            else
            {
                //对于电影，通常可以从"c"元素中，读取到2个下载地址，第一个是低分辨率，第二个是高分辨率
                //但也有可能只有1个下载地址，此时第二个地址为空串
                if (addresses[1].isEmpty())
                {
                    movieAddress.put("highResolution", addresses[0]);
                }
                else
                {
                    movieAddress.put("highResolution", addresses[1]);
                    movieAddress.put("lowResolution", addresses[0]);
                }
            }
        }
        //若该movieId对应的是剧集，则此时还需要知道movieInfo中的"contentNum"
        else if (contentSize > 2)
        {
            int contentNum = Integer.parseInt(movieInfo.get("contentNum"));
            addresses[contentNum] = "http://" + hostIP + "/kuu"
                                    + addresses[contentNum].charAt(0) + "/"
                                    + addresses[contentNum].substring(addresses[contentNum].lastIndexOf("\\") + 1);
            movieAddress.put("highResolution", addresses[contentNum]);
        }
        return movieAddress;
    }

    /**
     * 当无法获取movieId时，可以调用该方法获取用户最近一次播放的影片的下载地址
     * @param hostIP 服务器IP
     * @return 用户最近一次播放的影片的下载地址
     * @throws IOException  若需要访问的XML文件的元素不存在时，抛出该异常
     * @throws DocumentException 若需要访问XML文件在服务器上不存在时，抛出该异常
     */
    public static String getMovieAddressByCache(String hostIP)
            throws IOException, DocumentException
    {
        //movieAddress将当作该函数的结果返回
        String movieAddress = "";
        
        //以下几行代码用于获取用户系统中最大的磁盘分区号
        File[] partitions = File.listRoots();
        long freeSpace = Long.MIN_VALUE;
        File targetPartition = null;
        for (File partition : partitions)
        {
            if (partition.getFreeSpace() > freeSpace)
            {
                targetPartition = partition;
                freeSpace = partition.getFreeSpace();
            }
        }

        //"CloudPxp_Cache"为用户使用云窗时所创建的缓存文件夹
        File cacheFolder = new File(targetPartition.getCanonicalPath()
                                    + "CloudPxp_Cache");
        if (cacheFolder.exists())
        {
            //列出cacheFolder中所有扩展名为".hash"的文件
            File[] hashFiles = cacheFolder.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String fileName)
                {
                    if (fileName.toLowerCase().endsWith(".hash"))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            });
            
            //以下几行代码用于找出hashFiles中最新的文件，
            //最新是指文件的最后修改时间最大
            long lastModifyTime = Long.MIN_VALUE;
            File latestHashFile = null;
            for (File hashFile : hashFiles)
            {
                if (hashFile.lastModified() > lastModifyTime)
                {
                    lastModifyTime = hashFile.lastModified();
                    latestHashFile = hashFile;
                }
            }
            
            //若找到了最新的.hash文件
            if (latestHashFile != null)
            {
                //列出所有的扩展名不是.hash的文件
                String[] movies = cacheFolder.list(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String fileName)
                    {
                        if (fileName.toLowerCase().endsWith(".hash"))
                        {
                            return false;
                        }
                        else
                        {
                            return true;
                        }
                    }
                });
                
                //以下几行代码，用于从movies中找出与latestHashFile同名的，
                //但扩展名不是.hash的文件，放入latestMovie中
                String latestMovie = null;
                String latestHashFileName = latestHashFile.getName();
                
                // ".hash"的长度为5
                latestHashFileName = latestHashFileName.substring(0,
                        latestHashFileName.length() - 5);
                for (String movie : movies)
                {
                    if (movie.contains(latestHashFileName))
                    {
                        latestMovie = movie;
                        break;
                    }
                }
                
                //若找到了与latestHashFile同名的，但扩展名不是.hash的文件，
                if (latestMovie != null)
                {
                    SAXReader reader = new SAXReader();
                    reader.setEncoding("GBK");
                    Document doc;
                    
                    //读取服务器上BarSet.xml中的"path"元素的值，该元素记录了
                    //该服务器上用于存放视频文件的分区有哪些
                    doc = reader.read(new URL("http://" + hostIP
                                              + "/bar/BarSet.xml"));
                    Element root = doc.getRootElement();
                    if (root.element("path") == null)
                    {
                        throw new IOException();
                    }
                    String[] paths = root.element("path").getTextTrim().split(
                            ",");

                    //遍历每一个可能的分区，看是否存在latestMovie
                    for (String path : paths)
                    {
                        String possibleAddress = "http://" + hostIP + "/kuu"
                                                 + path.charAt(0) + "/"
                                                 + latestMovie;
                        URL url = new URL(possibleAddress);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        if (conn.getResponseCode() == 200)
                        {
                            movieAddress = possibleAddress;
                            break;
                        }
                    }
                }
            }
        }
        return movieAddress;
    }
}
