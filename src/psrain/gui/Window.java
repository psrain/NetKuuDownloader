/* 文件名：       Window.java
 * 描述：           该文件定义了类Window，该类提供了本程序的GUI。
 * 创建人：       psrain
 * 创建时间：   2014.4.8
 * 修改人：       psrain
 * 修改时间：   2014.5.2
 * 修改内容：    自动设置服务器IP。
 */

package psrain.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import psrain.util.MovieUtil;
import psrain.util.RegexUtil;
import psrain.util.SystemInfo;

/**
 * 类Window提供了本程序的GUI。
 * @author psrain
 *
 */
public class Window
{
    private static Display display;
    private static Shell shell;

    //imgLabel用于显示影片海报
    private static Label imgLabel;
    
    //movieNameText用于显示影片名
    private static Text movieNameText;
    
    //movieDirectorText用于显示影片导演
    private static Text movieDirectorText;
    
    //movieActorText用于显示影片演员
    private static Text movieActorText;
    
    //movieTypeText用于显示影片类型
    private static Text movieTypeText;
    
    //movieRegionText用于显示影片地区
    private static Text movieRegionText;
    
    //movieAddressText用于显示影片下载地址
    private static Text movieAddressText;
    
    //hostIPText用于让用户输入服务器IP
    private static Text hostIPText;
    
    //“查询”按钮
    private static Button searchButton;
    
    //“转码”按钮
    private static Button decodeButton;

    private static final String info1 = "请使用下载软件（如：迅雷）下载，下载后，若文件扩展名不正确，"
                                      + "请手动修改。若无法播放，请使用“转码”功能。";
    private static final String info2 = "抱歉，无法获取影片详细信息，仅提供下载地址。";

    public static void main(String[] args)
    {
        display = Display.getDefault();
        shell = new Shell();
        shell.setSize(760, 450);
        shell.setText("NetKuu Downloader");
        addWidgets();
        hostIPText.setText(SystemInfo.getHostIP());
        shell.open();
        shell.layout();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
            {
                display.sleep();
            }
        }
        display.dispose();
    }

    /**
     * 为窗口添加所有控件及需要的监听器
     */
    private static void addWidgets()
    {
        shell.setLayout(new GridLayout(6, false));

        imgLabel = new Label(shell, SWT.BORDER);
        GridData gd_imgLabel = new GridData(SWT.CENTER, SWT.CENTER, false,
                                            false, 1, 7);
        gd_imgLabel.widthHint = 300;
        gd_imgLabel.heightHint = 400;
        imgLabel.setLayoutData(gd_imgLabel);

        Label hostIPLabel = new Label(shell, SWT.NONE);
        hostIPLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                                               false, 1, 1));
        hostIPLabel.setText("服务器IP：");

        hostIPText = new Text(shell, SWT.BORDER);
        hostIPText.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == SWT.CR)
                {
                    search();
                }
            }
        });
        GridData gd_hostIPText = new GridData(SWT.FILL, SWT.CENTER, true,
                                              false, 1, 1);
        gd_hostIPText.widthHint = 80;
        hostIPText.setLayoutData(gd_hostIPText);

        searchButton = new Button(shell, SWT.NONE);
        searchButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                clearContent();
                search();
            }
        });
        GridData gd_searchButton = new GridData(SWT.LEFT, SWT.CENTER, false,
                                                false, 1, 1);
        gd_searchButton.widthHint = 80;
        searchButton.setLayoutData(gd_searchButton);
        searchButton.setText("查询");

        Button clearButton = new Button(shell, SWT.NONE);
        clearButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                clearContent();
            }
        });
        GridData gd_clearButton = new GridData(SWT.LEFT, SWT.CENTER, false,
                                               false, 1, 1);
        gd_clearButton.widthHint = 80;
        clearButton.setLayoutData(gd_clearButton);
        clearButton.setText("清除");

        decodeButton = new Button(shell, SWT.NONE);
        decodeButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                decode();
            }
        });
        GridData gd_decodeButton = new GridData(SWT.LEFT, SWT.CENTER, false,
                                                false, 1, 1);
        gd_decodeButton.widthHint = 80;
        decodeButton.setLayoutData(gd_decodeButton);
        decodeButton.setText("转码...");

        Label movieNameLabel = new Label(shell, SWT.NONE);
        movieNameLabel.setText("片名：");

        movieNameText = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
        movieNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                                                 false, 4, 1));

        Label movieDirectorLabel = new Label(shell, SWT.NONE);
        movieDirectorLabel.setText("导演：");

        movieDirectorText = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
        movieDirectorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                                                     true, false, 4, 1));

        Label movieActorLabel = new Label(shell, SWT.NONE);
        movieActorLabel.setText("演员：");

        movieActorText = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
        movieActorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                                                  false, 4, 1));

        Label movieTypeLabel = new Label(shell, SWT.NONE);
        movieTypeLabel.setText("类型：");

        movieTypeText = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
        movieTypeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                                                 false, 4, 1));

        Label movieRegionLabel = new Label(shell, SWT.NONE);
        movieRegionLabel.setText("地区：");

        movieRegionText = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
        movieRegionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                                                   false, 4, 1));

        Label movieAddressLabel = new Label(shell, SWT.NONE);
        movieAddressLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
                                                     false, 1, 1));
        movieAddressLabel.setText("地址：");

        movieAddressText = new Text(shell, SWT.BORDER | SWT.READ_ONLY
                                           | SWT.WRAP | SWT.V_SCROLL
                                           | SWT.MULTI);
        movieAddressText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                                                    true, 4, 1));
    }
    
    /**
     * 查找用户最近一次使用云窗所播放的影片的下载地址，并将下载地址以及关于该影片的
     * 一些其他信息（片名、导演、演员等）显示给用户（如果找到的话）
     */
    private static void search()
    {
        String hostIP = hostIPText.getText().trim();
        if (RegexUtil.isIP(hostIP))
        {
            searchButton.setEnabled(false);
            searchButton.setText("请稍后");
            Thread searchThread = new Thread(new SearchAction(hostIP));
            searchThread.start();
        }
        else
        {
            showMessage(shell, SWT.ICON_WARNING | SWT.YES, "提示", "IP地址非法");
        }
    }

    /**
     * 对用户指定的文件进行转码。
     * 即将该文件的前160个字节按位取反
     */
    private static void decode()
    {
        FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
        final String moviePath = fileDialog.open();
        if (moviePath != null)
        {
            decodeButton.setText("请稍后");
            decodeButton.setEnabled(false);
            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    RandomAccessFile movie = null;
                    try
                    {
                        movie = new RandomAccessFile(moviePath, "rw");
                        byte[] movieData = new byte[0xa0];
                        
                        //读取前160个字节
                        movie.read(movieData);
                        
                        //进行按位取反
                        for (int i = 0; i < movieData.length; i++)
                        {
                            movieData[i] = (byte) ~movieData[i];
                        }
                        
                        //将取反后的结果写回
                        movie.seek(0);
                        movie.write(movieData);
                        display.asyncExec(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                showMessage(shell, SWT.ICON_INFORMATION | SWT.YES,
                                        "提示",
                                        "转码成功" + System.getProperty("line.separator")
                                                + "此过程可逆");
                            }
                        });
                        
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        display.asyncExec(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                showMessage(shell, SWT.ICON_WARNING | SWT.YES, "提示",
                                        "转码失败");
                            }
                        });
                        
                    }
                    finally
                    {
                        if (movie != null)
                        {
                            try
                            {
                                movie.close();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            } 
                        }
                        display.asyncExec(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                decodeButton.setText("转码...");
                                decodeButton.setEnabled(true);
                            }
                        });
                        
                    }

                }

            };
            Thread decodeThread = new Thread(runnable);
            decodeThread.start();
        }
    }

    /**
     * 清空GUI中所有的内容
     */
    private static void clearContent()
    {
        imgLabel.setImage(null);
        movieNameText.setText("");
        movieDirectorText.setText("");
        movieActorText.setText("");
        movieTypeText.setText("");
        movieRegionText.setText("");
        movieAddressText.setText("");
    }

    /**
     * 该函数会生成一个MessageBox对象来显示指定的信息
     * @param shell 对话框所属的shell
     * @param type 对话框的类型
     * @param title 对话框的百题
     * @param message 对话框的内容
     * @return MessageBox对象的返回值
     */
    private static int showMessage(Shell shell, int type, String title,
                                   String message)
    {
        MessageBox box = new MessageBox(shell, type);
        box.setText(title);
        box.setMessage(message);
        return box.open();
    }

    /**
     * 静态内部类SearchAction实现了Runnable接口，
     * 当用户点击“查询”按钮时，将启动一个新的线程来执行查询操作
     * @author psrain
     */
    static class SearchAction implements Runnable
    {
        private String hostIP;

        private Image movieImg = null;

        public SearchAction(String hostIP)
        {
            this.hostIP = hostIP;
        }

        @Override
        public void run()
        {

            //从浏览器历史记录中查找用户点播记录
            Map<String, String> movieInfo = MovieUtil.getMovieInfoByHistory();

            try
            {
                //若在浏览器历史记录中未找到用户点播记录
                if (movieInfo.get("movieId").isEmpty())
                {
                    setAddByCache(hostIP);
                }
                else
                {
                    setContentByMovieInfo(movieInfo, hostIP);
                }
            }
            catch (IOException | DocumentException e)
            {
                e.printStackTrace();
                display.asyncExec(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        movieAddressText.setText("网络访问出错");
                    }
                });
            }
            finally
            {
                display.asyncExec(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        searchButton.setEnabled(true);
                        searchButton.setText("查询");
                    }
                });
            }

        }

        /**
         * 根据参数movieInfo和hostIP，查找指定影片的相关信息，并显示给用户
         * @param movieInfo Map<String, String>类型的对象，包含键"movieId"
         *        和"contentNum"
         * @param hostIP 服务器IP
         * @throws DocumentException 若需要访问XML文件在服务器上不存在时，抛出该异常
         * @throws IOException 若需要访问的XML文件的元素不存在，或影片海报不存在，抛出该异常
         */
        private void setContentByMovieInfo(Map<String, String> movieInfo,
                                         String hostIP)
                throws DocumentException, IOException
        {
            SAXReader reader = new SAXReader();
            reader.setEncoding("GBK");
            Document doc;
            
            //从film.xml中，可以读出影片相关的一些信息
            doc = reader.read(new URL("http://" + hostIP + "/mov/"
                                      + movieInfo.get("movieId") + "/film.xml"));
            Element root = doc.getRootElement();
            
            //若以下任一元素在file.xml中未找到，则函数无法继续执行，应抛出异常
            if (root.element("name") == null
                || root.element("director") == null
                || root.element("actor") == null
                || root.element("region") == null
                || root.element("filmtype") == null)
            {
                throw new IOException();
            }
            final String movieName = root.element("name").getTextTrim();
            final String movieDirector = root.element("director").getTextTrim();
            final String movieActor = root.element("actor").getTextTrim();
            final String movieRegion = root.element("region").getTextTrim();
            final String movieType = root.element("filmtype").getTextTrim();
            URL imgURL = new URL("http://" + hostIP + "/mov/"
                                 + movieInfo.get("movieId") + "/1.jpg");
            InputStream imgInputStream = null;
            try
            {
                imgInputStream = imgURL.openStream();
                movieImg = new Image(display, imgInputStream);
            }
            finally
            {
                if (imgInputStream != null)
                {
                    imgInputStream.close();
                }
            }
            display.asyncExec(new Runnable()
            {
                @Override
                public void run()
                {
                    movieNameText.setText(movieName);
                    movieDirectorText.setText(movieDirector);
                    movieActorText.setText(movieActor);
                    movieRegionText.setText(movieRegion);
                    movieTypeText.setText(movieType);
                    if (movieImg != null)
                    {
                        imgLabel.setImage(movieImg);
                    }
                }

            });
            setAddByMovInfo(movieInfo, hostIP);
        }

        /**
         * 根据参数movieInfo和hostIP，查找指定影片的下载地址，并显示给用户
         * @param movieInfo Map<String, String>类型的对象，包含键"movieId"和"contentNum"
         * @param hostIP 服务器IP
         * @throws DocumentException 若需要访问XML文件在服务器上不存在时，抛出该异常
         * @throws IOException 若需要访问的XML文件的元素不存在，或影片海报不存在，抛出该异常
         */
        private void setAddByMovInfo(Map<String, String> movieInfo, String hostIP)
                throws DocumentException, IOException
        {
            Map<String, String> movieAddress = MovieUtil.getMovieAddressByInfo(
                    movieInfo, hostIP);
            final String highResolution = movieAddress.get("highResolution");
            final String lowResolution = movieAddress.get("lowResolution");
            
            //highResolution不为空，表示找到了下载地址
            if (!highResolution.isEmpty())
            {
                //lowResolution为空，表示只找到了一个下载地址
                if (lowResolution.isEmpty())
                {
                    display.asyncExec(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            movieAddressText.setText(info1
                                                     + System.getProperty("line.separator")
                                                     + highResolution);
                        }

                    });
                }
                //找到了2个下载地址
                else
                {
                    display.asyncExec(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            movieAddressText.setText(info1
                                                     + System.getProperty("line.separator")
                                                     + "高分辨率："
                                                     + highResolution
                                                     + System.getProperty("line.separator")
                                                     + "低分辨率：" + lowResolution);
                        }
                    });
                }
            }
            //为找到任何下载地址
            else
            {
                display.asyncExec(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        movieAddressText.setText("抱歉，未找到下载地址");
                    }

                });
            }
        }

        /**
         * 根据参数hostIP，查找指定影片的下载地址，并显示给用户
         * @param hostIP 服务器IP
         * @throws IOException 若需要访问的XML文件的元素不存在，抛出该异常
         * @throws DocumentException 若需要访问XML文件在服务器上不存在时，抛出该异常
         */
        private void setAddByCache(String hostIP) throws IOException,
                DocumentException
        {
            final String movieAddress = MovieUtil.getMovieAddressByCache(hostIP);
            display.asyncExec(new Runnable()
            {
                @Override
                public void run()
                {
                    if (!movieAddress.isEmpty())
                    {
                        movieAddressText.setText(info1
                                                 + System.getProperty("line.separator")
                                                 + info2
                                                 + System.getProperty("line.separator")
                                                 + movieAddress);
                    }
                    else
                    {
                        movieAddressText.setText("未找到任何播放记录");
                    }
                }
            });
        }
    }
}
