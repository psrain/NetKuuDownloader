/* 文件名：       NumUtil.java
 * 描述：           该文件定义了类NumUtil，该类仅包含一个方法Bytes2Long，该方法用来将用byte类型
 *         的数组表示的整数转换为long类型。
 * 创建人：       psrain
 * 创建时间：   2014.4.8
 */

package psrain.util;

/**
 * 类NumUtil提供了静态方法Bytes2Long，用来将用byte类型
 * 表示的数组表示的整数转换为long类型。
 * @author psrain
 *
 */
public class NumUtil
{
    /**
     * 将参数binaryNumber转换为long类型。该方法通常用来将从二进制文件中
     * 读取的表示整数的字节数组（4或8字节）还原为整数。
     * @param binaryNumber 以字节数组表示的整数
     * @return 以long表示的整数
     */
    public static long Bytes2Long(byte[] binaryNumber)
    {
        long num = 0;
        for (int i = 0; i < binaryNumber.length; i++)
        {
            long tmp = (long) binaryNumber[i] & 0xff;
            tmp = tmp << (8 * i);
            num = num | tmp;
        }
        return num;
    }
}
