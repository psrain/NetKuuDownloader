/* 文件名：       DebugOut.java
 * 描述：           该文件定义了类DebugOut，该类提供了方法println来替代System.out.println，
 *         并提供了可以控制是否打印输出的开关（常量DEBUG）。当DEBUG为true时打印输出，为false
 *         则不打印。
 * 创建人：       psrain
 * 创建时间：   2014.4.8
 */

package psrain.util;

/**
 * 类DebugOut，提供了带有控制开关的方法println，用来替代System.out.println方法。
 * @author psrain
 */
public class DebugOut
{
    private static final boolean DEBUG = true;
    
    /**
     * 打印参数str
     * @param obj 将要被打印的对象
     */
    public static void println(Object obj)
    {
        if (DEBUG)
        {
            System.out.println(obj);
        }
    }
}
