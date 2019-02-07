package ravindra.avs_provisioning;

import jdk.nashorn.internal.parser.JSONParser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by Ravindra on 22/9/2017.
 */
public class callbackHandlerServlet extends HttpServlet {




    public void doGet(HttpServletRequest req,
                        HttpServletResponse res) throws IOException
    {
        HttpSession session1 = req.getSession();
        String client_id = (String) session1.getAttribute("client_id");
        String redirectPage = "fail.html";
        String root_path= String.valueOf(req.getRequestURL());
        System.out.println("root path ="+root_path);

        String servlet_path=String.valueOf(req.getRequestURI());
        System.out.println("servet path = "+ servlet_path);
        System.out.println("mod path = "+ servlet_path.substring(servlet_path.indexOf("callbackfunction"),servlet_path.length()));
        root_path=root_path.substring(0, root_path.indexOf("callbackfunction"));
        System.out.println("mod root path = " + root_path);
        if (client_id !=null) {

            String client_secret = (String) session1.getAttribute("client_secret");
            String referer = req.getHeader("referer");
            String auth_code = req.getParameter("code");


            System.out.println("root_path = "+ root_path);

           // System.out.println("Servet Path = "+ String.valueOf(req.getServletPath())) ;

            //System.out.println("path info "+ String.valueOf(req.getPathInfo()));


            String redirect_uri = req.getParameter("state");
            if (auth_code != null) {

                String jsonData = "{\"grant_type\":\"authorization_code\",\"code\":\"" + auth_code;
                jsonData += "\",\"client_id\":\"" + client_id + "\",\"client_secret\":\"" + client_secret + "\",\"redirect_uri\":\"" + redirect_uri + "\"}";
                String url = "https://api.amazon.com/auth/o2/token";
                String postResult = executePost(url, jsonData);

                if (postResult != null) {
                    if (postResult.contains("access_token")) {
                        InputStream in;
                        Properties prop = new Properties();

                        try {
                            ResourceBundle resource = ResourceBundle.getBundle("avs-config");

                            String avsproductFile = resource.getString("avsproduct_file");
                            //ClassLoader classLoader1 = Thread.currentThread().getContextClassLoader();
                            in = new FileInputStream(avsproductFile);


                            prop.load(in);

                            prop.setProperty("token", postResult);
                            // ServletContext context = req.getServletContext();
                            // String path = context.getRealPath("/WEB-INF/classes/");
                            prop.store(new FileOutputStream(avsproductFile), null);

                            redirectPage=root_path+"static/success.html";

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            redirectPage=root_path+"static/ErrorTokenWrite.html";
                        }
                    } else {
                        //likely a failure as there is no access token
                        redirectPage=root_path+"static/fail.html";
                    }
                } else {
                    redirectPage=root_path+"static/fail.html";
                }


            } else {
                redirectPage=root_path+"static/fail.html";
            }
        }
        else
        {
            redirectPage =root_path+"static/fail.html";
        }

        res.sendRedirect(redirectPage);


    }

    public String executePost(String urlStr, String dataStr) throws IOException
    {
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Cache-Control", "no-cache");

            connection.setUseCaches(false);

            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(dataStr);
            wr.flush();
            wr.close();

            /// response handle
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        }
        catch(IOException ex1)
        {
           return null;
        }
        finally{
            if(connection != null) {
                connection.disconnect();
            }
        }



    }
}
